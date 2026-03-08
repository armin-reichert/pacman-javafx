/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d3.config.ActorConfig3D;
import de.amr.pacmanfx.ui.d3.config.Config3D;
import de.amr.pacmanfx.ui.d3.config.LevelCounterConfig3D;
import de.amr.pacmanfx.ui.d3.config.LivesCounterConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.*;
import de.amr.pacmanfx.uilib.widgets.MessageView;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

/**
 * Represents the complete 3D visualization of a single Pac-Man game level.
 * <p>
 * This class serves as the root node for all 3D elements of a level, including:
 * <ul>
 *   <li>Maze (floor, walls, house, pellets, energizers, particles)</li>
 *   <li>Actors (Pac-Man, ghosts, bonus symbol)</li>
 *   <li>Counters (lives, level number)</li>
 *   <li>Lights (ambient + ghost-specific point light)</li>
 *   <li>Dynamic messages (READY!, test mode, etc.)</li>
 *   <li>Animations (level complete, flashing walls, etc.)</li>
 * </ul>
 * <p>
 * It manages component creation, per-frame updates, draw mode changes, and full resource
 * cleanup via {@link DisposableGraphicsObject} and a {@link List} of {@link Disposable} components.
 * <p>
 * Instances are created by {@link PlayScene3D} and disposed when the level ends or the
 * 3D view is deactivated.
 *
 * @see PlayScene3D
 * @see Maze3D
 * @see PacBase3D
 * @see MutableGhost3D
 * @see DisposableGraphicsObject
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    /** Standard "READY!" message shown at level start */
    public static final String READY_MESSAGE_TEXT = "READY!";

    /** Test mode overlay message format */
    public static final String TEST_MESSAGE_TEXT = "LEVEL %d (TEST)";

    /** Default display duration for READY! message */
    public static final float READY_MESSAGE_DISPLAY_SECONDS = 2.5f;

    private final GameLevel level;
    private final UIConfig uiConfig;
    private final Config3D config3D;
    private final AnimationRegistry animationRegistry = new AnimationRegistry();

    private Node[] livesCounterShapes;

    private AmbientLight ambientLight;
    private PointLight ghostLight;

    private Maze3D maze3D;
    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private PacBase3D pac3D;
    private List<MutableGhost3D> ghosts3D;
    private Bonus3D bonus3D;
    private MessageView messageView;

    private GameLevel3DAnimations animations;

    private final List<Disposable> disposables = new ArrayList<>();

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param uiConfig global UI configuration (provides 3D settings, colors, models)
     * @param level    the game level to visualize
     */
    public GameLevel3D(UIConfig uiConfig, GameLevel level) {
        this.level = requireNonNull(level);
        this.uiConfig = requireNonNull(uiConfig);
        this.config3D = uiConfig.config3D();

        createLevelCounter3D();
        createLivesCounter3D();
        createPac3D();
        createGhosts3D();
        createMaze3D();
        createLights();

        arrangeChildren();

        PROPERTY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);
        setMouseTransparent(true); // this increases performance, they say...
    }

    /**
     * Arranges all direct children in the correct rendering order.
     * <p>
     * Order matters for correct transparency: actors and effects must appear
     * in front of walls/house.
     */
    private void arrangeChildren() {
        getChildren().add(maze3D.floor());
        getChildren().addAll(maze3D.particlesGroup());
        getChildren().add(levelCounter3D);
        getChildren().add(livesCounter3D);
        getChildren().addAll(pac3D, pac3D.light());
        getChildren().addAll(ghosts3D);
        getChildren().addAll(maze3D.food().energizers3D().stream().map(Energizer3D::shape).toList());
        getChildren().addAll(maze3D.food().pellets3D());
        getChildren().add(maze3D.house().root());
        getChildren().add(maze3D.house().doors()); // Note order of addition!
        getChildren().add(maze3D);
        getChildren().add(ambientLight);
        getChildren().add(ghostLight);
    }

    /** @return registry for all level-specific animations */
    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    /** @return the underlying game level model */
    public GameLevel level() {
        return level;
    }

    /** @return UI configuration used for this level */
    public UIConfig uiConfig() {
        return uiConfig;
    }

    /** @return 3D-specific configuration */
    public Config3D config3D() {
        return config3D;
    }

    /** @return the maze visualization component */
    public Maze3D maze3D() {
        return maze3D;
    }

    /** @return level number counter visualization */
    public LevelCounter3D levelCounter3D() {
        return levelCounter3D;
    }

    /** @return current message overlay view (READY!, test mode, etc.) */
    public MessageView messageView() {
        return messageView;
    }

    /** @return point light used for ghost highlighting */
    public PointLight ghostLight() {
        return ghostLight;
    }

    /** @return optional animations controller for this level */
    public Optional<GameLevel3DAnimations> animations() {
        return Optional.ofNullable(animations);
    }

    /** Sets the animation controller for level-specific effects */
    public void setAnimations(GameLevel3DAnimations animations) {
        this.animations = requireNonNull(animations);
    }

    /**
     * Determines if the given ghost's center position is outside the visible world bounds.
     *
     * @param ghost the ghost to check
     * @return true if the ghost is outside the maze bounds
     */
    private boolean outsideWorld(Ghost ghost) {
        Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
    }

    /**
     * Creates and initializes the 3D representation of Pac-Man.
     */
    private void createPac3D() {
        final ActorConfig3D actorConfig = config3D.actor();
        pac3D = uiConfig.createPac3D(animationRegistry, level.pac(), actorConfig.pacSize());
        pac3D.init(level);

        disposables.add(pac3D);
    }

    /**
     * Creates and initializes all ghost 3D representations.
     */
    private void createGhosts3D() {
        ghosts3D = level.ghosts().map(ghost -> createMutatingGhost3D(config3D.actor(), ghost)).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.init(level));

        disposables.addAll(ghosts3D);
    }

    /**
     * Creates a color set for a ghost based on its personality.
     *
     * @param personality the ghost personality ID (0–3)
     * @return the color set for normal, frightened, and flashing states
     */
    private GhostColorSet createGhostColorSet(byte personality) {
        AssetMap assets = uiConfig.assets();
        return new GhostColorSet(
            new GhostComponentColors(
                assets.color("ghost.%d.color.normal.dress".formatted(personality)),
                assets.color("ghost.%d.color.normal.pupils".formatted(personality)),
                assets.color("ghost.%d.color.normal.eyeballs".formatted(personality))
            ),
            new GhostComponentColors(
                assets.color("ghost.color.frightened.dress"),
                assets.color("ghost.color.frightened.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            ),
            new GhostComponentColors(
                assets.color("ghost.color.flashing.dress"),
                assets.color("ghost.color.flashing.pupils"),
                assets.color("ghost.color.frightened.eyeballs")
            )
        );
    }

    /**
     * Creates a mutable 3D ghost representation for the given model ghost.
     *
     * @param actorConfig configuration for actor sizes
     * @param ghost       the model ghost
     * @return the 3D ghost with visibility binding
     */
    private MutableGhost3D createMutatingGhost3D(ActorConfig3D actorConfig, Ghost ghost) {
        final byte id = ghost.personality();
        final var mutatingGhost3D = new MutableGhost3D(
            animationRegistry,
            ghost,
            createGhostColorSet(id),
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh(),
            actorConfig.ghostSize(),
            level.numFlashes()
        );
        mutatingGhost3D.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        ));
        return mutatingGhost3D;
    }

    /**
     * Creates and initializes the lives counter visualization.
     */
    private void createLivesCounter3D() {
        final LivesCounterConfig3D config = config3D.livesCounter();
        livesCounterShapes = new Node[config.capacity()];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = uiConfig.createLivesCounterShape3D(config.shapeSize());
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(config.pillarColor());
        livesCounter3D.plateColorProperty().set(config.plateColor());

        disposables.add(livesCounter3D);
    }

    /**
     * Creates and initializes the level number counter visualization.
     */
    private void createLevelCounter3D() {
        final LevelCounterConfig3D config = config3D.levelCounter();
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-config.elevation());

        disposables.add(levelCounter3D);
    }

    /**
     * Creates ambient and ghost-specific point lights.
     */
    private void createLights() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);

        ghostLight = new PointLight();
    }

    /**
     * Creates and initializes the maze visualization, including color scheme adjustment.
     */
    private void createMaze3D() {
        WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final boolean wallsVeryDark = Color.valueOf(colorScheme.wallFill()).getBrightness() < 0.1;
        if (wallsVeryDark) {
            final String notTooDarkColor = config3D.maze().darkWallFillColor();
            colorScheme = new WorldMapColorScheme(
                notTooDarkColor,
                colorScheme.wallStroke(),
                colorScheme.door(),
                colorScheme.pellet());
        }

        final List<PhongMaterial> ghostNormalDressMaterials = ghosts3D.stream()
            .map(MutableGhost3D::ghost3D)
            .map(Ghost3D::normalMaterialSet)
            .map(Ghost3D.MaterialSet::dress)
            .toList();

        maze3D = new Maze3D(config3D, colorScheme, level, animationRegistry, ghostNormalDressMaterials);
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);

        disposables.add(maze3D);
    }

    /** @return lives counter visualization (optional if not created) */
    public Optional<LivesCounter3D> livesCounter3D() {
        return Optional.ofNullable(livesCounter3D);
    }

    /** @return Pac-Man 3D representation (optional if not created) */
    public Optional<PacBase3D> pac3D() {
        return Optional.ofNullable(pac3D);
    }

    /** @return immutable list of all ghost 3D representations */
    public List<MutableGhost3D> ghosts3D() {
        return List.copyOf(ghosts3D);
    }

    /** @return optional bonus visualization */
    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    /** @return animation registry for this level */
    public AnimationRegistry animationManager() {
        return animationRegistry;
    }

    /**
     * Called once per game tick/frame to update all dynamic elements.
     */
    public void update() {
        pac3D.update(level);
        ghosts3D.forEach(ghost3D -> ghost3D.update(level));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(level));
        if (maze3D != null) {
            maze3D.house().update(level);
        }
        updateLivesCounter3D();
    }

    /**
     * Updates the lives counter visibility and count based on game state.
     */
    private void updateLivesCounter3D() {
        if (livesCounter3D != null) {
            final GameControl gameControl = level.game().control();
            final boolean oneMore = gameControl.state().nameMatches(GameControl.CommonGameState.STARTING_GAME_OR_LEVEL.name())
                && !level.pac().isVisible();
            final boolean visible = level.game().canStartNewGame();
            int lifeCount = level.game().lifeCount() - 1;
            // when the game starts and Pac-Man is not yet visible, show one more
            if (oneMore) lifeCount += 1;
            livesCounter3D.livesCountProperty().set(lifeCount);
            livesCounter3D.setVisible(visible);
        }
    }

    /**
     * Plays the level completion animation sequence and resets game timer.
     *
     * @param state the current game state (used to determine cut-scene follow-up)
     */
    public void playLevelEndAnimation(State<Game> state) {
        final boolean cutSceneFollows = level.cutSceneNumber() != 0;
        final Animation levelCompletedAnimation = animations.selectLevelCompleteAnimation(cutSceneFollows).animationFX();
        final PerspectiveID perspectiveBeforeAnimation = GameUI.PROPERTY_3D_PERSPECTIVE_ID.get();

        final var animationSequence = new SequentialTransition(
            pauseSecThen(2, () -> {
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            levelCompletedAnimation,
            pauseSec(0.25)
        );

        animationSequence.setOnFinished(_ -> {
            GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
            state.timer().expire();
        });

        state.timer().resetIndefiniteTime(); // freeze game control until animation ends
        animationSequence.play();
    }

    /**
     * Handles Pac-Man eating food at the given tile (pellet or energizer).
     *
     * @param tile the tile where food was eaten
     */
    public void eatFood(Vector2i tile) {
        final Energizer3D energizer3D = maze3D.food().energizers3D().stream()
            .filter(e3D -> tile.equals(e3D.tile()))
            .findFirst().orElse(null);
        if (energizer3D != null) {
            maze3D.food().createEnergizerExplosion(energizer3D);
            energizer3D.onEaten();
        } else {
            maze3D.food().pellets3D().stream()
                .filter(pellet3D -> tile.equals(pellet3D.tile()))
                .findFirst()
                .ifPresent(this::eatPellet3D);
        }
    }

    /**
     * Removes all pellet visualizations (used when all pellets are eaten at once).
     */
    public void eatAllPellets3D() {
        maze3D.food().pellets3D().forEach(pellet3D -> getChildren().remove(pellet3D));
    }

    /**
     * Schedules removal of a single pellet after a short delay (visual feedback).
     *
     * @param pellet3D the pellet shape to remove
     */
    public void eatPellet3D(Pellet3D pellet3D) {
        pauseSecThen(0.05, () -> getChildren().remove(pellet3D)).play();
    }

    /**
     * Displays the "READY!" message centered under the ghost house.
     */
    public void showReadyMessage() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.optHouse().ifPresentOrElse(house -> {
            final Vector2f center = house.centerPositionUnderHouse();
            showAnimatedMessage(READY_MESSAGE_TEXT, READY_MESSAGE_DISPLAY_SECONDS, center.x(), center.y());
        }, () -> Logger.error("Cannot display READY message: no house in this game level! WTF?"));
    }

    /**
     * Displays the test mode overlay message.
     */
    public void showTestMessage() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final double x = terrain.numCols() * HTS;
        final double y = (terrain.numRows() - 2) * TS;
        showAnimatedMessage(TEST_MESSAGE_TEXT.formatted(level.number()), 5, x, y);
    }

    /**
     * Shows a temporary animated message at the specified world coordinates.
     *
     * @param messageText    message content
     * @param displaySeconds duration before fade-out
     * @param centerX        x-coordinate (world units)
     * @param centerY        y-coordinate (world units)
     */
    public void showAnimatedMessage(String messageText, float displaySeconds, double centerX, double centerY) {
        if (messageView != null) {
            messageView.dispose();
            getChildren().remove(messageView);
        }
        messageView = MessageView.builder()
            .backgroundColor(Color.BLACK)
            .borderColor(Color.WHITE)
            .displaySeconds(displaySeconds)
            .font(GameUI_Resources.FONT_ARCADE_6)
            .text(messageText)
            .textColor(Color.YELLOW)
            .build(animationRegistry);

        getChildren().add(messageView);
        disposables.add(messageView);

        messageView.showCenteredAt(centerX, centerY);
    }

    /**
     * Replaces or creates the bonus visualization for the given bonus item.
     *
     * @param bonus the current bonus model
     */
    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        final ActorConfig3D actorConfig = config3D.actor();
        bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), actorConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  actorConfig.bonusPointsWidth());
        bonus3D.showEdible();
        getChildren().add(bonus3D);

        disposables.add(bonus3D);
    }

    /**
     * Rebuilds the level counter visualization using the latest configuration.
     *
     * @param config current level counter settings
     */
    public void rebuildLevelCounter3D(LevelCounterConfig3D config) {
        if (levelCounter3D != null) {
            levelCounter3D.rebuild(config, level);
        }
    }

    /**
     * Updates draw mode (wireframe/solid) for all relevant 3D shapes.
     * Called when {@link GameUI#PROPERTY_3D_DRAW_MODE} changes.
     */
    private void handleDrawModeChange(ObservableValue<? extends DrawMode> obs, DrawMode oldDrawMode, DrawMode drawMode) {
        final Predicate<Node> excludeNone = _ -> false;
        try {
            if (maze3D != null) {
                setDrawModeExcluding(maze3D, node -> node instanceof Pellet3D, drawMode);
            }
            if (pac3D != null) {
                setDrawModeExcluding(pac3D, excludeNone, drawMode);
            }
            if (livesCounter3D != null) {
                setDrawModeExcluding(livesCounter3D, excludeNone, drawMode);
            }
            if (ghosts3D != null) {
                ghosts3D.forEach(ghost3D -> setDrawModeExcluding(ghost3D, excludeNone, drawMode));
            }
        }
        catch (Exception x) {
            Logger.error(x, "Could not change 3D draw mode");
        }
    }

    /**
     * Applies the given draw mode to all {@link Shape3D} descendants of a node,
     * excluding those matching the filter.
     *
     * @param node             root node to search (null is ignored)
     * @param exclusionFilter  shapes matching this predicate are skipped
     * @param drawMode         new draw mode (wireframe/solid)
     */
    private static void setDrawModeExcluding(Node node, Predicate<Node> exclusionFilter, DrawMode drawMode) {
        if (node == null) return; //TODO why does this happen?
        node.lookupAll("*").stream()
            .filter(exclusionFilter.negate())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    /**
     * Releases all resources held by this level.
     * <p>
     * Clears animations, unbinds listeners, disposes all registered components,
     * cleans lights and the entire scene graph, and removes all children.
     */
    public void dispose() {
        Logger.info("Disposing game level 3D...");
        animationRegistry.clear();
        PROPERTY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        cleanupLight(ambientLight); ambientLight = null;
        cleanupLight(ghostLight);   ghostLight = null;
        disposables.forEach(Disposable::dispose);
        disposables.clear();
        if (livesCounterShapes != null) {
            disposeAll(List.of(livesCounterShapes));
            livesCounterShapes = null;
        }
        cleanupGroup(this, true);
        Logger.info("Cleaned and removed all nodes under game level 3D");
    }
}
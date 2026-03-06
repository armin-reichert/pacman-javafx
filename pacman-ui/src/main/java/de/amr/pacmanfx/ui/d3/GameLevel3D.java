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
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d3.config.*;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of game level.
 */
public class GameLevel3D extends Group implements Disposable {

    private static MeshView[] createMeshViews(int n, Mesh mesh) {
        final var meshViews = new MeshView[n];
        for (int i = 0; i < meshViews.length; ++i) {
            meshViews[i] = new MeshView(mesh);
        }
        return meshViews;
    }

    private final GameLevel level;
    private final UIConfig uiConfig;
    private final AnimationRegistry animationRegistry = new AnimationRegistry();

    private MeshView[] ghostDressMeshViews;
    private MeshView[] ghostPupilsMeshViews;
    private MeshView[] ghostEyesMeshViews;

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

    public GameLevel3D(GameUI ui, GameLevel level) {
        requireNonNull(ui);
        this.level = requireNonNull(level);
        this.uiConfig = ui.currentConfig();

        final int numGhosts = (int) level.ghosts().count();
        ghostDressMeshViews  = createMeshViews(numGhosts, Models3D.GHOST_MODEL.dressMesh());
        ghostPupilsMeshViews = createMeshViews(numGhosts, Models3D.GHOST_MODEL.pupilsMesh());
        ghostEyesMeshViews   = createMeshViews(numGhosts, Models3D.GHOST_MODEL.eyeballsMesh());

        createLevelCounter3D(uiConfig.config3D().levelCounter());
        createLivesCounter3D(uiConfig.config3D().livesCounter());

        createPac3D(uiConfig.config3D().actor());

        ghosts3D = level.ghosts().map(ghost -> createMutatingGhost3D(uiConfig.config3D().actor(), ghost)).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.init(level));

        final List<PhongMaterial> ghostNormalDressMaterials = ghosts3D.stream()
            .map(MutableGhost3D::ghost3D)
            .map(Ghost3D::normalMaterialSet)
            .map(Ghost3D.MaterialSet::dress)
            .toList();
        createMaze3D(uiConfig.config3D(), ghostNormalDressMaterials);

        createLights();

        // Note: The order in which children are added matters!
        // Walls and house must be added *after* the actors and swirls, otherwise the transparency is not working correctly.
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

        PROPERTY_3D_DRAW_MODE.addListener(this::handleDrawModeChange);
        setMouseTransparent(true); // this increases performance, they say...
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public GameLevel level() {
        return level;
    }

    public Maze3D maze3D() {
        return maze3D;
    }

    public PointLight ghostLight() {
        return ghostLight;
    }

    public Optional<GameLevel3DAnimations> animations() {
        return Optional.ofNullable(animations);
    }

    public void setAnimations(GameLevel3DAnimations animations) {
        this.animations = requireNonNull(animations);
    }

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

    private MutableGhost3D createMutatingGhost3D(ActorConfig3D actorConfig, Ghost ghost) {
        final byte id = ghost.personality();
        final var mutatingGhost3D = new MutableGhost3D(
            animationRegistry,
            ghost,
            createGhostColorSet(id),
            ghostDressMeshViews[id],
            ghostPupilsMeshViews[id],
            ghostEyesMeshViews[id],
            actorConfig.ghostSize(),
            level.numFlashes()
        );
        mutatingGhost3D.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        ));
        return mutatingGhost3D;
    }

    private boolean outsideWorld(Ghost ghost) {
        Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
    }

    private void createPac3D(ActorConfig3D actorConfig) {
        pac3D = uiConfig.createPac3D(animationRegistry, level.pac(), actorConfig.pacSize());
        pac3D.init(level);
    }

    private void createLivesCounter3D(LivesCounterConfig3D config) {
        final double shapeSize  = config.shapeSize();
        final int capacity      = config.capacity();
        final Color pillarColor = config.pillarColor();
        final Color plateColor  = config.plateColor();
        livesCounterShapes = new Node[capacity];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = uiConfig.createLivesCounterShape3D(shapeSize);
        }
        livesCounter3D = new LivesCounter3D(animationRegistry, livesCounterShapes);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(pillarColor);
        livesCounter3D.plateColorProperty().set(plateColor);
    }

    private void createLevelCounter3D(LevelCounterConfig3D config) {
        WorldMap worldMap = level.worldMap();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (worldMap.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-config.elevation());
    }

    private void createLights() {
        ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PROPERTY_3D_LIGHT_COLOR);

        ghostLight = new PointLight();
    }

    private void createMaze3D(Config3D config3D, List<PhongMaterial> ghostMaterials) {
        WorldMapColorScheme colorScheme = adjustColorScheme(config3D.maze(), uiConfig.colorScheme(level.worldMap()));
        maze3D = new Maze3D(config3D, colorScheme, level, animationRegistry, ghostMaterials);
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
    }

    private WorldMapColorScheme adjustColorScheme(MazeConfig3D mazeConfig3D, WorldMapColorScheme proposedColorScheme) {
        final boolean isFillColorDark = Color.valueOf(proposedColorScheme.wallFill()).getBrightness() < 0.1;
        return isFillColorDark
            ? new WorldMapColorScheme(
                mazeConfig3D.darkWallFillColor(),
                proposedColorScheme.wallStroke(),
                proposedColorScheme.door(),
                proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public PacBase3D pac3D() { return pac3D; }

    public List<MutableGhost3D> ghosts3D() { return Collections.unmodifiableList(ghosts3D); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }


    public AnimationRegistry animationManager() { return animationRegistry; }

    /**
     * Called on each clock tick (frame).
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

    public void onStartingGame() {
        maze3D.food().energizers3D().forEach(Energizer3D::stopPumping);
        if (levelCounter3D != null) {
            levelCounter3D.rebuild(uiConfig.config3D().levelCounter(), level);
        }
    }

    public void onHuntingStart() {
        pac3D.init(level);
        ghosts3D.forEach(ghost3D -> ghost3D.init(level));
        maze3D.food().energizers3D().forEach(Energizer3D::startPumping);
        maze3D.food().startAnimation();
        if (animations != null) {
            animations.ghostLightAnimation().playFromStart();
        }
    }

    public void onPacManDying(State<Game> gameState, SoundManager soundManager) {
        soundManager.stopAll();
        if (animations != null) {
            animations.ghostLightAnimation().stop();
            animations.wallColorFlashingAnimation().stop();
        }
        ghosts3D.forEach(MutableGhost3D::stopAllAnimations);
        bonus3D().ifPresent(Bonus3D::expire);
        // Do one last update before dying animation starts
        pac3D.update(level);

        gameState.timer().resetIndefiniteTime(); // keep game state until Pac-Man animation ends
        final var dyingAnimation = new SequentialTransition(
            pauseSec(1.5),
            doNow(() -> soundManager.play(SoundID.PAC_MAN_DEATH)),
            pac3D.dyingAnimation().animationFX(),
            pauseSec(0.5)
        );
        dyingAnimation.setOnFinished(_ -> gameState.timer().expire());
        dyingAnimation.play();
    }

    public void onEatingGhost() {
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            byte personality = killedGhost.personality();
            int killedIndex = level.energizerVictims().indexOf(killedGhost);
            Image pointsImage = uiConfig.killedGhostPointsImage(killedIndex);
            ghosts3D.get(personality).setNumberImage(pointsImage);
        });
    }

    public void onLevelComplete(State<Game> state, SoundManager soundManager) {
        soundManager.stopAll();
        animationRegistry.stopAllAnimations();

        maze3D.food().stopAnimation();
        maze3D.particlesGroup().getChildren().clear();
        maze3D.food().energizers3D().forEach(Energizer3D::stopPumping); //TODO needed?
        // hide 3D food explicitly because level might have been completed using cheat!
        maze3D.food().pellets3D().forEach(pellet3D -> pellet3D.setVisible(false));
        maze3D.food().energizers3D().forEach(Energizer3D::hide);
        maze3D.house().hideDoors();

        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));

        if (messageView != null) {
            messageView.setVisible(false);
        }

        if (animations == null) {
            pauseSecThen(2, () -> state.timer().expire()).play();
            return;
        }

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
            maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
            GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            state.timer().expire();
        });

        state.timer().resetIndefiniteTime(); // game continues after animation sequence ends
        animationSequence.play();
    }

    public void onGameOver(State<Game> state, SoundManager soundManager) {
        state.timer().restartSeconds(3);
        animations.ghostLightAnimation().stop();
        maze3D.food().energizers3D().forEach(Energizer3D::hide);
        maze3D.food().stopAnimation();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        soundManager.stopAll();
        soundManager.play(SoundID.GAME_OVER);
    }

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

    public void eatAllPellets3D() {
        maze3D.food().pellets3D().forEach(pellet3D -> getChildren().remove(pellet3D));
    }

    // Removes the pellet after a small delay to let pellet not directly disappear when Pac-Man enters the tile
    public void eatPellet3D(Pellet3D pellet3D) {
        pauseSecThen(0.05, () -> getChildren().remove(pellet3D)).play();
    }

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
        messageView.showCenteredAt(centerX, centerY);
    }

    public void updateBonus3D(Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        final ActorConfig3D actorConfig = uiConfig.config3D().actor();
        bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), actorConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  actorConfig.bonusPointsWidth());
        getChildren().add(bonus3D);
        bonus3D.showEdible();
    }

    public void rebuildLevelCounter3D(LevelCounterConfig3D config) {
        if (levelCounter3D != null) {
            levelCounter3D.rebuild(config, level);
        }
    }

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

    private static void setDrawModeExcluding(Node node, Predicate<Node> exclusionFilter, DrawMode drawMode) {
        if (node == null) return; //TODO why does this happen?
        node.lookupAll("*").stream()
            .filter(exclusionFilter.negate())
            .filter(Shape3D.class::isInstance)
            .map(Shape3D.class::cast)
            .forEach(shape3D -> shape3D.setDrawMode(drawMode));
    }

    public void dispose() {
        Logger.info("Disposing game level 3D...");

        animationRegistry.clear();

        PROPERTY_3D_DRAW_MODE.removeListener(this::handleDrawModeChange);
        Logger.info("Removed 'draw mode' listener");

        getChildren().clear();
        Logger.info("Removed all nodes under game level");

        if (ambientLight != null) {
            ambientLight.colorProperty().unbind();
            ambientLight = null;
            Logger.info("Unbound and cleared ambient light");
        }
        if (ghostLight != null) {
            ghostLight.colorProperty().unbind();
            ghostLight = null;
            Logger.info("Unbound and cleared ghost light");
        }
        if (maze3D != null) {
            maze3D.dispose();
            maze3D = null;
        }
        if (livesCounterShapes != null) {
            disposeAll(List.of(livesCounterShapes));
            livesCounterShapes = null;
        }
        if (livesCounter3D != null) {
            livesCounter3D.dispose();
            livesCounter3D = null;
            Logger.info("Disposed lives counter 3D");
        }
        if (levelCounter3D != null) {
            levelCounter3D.dispose();
            levelCounter3D = null;
            Logger.info("Disposed level counter 3D");
        }
        if (pac3D != null) {
            pac3D.dispose();
            pac3D = null;
            Logger.info("Disposed Pac 3D");
        }
        if (ghosts3D != null) {
            disposeAll(ghosts3D);
            ghosts3D = null;
            Logger.info("Disposed 3D ghosts");
        }
        if (bonus3D != null) {
            bonus3D.dispose();
            bonus3D = null;
            Logger.info("Disposed 3D bonus");
        }
        if (messageView != null) {
            messageView.dispose();
            messageView = null;
            Logger.info("Disposed message view");
        }
        if (ghostDressMeshViews != null) {
            for (MeshView meshView : ghostDressMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostDressMeshViews = null;
            Logger.info("Cleared dress mesh views");
        }
        if (ghostPupilsMeshViews != null) {
            for (MeshView meshView : ghostPupilsMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostPupilsMeshViews = null;
            Logger.info("Cleared pupils mesh views");
        }
        if (ghostEyesMeshViews != null) {
            for (MeshView meshView : ghostEyesMeshViews) {
                meshView.setMesh(null);
                meshView.materialProperty().unbind();
                meshView.setMaterial(null);
            }
            ghostEyesMeshViews = null;
            Logger.info("Cleared eyes mesh views");
        }
    }
}
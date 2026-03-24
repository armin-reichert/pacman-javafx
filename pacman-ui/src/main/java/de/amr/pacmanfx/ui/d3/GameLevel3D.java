/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d3.animation.GameLevel3DAnimations;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.actor.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.actor.GhostAppearance3D;
import de.amr.pacmanfx.uilib.model3D.actor.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.ui.GameUI.*;
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
 * @see Pac3D
 * @see GhostAppearance3D
 * @see DisposableGraphicsObject
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    private final GameLevel level;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final List<Disposable> disposables = new ArrayList<>();

    private LevelCounter3D levelCounter3D;

    private LivesCounter3D livesCounter3D;
    private Node[] livesCounterShapes;

    private Maze3D maze3D;
    private MazeFood3D food3D;
    private Pac3D pac3D;
    private List<GhostAppearance3D> ghosts3D;
    private Bonus3D bonus3D;

    private GameLevel3DAnimations animations;
    private GameLevel3DMessageManager messageManager;

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param uiConfig global UI configuration (provides 3D settings, colors, models)
     * @param level    the game level to visualize
     */
    public GameLevel3D(UIConfig uiConfig, GameLevel level) {
        requireNonNull(uiConfig);
        this.level = requireNonNull(level);

        final EntityConfig entityConfig = requireNonNull(uiConfig.entityConfig());

        createPac3D(uiConfig.factory3D(), entityConfig.pacConfig());
        createGhosts3D(uiConfig.factory3D(), entityConfig.ghostConfigs());

        // These materials are used by the energizer particles
        final List<PhongMaterial> ghostDressMaterials = ghosts3D.stream()
            .map(mutableGhost3D -> mutableGhost3D.ghost3D().materials().normal().dress())
            .toList();

        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        createMaze3D(uiConfig, colorScheme);
        createFood3D(uiConfig, ghostDressMaterials);

        createLevelCounter3D(uiConfig, entityConfig.levelCounter());
        createLivesCounter3D(uiConfig, entityConfig.livesCounter());
        createMessageManager();

        arrangeEntities();

        setMouseTransparent(true); // this increases performance they say...
    }

    /**
     * Releases all resources held by this level.
     * <p>
     * Clears animations, unbinds listeners, disposes all registered components,
     * cleans lights and the entire scene graph, and removes all children.
     */
    @Override
    public void dispose() {
        Logger.info("Disposing game level 3D...");
        animationRegistry.clear();
        if (livesCounterShapes != null) {
            disposeAll(List.of(livesCounterShapes));
            livesCounterShapes = null;
        }
        disposables.forEach(Disposable::dispose);
        disposables.clear();
        cleanupGroup(this, true);
        Logger.info("Cleaned and removed all nodes under game level 3D");
    }

    // Accessors

    /** @return registry for all level-specific animations */
    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    /** @return the underlying game level model */
    public GameLevel level() {
        return level;
    }

    /** @return the maze visualization component */
    public Maze3D maze3D() {
        return maze3D;
    }

    public MazeFood3D food3D() {
        return food3D;
    }

    public GameLevel3DMessageManager messageManager() {
        return messageManager;
    }

    /** @return optional animations controller for this level */
    public Optional<GameLevel3DAnimations> animations() {
        return Optional.ofNullable(animations);
    }

    /** Sets the animation controller for level-specific effects */
    public void setAnimations(GameLevel3DAnimations animations) {
        this.animations = requireNonNull(animations);
        //TODO: reconsider
        final PointLight ghostLight = animations.ghostLightAnimation().light();
        getChildren().add(ghostLight);
    }

    /** @return lives counter visualization */
    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    /** @return Pac-Man 3D representation */
    public Pac3D pac3D() {
        return pac3D;
    }

    /** @return immutable list of all ghost 3D representations */
    public List<GhostAppearance3D> ghosts3D() {
        return List.copyOf(ghosts3D);
    }

    /** @return optional bonus visualization */
    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    // Lifecycle and updates

    /**
     * Called once per game tick/frame to update all dynamic elements.
     */
    public void update() {
        pac3D.update(level);
        ghosts3D.forEach(ghost3D -> ghost3D.update(level));
        bonus3D().ifPresent(bonus3D -> bonus3D.update(level));
        if (maze3D.house() != null) {
            maze3D.house().update(level);
        }
        livesCounter3D.update(level);
    }

    /**
     * Replaces or creates the bonus visualization for the given bonus item.
     *
     * @param uiConfig the UI configuration
     * @param bonus the new bonus
     */
    public void replaceBonus3D(UIConfig uiConfig, Bonus bonus) {
        requireNonNull(bonus);
        if (bonus3D != null) {
            getChildren().remove(bonus3D);
            bonus3D.dispose();
        }
        final BonusConfig bonusConfig = uiConfig.entityConfig().bonusConfig();
        bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), bonusConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  bonusConfig.bonusPointsWidth());
        bonus3D.showEdible();
        getChildren().add(bonus3D);

        disposables.add(bonus3D);
    }

    /**
     * Rebuilds the level counter visualization using the latest configuration.
     */
    public void rebuildLevelCounter3D(LevelCounterConfig3D levelCounterConfig3D) {
        levelCounter3D.rebuild(levelCounterConfig3D, level.game().levelCounter().symbols());
    }

    // private

    /**
     * Arranges all direct children in the correct rendering order.
     * <p>
     * Order matters for correct transparency: actors and effects must appear
     * in front of walls/house.
     */
    private void arrangeEntities() {
        getChildren().add(maze3D.floor());
        getChildren().addAll(maze3D.particlesGroup());
        getChildren().add(levelCounter3D);
        getChildren().add(livesCounter3D);
        getChildren().addAll(pac3D, pac3D.light());
        getChildren().addAll(ghosts3D);
        getChildren().addAll(food3D.energizers3D().stream().map(Energizer3D::shape).toList());
        getChildren().addAll(food3D.pellets3D().stream().map(Pellet3D::shape).toList());
        getChildren().add(maze3D.house().root());
        getChildren().add(maze3D.house().doors());
        getChildren().add(maze3D);
    }

    /**
     * Creates and initializes the 3D representation of Pac-Man.
     */
    private void createPac3D(Factory3D factory3D, PacConfig pacConfig) {
        pac3D = factory3D.createPac3D(level.pac(), pacConfig, animationRegistry);
        pac3D.init(level);

        disposables.add(pac3D);
    }

    /**
     * Creates and initializes all ghost 3D representations.
     */
    private void createGhosts3D(Factory3D factory3D, List<GhostConfig> ghostConfigs) {
        ghosts3D = level.ghosts()
            .map(ghost -> createGhostAppearance3D(factory3D, ghostConfigs, ghost))
            .toList();

        ghosts3D.forEach(ghost3D -> ghost3D.init(level));

        disposables.addAll(ghosts3D);
    }

    /**
     * Creates a 3D ghost representation for the given model ghost.
     */
    private GhostAppearance3D createGhostAppearance3D(Factory3D factory3D, List<GhostConfig> ghostConfigs, Ghost ghost) {
        final var ghostAppearance3D = factory3D.createGhostAppearance3D(
            ghost,
            ghostConfigs.get(ghost.personality()),
            animationRegistry
        );
        ghostAppearance3D.setNumFlashes(level.numFlashes());
        final BooleanBinding visibleInsideWorld = Bindings.createBooleanBinding(
            () -> ghost.isVisible() && !outsideWorld(level.worldMap(), ghost),
            ghost.visibleProperty(), ghost.positionProperty()
        );
        ghostAppearance3D.visibleProperty().bind(visibleInsideWorld);
        return ghostAppearance3D;
    }

    /**
     * Creates and initializes the lives counter visualization.
     */
    private void createLivesCounter3D(UIConfig uiConfig, LivesCounterConfig3D config) {
        livesCounterShapes = new Node[config.capacity()];
        for (int i = 0; i < livesCounterShapes.length; ++i) {
            livesCounterShapes[i] = uiConfig.factory3D().createLivesCounterShape3D(uiConfig.entityConfig());
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
    private void createLevelCounter3D(UIConfig uiConfig, LevelCounterConfig3D config) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-config.elevation());

        disposables.add(levelCounter3D);
    }

    /**
     * Creates and initializes the maze visualization, including color scheme adjustment.
     */
    private void createMaze3D(UIConfig uiConfig, WorldMapColorScheme colorScheme) {
        maze3D = new Maze3D(
            level,
            uiConfig.factory3D(),
            uiConfig.entityConfig(),
            colorScheme,
            animationRegistry
        );
        maze3D.wallOpacityProperty().bind(PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
        maze3D.floorColorProperty().bind(PROPERTY_3D_FLOOR_COLOR);

        disposables.add(maze3D);
    }

    private void createFood3D(UIConfig uiConfig, List<PhongMaterial> ghostMaterials) {

        food3D = new MazeFood3D(
            uiConfig.factory3D(),
            uiConfig.entityConfig().pellet(),
            uiConfig.entityConfig().energizer(),
            uiConfig.colorScheme(level.worldMap()),
            animationRegistry, level, ghostMaterials,
            maze3D);

        disposables.add(food3D);
    }

    private void createMessageManager() {
        this.messageManager = new GameLevel3DMessageManager(animationRegistry, this);
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.optHouse().ifPresentOrElse(
            house -> messageManager.setReadyMessageCenter(house.centerPositionUnderHouse()),
            () -> {
                Logger.error("No house in this game level! WTF?");
                final double x = terrain.numCols() * HTS, y = terrain.numRows() * HTS;
                messageManager.setReadyMessageCenter(vec2_float(x, y));
            });
        messageManager.setTestMessageCenter(
            vec2_float(terrain.numCols() * HTS, (terrain.numRows() - 2) * TS));

        disposables.add(messageManager);
    }

    /**
     * Determines if the given ghost's center position is outside the visible world bounds.
     *
     * @param worldMap the world map
     * @param ghost the ghost to check
     * @return true if the ghost is outside the world bounds
     */
    private static boolean outsideWorld(WorldMap worldMap, Ghost ghost) {
        final Vector2f center = ghost.center();
        return center.x() < HTS || center.x() > worldMap.numCols() * TS - HTS;
    }
}
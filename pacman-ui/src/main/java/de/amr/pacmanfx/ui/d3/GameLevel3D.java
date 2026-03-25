/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelAware;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.BonusConfig;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.config.LevelCounterConfig3D;
import de.amr.pacmanfx.ui.config.LivesCounterConfig3D;
import de.amr.pacmanfx.ui.d3.animation.GameLevel3DAnimations;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.Vector2f.vec2_float;
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
public class GameLevel3D extends Group implements GameLevelAware, DisposableGraphicsObject {

    private final GameLevel level;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final Set<GameLevelAware> entities = new HashSet<>();

    private Node[] livesCounterShapes;

    private LevelCounter3D levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private Maze3D maze3D;
    private MazeFood3D food3D;
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
        createEntities(uiConfig);
        addChildrenInRightOrder();
        setMouseTransparent(true); // this increases performance they say...
    }

    public void initPacZPosition(Pac3D pac3D) {
        // Set height over floor. Top of floor is at z=0.
        pac3D.setTranslateZ(-0.5 * pac3D.getBoundsInLocal().getDepth());
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
        if (food3D != null) {
            food3D.dispose();
            food3D = null;
        }
        if (messageManager != null) {
            messageManager.dispose();
            messageManager = null;
        }
        entities.stream().filter(Disposable.class::isInstance).map(Disposable.class::cast).forEach(Disposable::dispose);
        entities.clear();

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
        return entities.stream().filter(Pac3D.class::isInstance).map(Pac3D.class::cast).findFirst().orElseThrow();
    }

    /** @return stream of all ghost 3D representations */
    public Stream<GhostAppearance3D> ghostAppearances3D() {
        return entities.stream().filter(GhostAppearance3D.class::isInstance).map(GhostAppearance3D.class::cast);
    }

    /** @return optional bonus visualization */
    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    // GameLevelAware interface

    @Override
    public void init(GameLevel level) {
        entities.forEach(entity -> entity.init(level));
    }

    /**
     * Called once per game tick/frame to update all dynamic elements.
     */
    @Override
    public void update(GameLevel level) {
        entities.forEach(entity -> entity.update(level));
    }

    // Others

    /**
     * Replaces or creates the bonus visualization for the given bonus item.
     *
     * @param uiConfig the UI configuration
     * @param bonus the new bonus
     */
    public void addOrReplaceBonus3D(UIConfig uiConfig, Bonus bonus) {
        requireNonNull(uiConfig);
        requireNonNull(bonus);
        final BonusConfig bonusConfig = uiConfig.entityConfig().bonusConfig();
        if (bonus3D != null) {
            bonus3D.dispose();
            entities.remove(bonus3D);
            getChildren().remove(bonus3D);
        }
        bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), bonusConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  bonusConfig.bonusPointsWidth());
        bonus3D.showEdible();

        entities.add(bonus3D);
        getChildren().add(bonus3D);
    }

    // private

    private List<PhongMaterial> dressMaterials(Collection<GhostAppearance3D> ghostsAppearances) {
        return ghostsAppearances.stream()
            .map(GhostAppearance3D::ghost3D)
            .map(Ghost3D::materials)
            .map(GhostMaterials::normal)
            .map(GhostComponentMaterials::dress)
            .toList();
    }

    /**
     * Arranges all direct children in the correct rendering order.
     * <p>
     * Order matters for correct transparency: actors and effects must appear
     * in front of walls/house.
     */
    private void addChildrenInRightOrder() {
        getChildren().add(maze3D.floor());
        getChildren().addAll(maze3D.particlesGroup());
        getChildren().add(levelCounter3D);
        getChildren().add(livesCounter3D);
        getChildren().add(pac3D());
        pac3D().light().ifPresent(pacLight -> getChildren().add(pacLight));
        getChildren().addAll(ghostAppearances3D().toList());
        getChildren().addAll(food3D.energizers3D().stream().map(Energizer3D::shape).toList());
        getChildren().addAll(food3D.pellets3D().stream().map(Pellet3D::shape).toList());
        getChildren().add(maze3D.house().root());
        getChildren().add(maze3D.house().doors());
        getChildren().add(maze3D);
    }

    private void createEntities(UIConfig uiConfig) {
        entities.clear();
        final EntityConfig entityConfig = requireNonNull(uiConfig.entityConfig());
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        createPac3D(uiConfig.factory3D(), entityConfig.pacConfig());
        createGhostAppearances3D(uiConfig.factory3D(), entityConfig.ghostConfigs());
        createMaze3D(uiConfig, colorScheme);
        createLevelCounter3D(uiConfig, entityConfig.levelCounter());
        createLivesCounter3D(uiConfig, entityConfig.livesCounter());
        // food is added to the scene children list
        createFood3D(uiConfig, dressMaterials(ghostAppearances3D().toList()));
        createMessageManager();
    }

    private void createPac3D(Factory3D factory3D, PacConfig pacConfig) {
        final Pac3D pac3D = factory3D.createPac3D(level.pac(), pacConfig, animationRegistry);
        pac3D.createPowerLight(pacConfig);
        initPacZPosition(pac3D);
        entities.add(pac3D);
    }

    private void createGhostAppearances3D(Factory3D factory3D, List<GhostConfig> ghostConfigs) {
        final List<GhostAppearance3D> ghostAppearances3D = level.ghosts()
            .map(ghost -> {
                final var ghostAppearance3D = createGhostAppearance3D(factory3D, ghostConfigs, ghost);
                ghostAppearance3D.init(level);
                ghostAppearance3D.setTranslateZ(-0.5 * ghostAppearance3D.getBoundsInLocal().getDepth() - 1);
                return ghostAppearance3D;
            })
            .toList();
        entities.addAll(ghostAppearances3D);
    }

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
        entities.add(livesCounter3D);
    }

    private void createLevelCounter3D(UIConfig uiConfig, LevelCounterConfig3D config) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-config.elevation());
        entities.add(levelCounter3D);
    }

    private void createMaze3D(UIConfig uiConfig, WorldMapColorScheme colorScheme) {
        maze3D = uiConfig.factory3D().createMaze3D(level, uiConfig.entityConfig(), colorScheme, animationRegistry);
        entities.add(maze3D);
    }

    private void createFood3D(UIConfig uiConfig, List<PhongMaterial> ghostMaterials) {
        food3D = new MazeFood3D(
            uiConfig.factory3D(),
            uiConfig.entityConfig().pellet(),
            uiConfig.entityConfig().energizer(),
            uiConfig.colorScheme(level.worldMap()),
            animationRegistry, level, ghostMaterials,
            maze3D);
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
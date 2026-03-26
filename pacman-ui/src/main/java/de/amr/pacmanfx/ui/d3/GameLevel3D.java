/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntitySet;
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
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    private final GameLevel level;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final GameLevelEntitySet entities = new GameLevelEntitySet();

    private MazeFood3D food3D;
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

    public void resetPacZPosition(Pac3D pac3D) {
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
        if (food3D != null) {
            food3D.dispose();
            food3D = null;
        }
        if (messageManager != null) {
            messageManager.dispose();
            messageManager = null;
        }
        entities.dispose();

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
    public Optional<Maze3D> maze3D() {
        return entities.entitiesOfType(Maze3D.class).findFirst();
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

    public void startTrackingPac() {
        if (livesCounter3D().isEmpty()) {
            Logger.error("Cannot track Pac-Man, no 3D lives counter exists");
            return;
        }
        if (pac3D().isEmpty()) {
            Logger.error("Cannot track Pac-Man, no 3D Pac-Man exists");
        }
        livesCounter3D().get().startTracking(pac3D().get());
    }

    public Optional<LevelCounter3D> levelCounter3D() {
        return entities.entitiesOfType(LevelCounter3D.class).findFirst();
    }

    /** @return lives counter visualization */
    public Optional<LivesCounter3D> livesCounter3D() {
        return entities.entitiesOfType(LivesCounter3D.class).findFirst();
    }

    /** @return Pac-Man 3D representation */
    public Optional<Pac3D> pac3D() {
        return entities.entitiesOfType(Pac3D.class).findFirst();
    }

    /** @return stream of all ghost 3D representations */
    public Stream<GhostAppearance3D> ghostAppearances3D() {
        return entities.entitiesOfType(GhostAppearance3D.class);
    }

    public Optional<GhostAppearance3D> ghostAppearance3D(byte personality) {
        return ghostAppearances3D().filter(ga3D -> ga3D.ghost().personality() == personality).findFirst();
    }

    /** @return optional bonus visualization */
    public Optional<Bonus3D> bonus3D() {
        return entities.entitiesOfType(Bonus3D.class).findFirst();
    }

    public void init(GameLevel level) {
        entities.init(level);
    }

    /**
     * Called once per game tick/frame to update all dynamic elements.
     */
    public void update(GameLevel level) {
        entities.update(level);
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
        bonus3D().ifPresent(bonus3D -> {
            bonus3D.dispose();
            entities.removeEntity(bonus3D);
            getChildren().remove(bonus3D);
        });
        final var bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), bonusConfig.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  bonusConfig.bonusPointsWidth());
        bonus3D.showEdible();

        entities.addEntity(bonus3D);
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
        maze3D().ifPresent(maze3D -> {
            getChildren().add(maze3D.floor());
            getChildren().addAll(maze3D.particlesGroup());
        });

        getChildren().add(levelCounter3D().orElseThrow());
        getChildren().add(livesCounter3D().orElseThrow());
        final Pac3D pac3D = pac3D().orElseThrow();
        getChildren().add(pac3D);
        pac3D.light().ifPresent(pacLight -> getChildren().add(pacLight));
        getChildren().addAll(ghostAppearances3D().toList());

        getChildren().addAll(food3D.energizers3D().stream().map(Energizer3D::shape).toList());
        getChildren().addAll(food3D.pellets3D().stream().map(Pellet3D::shape).toList());

        maze3D().ifPresent(maze3D -> {
            getChildren().add(maze3D.house().root());
            getChildren().add(maze3D.house().doors());
            getChildren().add(maze3D);
        });
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
        food3D = new MazeFood3D(uiConfig, animationRegistry, level, dressMaterials(ghostAppearances3D().toList()),
            maze3D().orElseThrow());
        createMessageManager();
    }

    private void createPac3D(Factory3D factory3D, PacConfig pacConfig) {
        final var pac3D = factory3D.createPac3D(level.pac(), pacConfig, animationRegistry);
        pac3D.createPowerLight(pacConfig);
        resetPacZPosition(pac3D);
        entities.addEntity(pac3D);
    }

    private void createGhostAppearances3D(Factory3D factory3D, List<GhostConfig> ghostConfigs) {
        final var ghostAppearances3D = level.ghosts()
            .map(ghost -> {
                final var ghostAppearance3D = createGhostAppearance3D(factory3D, ghostConfigs, ghost);
                ghostAppearance3D.init(level);
                ghostAppearance3D.setTranslateZ(-0.5 * ghostAppearance3D.getBoundsInLocal().getDepth() - 1);
                return ghostAppearance3D;
            })
            .toList();
        entities.addAllEntities(ghostAppearances3D);
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
        final var livesCounter3D = new LivesCounter3D(uiConfig, animationRegistry);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.pillarColorProperty().set(config.pillarColor());
        livesCounter3D.plateColorProperty().set(config.plateColor());
        entities.addEntity(livesCounter3D);
    }

    private void createLevelCounter3D(UIConfig uiConfig, LevelCounterConfig3D config) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS * (terrain.numCols() - 2));
        levelCounter3D.setTranslateY(2 * TS);
        levelCounter3D.setTranslateZ(-config.elevation());
        entities.addEntity(levelCounter3D);
    }

    private void createMaze3D(UIConfig uiConfig, WorldMapColorScheme colorScheme) {
        final var maze3D = uiConfig.factory3D().createMaze3D(level, uiConfig.entityConfig(), colorScheme, animationRegistry);
        entities.addEntity(maze3D);
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
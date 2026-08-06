/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.bonus.Bonus;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.ghost.GhostState;
import de.amr.pacmanfx.core.entities.house.House;
import de.amr.pacmanfx.core.entities.levelCounter.LevelCounter;
import de.amr.pacmanfx.core.entities.livescounter.LivesCounter;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.GhostLightRelayAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.gamescene.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.LevelCounterView3DAnimationID;
import de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.LevelCounterView3DComp;
import de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.LevelCounterView3DSystem;
import de.amr.pacmanfx.ui.gamescene.d3.entities.livescounter.LivesCounterView3DComp;
import de.amr.pacmanfx.ui.gamescene.d3.entities.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.settings.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.vm.Game3DSettingsVM;
import de.amr.pacmanfx.ui.vm.GameUISettingsVM;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.entities3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.entities3D.animation.Pool;
import de.amr.pacmanfx.uilib.entities3D.bonus.*;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DViewSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.Ghost3DAppearanceController;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.Ghost3DTransformController;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.Ghost3DWrapperToBeRemoved;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.PacSettings;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DTransformSystem;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    public enum AnimationID implements Named {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    //TODO remove
    public final List<Ghost3DWrapperToBeRemoved> ghosts3D = new ArrayList<>(4); // RED, PINK, CYAN, ORANGE

    private final Map<Vector2i, Energizer3D> energizer3DByTile = new HashMap<>();

    private final Map<Vector2i, Pellet3D> pellet3DByTile = new HashMap<>();

    private final GameContext gameContext;

    private final GameLevel level;

    private final GameVariantConfig gameVariantConfig;

    private final GameUISettingsVM viewModel;

    private final AnimationRegistry animationRegistry = new AnimationRegistry();

    private final PointLight ghostHunterLight = new PointLight();

    private Maze3D maze3D;

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    private MessageManager3D messageManager;

    public GameLevel3D(
        GameUISettingsVM viewModel,
        GameContext gameContext,
        GameVariantConfig gameVariantConfig)
    {
        this.viewModel = requireNonNull(viewModel);
        this.gameContext = requireNonNull(gameContext);
        this.gameVariantConfig = requireNonNull(gameVariantConfig);

        this.level = gameContext.assertLevel();

        createMaze3D();
        createFood3D();
        createPac3D(level.entities().pac());
        createGhosts3D();
        createLivesCounter3D();
        createMessageManager();

        buildHierarchy();

        createAnimations(Game3DSettingsVM.DEFAULT_PARTICLE_ANIMATION_CONFIG);

        setMouseTransparent(true); // this increases performance they say...
    }

    public void createAnimations(ParticlesAnimationConfig particlesConfig) {
        final GameVariantRenderConfig renderConfig = gameVariantConfig.renderConfig();
        final WorldMapColorSchemeImpl mapColorScheme = renderConfig.colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        animationRegistry.register(AnimationID.WALL_COLOR_FLASHING,
            new WallColorFlashingAnimation(mapColorScheme, maze3D.materials().get("wallTopMaterial")));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_FULL, new LevelCompletedAnimation(this));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(this));
        createEnergizerParticlesAnimation(particlesConfig);
        createGhostLightAnimation();
    }

    public void updateEntities() {
        updateLivesCounter();
        updateHouse();
        updatePac();
        updateGhosts();
        updateBonus();
    }

    @Override
    public void dispose() {
        animationRegistry.dispose();
        if (maze3D != null) {
            maze3D.dispose();
        }
        if (particlePool != null) {
            particlePool.dispose();
        }
        if (messageManager != null) {
            messageManager.dispose();
            messageManager = null;
        }
        cleanupGroup(this, true);
    }

    // Public accessors

    public Maze3D maze3D() {
        return maze3D;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return gameVariantConfig.optSoundEffects();
    }

    public GameLevel level() {
        return level;
    }

    public MessageManager3D messageManager() {
        return messageManager;
    }

    //TODO remove
    public Ghost3DWrapperToBeRemoved ghost3D(GhostPersonality personality) {
        requireNonNull(personality);
        return ghosts3D.get(personality.ordinal());
    }

    public Stream<Energizer3D> energizers3D() {
        return energizer3DByTile.values().stream();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(energizer3DByTile.get(tile));
    }

    public Stream<Pellet3D> pellets3D() {
        return pellet3DByTile.values().stream();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(pellet3DByTile.get(tile));
    }

    public void cleanupFoodAndParticles() {
        animationRegistry.optAnimation(AnimationID.PARTICLES).ifPresent(ManagedAnimation::stop);
        energizer3DByTile.values().forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        pellet3DByTile.values().forEach(pellet3D -> pellet3D.shape().setVisible(false));
        maze3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
//        Ufx.setDrawMode(entitySet3D.pac3D.root(), drawMode);
        for (Ghost3DWrapperToBeRemoved ghost3D : ghosts3D) {
            Ufx.setDrawMode(ghost3D.root(), drawMode);
        }
        Ufx.setDrawMode(maze3D, drawMode);
    }

    public void activateBonus3D(Bonus bonus) {
        ensureBonus3DViewExists(bonus);
        Bonus3DViewSystem.lookEdible(bonus);
    }

    public void ensureBonus3DViewExists(Bonus bonus) {
        if (!bonus.hasComponent(Bonus3DViewComp.class)) {
            final var view3D = createBonusView3D(bonus);
            getChildren().add(view3D.root());
            Bonus3DViewSystem.update(bonus, animationRegistry);
        }
    }

    // Private area, no trespassing!

    private void updateLivesCounter() {
        final LivesCounter livesCounter = level.entities().entitySet().uniqueOfType(LivesCounter.class);
        LivesCounterView3DSystem.update(livesCounter);
    }

    private void updatePac() {
        final Pac pac = level.entities().pac();
        Pac3DTransformSystem.update(pac, level);
        Pac3DAnimationSystem.update(pac, gameContext.systems().pacState());
        Pac3DAnimationSystem.updatePowerLight(pac);
    }

    private void updateGhosts() {
        //TODO remove
        ghosts3D.forEach(ghost3D -> ghost3D.update(gameContext));

        // In the new implementation, use:
        level.entities().ghosts().forEach(ghost -> {
            Ghost3DViewSystem.update(ghost);
            Ghost3DMovementSystem.update(ghost);
        });
    }

    private void updateHouse() {
        final House house = level.entities().entitySet().uniqueOfType(House.class);
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);

        boolean accessRequested = level
            .ghostsInAnyOfStates(Set.of(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE))
            .filter(ghost -> house.isDoorAt(WorldNavigationSystem.computeTile(ghost)))
            .anyMatch(GameEntity::isVisible);

        boolean ghostNearHouseEntry = level
            .ghostsInAnyOfStates(Set.of(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE))
            .filter(ghost -> ghost.pos().asVector2f().euclideanDist(house.floorplan().entryPosition())
                <= view3D.doorSensitivity())
            .anyMatch(GameEntity::isVisible);

        House3DSystem.showLight(house, ghostNearHouseEntry);
        House3DAnimationSystem.update(house, accessRequested);
    }

    private void updateBonus() {
        level.optBonus().ifPresent(bonus -> {
            ensureBonus3DViewExists(bonus);
            Bonus3DMovementSystem.update(bonus);
        });
    }

    private void createMaze3D() {
        final WorldMapColorSchemeImpl colorScheme = gameVariantConfig.renderConfig().colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().entitySet().uniqueOfType(House.class);
        maze3D = gameVariantConfig.factory3D().createMaze3D(
            house,
            terrain,
            gameVariantConfig.worldSettings(),
            colorScheme,
            animationRegistry);

        maze3D.drawModeProperty()      .bind(viewModel.common3D.drawModeProperty);
        maze3D.wallOpacityProperty()   .bind(viewModel.maze3D.wallOpacityProperty);
        maze3D.wallBaseHeightProperty().bind(viewModel.maze3D.wallHeightProperty);
        maze3D.floorColorProperty()    .bind(viewModel.maze3D.floorColorProperty);
    }

    private void createFood3D() {
        final WorldMapColorSchemeImpl colorScheme = gameVariantConfig.renderConfig().colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        final Pellet3DSettings pelletConfig3D = gameVariantConfig.worldSettings().pellet();
        final double pelletZ = maze3D.floorTop() - pelletConfig3D.floorElevation();

        final Energizer3DSettings energizerConfig3D = gameVariantConfig.worldSettings().energizer();
        final double energizerZ = maze3D.floorTop() - energizerConfig3D.floorElevation();

        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .forEach(tile -> {
                if (foodLayer.isEnergizerTile(tile)) {
                    energizer3DByTile.put(tile, createEnergizer3D(tile, energizerZ, foodMaterial));
                } else {
                    pellet3DByTile.put(tile, createPellet3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private Pellet3D createPellet3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Pellet3D pellet3D = gameVariantConfig.factory3D().createPellet3D(gameVariantConfig.worldSettings().pellet(), foodMaterial);
        pellet3D.setLocation(tile, z);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Energizer3D energizer3D = gameVariantConfig.factory3D().createEnergizer3D(
            gameVariantConfig.worldSettings().energizer(), foodMaterial, animationRegistry);
        energizer3D.setLocation(tile, z);
        return energizer3D;
    }

    private Bonus3DViewComp createBonusView3D(Bonus bonus) {
        final Bonus3DSettings config = gameVariantConfig.worldSettings().bonus();
        final GameVariantRenderConfig renderConfig = gameVariantConfig.renderConfig();
        final Bonus3DViewComp view3D = new Bonus3DViewComp(
            renderConfig.bonusSymbolImage(bonus.data().symbolCode()),
            config.symbolWidth(),
            renderConfig.bonusValueImage(bonus.data().symbolCode()),
            config.pointsWidth()
        );
        bonus.setComponent(Bonus3DViewComp.class, view3D);
        animationRegistry.register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());
        return  view3D;
    }

    public void createLevelCounterView3D(LevelCounter levelCounter) {
        if (!levelCounter.hasComponent(LevelCounterView3DComp.class)) {
            final LevelCounterView3DComp view3D = new LevelCounterView3DComp();
            levelCounter.setComponent(LevelCounterView3DComp.class, view3D);
            animationRegistry.register(LevelCounterView3DAnimationID.LEVEL_COUNTER_SPINNING, view3D.spinningAnimation());
            Logger.info("Level counter now has a 3D view");
        }
        else {
            Logger.info("Level counter already has a 3D view!");
        }

        // Recreate all 3D entries in the level counter group
        LevelCounterView3DSystem.updateLevelCounter3D(gameVariantConfig, levelCounter, level);

        // Add level counter 3D root into this group
        final LevelCounterView3DComp view3D = levelCounter.requireComponent(LevelCounterView3DComp.class);
        getChildren().add(view3D.root());
    }

    private void createPac3D(Pac pac) {
        final PacSettings settings = gameVariantConfig.worldSettings().pac();
        gameVariantConfig.factory3D().createPac3D(pac, settings, animationRegistry);
        pac.requireComponent(Pac3DViewComp.class).drawModeProperty().bind(viewModel.common3D.drawModeProperty);
    }

    private void createGhosts3D() {
        final List<GhostSettings> settings = gameVariantConfig.worldSettings().ghosts();

        //TODO remove
        ghosts3D.clear();
        for (var gp : GhostPersonality.values()) {
            final var ghostSettings = settings.get(gp.ordinal());
            final Ghost ghost = level.ghost(gp);
            final Ghost3DWrapperToBeRemoved ghost3D = createGhost3D(ghostSettings, ghost);
            ghost3D.drawModeProperty().bind(viewModel.common3D.drawModeProperty);
            ghost3D.init(gameContext);
            ghosts3D.add(ghost3D);
        }

        level.entities().ghosts().forEach(ghost -> {
            final var ghostSettings = settings.get(ghost.personality().ordinal());
            gameVariantConfig.factory3D().createGhost3D(ghost, ghostSettings, animationRegistry);
        });
    }

    private Ghost3DWrapperToBeRemoved createGhost3D(GhostSettings ghostConfig, Ghost ghost) {
        final Ghost3DWrapperToBeRemoved ghost3D = gameVariantConfig.factory3D().createGhost3D_obsolete(ghost, ghostConfig, animationRegistry);
        ghost3D.setAppearanceController(new Ghost3DAppearanceController());
        ghost3D.setTransformController(new Ghost3DTransformController());
        return ghost3D;
    }

    private void createLivesCounter3D() {
        final LivesCounter livesCounter = level.entities().entitySet().uniqueOfType(LivesCounter.class);
        if (!livesCounter.hasComponent(LivesCounterView3DComp.class)) {
            final LivesCounterView3DComp view3D = new LivesCounterView3DComp(gameVariantConfig.factory3D(), gameVariantConfig.worldSettings());
            livesCounter.setComponent(LivesCounterView3DComp.class, view3D);
            view3D.root().setTranslateX(2 * WorldMap.TS);
            view3D.root().setTranslateY(2 * WorldMap.TS);
        }
    }

    private void createMessageManager() {
        messageManager = new MessageManager3D(animationRegistry, this);

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().entitySet().uniqueOfType(House.class);
        if (house != null) {
            messageManager.setMessageCenter(MessageManager3D.MessageType.READY, house.centerPositionUnderHouse());
        } else {
            Logger.error("No house in this game level! WTF?");
            final double x = terrain.numCols() * WorldMap.HTS, y = terrain.numRows() * WorldMap.HTS;
            messageManager.setMessageCenter(MessageManager3D.MessageType.READY, vec2_float(x, y));
        }
        messageManager.setMessageCenter(MessageManager3D.MessageType.TEST,
            vec2_float(terrain.numCols() * WorldMap.HTS, (terrain.numRows() - 2) * WorldMap.TS));
    }

    // Order matters for correct transparency!
    private void buildHierarchy() {
        final LivesCounter livesCounter = level.entities().entitySet().uniqueOfType(LivesCounter.class);
        final Pac pac = level.entities().pac();
        final House house = level.entities().entitySet().uniqueOfType(House.class);

        getChildren().add(livesCounter.requireComponent(LivesCounterView3DComp.class).root());

        getChildren().add(pac.requireComponent(Pac3DViewComp.class).root());
        getChildren().add(pac.requireComponent(Pac3DViewComp.class).powerLight());

        //TODO change
//        for (var ghost3D : ghosts3D) { getChildren().add(ghost3D.root()); }

        for (var ghost: level.entities().ghosts()) {
            getChildren().add(ghost.requireComponent(Ghost3DViewComp.class).root());
        }

        energizer3DByTile.values().stream().map(Energizer3D::shape).forEach(getChildren()::add);
        pellet3DByTile.values().stream().map(Pellet3D::shape).forEach(getChildren()::add);

        getChildren().add(maze3D.particlesGroup());
        getChildren().add(maze3D);

        getChildren().add(house.requireComponent(House3DViewComp.class).root());
        getChildren().add(house.requireComponent(House3DViewComp.class).doors());

        getChildren().add(ghostHunterLight);
    }

    // --- Animations

    private void createEnergizerParticlesAnimation(ParticlesAnimationConfig particlesAnimationConfig) {
        final List<PhongMaterial> ghostDressMaterials = ghosts3D.stream()
            .map(ghost3D -> ghost3D.materials().normalMaterial().dressMaterial())
            .toList();

        final ExplosionConfig config = particlesAnimationConfig.explosion();

        particlePool = new Pool<>(300, 300,
            () -> {
                final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
                final double scale = Math.clamp(RANDOM_GENERATOR.nextGaussian(2, 0.1), 0.5, 4);
                final double radius = scale * config.particleMeanRadius();
                return new EnergizerParticle3D(radius, material, Vector3f.ZERO);
            },
            particle -> {
                particle.reset();
                particle.shape().setVisible(false);
            }
        );

        final House house = level.entities().entitySet().uniqueOfType(House.class);

        animationRegistry.register(AnimationID.PARTICLES, new ParticlesAnimation3D(
            house,
            ghostDressMaterials,
            particlePool,
            particlesAnimationConfig,
            maze3D.particlesGroup(),
            particle -> particle.collidesWith(maze3D.floor()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void createGhostLightAnimation() {
        final var ghostLightAnimation = new GhostLightRelayAnimation(ghostHunterLight, ghosts3D);
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
    }
}
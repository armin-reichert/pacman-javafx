/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.*;
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
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.entities3D.animation.Pool;
import de.amr.pacmanfx.uilib.entities3D.bonus.anim.Bonus3DAnimationID;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DSettings;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.bonus.system.Bonus3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3D.bonus.system.Bonus3DViewSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DAppearanceSystem;
import de.amr.pacmanfx.uilib.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.PacSettings;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DTransformSystem;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.entities3D.world.NumberBox3D;
import de.amr.pacmanfx.uilib.entities3D.world.Pellet3D;
import javafx.scene.Group;
import javafx.scene.Node;
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

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    public enum AnimationID implements Named {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    private final Map<Vector2i, Energizer3D> energizer3DByTile = new HashMap<>();

    private final Map<Vector2i, Pellet3D> pellet3DByTile = new HashMap<>();

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

    public void updateEntities3D() {
        updateLivesCounter3D();
        updateHouse3D();
        updatePac3D();
        updateGhosts3D();
        updateBonus3D();
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
        Ufx.setDrawMode(level.entities().pac().requireComp(Pac3DViewComp.class).root(), drawMode);
        for (var ghost : level.entities().ghosts()) {
            Ufx.setDrawMode(ghost.requireComp(Ghost3DViewComp.class).root(), drawMode);
        }
        Ufx.setDrawMode(maze3D, drawMode);
    }

    public void activateBonus3D(Bonus bonus) {
        ensureBonus3DViewExists(bonus);
        Bonus3DViewSystem.lookEdible(bonus);
    }

    public void ensureBonus3DViewExists(Bonus bonus) {
        if (!bonus.hasComp(Bonus3DViewComp.class)) {
            final var view3D = createBonusView3D(bonus);
            getChildren().add(view3D.root());
            Bonus3DViewSystem.update(bonus, animationRegistry);
        }
    }

    public void addKilledGhostNumberBox(Ghost ghost) {
        final Factory3D factory3D = gameVariantConfig.factory3D();

        final int killIndex = level.indexInGhostKilledChain(ghost);
        final Node numberBoxNode = factory3D.createNumberBox3D(gameVariantConfig, killIndex);

        final Ghost3DViewComp ghost3DView = ghost.requireComp(Ghost3DViewComp.class);
        numberBoxNode.setTranslateX(ghost3DView.root().getTranslateX());
        numberBoxNode.setTranslateY(ghost3DView.root().getTranslateY());
        numberBoxNode.setTranslateZ(ghost3DView.root().getTranslateZ());
        getChildren().add(numberBoxNode);

        //TODO move into animation system
        if (numberBoxNode instanceof NumberBox3D numberBox3D) {
            final double risingHeight = (killIndex + 1) * 12;
            final var animation = new HideGhost3DRiseNumberBoxAnimation(ghost3DView, numberBox3D, risingHeight);
            animation.delegate().setOnFinished(_ -> getChildren().remove(numberBoxNode));
            animation.playFromStart();
        }
    }

    // Private area, no trespassing!

    private void updateLivesCounter3D() {
        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        LivesCounterView3DSystem.update(livesCounter);
    }

    private void updatePac3D() {
        final Pac pac = level.entities().pac();
        Pac3DTransformSystem.update(pac, level);
        Pac3DAnimationSystem.update(pac);
        Pac3DAnimationSystem.updatePowerLight(pac);
    }

    private void updateGhosts3D() {
        level.entities().ghosts().forEach(ghost -> {
            Ghost3DMovementSystem.update(ghost);
            Ghost3DAppearanceSystem.update(ghost);
        });
    }

    private void updateHouse3D() {
        final House house = level.entities().theOne(House.class);

        boolean accessRequested = level.ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(WorldNavigationSystem.computeTile(ghost)))
            .anyMatch(GameEntity::isVisible);

        boolean ghostNearHouseDoor = level.ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);

        House3DSystem.showLight(house, ghostNearHouseDoor);
        House3DAnimationSystem.update(house, accessRequested);
    }

    private boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final House3DViewComp view3D = house.requireComp(House3DViewComp.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.pos().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }

    private void updateBonus3D() {
        level.optBonus().ifPresent(bonus -> {
            ensureBonus3DViewExists(bonus);
            Bonus3DMovementSystem.update(bonus);
        });
    }

    private void createMaze3D() {
        final WorldMapColorSchemeImpl colorScheme = gameVariantConfig.renderConfig().colorScheme(level.worldMap(), gameVariantConfig.worldSettings());
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().theOne(House.class);
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
        bonus.setComp(Bonus3DViewComp.class, view3D);
        animationRegistry.register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());
        return  view3D;
    }

    public void createLevelCounterView3D(LevelCounter levelCounter) {
        if (!levelCounter.hasComp(LevelCounterView3DComp.class)) {
            final LevelCounterView3DComp view3D = new LevelCounterView3DComp();
            levelCounter.setComp(LevelCounterView3DComp.class, view3D);
            animationRegistry.register(LevelCounterView3DAnimationID.LEVEL_COUNTER_SPINNING, view3D.spinningAnimation());
            Logger.info("Level counter now has a 3D view");
        }
        else {
            Logger.info("Level counter already has a 3D view!");
        }

        // Recreate all 3D entries in the level counter group
        LevelCounterView3DSystem.updateLevelCounter3D(gameVariantConfig, levelCounter, level);

        // Add level counter 3D root into this group
        final LevelCounterView3DComp view3D = levelCounter.requireComp(LevelCounterView3DComp.class);
        getChildren().add(view3D.root());
    }

    private void createPac3D(Pac pac) {
        final PacSettings settings = gameVariantConfig.worldSettings().pac();
        gameVariantConfig.factory3D().createPac3D(pac, settings, animationRegistry);
        pac.requireComp(Pac3DViewComp.class).drawModeProperty().bind(viewModel.common3D.drawModeProperty);
    }

    private void createGhosts3D() {
        final List<GhostSettings> settings = gameVariantConfig.worldSettings().ghosts();
        level.entities().ghosts().forEach(ghost -> {
            final var ghostSettings = settings.get(ghost.personality().ordinal());
            gameVariantConfig.factory3D().createGhost3D(ghost, ghostSettings, animationRegistry);
        });
    }

    private void createLivesCounter3D() {
        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        if (!livesCounter.hasComp(LivesCounterView3DComp.class)) {
            final LivesCounterView3DComp view3D = new LivesCounterView3DComp(gameVariantConfig.factory3D(), gameVariantConfig.worldSettings());
            livesCounter.setComp(LivesCounterView3DComp.class, view3D);
            view3D.root().setTranslateX(2 * WorldMap.TS);
            view3D.root().setTranslateY(2 * WorldMap.TS);
        }
    }

    private void createMessageManager() {
        messageManager = new MessageManager3D(animationRegistry, this);

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = level.entities().theOne(House.class);
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
        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        final Pac pac = level.entities().pac();
        final House house = level.entities().theOne(House.class);

        getChildren().add(livesCounter.requireComp(LivesCounterView3DComp.class).root());

        getChildren().add(pac.requireComp(Pac3DViewComp.class).root());
        getChildren().add(pac.requireComp(Pac3DViewComp.class).powerLight());

        for (var ghost: level.entities().ghosts()) {
            getChildren().add(ghost.requireComp(Ghost3DViewComp.class).root());
        }

        energizer3DByTile.values().stream().map(Energizer3D::shape).forEach(getChildren()::add);
        pellet3DByTile.values().stream().map(Pellet3D::shape).forEach(getChildren()::add);

        getChildren().add(maze3D.particlesGroup());
        getChildren().add(maze3D);

        getChildren().add(house.requireComp(House3DViewComp.class).root());
        getChildren().add(house.requireComp(House3DViewComp.class).doors());

        getChildren().add(ghostHunterLight);
    }

    // --- Animations

    private void createEnergizerParticlesAnimation(ParticlesAnimationConfig particlesAnimationConfig) {
        final List<PhongMaterial> ghostDressMaterials = level.entities().ghosts().stream()
            .map(ghost -> ghost.requireComp(Ghost3DViewComp.class))
            .map(ghostView3D -> ghostView3D.appearanceMaterialSet().normal().dress())
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

        final House house = level.entities().theOne(House.class);

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
        final var ghostLightAnimation = new GhostLightRelayAnimation(
            ghostHunterLight, level.entities().ghosts(), gameVariantConfig.worldSettings().ghosts());
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
    }
}
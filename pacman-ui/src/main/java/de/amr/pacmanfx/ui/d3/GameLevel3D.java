/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntitySet;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.BonusConfig;
import de.amr.pacmanfx.ui.config.EnergizerConfig3D;
import de.amr.pacmanfx.ui.config.PelletConfig3D;
import de.amr.pacmanfx.ui.d3.animation.GhostLightRelayAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.d3.entities.LevelCounter3D;
import de.amr.pacmanfx.ui.d3.entities.LivesCounter3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.model3D.animation.Pool;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DAppearanceController;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DTransformController;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
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
import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    public enum AnimationID implements AnimationIdentifier {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    public static class EntityCache {
        Pac3D pac3D;
        Maze3D maze3D;
        Ghost3D[] ghosts3D; // order: RED, PINK, CYAN, ORANGE
        LivesCounter3D livesCounter3D;
        LevelCounter3D levelCounter3D;
        final Map<Vector2i, Energizer3D> energizer3DByTile = new HashMap<>();
        final Map<Vector2i, Pellet3D> pellet3DByTile = new HashMap<>();

        void clear() {
            pac3D = null;
            maze3D = null;
            if (ghosts3D != null) {
                ghosts3D = null;
            }
            livesCounter3D = null;
            levelCounter3D = null;
            energizer3DByTile.clear();
            pellet3DByTile.clear();
        }
    }

    // Access to game model
    private final GameLevel level;

    private final UIConfig uiConfig;

    private final GameLevelEntitySet entitySet = new GameLevelEntitySet();

    private final EntityCache entityCache = new EntityCache();

    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final PointLight ghostHunterLight = new PointLight();

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    private MessageManager3D messageManager;

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param level          the game level to visualize
     * @param uiConfig       the global UI configuration (provides 3D settings, colors, models)
     */
    public GameLevel3D(GameLevel level, UIConfig uiConfig) {
        this.level = requireNonNull(level);
        this.uiConfig = requireNonNull(uiConfig);

        createMaze3D();
        createFood3D();
        createPac3D();
        createGhosts3D();
        createLevelCounter3D();
        createLivesCounter3D();
        createMessageManager();

        buildHierarchy();

        setMouseTransparent(true); // this increases performance they say...
    }

    public void createAnimations(ParticlesAnimationConfig particlesConfig) {
        final WorldMapColorScheme mapColorScheme = uiConfig.colorScheme(level.worldMap());
        animationRegistry.register(AnimationID.WALL_COLOR_FLASHING, new WallColorFlashingAnimation(this, mapColorScheme));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_FULL, new LevelCompletedAnimation(this));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(this));
        createEnergizerParticlesAnimation(particlesConfig);
        createGhostLightAnimation();
    }

    /**
     * Starts the lives counter symbols following Pac-Man with their eyes.
     */
    public void startLivesCounterTrackingPac() {
        entityCache.livesCounter3D.startTracking(entityCache.pac3D);
    }

    @Override
    public void dispose() {
        animationRegistry.dispose();

        entityCache.clear();
        entitySet.dispose();

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

    public UIConfig uiConfig() {
        return uiConfig;
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return uiConfig.optSoundEffects();
    }

    public GameLevel level() {
        return level;
    }

    public MessageManager3D messageManager() {
        return messageManager;
    }

    public GameLevelEntitySet entities() {
        return entitySet;
    }

    public Maze3D maze3D() {
        return entityCache.maze3D;
    }

    public Pac3D pac3D() {
        return entityCache.pac3D;
    }

    public List<Ghost3D> ghosts3D() {
        return List.of(entityCache.ghosts3D);
    }

    public Ghost3D ghost3D(byte personality) {
        requireValidGhostPersonality(personality);
        return entityCache.ghosts3D[personality];
    }

    public Stream<Energizer3D> energizers3D() {
        return entityCache.energizer3DByTile.values().stream();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(entityCache.energizer3DByTile.get(tile));
    }

    public Stream<Pellet3D> pellets3D() {
        return entityCache.pellet3DByTile.values().stream();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(entityCache.pellet3DByTile.get(tile));
    }

    public void cleanupFoodAndParticles() {
        animationRegistry.optAnimation(AnimationID.PARTICLES).ifPresent(ManagedAnimation::stop);
        entitySet.selectAllOfType(Energizer3D.class).forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        entitySet.selectAllOfType(Pellet3D.class).forEach(pellet3D -> pellet3D.shape().setVisible(false));
        entityCache.maze3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        entitySet.selectAllOfType(Pac3D.class).forEach(pac3D -> Ufx.setDrawMode(pac3D, drawMode));
        entitySet.selectAllOfType(Ghost3D.class).forEach(ghost3D -> Ufx.setDrawMode(ghost3D, drawMode));
        Ufx.setDrawMode(entityCache.maze3D, drawMode);
    }

    public void addOrReplaceBonus3D(Bonus bonus) {
        // Make list copy to avoid exception when removing inside for-each
        List.copyOf(entitySet.selectAllOfType(Bonus3D.class).toList()).forEach(bonus3D -> {
            entitySet.remove(bonus3D);
            getChildren().remove(bonus3D.root());
            bonus3D.dispose();
        });
        final Bonus3D bonus3D = createBonus3D(bonus);
        getChildren().add(bonus3D.root());
        bonus3D.lookEdible();
    }

    // Private area, no trespassing!

    private void createMaze3D() {
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        entityCache.maze3D = uiConfig.factory3D().createMaze3D(terrain, uiConfig.entityConfig(), colorScheme, animationRegistry);
        entityCache.maze3D.wallOpacityProperty().bind(GameUIConstants.PROPERTY_3D_WALL_OPACITY);
        entityCache.maze3D.wallBaseHeightProperty().bind(GameUIConstants.PROPERTY_3D_WALL_HEIGHT);
        entityCache.maze3D.floorColorProperty().bind(GameUIConstants.PROPERTY_3D_FLOOR_COLOR);
        entitySet.add(entityCache.maze3D);
    }

    private void createFood3D() {
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        final PelletConfig3D pelletConfig3D = uiConfig.entityConfig().pellet();
        final double pelletZ = entityCache.maze3D.floorTop() - pelletConfig3D.floorElevation();

        final EnergizerConfig3D energizerConfig3D = uiConfig.entityConfig().energizer();
        final double energizerZ = entityCache.maze3D.floorTop() - energizerConfig3D.floorElevation();

        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .forEach(tile -> {
                if (foodLayer.isEnergizerTile(tile)) {
                    entityCache.energizer3DByTile.put(tile, createEnergizer3D(tile, energizerZ, foodMaterial));
                } else {
                    entityCache.pellet3DByTile.put(tile, createPellet3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private Pellet3D createPellet3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(uiConfig.entityConfig().pellet(), foodMaterial);
        pellet3D.setLocation(tile, z);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(
            uiConfig.entityConfig().energizer(), animationRegistry, foodMaterial);
        energizer3D.setLocation(tile, z);
        return energizer3D;
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        final BonusConfig config = uiConfig.entityConfig().bonusConfig();
        final Bonus3D bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), config.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  config.bonusPointsWidth());
        entitySet.add(bonus3D);
        return bonus3D;
    }

    private void createPac3D() {
        final PacConfig config = uiConfig.entityConfig().pacConfig();
        entityCache.pac3D = uiConfig.factory3D().createPac3D(level.pac(), config, animationRegistry);
        entitySet.add(entityCache.pac3D);
    }

    private void createGhosts3D() {
        final List<GhostConfig> ghostConfigs = uiConfig.entityConfig().ghostConfigs();
        entityCache.ghosts3D = Stream.of(RED_GHOST_SHADOW, PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(ghost -> {
                final Ghost3D ghost3D = createGhost3D(ghostConfigs.get(ghost.personality()), ghost);
                ghost3D.init(level);
                return ghost3D;
            }).toArray(Ghost3D[]::new);

        for (var ghost3D : entityCache.ghosts3D) {
            entitySet.add(ghost3D);
        }
    }

    private Ghost3D createGhost3D(GhostConfig ghostConfig, Ghost ghost) {
        final Ghost3D ghost3D = uiConfig.factory3D().createGhost3D(ghost, ghostConfig, animationRegistry);
        ghost3D.setAppearanceController(new Ghost3DAppearanceController());
        ghost3D.setTransformController(new Ghost3DTransformController());
        return ghost3D;
    }

    private void createLivesCounter3D() {
        entityCache.livesCounter3D = new LivesCounter3D(uiConfig);
        entityCache.livesCounter3D.setTranslateX(2 * TS);
        entityCache.livesCounter3D.setTranslateY(2 * TS);
        entitySet.add(entityCache.livesCounter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        entityCache.levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        entityCache.levelCounter3D.setTranslateX(TS(terrain.numCols() - 2));
        entityCache.levelCounter3D.setTranslateY(TS(2));
        entityCache.levelCounter3D.setTranslateZ(-uiConfig.entityConfig().levelCounter().elevation());
        entitySet.add(entityCache.levelCounter3D);
    }

    private void createMessageManager() {
        messageManager = new MessageManager3D(animationRegistry, this);
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.optHouse().ifPresentOrElse(
            house -> messageManager.setMessageCenter(MessageManager3D.MessageType.READY, house.centerPositionUnderHouse()),
            () -> {
                Logger.error("No house in this game level! WTF?");
                final double x = terrain.numCols() * HTS, y = terrain.numRows() * HTS;
                messageManager.setMessageCenter(MessageManager3D.MessageType.READY, vec2_float(x, y));
            });
        messageManager.setMessageCenter(MessageManager3D.MessageType.TEST,
            vec2_float(terrain.numCols() * HTS, (terrain.numRows() - 2) * TS));
    }

    // Order matters for correct transparency!
    private void buildHierarchy() {
        getChildren().add(entityCache.levelCounter3D);
        getChildren().add(entityCache.livesCounter3D);
        getChildren().add(entityCache.pac3D);
        entityCache.pac3D.powerLight().ifPresent(getChildren()::add);
        for (var ghost3D : entityCache.ghosts3D) { getChildren().add(ghost3D); }
        entityCache.energizer3DByTile.values().stream().map(Energizer3D::shape).forEach(getChildren()::add);
        entityCache.pellet3DByTile.values().stream().map(Pellet3D::shape).forEach(getChildren()::add);
        getChildren().add(entityCache.maze3D.particlesGroup());
        getChildren().add(entityCache.maze3D);
        getChildren().add(entityCache.maze3D.house().root());
        getChildren().add(entityCache.maze3D.house().doors());
        getChildren().add(ghostHunterLight);
    }

    // --- Animations

    private void createEnergizerParticlesAnimation(ParticlesAnimationConfig particlesAnimationConfig) {
        final List<PhongMaterial> ghostDressMaterials = Stream.of(entityCache.ghosts3D)
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

        final House house = level.worldMap().terrainLayer().house();

        animationRegistry.register(AnimationID.PARTICLES, new ParticlesAnimation3D(
            house,
            ghostDressMaterials,
            particlePool,
            particlesAnimationConfig,
            entityCache.maze3D.particlesGroup(),
            particle -> particle.collidesWith(entityCache.maze3D.floor()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void createGhostLightAnimation() {
        final var ghostLightAnimation = new GhostLightRelayAnimation(ghostHunterLight, List.of(entityCache.ghosts3D));
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelEntitySet;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.config.world.Bonus3DSettings;
import de.amr.pacmanfx.ui.config.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.config.world.Pellet3DSettings;
import de.amr.pacmanfx.ui.gamescene.d3.animation.GhostLightRelayAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.gamescene.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.entities.LevelCounter3D;
import de.amr.pacmanfx.ui.gamescene.d3.entities.LivesCounter3D;
import de.amr.pacmanfx.ui.gamescene.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.viewmodel.UISettings3DVM;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.model3D.animation.Pool;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DAppearanceController;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DTransformController;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostSettings;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.PacSettings;
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
import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    public enum AnimationID implements Identifier {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    public static class EntitySet extends GameLevelEntitySet {

        // Cached for faster access
        private Pac3D pac3D;
        private Maze3D maze3D;
        private List<Ghost3D> ghosts3D = new ArrayList<>(4); // order: RED, PINK, CYAN, ORANGE
        private LivesCounter3D livesCounter3D;
        private LevelCounter3D levelCounter3D;
        private final Map<Vector2i, Energizer3D> energizer3DByTile = new HashMap<>();
        private final Map<Vector2i, Pellet3D> pellet3DByTile = new HashMap<>();

        @Override
        public void clear() {
            super.clear();
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

        public Pac3D pac3D() {
            return pac3D;
        }

        public Maze3D maze3D() {
            return maze3D;
        }

        public List<Ghost3D> ghosts3D() {
            return ghosts3D;
        }

        public LivesCounter3D livesCounter3D() {
            return livesCounter3D;
        }

        public LevelCounter3D levelCounter3D() {
            return levelCounter3D;
        }

        public Map<Vector2i, Energizer3D> energizer3DByTile() {
            return energizer3DByTile;
        }

        public Map<Vector2i, Pellet3D> pellet3DByTile() {
            return pellet3DByTile;
        }
    }

    // Access to game model
    private final GameLevel level;

    private final GameVariantConfig uiConfig;

    private final EntitySet entitySet = new EntitySet();

    private final AnimationRegistry animationRegistry = new AnimationRegistry();

    private final PointLight ghostHunterLight = new PointLight();

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    private MessageManager3D messageManager;

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param gameContext the current game context
     * @param level       the game level to visualize
     * @param uiConfig    the global UI configuration (provides 3D settings, colors, models)
     */
    public GameLevel3D(UISettings3DVM globals3D, GameContext gameContext, GameLevel level, GameVariantConfig uiConfig) {
        this.level = requireNonNull(level);
        this.uiConfig = requireNonNull(uiConfig);

        createMaze3D(globals3D);
        createFood3D();
        createPac3D();
        createGhosts3D(gameContext);
        createLevelCounter3D();
        createLivesCounter3D();
        createMessageManager();

        buildHierarchy();

        setMouseTransparent(true); // this increases performance they say...
    }

    public void createAnimations(ParticlesAnimationConfig particlesConfig) {
        final WorldMapColorScheme mapColorScheme = uiConfig.colorScheme(level.worldMap());
        animationRegistry.register(AnimationID.WALL_COLOR_FLASHING,
            new WallColorFlashingAnimation(mapColorScheme, entities().maze3D.materials().get("wallTopMaterial")));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_FULL, new LevelCompletedAnimation(this));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(this));
        createEnergizerParticlesAnimation(particlesConfig);
        createGhostLightAnimation();
    }

    /**
     * Starts the lives counter symbols following Pac-Man with their eyes.
     */
    public void startLivesCounterTrackingPac() {
        entitySet.livesCounter3D.startTracking(entitySet.pac3D);
    }

    @Override
    public void dispose() {
        animationRegistry.dispose();

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

    public GameVariantConfig uiConfig() {
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

    public EntitySet entities() {
        return entitySet;
    }

    public Ghost3D ghost3D(byte personality) {
        requireValidGhostPersonality(personality);
        return entitySet.ghosts3D.get(personality);
    }

    public Stream<Energizer3D> energizers3D() {
        return entitySet.energizer3DByTile.values().stream();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return Optional.ofNullable(entitySet.energizer3DByTile.get(tile));
    }

    public Stream<Pellet3D> pellets3D() {
        return entitySet.pellet3DByTile.values().stream();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return Optional.ofNullable(entitySet.pellet3DByTile.get(tile));
    }

    public void cleanupFoodAndParticles() {
        animationRegistry.optAnimation(AnimationID.PARTICLES).ifPresent(ManagedAnimation::stop);
        entitySet.energizer3DByTile.values().forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        entitySet.pellet3DByTile.values().forEach(pellet3D -> pellet3D.shape().setVisible(false));
        entitySet.maze3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        Ufx.setDrawMode(entitySet.pac3D, drawMode);
        for (Ghost3D ghost3D : entitySet.ghosts3D) {
            Ufx.setDrawMode(ghost3D, drawMode);
        }
        Ufx.setDrawMode(entitySet.maze3D, drawMode);
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

    private void createMaze3D(UISettings3DVM globals3D) {
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        entitySet.maze3D = uiConfig.factory3D().createMaze3D(
            globals3D.drawModeProperty,
            terrain,
            uiConfig.worldConfig(),
            colorScheme,
            animationRegistry);

        entitySet.maze3D.wallOpacityProperty()   .bind(globals3D.mazeWallOpacityProperty);
        entitySet.maze3D.wallBaseHeightProperty().bind(globals3D.mazeWallHeightProperty);
        entitySet.maze3D.floorColorProperty()    .bind(globals3D.mazeFloorColorProperty);

        entitySet.add(entitySet.maze3D);
    }

    private void createFood3D() {
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        final Pellet3DSettings pelletConfig3D = uiConfig.worldConfig().pellet();
        final double pelletZ = entitySet.maze3D.floorTop() - pelletConfig3D.floorElevation();

        final Energizer3DSettings energizerConfig3D = uiConfig.worldConfig().energizer();
        final double energizerZ = entitySet.maze3D.floorTop() - energizerConfig3D.floorElevation();

        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .forEach(tile -> {
                if (foodLayer.isEnergizerTile(tile)) {
                    entitySet.energizer3DByTile.put(tile, createEnergizer3D(tile, energizerZ, foodMaterial));
                } else {
                    entitySet.pellet3DByTile.put(tile, createPellet3D(tile, pelletZ, foodMaterial));
                }
            });
    }

    private Pellet3D createPellet3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(uiConfig.worldConfig().pellet(), foodMaterial);
        pellet3D.setLocation(tile, z);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, double z, PhongMaterial foodMaterial) {
        final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(
            uiConfig.worldConfig().energizer(), foodMaterial, animationRegistry);
        energizer3D.setLocation(tile, z);
        return energizer3D;
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        final Bonus3DSettings config = uiConfig.worldConfig().bonus();
        final Bonus3D bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbolCode()), config.symbolWidth(),
            uiConfig.bonusValueImage(bonus.symbolCode()),  config.pointsWidth());
        entitySet.add(bonus3D);
        return bonus3D;
    }

    private void createPac3D() {
        final PacSettings config = uiConfig.worldConfig().pac();
        entitySet.pac3D = uiConfig.factory3D().createPac3D(level.entities().pac(), config, animationRegistry);
        entitySet.add(entitySet.pac3D);
    }

    private void createGhosts3D(GameContext gameContext) {
        final List<GhostSettings> ghostConfigs = uiConfig.worldConfig().ghosts();
        entitySet.ghosts3D = Stream.of(GameModel.RED_GHOST_SHADOW, GameModel.PINK_GHOST_SPEEDY, GameModel.CYAN_GHOST_BASHFUL, GameModel.ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(ghost -> {
                final Ghost3D ghost3D = createGhost3D(ghostConfigs.get(ghost.personality()), ghost);
                ghost3D.init(gameContext, level);
                return ghost3D;
            }).toList();

        for (var ghost3D : entitySet.ghosts3D) {
            entitySet.add(ghost3D);
        }
    }

    private Ghost3D createGhost3D(GhostSettings ghostConfig, Ghost ghost) {
        final Ghost3D ghost3D = uiConfig.factory3D().createGhost3D(ghost, ghostConfig, animationRegistry);
        ghost3D.setAppearanceController(new Ghost3DAppearanceController());
        ghost3D.setTransformController(new Ghost3DTransformController());
        return ghost3D;
    }

    private void createLivesCounter3D() {
        entitySet.livesCounter3D = new LivesCounter3D(uiConfig.factory3D(), uiConfig.worldConfig());
        entitySet.livesCounter3D.setTranslateX(2 * WorldMap.TS);
        entitySet.livesCounter3D.setTranslateY(2 * WorldMap.TS);
        entitySet.add(entitySet.livesCounter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        entitySet.levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        entitySet.levelCounter3D.setTranslateX(WorldMap.TS(terrain.numCols() - 2));
        entitySet.levelCounter3D.setTranslateY(WorldMap.TS(2));
        entitySet.levelCounter3D.setTranslateZ(-uiConfig.worldConfig().levelCounter().elevation());
        entitySet.add(entitySet.levelCounter3D);
    }

    private void createMessageManager() {
        messageManager = new MessageManager3D(animationRegistry, this);
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.optHouse().ifPresentOrElse(
            house -> messageManager.setMessageCenter(MessageManager3D.MessageType.READY, house.centerPositionUnderHouse()),
            () -> {
                Logger.error("No house in this game level! WTF?");
                final double x = terrain.numCols() * WorldMap.HTS, y = terrain.numRows() * WorldMap.HTS;
                messageManager.setMessageCenter(MessageManager3D.MessageType.READY, vec2_float(x, y));
            });
        messageManager.setMessageCenter(MessageManager3D.MessageType.TEST,
            vec2_float(terrain.numCols() * WorldMap.HTS, (terrain.numRows() - 2) * WorldMap.TS));
    }

    // Order matters for correct transparency!
    private void buildHierarchy() {
        getChildren().add(entitySet.levelCounter3D);
        getChildren().add(entitySet.livesCounter3D);
        getChildren().add(entitySet.pac3D);
        entitySet.pac3D.powerLight().ifPresent(getChildren()::add);
        for (var ghost3D : entitySet.ghosts3D) { getChildren().add(ghost3D); }
        entitySet.energizer3DByTile.values().stream().map(Energizer3D::shape).forEach(getChildren()::add);
        entitySet.pellet3DByTile.values().stream().map(Pellet3D::shape).forEach(getChildren()::add);
        getChildren().add(entitySet.maze3D.particlesGroup());
        getChildren().add(entitySet.maze3D);
        getChildren().add(entitySet.maze3D.house().root());
        getChildren().add(entitySet.maze3D.house().doors());
        getChildren().add(ghostHunterLight);
    }

    // --- Animations

    private void createEnergizerParticlesAnimation(ParticlesAnimationConfig particlesAnimationConfig) {
        final List<PhongMaterial> ghostDressMaterials = entitySet.ghosts3D().stream()
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
            entitySet.maze3D.particlesGroup(),
            particle -> particle.collidesWith(entitySet.maze3D.floor()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void createGhostLightAnimation() {
        final var ghostLightAnimation = new GhostLightRelayAnimation(ghostHunterLight, entitySet.ghosts3D());
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
    }
}
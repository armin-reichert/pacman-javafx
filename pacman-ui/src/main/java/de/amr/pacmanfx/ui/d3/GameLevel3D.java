/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static de.amr.basics.math.RandomNumberSupport.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    public enum AnimationID {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    private static final Comparator<Ghost3D> BY_PERSONALITY = Comparator.comparingInt(ghost3D -> ghost3D.ghost().personality());

    // Access to game model
    private final GameLevel level;

    private final UIConfig uiConfig;

    private final GameLevelEntitySet entities3D = new GameLevelEntitySet();

    // Cached for efficiency
    private Pac3D pac3D;
    private Maze3D maze3D;
    private List<Ghost3D> ghosts3D;
    private LivesCounter3D livesCounter3D;
    private LevelCounter3D levelCounter3D;

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
        livesCounter3D.startTracking(pac3D);
    }

    @Override
    public void dispose() {
        animationRegistry.dispose();
        entities3D.dispose();
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
        return entities3D;
    }

    public Maze3D maze3D() {
        return maze3D;
    }

    public Pac3D pac3D() {
        return pac3D;
    }

    public List<Ghost3D> ghosts3D() {
        return ghosts3D;
    }

    public Optional<Ghost3D> ghost3D(byte personality) {
        requireValidGhostPersonality(personality);
        return ghosts3D.stream().filter(ghost3D -> ghost3D.ghost().personality() == personality).findFirst();
    }

    public Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return entities3D.selectWhere(Energizer3D.class, e3D -> tile.equals(e3D.tile())).findFirst();
    }

    public Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return entities3D.selectWhere(Pellet3D.class, p3D -> tile.equals(p3D.tile())).findFirst();
    }

    public void cleanupFoodAndParticles() {
        animationRegistry.optAnimation(AnimationID.PARTICLES).ifPresent(ManagedAnimation::stop);
        entities3D.selectAllOfType(Energizer3D.class).forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        entities3D.selectAllOfType(Pellet3D.class).forEach(pellet3D -> pellet3D.shape().setVisible(false));
        maze3D.particlesGroup().getChildren().clear();
    }

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        entities3D.selectAllOfType(Pac3D.class).forEach(pac3D -> Ufx.setDrawMode(pac3D, drawMode));
        entities3D.selectAllOfType(Ghost3D.class).forEach(ghost3D -> Ufx.setDrawMode(ghost3D, drawMode));
        Ufx.setDrawMode(maze3D, drawMode);
    }

    public void addOrReplaceBonus3D(Bonus bonus) {
        // Make list copy to avoid exception when removing inside for-each
        List.copyOf(entities3D.selectAllOfType(Bonus3D.class).toList()).forEach(bonus3D -> {
            entities3D.remove(bonus3D);
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
        maze3D = uiConfig.factory3D().createMaze3D(terrain, uiConfig.entityConfig(), colorScheme, animationRegistry);
        maze3D.wallOpacityProperty().bind(GameUIConstants.PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(GameUIConstants.PROPERTY_3D_WALL_HEIGHT);
        maze3D.floorColorProperty().bind(GameUIConstants.PROPERTY_3D_FLOOR_COLOR);
        entities3D.add(maze3D);
    }

    private void createFood3D() {
        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());
        final FoodLayer foodLayer = level.worldMap().foodLayer();

        final PhongMaterial foodMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));

        // Pellets
        final PelletConfig3D pelletConfig3D = uiConfig.entityConfig().pellet();
        final double pelletZ = maze3D.floorTop() - pelletConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .filter(foodLayer::hasFoodAtTile)
            .map(tile -> {
                final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(pelletConfig3D, foodMaterial);
                pellet3D.setLocation(tile, pelletZ);
                return pellet3D;
            })
            .forEach(entities3D::add);

        // Energizers
        final EnergizerConfig3D energizerConfig3D = uiConfig.entityConfig().energizer();
        final double energizerZ = maze3D.floorTop() - energizerConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(foodLayer::isEnergizerTile)
            .map(tile -> {
                final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(energizerConfig3D, animationRegistry, foodMaterial);
                energizer3D.setLocation(tile, energizerZ);
                return energizer3D;
            })
            .forEach(entities3D::add);
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        final BonusConfig config = uiConfig.entityConfig().bonusConfig();
        final Bonus3D bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), config.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  config.bonusPointsWidth());
        entities3D.add(bonus3D);
        return bonus3D;
    }

    private void createPac3D() {
        final PacConfig config = uiConfig.entityConfig().pacConfig();
        pac3D = uiConfig.factory3D().createPac3D(level.pac(), config, animationRegistry);
        entities3D.add(pac3D);
    }

    private void createGhosts3D() {
        final List<GhostConfig> ghostConfigs = uiConfig.entityConfig().ghostConfigs();
        ghosts3D = level.ghosts()
            .map(ghost -> {
                final Ghost3D ghost3D = createGhost3D(ghostConfigs.get(ghost.personality()), ghost);
                ghost3D.init(level);
                return ghost3D;
            }).toList();
        ghosts3D.forEach(entities3D::add);
    }

    private Ghost3D createGhost3D(GhostConfig ghostConfig, Ghost ghost) {
        final Ghost3D ghost3D = uiConfig.factory3D().createGhost3D(ghost, ghostConfig, animationRegistry);
        ghost3D.setAppearanceController(new Ghost3DAppearanceController());
        ghost3D.setTransformController(new Ghost3DTransformController());
        return ghost3D;
    }

    private void createLivesCounter3D() {
        livesCounter3D = new LivesCounter3D(uiConfig);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        entities3D.add(livesCounter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        levelCounter3D = new LevelCounter3D(animationRegistry, uiConfig);
        levelCounter3D.setTranslateX(TS(terrain.numCols() - 2));
        levelCounter3D.setTranslateY(TS(2));
        levelCounter3D.setTranslateZ(-uiConfig.entityConfig().levelCounter().elevation());
        entities3D.add(levelCounter3D);
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
        getChildren().addAll(levelCounter3D, livesCounter3D, pac3D);
        pac3D.powerLight().ifPresent(getChildren()::add);
        entities3D.selectAllOfType(Ghost3D.class).sorted(BY_PERSONALITY).forEach(getChildren()::add);
        entities3D.selectAllOfType(Energizer3D.class).map(Energizer3D::shape).forEach(getChildren()::add);
        entities3D.selectAllOfType(Pellet3D.class).map(Pellet3D::shape).forEach(getChildren()::add);
        getChildren().addAll(maze3D.particlesGroup());
        getChildren().addAll(maze3D, maze3D.house().root(), maze3D.house().doors());
        getChildren().add(ghostHunterLight);
    }

    // --- Animations

    private void createEnergizerParticlesAnimation(ParticlesAnimationConfig particlesAnimationConfig) {
        final List<PhongMaterial> ghostDressMaterials = entities3D.selectAllOfType(Ghost3D.class).sorted(BY_PERSONALITY)
            .map(ghost3D -> ghost3D.materials().normalMaterial().dressMaterial())
            .toList();

        final ExplosionConfig config = particlesAnimationConfig.explosion();

        particlePool = new Pool<>(1000, 200,
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
            maze3D.particlesGroup(),
            particle -> particle.collidesWith(maze3D.floor()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void createGhostLightAnimation() {
        final var ghostLightAnimation = new GhostLightRelayAnimation(ghostHunterLight,
            entities3D.selectAllOfType(Ghost3D.class).sorted(BY_PERSONALITY).toList());
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
    }
}
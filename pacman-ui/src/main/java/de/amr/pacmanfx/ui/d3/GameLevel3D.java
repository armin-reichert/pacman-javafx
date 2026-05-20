/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.model.Game;
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
import de.amr.pacmanfx.ui.d3.animation.GhostLightAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.entities.LevelCounter3D;
import de.amr.pacmanfx.ui.d3.entities.LivesCounter3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.*;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DAppearanceController;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3DTransformController;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import de.amr.pacmanfx.uilib.model3D.world.Pellet3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D visualization of a Pac-Man game level.
 */
public class GameLevel3D extends Group implements DisposableGraphicsObject {

    public enum AnimationID {
        ENERGIZER_PARTICLES_MOVEMENT,
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL, 
        LEVEL_COMPLETED_SHORT,
        WALL_COLOR_FLASHING
    }

    private class ManagedEnergizerParticlesAnimation3D extends ManagedAnimation {

        EnergizerParticlesAnimation3D energizerParticlesAnimation3D;

        public ManagedEnergizerParticlesAnimation3D() {
            super("Energizer Particles Animation");
            setFactory(this::createAnimationFX);
        }

        public void triggerExplosion(Point3D center) {
            animationFX(); // ensure wrapped animation is created
            energizerParticlesAnimation3D.triggerExplosion(center);
        }

        private Animation createAnimationFX() {
            final Maze3D maze3D = entities3D.unique(Maze3D.class);
            final House house = level.worldMap().terrainLayer().house();

            // The 3 ghost revival positions inside the house from left to right
            final List<Vector3f> swirlBases = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
                .map(house::ghostRevivalTile)
                .map(tile -> tile.scaled(TS).plus(HTS, HTS))
                .map(pos -> new Vector3f(pos.x(), pos.y(), 0))
                .toList();

            energizerParticlesAnimation3D = new EnergizerParticlesAnimation3D(
                particleAnimationConfig,
                swirlBases,
                ghostDressMaterials,
                particlePool,
                maze3D.particlesGroup()
            );
            energizerParticlesAnimation3D.setFloorCollisionTest(particle -> particle.collidesWith(maze3D.floor()));
            energizerParticlesAnimation3D.setOutOfWorldTest(particle -> particle.pos().z() > 50); // positive z is below maze floor

            final var timeline = new Timeline(new KeyFrame(Duration.millis(16.666), _ -> energizerParticlesAnimation3D.tick()));
            timeline.setCycleCount(Animation.INDEFINITE);

            return timeline;
        }
    }

    private static final Comparator<Ghost3D> BY_PERSONALITY = Comparator.comparingInt(ghost3D -> ghost3D.ghost().personality());

    private final GameLevel level;
    private final GameLevelEntitySet entities3D = new GameLevelEntitySet();
    private final AnimationRegistry animationRegistry = new AnimationRegistry();
    private final UIConfig uiConfig;

    private final List<PhongMaterial> ghostDressMaterials;
    private final Pool<EnergizerParticle3D> particlePool;

    private final ParticleAnimationConfig particleAnimationConfig;

    private MessageManager3D messageManager;

    /**
     * Creates a new 3D level representation for the given game level.
     *
     * @param level          the game level to visualize
     * @param uiConfig       the global UI configuration (provides 3D settings, colors, models)
     * @param particleAnimationConfig particle animation configuration
     * @param localizedTexts the resource bundle containing the localized UI texts
     */
    public GameLevel3D(GameLevel level, UIConfig uiConfig, ParticleAnimationConfig particleAnimationConfig, ResourceBundle localizedTexts) {
        this.level = requireNonNull(level);
        this.uiConfig = requireNonNull(uiConfig);
        this.particleAnimationConfig = requireNonNull(particleAnimationConfig);
        requireNonNull(localizedTexts);

        final WorldMapColorScheme mapColorScheme = uiConfig.colorScheme(level.worldMap());
        createPac3D(uiConfig.entityConfig().pacConfig());
        createGhosts3D(uiConfig.entityConfig().ghostConfigs());
        createMaze3D(mapColorScheme);
        createLevelCounter3D();
        createLivesCounter3D();
        createFood3D();
        createMessageManager();
        buildHierarchy();

        ghostDressMaterials = ghosts3DByPersonality()
            .map(ghost3D -> ghost3D.materials().normalMaterial().dressMaterial())
            .toList();

        particlePool = new Pool<>(1000,
            () -> createExplosionParticle(particleAnimationConfig.explosion()),
            particle -> {
                particle.reset();
                particle.shape().setVisible(false);
            }
        );

        // Maze3D must exist when energizer animations are created!
        createAnimations(mapColorScheme);

        setMouseTransparent(true); // this increases performance they say...
    }

    @Override
    public void dispose() {
        animationRegistry.dispose();
        entities3D.dispose();
        particlePool.dispose();
        cleanupGroup(this, true);
        if (messageManager != null) {
            messageManager.dispose();
            messageManager = null;
        }
    }

    /**
     * Starts the lives counter symbols following Pac-Man with their eyes.
     */
    public void startLivesCounterTrackingPac() {
        final LivesCounter3D livesCounter3D = entities3D.unique(LivesCounter3D.class);
        final Pac3D pac3D = entities3D.unique(Pac3D.class);
        livesCounter3D.startTracking(pac3D);
    }

    // Public accessors

    public UIConfig uiConfig() {
        return uiConfig;
    }

    public GameLevelEntitySet entities() {
        return entities3D;
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

    public void setDrawMode(DrawMode drawMode) {
        requireNonNull(drawMode);
        entities3D.all().forEach(entity -> {
            switch (entity) {
                case Pac3D pac3D -> setDrawMode(pac3D, drawMode);
                case Ghost3D ghost3D -> setDrawMode(ghost3D, drawMode);
                case Maze3D m3D -> setDrawMode(m3D, drawMode);
                default -> {}
            }
        });
    }

    // Private area, no trespassing

    public Optional<Ghost3D> ghost3D(byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return entities3D.allWhere(Ghost3D.class, ghost3D -> ghost3D.ghost().personality() == personality).findFirst();
    }

    // Order matters for correct transparency!
    private void buildHierarchy() {
        final Maze3D maze3D = entities3D.unique(Maze3D.class);
        final Pac3D pac3D = entities3D.unique(Pac3D.class);
        final LevelCounter3D levelCounter3D = entities3D.unique(LevelCounter3D.class);
        final LivesCounter3D livesCounter3D = entities3D.unique(LivesCounter3D.class);
        getChildren().addAll(levelCounter3D, livesCounter3D, pac3D);
        pac3D.powerLight().ifPresent(getChildren()::add);
        entities3D.all(Ghost3D.class).sorted(BY_PERSONALITY).forEach(getChildren()::add);
        entities3D.all(Energizer3D.class).map(Energizer3D::shape).forEach(getChildren()::add);
        entities3D.all(Pellet3D.class).map(Pellet3D::shape).forEach(getChildren()::add);
        getChildren().addAll(maze3D.particlesGroup());
        getChildren().addAll(maze3D, maze3D.house().root(), maze3D.house().doors());
    }

    private void createPac3D(PacConfig config) {
        final Pac3D pac3D = uiConfig.factory3D().createPac3D(level.pac(), config, animationRegistry);
        entities3D.add(pac3D);
    }

    private void createGhosts3D(List<GhostConfig> ghostConfigs) {
        level.ghosts().map(ghost -> {
            final GhostConfig config = ghostConfigs.get(ghost.personality());
            final Ghost3D ghost3D = createGhost3D(config, ghost);
            ghost3D.init(level);
            return ghost3D;
        }).forEach(entities3D::add);
    }

    private Ghost3D createGhost3D(GhostConfig ghostConfig, Ghost ghost) {
        final var ghost3D = uiConfig.factory3D().createGhost3D(ghost, ghostConfig, animationRegistry);
        ghost3D.setAppearanceController(new Ghost3DAppearanceController());
        ghost3D.setTransformController(new Ghost3DTransformController());
        return ghost3D;
    }

    private void createLivesCounter3D() {
        final var counter3D = new LivesCounter3D(uiConfig);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        entities3D.add(counter3D);
    }

    private void createLevelCounter3D() {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final var counter3D = new LevelCounter3D(animationRegistry, uiConfig);
        counter3D.setTranslateX(TS(terrain.numCols() - 2));
        counter3D.setTranslateY(TS(2));
        counter3D.setTranslateZ(-uiConfig.entityConfig().levelCounter().elevation());
        entities3D.add(counter3D);
    }

    private void createMaze3D(WorldMapColorScheme colorScheme) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Maze3D maze3D = uiConfig.factory3D().createMaze3D(terrain, uiConfig.entityConfig(), colorScheme, animationRegistry);
        maze3D.wallOpacityProperty().bind(GameUIConstants.PROPERTY_3D_WALL_OPACITY);
        maze3D.wallBaseHeightProperty().bind(GameUIConstants.PROPERTY_3D_WALL_HEIGHT);
        maze3D.floorColorProperty().bind(GameUIConstants.PROPERTY_3D_FLOOR_COLOR);
        entities3D.add(maze3D);
    }

    private void createMessageManager() {
        this.messageManager = new MessageManager3D(animationRegistry, this);
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

    private void setDrawMode(Group group, DrawMode drawMode) {
        for (Node node : group.getChildren()) {
            if (node instanceof Group subGroup) {
                setDrawMode(subGroup, drawMode);
            }
            else if (node instanceof Shape3D shape3D) {
                shape3D.setDrawMode(drawMode);
            }
        }
    }

    // Bonus

    protected void addOrReplaceBonus3D(Bonus bonus) {
        // Make list copy to avoid exception when removing inside for-each
        List.copyOf(entities3D.all(Bonus3D.class).toList()).forEach(bonus3D -> {
            entities3D.remove(bonus3D);
            getChildren().remove(bonus3D.root());
            bonus3D.dispose();
        });
        final Bonus3D bonus3D = createBonus3D(bonus);
        getChildren().add(bonus3D.root());
        bonus3D.lookEdible();
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        final BonusConfig config = uiConfig.entityConfig().bonusConfig();
        final Bonus3D bonus3D = new Bonus3D(animationRegistry, bonus,
            uiConfig.bonusSymbolImage(bonus.symbol()), config.bonusSymbolWidth(),
            uiConfig.bonusValueImage(bonus.symbol()),  config.bonusPointsWidth());
        entities3D.add(bonus3D);
        return bonus3D;
    }

    // Food (pellets and energizers)

    public static final double PELLET_EATING_DELAY_SEC = 0.05;

    private void createFood3D() {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Maze3D maze3D = entities3D.unique(Maze3D.class);

        final WorldMapColorScheme colorScheme = uiConfig.colorScheme(level.worldMap());

        // Pellets
        final PelletConfig3D pelletConfig3D = uiConfig.entityConfig().pellet();
        final var pelletMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.pellet()));
        final double pelletZ = maze3D.floorTop() - pelletConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(tile -> !foodLayer.isEnergizerTile(tile))
            .map(tile -> {
                final Pellet3D pellet3D = uiConfig.factory3D().createPellet3D(pelletConfig3D, pelletMaterial);
                pellet3D.setLocation(tile, pelletZ);
                return pellet3D;
            }).forEach(entities3D::add);

        // Energizers
        // TODO: Use different material?
        final EnergizerConfig3D energizerConfig3D = uiConfig.entityConfig().energizer();
        final double energizerZ = maze3D.floorTop() - energizerConfig3D.floorElevation();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(foodLayer::isEnergizerTile)
            .map(tile -> {
                final Energizer3D energizer3D = uiConfig.factory3D().createEnergizer3D(energizerConfig3D, animationRegistry, pelletMaterial);
                energizer3D.setLocation(tile, energizerZ);
                return energizer3D;
            })
            .forEach(entities3D::add);
    }

    protected void eatFoodAtTile(Vector2i tile) {
        final boolean energizerEaten = level.worldMap().foodLayer().isEnergizerTile(tile);
        if (energizerEaten) {
            energizer3DAt(tile).ifPresent(energizer3D -> {
                Logger.info("Eat energizer 3D at tile " + tile);
                energizer3D.stopPumping();
                energizer3D.hide();
                triggerEnergizerExplosion(energizer3D.shape().localToScene(Point3D.ZERO));
            });
        } else {
            pellet3DAtTile(tile).ifPresent(this::removePelletAfterDelay);
        }
    }

    private void triggerEnergizerExplosion(Point3D center) {
        final ManagedEnergizerParticlesAnimation3D particlesAnimation = animationRegistry
            .animation(AnimationID.ENERGIZER_PARTICLES_MOVEMENT, ManagedEnergizerParticlesAnimation3D.class);
        particlesAnimation.triggerExplosion(center);
    }

    private Optional<Energizer3D> energizer3DAt(Vector2i tile) {
        return entities3D.allWhere(Energizer3D.class, e3D -> tile.equals(e3D.tile())).findFirst();
    }

    private Optional<Pellet3D> pellet3DAtTile(Vector2i tile) {
        return entities3D.allWhere(Pellet3D.class, p3D -> tile.equals(p3D.tile())).findFirst();
    }

    private void removePelletAfterDelay(Pellet3D pellet3D) {
        pauseSecThen(PELLET_EATING_DELAY_SEC, () -> getChildren().remove(pellet3D.shape())).play();
    }

    /**
     * Removes all pellet visualizations (used when all pellets are eaten at once).
     */
    public void removeAllPellets3D() {
        entities3D.all(Pellet3D.class)
            .map(Pellet3D::shape)
            .forEach(shape -> getChildren().remove(shape));
    }

    // Particles animation

    private EnergizerParticle3D createExplosionParticle(ExplosionConfig config) {
        final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
        final double radius = Math.clamp(RANDOM_GENERATOR.nextGaussian(2, 0.1), 0.5, 4) * config.particleMeanRadius();
        return new EnergizerParticle3D(radius, material, Vector3f.ZERO);
    }

    protected void cleanupFoodAndParticles() {
        animationRegistry.animation(AnimationID.ENERGIZER_PARTICLES_MOVEMENT).stop();
        entities3D.all(Energizer3D.class).forEach(energizer3D -> {
            energizer3D.stopPumping();
            energizer3D.hide();
        });
        // Hide 3D food explicitly (handles cheat-eat-all case)
        entities3D.all(Pellet3D.class).forEach(pellet3D -> pellet3D.shape().setVisible(false));
        entities3D.unique(Maze3D.class).particlesGroup().getChildren().clear();
    }

    // Animations

    private Stream<Ghost3D> ghosts3DByPersonality() {
        return entities3D.all(Ghost3D.class).sorted(BY_PERSONALITY);
    }

    private void createAnimations(WorldMapColorScheme colorScheme) {
        animationRegistry.register(AnimationID.WALL_COLOR_FLASHING, new WallColorFlashingAnimation(this, colorScheme));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_FULL, new LevelCompletedAnimation(this));
        animationRegistry.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(this));

        animationRegistry.register(AnimationID.ENERGIZER_PARTICLES_MOVEMENT, new ManagedEnergizerParticlesAnimation3D());

        //TODO: this is ugly and should be changed
        final var ghostLightAnimation = new GhostLightAnimation(ghosts3DByPersonality().toList());
        animationRegistry.register(AnimationID.GHOST_LIGHT, ghostLightAnimation);
        getChildren().addAll(ghostLightAnimation.light());
    }

    protected Animation createPacDyingAnimationSeq(Pac3D pac3D) {
        return new SequentialTransition(
            Ufx.doNow(() -> {
                pac3D.update(level);
                animationRegistry.animation(Pac3D.AnimationID.CHEWING).stop();
                animationRegistry.animation(Pac3D.AnimationID.MOVING).stop();
            }),
            Ufx.pauseSecThen(1.5, () -> optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound)),
            animationRegistry.animation(Pac3D.AnimationID.DYING).animationFX(),
            Ufx.pauseSec(0.5)
        );
    }

    /**
     * Plays the level completion animation sequence and resets game timer.
     *
     * @param maze3D the 3D maze to be animated
     * @param gameState the current game state (used to determine cut-scene follow-up)
     */
    public void playLevelEndAnimation(Maze3D maze3D, State<Game> gameState) {
        final boolean cutScene = level.cutSceneNumber() != 0;
        final PerspectiveID perspectiveBeforeAnimation = GameUIConstants.PROPERTY_3D_PERSPECTIVE_ID.get();

        final var seq = new SequentialTransition(
            pauseSecThen(2, () -> {
                GameUIConstants.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            animationRegistry.animation(cutScene ? AnimationID.LEVEL_COMPLETED_SHORT: AnimationID.LEVEL_COMPLETED_FULL).animationFX(),
            pauseSec(0.25)
        );

        seq.setOnFinished(_ -> {
            GameUIConstants.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(GameUIConstants.PROPERTY_3D_WALL_HEIGHT);
            gameState.expire();
        });

        gameState.lock();
        seq.play();
    }
}
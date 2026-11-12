/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Disposable;
import de.amr.basics.Pool;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.Ghost3DAnimationID;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DAnimations;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.house.House3DAnimation;
import de.amr.pacmanfx.ui.entities3D.house.House3DAnimationID;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounterView3D;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounterView3DAnimation;
import de.amr.pacmanfx.ui.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.messageview.comp.MessageView3DComp;
import de.amr.pacmanfx.ui.entities3D.pac.anim.MsPacManDyingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.anim.PacChewingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.anim.PacManDyingAnimation3D;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.entities3D.world.EnergizerParticle3D;
import de.amr.pacmanfx.ui.entities3D.world.EnergizerView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.GhostLightRelayAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsViewModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

import java.util.List;

import static de.amr.basics.math.RandomNumbers.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static java.util.Objects.requireNonNull;

public class PlaySceneView3DAnimationSystem implements Disposable {

    private final AnimationRegistry registry;
    
    public PlaySceneView3DAnimationSystem(AnimationRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    public AnimationRegistry registry() {
        return registry;
    }

    public void stopAllAnimations() {
        registry.stopAllAnimations();
    }

    public void startEnergizerPumping(GameLevelView3D level3D) {
        if (level3D != null) {
            level3D.energizers3D().forEach(this::startPumping);
        }
    }

    public void stopEnergizerPumping(GameLevelView3D level3D) {
        if (level3D != null) {
            level3D.energizers3D().forEach(this::stopPumping);
        }
    }

    public void stopWallFlashing() {
        registry.optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);
    }

    public void startWallFlashing() {
        registry.optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::replay);
    }

    public void startParticlesAnimation() {
        registry.optAnimation(PlayScene3DAnimationID.PARTICLES).ifPresent(ManagedAnimation::replay);
    }

    public void stopParticlesAnimation() {
        registry.optAnimation(PlayScene3DAnimationID.PARTICLES).ifPresent(ManagedAnimation::stop);
    }

    public void startGhostLightAnimation() {
        registry.optAnimation(PlayScene3DAnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::replay);
    }

    public void stopAnimationsBeforePacManDies() {
        registry.optAnimation(PlayScene3DAnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        registry.optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);
    }

    public void startPumping(EnergizerView3D energizerView3D) {
        final Vector2i tile = energizerView3D.tile();
        registry.optAnimation(EnergizerView3D.AnimationID.ENERGIZER_PUMPING.atTile(tile)).ifPresent(ManagedAnimation::playOrContinue);
    }

    public void stopPumping(EnergizerView3D energizerView3D) {
        final Vector2i tile = energizerView3D.tile();
        registry.optAnimation(EnergizerView3D.AnimationID.ENERGIZER_PUMPING.atTile(tile)).ifPresent(ManagedAnimation::stop);
    }

    @Override
    public void dispose() {
        if (particlePool != null) {
            particlePool.dispose();
        }
    }

    // --- Creation

    private final ParticlesAnimationConfig particlesAnimationConfig = Game3DSettingsViewModel.DEFAULT_PARTICLE_ANIMATION_CONFIG;

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    public void createPacAnimations(Pac pac) {
        if (pac.state().isMale()) {
            createPacManAnimations(pac);
        } else {
            createMsPacManAnimations(pac);
        }
    }

    public void createGhostAnimations(List<Ghost> ghosts, GameVariantUIConfig uiConfig, int numFlashes) {
        ghosts.forEach(ghost -> {
            final GhostSettings settings = uiConfig.worldSettings().ghosts().get(ghost.personality().ordinal());
            createGhostAnimations(ghost, settings, numFlashes);
        });
    }

    public void createGhostLightAnimation(List<Ghost> ghosts, GameVariantUIConfig config, PointLight ghostHunterLight) {
        final var animation = new GhostLightRelayAnimation(
            ghostHunterLight,
            ghosts,
            config.worldSettings().ghosts()
        );
        registry.register(PlayScene3DAnimationID.GHOST_LIGHT, animation);
    }

    public void createEnergizerAnimations(GameVariantUIConfig uiConfig, GameLevelView3D level3D) {
        createEnergizerAnimations(level3D, uiConfig.worldSettings().energizer());
        createEnergizerParticlesAnimation(level3D.maze3D(), level3D.level());
    }

    public void createMazeAnimations(GameVariantUIConfig uiConfig, GameLevelView3D level3D, int numFlashes) {
        final GenericWorldMapColorScheme mapColorScheme = uiConfig.renderConfig().colorScheme(level3D.level().worldMap(), uiConfig.worldSettings());

        registry.register(PlayScene3DAnimationID.WALL_COLOR_FLASHING,
            new WallColorFlashingAnimation(mapColorScheme, level3D.maze3D().materials().wallTopMaterial()));

        registry.register(PlayScene3DAnimationID.LEVEL_COMPLETED_FULL,
            new LevelCompletedAnimation(level3D, numFlashes, uiConfig.optSoundEffects().orElseThrow()));

        registry.register(PlayScene3DAnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(level3D, numFlashes));
    }

    public void createHouseAnimations(House house) {
        if (!house.hasComponent(House3DAnimation.class)) {
            final HouseView3D houseView3D = house.assertComponent(HouseView3D.class);
            final var animation = new House3DAnimation(houseView3D.barThicknessProperty);
            house.setComponent(House3DAnimation.class, animation);
            registry.register(House3DAnimationID.HOUSE_DOORS_MELTING, animation);
        }
    }

    public void createMessageViewAnimation(MessageView messageView) {
        messageView.setComponent(MessageView3DAnimationComp.class,
            new MessageView3DAnimationComp(registry, messageView.assertComponent(MessageView3DComp.class)));
    }

    public void createLevelCounterAnimation(LevelCounter levelCounter) {
        levelCounter.setComponent(LevelCounterView3DAnimation.class,
            new LevelCounterView3DAnimation(
                levelCounter.assertComponent(LevelCounterView3D.class),
                registry
            )
        );
    }

    private void createPacManAnimations(Pac pac) {
        final PacView3D view3D = pac.assertComponent(PacView3D.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HeadBangingAnimation3D(pac));
        anim3D.setDying(new PacManDyingAnimation3D(view3D));
    }

    private void createMsPacManAnimations(Pac pac) {
        final PacView3D view3D = pac.assertComponent(PacView3D.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HipSwayingAnimation3D(pac));
        anim3D.setDying(new MsPacManDyingAnimation3D(view3D));
    }

    private Pac3DAnimationComp ensurePacAnim3DExists(Pac pac) {
        if (!pac.hasComponent(Pac3DAnimationComp.class)) {
            final var anim3D = new Pac3DAnimationComp(registry);
            pac.setComponent(Pac3DAnimationComp.class, anim3D);
        }
        return pac.assertComponent(Pac3DAnimationComp.class);
    }

    private void createGhostAnimations(Ghost ghost, GhostSettings settings, int numFlashes) {
        final Ghost3DAnimations animations = ghost.assertComponent(Ghost3DAnimations.class);
        animations.createAnimations(ghost, settings, numFlashes);
        registry.register(Ghost3DAnimationID.FLASHING.key(ghost), animations.flashing());
        registry.register(Ghost3DAnimationID.DRESS_MOVEMENT.key(ghost), animations.dressMovement());
        registry.register(Ghost3DAnimationID.BRAKING.key(ghost), animations.braking());
    }

    private void createEnergizerAnimations(GameLevelView3D level3D, Energizer3DSettings settings) {
        final int pumpingFrequency = settings.pumpingFrequency();
        final double inflatedSize = settings.scalingInflated();
        final double expandedSize = settings.scalingExpanded();
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            final String animationID = EnergizerView3D.AnimationID.ENERGIZER_PUMPING.atTile(tile);
            registry.optAnimation(animationID).ifPresent(ManagedAnimation::dispose);
            final var pumping = createEnergizerPumpingAnimation(
                "Energizer Pumping, Tile %s".formatted(tile),
                energizer3D.root(),
                pumpingFrequency,
                inflatedSize,
                expandedSize
            );
            registry.register(animationID, pumping);
        });
    }

    private ManagedAnimation createEnergizerPumpingAnimation(
        String label,
        Shape3D shape3D,
        int pumpingFrequency,
        double inflatedSize,
        double expandedSize)
    {
        final var animation = new ManagedAnimation(label);
        animation.setAnimationFactory(() -> {
            final Duration duration = Duration.seconds(1).divide(2 * pumpingFrequency);
            final var pumping = new ScaleTransition(duration, shape3D);
            pumping.setAutoReverse(true);
            pumping.setCycleCount(Animation.INDEFINITE);
            pumping.setInterpolator(Interpolator.EASE_BOTH);
            pumping.setFromX(expandedSize);
            pumping.setFromY(expandedSize);
            pumping.setFromZ(expandedSize);
            pumping.setToX(inflatedSize);
            pumping.setToY(inflatedSize);
            pumping.setToZ(inflatedSize);
            return pumping;
        });
        return animation;
    }

    private void createEnergizerParticlesAnimation(WorldMapView3D maze3D, GameLevel level) {
        final ExplosionConfig explosionConfig = particlesAnimationConfig.explosion();

        final List<PhongMaterial> ghostDressMaterials = level.entitySet().ghosts()
            .map(ghost -> ghost.assertComponent(GhostView3D.class))
            .map(ghostView3D -> ghostView3D.appearanceMaterialSet().normalMaterials().dressMaterial())
            .toList();

        particlePool = new Pool<>(300, 300,
            () -> {
                final PhongMaterial material = ghostDressMaterials.get(randomInt(0, 4));
                final double scale = Math.clamp(RANDOM_GENERATOR.nextGaussian(2, 0.1), 0.5, 4);
                final double radius = scale * explosionConfig.particleMeanRadius();
                return new EnergizerParticle3D(radius, material, Vector3f.ZERO);
            },
            particle -> {
                particle.reset();
                particle.shape().setVisible(false);
            }
        );

        final House house = level.entitySet().theOne(House.class);

        registry.register(PlayScene3DAnimationID.PARTICLES, new ParticlesAnimation3D(
            house,
            ghostDressMaterials,
            particlePool,
            particlesAnimationConfig,
            maze3D.particlesGroup(),
            particle -> particle.collidesWith(maze3D.floor3D()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void disposeEnergizerAnimations(GameLevelView3D level3D) {
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            registry.optAnimation(EnergizerView3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
                .ifPresent(ManagedAnimation::dispose);
        });
    }
}

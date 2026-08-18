/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.GhostLightRelayAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimationShort;
import de.amr.pacmanfx.ui.gamescene.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimation3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.settings.world.Energizer3DSettings;
import de.amr.pacmanfx.ui.vm.Game3DSettingsVM;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.animation.EnergizerParticle3D;
import de.amr.pacmanfx.uilib.entities3D.animation.Pool;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.MsPacManDyingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.PacChewingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.PacManDyingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.RANDOM_GENERATOR;
import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class GameLevel3DAnimationManager implements Disposable {

    public enum AnimationID implements Named {
        GHOST_LIGHT,
        LEVEL_COMPLETED_FULL,
        LEVEL_COMPLETED_SHORT,
        PARTICLES,
        WALL_COLOR_FLASHING
    }

    private final AnimationRegistry registry;

    final ParticlesAnimationConfig particlesAnimationConfig = Game3DSettingsVM.DEFAULT_PARTICLE_ANIMATION_CONFIG;

    // The particle pool is only created when the animations are created
    private Pool<EnergizerParticle3D> particlePool;

    private final GameLevel3D level3D;

    public GameLevel3DAnimationManager(
        AnimationRegistry registry,
        GameLevel3D level3D,
        GameVariantConfig variantConfig,
        GameVariantUIConfig variantUIConfig)
    {
        this.registry = requireNonNull(registry);
        this.level3D = requireNonNull(level3D);
        requireNonNull(variantConfig);
        requireNonNull(variantUIConfig);

        final GameLevel level = level3D.level();
        final int numFlashes = variantConfig.rules().numLevelFlashes(level.number());
        final GameVariantRenderConfig renderConfig = variantUIConfig.renderConfig();
        final WorldMapColorSchemeImpl mapColorScheme = renderConfig.colorScheme(level.worldMap(), variantUIConfig.worldSettings());

        registry.register(AnimationID.WALL_COLOR_FLASHING,
            new WallColorFlashingAnimation(mapColorScheme, level3D.maze3D().materials().wallTopMaterial()));

        registry.register(AnimationID.LEVEL_COMPLETED_FULL,
            new LevelCompletedAnimation(level3D, variantConfig.rules().numLevelFlashes(level.number())));

        registry.register(AnimationID.LEVEL_COMPLETED_SHORT, new LevelCompletedAnimationShort(level3D, numFlashes));

        createHouseAnimations(level.entities().house());

        createEnergizerAnimations(variantUIConfig.worldSettings().energizer());

        createEnergizerParticlesAnimation(level3D.maze3D(), level);

        createGhostLightAnimation(variantUIConfig, level, level3D.ghostHunterLight());

        final Pac pac = level.entities().pac();
        if (pac.state().isMale()) {
            createPacManAnimations(pac);
        }
        else {
            createMsPacManAnimations(pac);
        }
        //else {
        // See! Only TWO genders exist! Compiler knows it too!
        //}

        createGhostAnimations(level, variantUIConfig.worldSettings().ghosts(), numFlashes);
    }

    public void stopAll() {
        registry.stopAllAnimations(); //TODO check this
    }

    public void startEnergizerPumping() {
        level3D.energizers3D().forEach(this::startPumping);
    }

    public void stopEnergizerPumping() {
        level3D.energizers3D().forEach(this::stopPumping);
    }

    public void stopWallFlashing() {
        registry
            .optAnimation(GameLevel3DAnimationManager.AnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::stop);
    }

    public void startWallFlashing() {
        registry
            .optAnimation(GameLevel3DAnimationManager.AnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void startParticlesAnimation() {
        registry.optAnimation(GameLevel3DAnimationManager.AnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void stopParticlesAnimation() {
        registry.optAnimation(GameLevel3DAnimationManager.AnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::stop);
    }

    public void startGhostLightAnimation() {
        registry.optAnimation(GameLevel3DAnimationManager.AnimationID.GHOST_LIGHT)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void stopAnimationsBeforePacManDies() {
        registry.optAnimation(GameLevel3DAnimationManager.AnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        registry.optAnimation(GameLevel3DAnimationManager.AnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);
    }

    @Override
    public void dispose() {
        disposeEnergizerAnimations();
        registry.dispose();
        if (particlePool != null) {
            particlePool.dispose();
        }
    }

    public AnimationRegistry registry() {
        return registry;
    }

    private void createPacManAnimations(Pac pac) {
        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HeadBangingAnimation3D(pac));
        anim3D.setDying(new PacManDyingAnimation3D(view3D));
    }

    private void createMsPacManAnimations(Pac pac) {
        final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
        final Pac3DAnimationComp anim3D = ensurePacAnim3DExists(pac);

        anim3D.setChewing(new PacChewingAnimation3D(pac));
//        anim3D.setMovement(new HipSwayingAnimation3D(pac));
        anim3D.setDying(new MsPacManDyingAnimation3D(view3D));
    }

    private Pac3DAnimationComp ensurePacAnim3DExists(Pac pac) {
        if (!pac.hasComp(Pac3DAnimationComp.class)) {
            final var anim3D = new Pac3DAnimationComp(registry);
            pac.setComp(Pac3DAnimationComp.class, anim3D);
        }
        return pac.reqComp(Pac3DAnimationComp.class);
    }

    private void createGhostAnimations(GameLevel level, List<GhostSettings> settingsByPersonality, int numFlashes) {
        level.entities().ghosts().forEach(ghost -> {
            final GhostSettings settings = settingsByPersonality.get(ghost.personality().ordinal());
            createGhostAnimations(ghost, settings, numFlashes);
        });
    }

    private void createGhostAnimations(Ghost ghost, GhostSettings settings, int numFlashes) {
        final Ghost3DAnimationComp anim3D = ensureGhostAnim3DExists(ghost);
        anim3D.build(registry, ghost, settings, numFlashes);

    }

    private Ghost3DAnimationComp ensureGhostAnim3DExists(Ghost ghost) {
        if (!ghost.hasComp(Ghost3DAnimationComp.class)) {
            ghost.setComp(Ghost3DAnimationComp.class, new Ghost3DAnimationComp());
        }
        return ghost.reqComp(Ghost3DAnimationComp.class);
    }

    private void createEnergizerParticlesAnimation(Maze3D maze3D, GameLevel level) {
        final ExplosionConfig explosionConfig = particlesAnimationConfig.explosion();

        final List<PhongMaterial> ghostDressMaterials = level.entities().ghosts().stream()
            .map(ghost -> ghost.reqComp(Ghost3DViewComp.class))
            .map(ghostView3D -> ghostView3D.appearanceMaterialSet().normal().dress())
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

        final House house = level.entities().house();

        registry.register(AnimationID.PARTICLES, new ParticlesAnimation3D(
            house,
            ghostDressMaterials,
            particlePool,
            particlesAnimationConfig,
            maze3D.particlesGroup(),
            particle -> particle.collidesWith(maze3D.floor3D()),
            particle -> particle.pos().z() > 50 // positive z is below maze floor
        ));
    }

    private void createGhostLightAnimation(GameVariantUIConfig gameVariantConfig, GameLevel level, PointLight ghostHunterLight) {
        final var animation = new GhostLightRelayAnimation(ghostHunterLight, level.entities().ghosts(),
            gameVariantConfig.worldSettings().ghosts());
        registry.register(AnimationID.GHOST_LIGHT, animation);
    }

    private void createHouseAnimations(House house) {
        final House3DViewComp house3D = house.reqComp(House3DViewComp.class);
        final var animation =  new House3DAnimationComp(registry);
        animation.createDoorsMeltingAnimationFactory(house3D.barThicknessProperty);
        if (!house.hasComp(House3DAnimationComp.class)) {
            house.setComp(House3DAnimationComp.class, animation);
        }
    }

    private void createEnergizerAnimations(Energizer3DSettings settings) {
        final int pumpingFrequency = settings.pumpingFrequency();
        final double inflatedSize = settings.scalingInflated();
        final double expandedSize = settings.scalingExpanded();
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            final String animationID = Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile);
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

    private void disposeEnergizerAnimations() {
        level3D.energizers3D().forEach(energizer3D -> {
            final Vector2i tile = energizer3D.tile();
            registry.optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
                .ifPresent(ManagedAnimation::dispose);
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

    public void startPumping(Energizer3D energizer3D) {
        final Vector2i tile = energizer3D.tile();
        registry.optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
            .ifPresent(ManagedAnimation::playOrContinue);
    }

    public void stopPumping(Energizer3D energizer3D) {
        final Vector2i tile = energizer3D.tile();
        registry.optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
            .ifPresent(ManagedAnimation::stop);
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.ui.entities3D.world.Energizer3D;

import static java.util.Objects.requireNonNull;

public class PlayScene3DAnimationSystem {

    public PlayScene3DAnimationSystem() {}

    public void stopAllAnimations(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry().stopAllAnimations(); //TODO check this
    }

    public void startEnergizerPumping(PlayScene3D playScene3D) {
        playScene3D.optGameLevel3D().ifPresent(level3D
            -> level3D.energizers3D().forEach(energizer -> startPumping(playScene3D, energizer)));
    }

    public void stopEnergizerPumping(PlayScene3D playScene3D) {
        playScene3D.optGameLevel3D().ifPresent(level3D
            -> level3D.energizers3D().forEach(energizer -> stopPumping(playScene3D, energizer)));
    }

    public void stopWallFlashing(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry()
            .optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::stop);
    }

    public void startWallFlashing(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry()
            .optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void startParticlesAnimation(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry().optAnimation(PlayScene3DAnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void stopParticlesAnimation(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry().optAnimation(PlayScene3DAnimationID.PARTICLES)
            .ifPresent(ManagedAnimation::stop);
    }

    public void startGhostLightAnimation(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry().optAnimation(PlayScene3DAnimationID.GHOST_LIGHT)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public void stopAnimationsBeforePacManDies(PlayScene3D playScene3D) {
        playScene3D.animations3D().registry().optAnimation(PlayScene3DAnimationID.GHOST_LIGHT).ifPresent(ManagedAnimation::stop);
        playScene3D.animations3D().registry().optAnimation(PlayScene3DAnimationID.WALL_COLOR_FLASHING).ifPresent(ManagedAnimation::stop);
    }

    public void startPumping(PlayScene3D playScene3D, Energizer3D energizer3D) {
        final Vector2i tile = energizer3D.tile();
        playScene3D.animations3D().registry().optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
            .ifPresent(ManagedAnimation::playOrContinue);
    }

    public void stopPumping(PlayScene3D playScene3D, Energizer3D energizer3D) {
        final Vector2i tile = energizer3D.tile();
        playScene3D.animations3D().registry().optAnimation(Energizer3D.AnimationID.ENERGIZER_PUMPING.atTile(tile))
            .ifPresent(ManagedAnimation::stop);
    }
}

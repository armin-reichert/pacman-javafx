/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Implemented by actors.
 */
public interface Animated {

    Optional<AnimationManager> animations();

    default void selectAnimation(String id) {
        requireNonNull(id);
        animations().ifPresent(am -> am.select(id));
    }

    default void selectAnimationFrame(String id, int frameIndex) {
        requireNonNull(id);
        animations().ifPresent(am -> am.selectFrame(id, frameIndex));
    }

    default void playAnimation(String id) {
        requireNonNull(id);
        animations().ifPresent(am -> am.play(id));
    }

    default void playAnimation() {
        animations().ifPresent(AnimationManager::play);
    }

    default void stopAnimation() {
        animations().ifPresent(AnimationManager::stop);
    }
}

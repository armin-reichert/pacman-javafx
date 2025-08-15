/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.Optional;

/**
 * Implemented by animated actors.
 */
public interface Animated {
    Optional<AnimationManager> animations();

    default void selectAnimation(String id) {
        animations().ifPresent(am -> am.select(id));
    }

    default void playAnimation(String id) {
        animations().ifPresent(AnimationManager::play);
    }

    default void playAnimation() {
        animations().ifPresent(AnimationManager::play);
    }

    default void stopAnimation() {
        animations().ifPresent(AnimationManager::stop);
    }
}

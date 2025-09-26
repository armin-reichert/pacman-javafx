/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Implemented by classes that support animations.
 */
public interface AnimationSupport {

    default Optional<AnimationManager> animationManager() {
        return Optional.empty();
    }

    default void selectAnimation(String animationID) {
        requireNonNull(animationID);
        animationManager().ifPresent(am -> am.select(animationID));
    }

    default void selectAnimationAt(String animationID, int frameIndex) {
        requireNonNull(animationID);
        animationManager().ifPresent(am -> am.selectFrame(animationID, frameIndex));
    }

    default void playAnimation(String animationID) {
        requireNonNull(animationID);
        animationManager().ifPresent(am -> am.play(animationID));
    }

    default void playAnimation() {
        animationManager().ifPresent(AnimationManager::play);
    }

    default void stopAnimation() {
        animationManager().ifPresent(AnimationManager::stop);
    }
}
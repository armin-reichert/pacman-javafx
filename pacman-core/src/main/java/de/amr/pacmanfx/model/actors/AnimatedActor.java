/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.Optional;

/**
 * Implemented by actors with animations.
 */
public interface AnimatedActor {

    Optional<ActorAnimationMap> animations();

    default void playAnimation() { animations().ifPresent(ActorAnimationMap::play); }

    default void playAnimation(String id) { animations().ifPresent(animations -> animations.playAnimation(id)); }

    default void stopAnimation() {
        animations().ifPresent(ActorAnimationMap::stop);
    }

    default void resetAnimation() {
        animations().ifPresent(ActorAnimationMap::reset);
    }

    default void selectAnimation(String id, int index) {
        animations().ifPresent(animations -> animations.selectAnimationAtFrame(id, index));
    }

    default void selectAnimation(String id) {
        animations().ifPresent(animations -> animations.selectAnimation(id));
    }
}

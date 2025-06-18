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

    Optional<ActorAnimationMap> animationMap();

    default void playAnimation() { animationMap().ifPresent(ActorAnimationMap::playSelectedAnimation); }

    default void playAnimation(String id) { animationMap().ifPresent(animationMap -> animationMap.playAnimation(id)); }

    default void stopAnimation() {
        animationMap().ifPresent(ActorAnimationMap::stopSelectedAnimation);
    }

    default void resetAnimation() {
        animationMap().ifPresent(ActorAnimationMap::resetSelectedAnimation);
    }

    default void selectAnimation(String id, int index) {
        animationMap().ifPresent(animationMap -> animationMap.selectAnimationAtFrame(id, index));
    }

    default void selectAnimation(String id) {
        animationMap().ifPresent(animationMap -> animationMap.selectAnimation(id));
    }
}

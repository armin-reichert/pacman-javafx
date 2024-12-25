/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface AnimatedActor2D {

    Actor2D actor();

    Optional<ActorAnimations> animations();

    default void startAnimation() {
        animations().ifPresent(ActorAnimations::start);
    }

    default void stopAnimation() {
        animations().ifPresent(ActorAnimations::stop);
    }

    default void resetAnimation() {
        animations().ifPresent(ActorAnimations::reset);
    }

    default void selectAnimation(String id, int index) {
        animations().ifPresent(animations -> animations.select(id, index));
    }

    default void selectAnimation(String id) {
        selectAnimation(id, 0);
    }
}
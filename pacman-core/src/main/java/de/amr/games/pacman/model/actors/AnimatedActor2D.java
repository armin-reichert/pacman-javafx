/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface AnimatedActor2D {

    Actor actor();

    Optional<Animations> animations();

    default void startAnimation() {
        animations().ifPresent(Animations::start);
    }

    default void stopAnimation() {
        animations().ifPresent(Animations::stop);
    }

    default void resetAnimation() {
        animations().ifPresent(Animations::reset);
    }

    default void selectAnimation(String id, int index) {
        animations().ifPresent(animations -> animations.select(id, index));
    }

    default void selectAnimation(String id) {
        selectAnimation(id, 0);
    }
}
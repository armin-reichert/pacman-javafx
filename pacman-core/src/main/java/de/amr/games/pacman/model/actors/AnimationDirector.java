/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface AnimationDirector {

    Optional<Animations> animations();

    default void selectAnimation(String name, int index) {
        animations().ifPresent(a -> a.select(name, index));
    }

    default void selectAnimation(String name) {
        animations().ifPresent(a -> a.select(name, 0));
    }

    default void startAnimation() {
        animations().ifPresent(Animations::startSelected);
    }

    default void stopAnimation() {
        animations().ifPresent(Animations::stopSelected);
    }

    default void resetAnimation() {
        animations().ifPresent(Animations::resetSelected);
    }
}
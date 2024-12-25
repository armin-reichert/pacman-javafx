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

    default Actor2D actor() {
        return (Actor2D) this;
    }

    Optional<Animations> animations();

    default void startAnimation() {
        animations().ifPresent(Animations::startCurrentAnimation);
    }

    default void stopAnimation() {
        animations().ifPresent(Animations::stopCurrentAnimation);
    }

    default void resetAnimation() {
        animations().ifPresent(Animations::resetCurrentAnimation);
    }

    default void selectAnimation(String name, int index) {
        animations().ifPresent(animations -> animations.select(name, index));
    }

    default void selectAnimation(String name) {
        selectAnimation(name, 0);
    }
}
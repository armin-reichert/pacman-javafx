/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import org.tinylog.Logger;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface AnimatedEntity {

    default Actor2D entity() {
        return (Actor2D) this;
    }

    boolean isVisible();

    void setAnimations(Animations animations);

    Animations animations();

    default Optional<Animations> optAnimations() {
        return Optional.ofNullable(animations());
    }

    default void startAnimation() {
        if (animations() != null) {
            animations().startCurrentAnimation();
        } else {
            Logger.warn("Trying to start animation before animations have been created!");
        }
    }

    default void stopAnimation() {
        if (animations() != null) {
            animations().stopCurrentAnimation();
        } else {
            Logger.warn("Trying to stop animation before animations have been created!");
        }
    }

    default void resetAnimation() {
        if (animations() != null) {
            animations().resetCurrentAnimation();
        } else {
            Logger.warn("Trying to reset animation before animations have been created!");
        }
    }

    default void selectAnimation(String name, int index) {
        if (animations() != null) {
            animations().select(name, index);
        } else {
            Logger.warn("Trying to select animation '{}' (index: {}) before animations have been created!", name, index);
        }
    }

    default void selectAnimation(String name) {
        selectAnimation(name, 0);
    }
}
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

    void setAnimations(Animations animations);

    Animations animationSet();

    default Optional<Animations> animations() {
        return Optional.ofNullable(animationSet());
    }

    default void startAnimation() {
        if (animationSet() != null) {
            animationSet().startSelected();
        } else {
            Logger.warn("Trying to start animation before animations have been created!");
        }
    }

    default void stopAnimation() {
        if (animationSet() != null) {
            animationSet().stopSelected();
        } else {
            Logger.warn("Trying to stop animation before animations have been created!");
        }
    }

    default void resetAnimation() {
        if (animationSet() != null) {
            animationSet().resetSelected();
        } else {
            Logger.warn("Trying to reset animation before animations have been created!");
        }
    }

    default void selectAnimation(String name, int index) {
        if (animationSet() != null) {
            animationSet().select(name, index);
        } else {
            Logger.warn("Trying to select animation '{}' (index: {}) before animations have been created!", name, index);
        }
    }

    default void selectAnimation(String name) {
        selectAnimation(name, 0);
    }
}
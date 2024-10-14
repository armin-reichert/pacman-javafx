/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface Animations {

    String currentAnimationID();

    Object currentAnimation();

    void select(String name, int index);

    void startCurrentAnimation();

    void stopCurrentAnimation();

    void resetCurrentAnimation();
}
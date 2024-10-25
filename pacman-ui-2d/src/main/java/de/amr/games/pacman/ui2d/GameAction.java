/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    void execute(GameContext context);

    default boolean isEnabled(GameContext context) { return true; }
}

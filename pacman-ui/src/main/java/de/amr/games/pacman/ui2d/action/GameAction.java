/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.action;


import de.amr.games.pacman.ui2d.GameContext;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    void execute(GameContext context);

    default boolean isEnabled(GameContext context) { return true; }
}

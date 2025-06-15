/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import org.tinylog.Logger;

/**
 * Common interface for game actions.
 */
public interface GameAction {

    static void executeIfEnabled(PacManGames_UI ui, GameAction action) {
        if (action.isEnabled(ui)) {
            action.execute(ui);
            Logger.trace("Action '{}' executed", action.name());
        } else {
            Logger.warn("Disabled action '{}' not executed", action.name());
        }
    }

    void execute(PacManGames_UI ui);

    default boolean isEnabled(PacManGames_UI ui) { return true; }

    default String name() { return toString(); }
}
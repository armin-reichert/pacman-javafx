/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import org.tinylog.Logger;

/**
 * Common interface for game actions.
 */
public interface GameAction {

    static void executeIfEnabled(GameUI ui, GameContext gameContext, GameAction action) {
        if (action.isEnabled(ui, gameContext)) {
            action.execute(ui, gameContext);
            Logger.trace("Action '{}' executed", action.name());
        } else {
            Logger.warn("Disabled action '{}' not executed", action.name());
        }
    }

    void execute(GameUI ui, GameContext gameContext);

    default boolean isEnabled(GameUI ui, GameContext gameContext) { return true; }

    default String name() { return toString(); }
}
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

    default void executeIfEnabled(GameUI ui, GameContext gameContext) {
        if (isEnabled(ui, gameContext)) {
            execute(ui, gameContext);
            Logger.trace("Action '{}' executed", name());
        } else {
            Logger.warn("Disabled action '{}' not executed", name());
        }
    }

    void execute(GameUI ui, GameContext gameContext);

    default boolean isEnabled(GameUI ui, GameContext gameContext) { return true; }

    default String name() { return toString(); }
}
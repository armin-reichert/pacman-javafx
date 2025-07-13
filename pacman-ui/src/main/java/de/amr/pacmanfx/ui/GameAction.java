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

    default void executeIfEnabled(GameUI ui) {
        if (isEnabled(ui)) {
            execute(ui);
            Logger.trace("Action '{}' executed", name());
        } else {
            Logger.warn("Disabled action '{}' not executed", name());
        }
    }

    void execute(GameUI ui);

    default boolean isEnabled(GameUI ui) { return true; }

    default String name() { return toString(); }
}
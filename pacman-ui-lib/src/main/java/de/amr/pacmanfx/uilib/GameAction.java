/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

import org.tinylog.Logger;

/**
 * Common interface for game actions.
 */
public interface GameAction {

    static void executeIfEnabled(GameAction action) {
        if (action.isEnabled()) {
            action.execute();
            Logger.trace("Action '{}' executed", action.name());
        } else {
            Logger.warn("Disabled action '{}' not executed", action.name());
        }
    }

    void execute();

    default boolean isEnabled() { return true; }

    default String name() { return toString(); }
}
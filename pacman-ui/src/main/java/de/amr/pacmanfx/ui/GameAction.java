/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Validations;
import org.tinylog.Logger;

/**
 * Common interface for game actions.
 */
public abstract class GameAction {

    private final String name;

    protected GameAction(String name) {
        this.name = Validations.requireValidIdentifier(name);
    }

    public abstract void execute(GameUI ui);

    public void executeIfEnabled(GameUI ui) {
        if (isEnabled(ui)) {
            execute(ui);
            Logger.trace("Action '{}' executed", name());
        } else {
            Logger.warn("Disabled action '{}' not executed", name());
        }
    }

    public boolean isEnabled(GameUI ui) { return true; }

    public String name() { return name; }
}
/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.ui.api.GameUI;
import org.tinylog.Logger;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    private final String name;

    protected GameAction(String name) {
        this.name = Validations.requireValidIdentifier(name);
    }

    public abstract void execute(GameUI ui);

    public final boolean executeIfEnabled(GameUI ui) {
        if (isEnabled(ui)) {
            execute(ui);
            Logger.trace("Action '{}' executed", name);
            return true;
        } else {
            Logger.warn("Action '{}' not executed (disabled)", name);
            return false;
        }
    }

    public boolean isEnabled(GameUI ui) { return true; }

    public String name() { return name; }
}
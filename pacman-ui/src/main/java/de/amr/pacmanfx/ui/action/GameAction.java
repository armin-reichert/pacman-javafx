/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.ui.GameUI;
import org.tinylog.Logger;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    private final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    public abstract void execute(GameUI ui);

    public final boolean executeIfEnabled(GameUI ui) {
        if (isEnabled(ui)) {
            execute(ui);
            Logger.trace("Action '{}' executed", id);
            return true;
        } else {
            Logger.warn("Action '{}' not executed (disabled)", id);
            return false;
        }
    }

    public String id() {
        return id;
    }

    public boolean isEnabled(GameUI ui) { return true; }

    public String resourceBundleKey() { return "action." + id; }
}
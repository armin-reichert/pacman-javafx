/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.ui.game.Game;
import org.tinylog.Logger;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    private final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    protected abstract void doAction(Game context);

    public boolean isEnabled(Game context) { return true; }

    public final boolean executeIfEnabled(Game context) {
        if (isEnabled(context)) {
            try {
                doAction(context);
                Logger.trace("Action '{}' executed", id);
                return true;
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", id);
                return false;
            }
        } else {
            Logger.warn("Action '{}' not executed (disabled)", id);
            return false;
        }
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }
}
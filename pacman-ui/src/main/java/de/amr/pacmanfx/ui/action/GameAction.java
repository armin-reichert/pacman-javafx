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

    protected final Game game;
    protected final String id;

    protected GameAction(Game game, String id) {
        this.game = game;
        this.id = Validations.requireValidIdentifier(id);
    }

    protected abstract void doAction();

    public boolean isEnabled() { return true; }

    public final boolean execute() {
        if (isEnabled()) {
            try {
                doAction();
                return true;
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", id);
                return false;
            }
        } else {
            Logger.warn("Action {}' not executed (disabled)", id);
            return false;
        }
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }
}
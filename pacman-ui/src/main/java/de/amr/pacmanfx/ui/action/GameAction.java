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

    private final Game game;
    private final String id;

    protected GameAction(Game game, String id) {
        this.game = game;
        this.id = Validations.requireValidIdentifier(id);
    }

    protected Game game() {
        return game;
    }

    protected abstract void doAction(Game game);

    public boolean isEnabled(Game game) { return true; }

    public final boolean execute(Game game) {
        if (isEnabled(game)) {
            try {
                doAction(game);
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
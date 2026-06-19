/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

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

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    /**
     * This method has to be implemented by subclasses.
     */
    protected abstract void doAction();

    /**
     * This method may be implemented by subclasses to define when this action is enabled.
     *
     * @return {@code true} if this action can be executed
     */
    public boolean isEnabled() { return true; }

    public final boolean execute() {
        boolean success = false;
        if (isEnabled()) {
            try {
                doAction();
                success = true;
                Logger.info("Action '{}' executed successfully", id);
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", id);
            }
        } else {
            Logger.warn("Action {}' not executed (disabled)", id);
        }

        // Clear the input that triggered this action
        game.input().keyboard().clearState();

        return success;
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }
}
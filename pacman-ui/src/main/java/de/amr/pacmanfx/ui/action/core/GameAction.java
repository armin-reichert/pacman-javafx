/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.pacmanfx.core.Validations;
import org.tinylog.Logger;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    protected final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    /**
     * This method has to be implemented by subclasses.
     */
    protected abstract void doAction(GameAppContext app);

    /**
     * This method may be implemented by subclasses to define when this action is enabled.
     *
     * @param app application context
     * @return {@code true} if this action can be executed
     */
    public boolean isEnabled(GameAppContext app) { return true; }

    public final boolean execute(GameAppContext app) {
        boolean success = false;
        if (isEnabled(app)) {
            try {
                doAction(app);
                success = true;
                Logger.info("Action '{}' executed successfully", id);
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", id);
            }
        } else {
            Logger.warn("Action {}' not executed (disabled)", id);
        }

        //TODO This is dubious!

        // Clear the input that triggered this action
        app.input().keyboard().clearState();

        return success;
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }
}
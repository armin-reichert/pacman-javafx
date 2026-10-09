/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.pacmanfx.core.Validations;
import org.tinylog.Logger;

/**
 * Common base class for game actions.
 */
public abstract class GameAction {

    public static boolean runAction(GameAction gameAction, GameActionExecutionContext context) {
        boolean success = false;
        if (gameAction.isEnabled(context)) {
            try {
                gameAction.execute(context);
                success = true;
                Logger.trace("Action '{}' executed successfully", gameAction.id());
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", gameAction.id());
            }
        } else {
            Logger.warn("Action {}' not executed (disabled)", gameAction.id());
        }

        //TODO This is dubious!
        // Clear the input that triggered this action
        context.input().keyboard().clearState();

        return success;

    }

    protected final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }

    public abstract void execute(GameActionExecutionContext context);

    public boolean isEnabled(GameActionExecutionContext context) { return true; }
}
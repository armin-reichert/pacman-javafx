/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;


import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.input.Keyboard;
import org.tinylog.Logger;

import java.util.Optional;

public class ActionExecutor {

    public static Optional<GameAction<GameEngineContext>> executeMatchingAction(
        ActionBindingsRegistry<GameEngineContext> registry,
        GameEngineContext context,
        Keyboard keyboard)
    {
        final Optional<GameAction<GameEngineContext>> matchingAction = registry.findActionMatchingPressedKeys(keyboard);
        matchingAction.ifPresent(action -> runAction(action, context, keyboard));
        return matchingAction;
    }

    public static boolean runAction(
        GameAction<GameEngineContext> gameAction,
        GameEngineContext context,
        Keyboard keyboard) {
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
        keyboard.clearState();

        return success;
    }
}

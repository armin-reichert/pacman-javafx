/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.action;

import de.amr.pacmanfx.engine.GameEngineContext;
import de.amr.pacmanfx.engine.input.Keyboard;
import org.tinylog.Logger;

import java.util.Optional;

public class ActionExecutor {

    public static <C extends GameEngineContext> Optional<GameAction<C>> executeMatchingAction(
        ActionBindingsRegistry<C> registry,
        C context)
    {
        final var matchingAction = registry.findActionMatchingPressedKeys(context.input().keyboard());
        matchingAction.ifPresent(action -> runAction(action, context));
        return matchingAction;
    }

    public static <C extends GameEngineContext> boolean runAction(GameAction<C> gameAction, C context) {
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
            Logger.warn("Action '{}' not executed (disabled)", gameAction.id());
        }

        //TODO This is dubious!
        context.input().keyboard().clearState();

        return success;
    }
}

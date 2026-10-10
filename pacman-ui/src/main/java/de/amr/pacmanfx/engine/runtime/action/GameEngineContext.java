/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.config.GameVariantManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import org.tinylog.Logger;

import java.util.Optional;

public interface GameEngineContext {

    default Optional<GameAction<GameEngineContext>> executeMatchingAction(
        ActionBindingsRegistry<GameEngineContext> registry) {
        final Optional<GameAction<GameEngineContext>> matchingAction = registry.findActionMatchingPressedKeys(input().keyboard());
        matchingAction.ifPresent(this::runAction);
        return matchingAction;
    }

    default boolean runAction(GameAction<GameEngineContext> gameAction) {
        boolean success = false;
        if (gameAction.isEnabled(this)) {
            try {
                gameAction.execute(this);
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
        input().keyboard().clearState();

        return success;
    }

    GameClock clock();

    Optional<GameContext> optCurrentGame();

    EngineLifecycle engineLife();

    GameSceneManager gameSceneManager();

    GameVariantManager gameVariantManager();

    Input input();

    RenderManager renderManager();

    SoundManager soundManager();

    TranslationManager translationManager();

    GameUI ui();

    DirectoryWatchdog watchdog();
}

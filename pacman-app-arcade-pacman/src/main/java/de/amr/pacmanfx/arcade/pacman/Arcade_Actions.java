/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.event.CreditAddedEvent;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;

public final class Arcade_Actions {

    private final GameAction actionInsertCoin;
    private final GameAction actionStartPlaying;

    private final Set<ActionKeyBinding> gameStartActionBindings;

    public Arcade_Actions(GameAppContext appContext) {

        actionInsertCoin = new GameAction(appContext, "insert_coin") {
            @Override
            public void doAction() {
                final CoinMechanism coinMechanism = appContext.coinMechanism();
                appContext.ui().sounds().stopVoiceAndDisposeVoicePlayer();
                appContext.ui().sounds().setEnabled(true);
                coinMechanism.insertCoin();
                gameContext().eventManager().publishGameEvent(new CreditAddedEvent(1));
                gameFlow().enterState(GameStateID.GAME_PREPARATION);
            }

            @Override
            public boolean isEnabled() {
                if (gameContext().coinMechanism().isFull()) {
                    return false;
                }
                // In demo level, coin can always be inserted
                if (gameContext().gamePlay().isDemoLevelRunning(gameContext())) {
                    return true;
                }
                final GameState gameState = gameContext().state();
                return GameStateID.GAME_INTRO.identifies(gameState)
                    || GameStateID.GAME_PREPARATION.identifies(gameState);
            }
        };

        actionStartPlaying = new GameAction(appContext, "start_playing") {
            @Override
            public void doAction() {
                appContext.ui().sounds().stopVoiceAndDisposeVoicePlayer();
                gameFlow().enterState(gameContext(), Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
            }

            @Override
            public boolean isEnabled() {
                if (gameContext().coinMechanism().isEmpty()) {
                    return false;
                }
                final GameState state = gameContext().state();
                return (GameStateID.GAME_INTRO.identifies(state)
                    || GameStateID.GAME_PREPARATION.identifies(state));
            }
        };

        gameStartActionBindings = Set.of(
            new ActionKeyBinding(actionInsertCoin,   bareKey(KeyCode.DIGIT5), bareKey(KeyCode.NUMPAD5)),
            new ActionKeyBinding(actionStartPlaying, bareKey(KeyCode.DIGIT1), bareKey(KeyCode.NUMPAD1))
        );
    }

    public GameAction actionInsertCoin() {
        return actionInsertCoin;
    }

    public GameAction actionStartPlaying() {
        return actionStartPlaying;
    }

    public Set<ActionKeyBinding> gameStartActionBindings() {
        return gameStartActionBindings;
    }
}

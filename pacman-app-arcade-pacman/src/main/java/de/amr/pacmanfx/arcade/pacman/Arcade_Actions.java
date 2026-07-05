/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;

public final class Arcade_Actions {

    private final GameAction actionInsertCoin;
    private final GameAction actionStartPlaying;

    private final Set<ActionKeyBinding> gameStartActionBindings;

    public Arcade_Actions(Game game) {

        actionInsertCoin = new GameAction(game, "insert_coin") {
            @Override
            public void doAction() {
                final CoinMechanism coinMechanism = game.coinMechanism();
                final GameContext gameContext = game.context();
                game.ui().sounds().stopAndDisposeVoice();
                game.ui().sounds().setEnabled(true);
                coinMechanism.insertCoin();
                gameContext.flow().publishGameEvent(new CreditAddedEvent(gameContext, 1));
                gameContext.flow().enterState(GameStateID.GAME_PREPARATION);
            }

            @Override
            public boolean isEnabled() {
                final CoinMechanism coinMechanism = game.coinMechanism();
                if (coinMechanism.isFull()) {
                    return false;
                }
                final GameContext context = game.context();
                // In demo level, coin can always be inserted
                if (context.gamePlay().isDemoLevelRunning(context)) {
                    return true;
                }
                final GameState gameState = context.state();
                return GameStateID.GAME_INTRO.identifies(gameState) || GameStateID.GAME_PREPARATION.identifies(gameState);
            }
        };

        actionStartPlaying = new GameAction(game, "start_playing") {
            @Override
            public void doAction() {
                game.ui().sounds().stopAndDisposeVoice();
                game.context().flow().enterState(Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
            }

            @Override
            public boolean isEnabled() {
                final CoinMechanism coinMechanism = game.coinMechanism();
                if (coinMechanism.isEmpty()) {
                    return false;
                }
                final GameContext gameContext = game.context();
                final GameModel gameModel = gameContext.model();
                final GameState gameState = gameContext.state();
                return (GameStateID.GAME_INTRO.identifies(gameState) || GameStateID.GAME_PREPARATION.identifies(gameState))
                    && gameModel.canStartNewGame(gameContext);
            }
        };

        gameStartActionBindings = Set.of(
            new ActionKeyBinding(actionInsertCoin(),   bareKey(KeyCode.DIGIT5), bareKey(KeyCode.NUMPAD5)),
            new ActionKeyBinding(actionStartPlaying(), bareKey(KeyCode.DIGIT1), bareKey(KeyCode.NUMPAD1))
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

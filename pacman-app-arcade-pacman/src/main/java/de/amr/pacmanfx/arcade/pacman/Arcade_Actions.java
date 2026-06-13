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
import de.amr.pacmanfx.ui.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public final class Arcade_Actions {

    private final Game game;

    private final Set<ActionKeyBinding> gameStartActionBindings;

    public Arcade_Actions(Game game) {
        this.game = game;

        gameStartActionBindings = Set.of(
            new ActionKeyBinding(actionInsertCoin(),    bare(KeyCode.DIGIT5), bare(KeyCode.NUMPAD5)),
            new ActionKeyBinding(actionStartPlaying(),  bare(KeyCode.DIGIT1), bare(KeyCode.NUMPAD1))
        );
    }

    public Set<ActionKeyBinding> gameStartActionBindings() {
        return gameStartActionBindings;
    }

    private GameAction actionInsertCoin;

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public GameAction actionInsertCoin() {
        if (actionInsertCoin == null) {
            actionInsertCoin = new GameAction(game, "insert_coin") {
                @Override
                public void doAction() {
                    final CoinMechanism coinMechanism = game.coinMechanism();
                    final GameContext gameContext = game.currentGameContext();
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
                    final GameContext gameContext = game.currentGameContext();
                    // In demo level, coin can always be inserted
                    if (gameContext.model().isDemoLevelRunning()) {
                        return true;
                    }
                    final GameState gameState = gameContext.state();
                    return GameStateID.GAME_INTRO.identifies(gameState) || GameStateID.GAME_PREPARATION.identifies(gameState);
                }
            };
        }
        return actionInsertCoin;
    }

    private GameAction actionStartPlaying;

    public GameAction actionStartPlaying() {
        if (actionStartPlaying == null) {
            actionStartPlaying = new GameAction(game, "start_playing") {
                @Override
                public void doAction() {
                    game.ui().sounds().stopAndDisposeVoice();
                    game.currentGameContext().flow().enterState(Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
                }

                @Override
                public boolean isEnabled() {
                    final CoinMechanism coinMechanism = game.coinMechanism();
                    if (coinMechanism.isEmpty()) {
                        return false;
                    }
                    final GameContext gameContext = game.currentGameContext();
                    final GameModel gameModel = gameContext.model();
                    final GameState gameState = gameContext.state();
                    return (GameStateID.GAME_INTRO.identifies(gameState) || GameStateID.GAME_PREPARATION.identifies(gameState))
                        && gameModel.canStartNewGame(gameContext);
                }
            };
        }
        return actionStartPlaying;
    }
}

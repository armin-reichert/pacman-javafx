/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;

import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.INTRO;
import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.SETTING_OPTIONS_FOR_START;

public interface ArcadeMachineActions {

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            final CoinMechanism coinMechanism = ui.gameContext().coinMechanism();
            final boolean acceptsCoin = coinMechanism.numCoins() < coinMechanism.maxCoins();
            if (acceptsCoin) {
                coinMechanism.insertCoin();
                ui.soundManager().setEnabled(true);
                ui.voicePlayer().stopVoice();
                final Game game = ui.gameContext().currentGame();
                if (game.control().state() != SETTING_OPTIONS_FOR_START) {
                    game.control().stateMachine().enterState(SETTING_OPTIONS_FOR_START);
                }
                game.publishGameEvent(new CreditAddedEvent(1));
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            if (game.isPlaying()) {
                return false;
            }
            final boolean noCredit = ui.gameContext().coinMechanism().isEmpty();
            final boolean demoLevel = game.optGameLevel().isPresent() && game.level().isDemoLevel();
            final StateMachine.State<Game> gameState = game.control().state();
            return gameState == SETTING_OPTIONS_FOR_START || gameState == INTRO || demoLevel || noCredit;
        }
    };

    GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.voicePlayer().stopVoice();
            ui.gameContext().currentGame().control().stateMachine().enterState(GameState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            boolean hasCredit = !ui.gameContext().coinMechanism().isEmpty();
            final StateMachine.State<Game> gameState = game.control().state();
            return hasCredit
                && (gameState == INTRO || gameState == SETTING_OPTIONS_FOR_START)
                && game.canStartNewGame();
        }
    };
}
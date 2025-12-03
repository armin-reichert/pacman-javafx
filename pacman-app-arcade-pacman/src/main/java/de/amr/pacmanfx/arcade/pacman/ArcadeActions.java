/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.INTRO;

public final class ArcadeActions {
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public static final GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (ui.context().coinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.soundManager().setEnabled(true);
                ui.context().coinMechanism().insertCoin();
                game.publishGameEvent(GameEvent.Type.CREDIT_ADDED);
            }
            game.control().enterState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.isPlaying()) {
                return false;
            }
            return game.control().state() == GameState.SETTING_OPTIONS_FOR_START
                || game.control().state() == GameState.INTRO
                || game.optGameLevel().isPresent() && game.level().isDemoLevel()
                || ui.context().coinMechanism().noCoin();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.context().currentGame().control().enterState(GameState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return ui.context().coinMechanism().numCoins() > 0
                && (game.control().state() == INTRO || game.control().state() == GameState.SETTING_OPTIONS_FOR_START)
                && game.canStartNewGame();
        }
    };
}
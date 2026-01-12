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
import de.amr.pacmanfx.ui.sound.SoundManager;

import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.INTRO;
import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.SETTING_OPTIONS_FOR_START;

public interface ArcadeActions {

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            final CoinMechanism coinMechanism = ui.context().coinMechanism();
            if (coinMechanism.numCoins() < CoinMechanism.MAX_COINS) {
                final SoundManager soundManager = ui.currentConfig().soundManager();
                coinMechanism.insertCoin();
                soundManager.setEnabled(true);
                ui.voicePlayer().stop();
                if (game.control().state() != SETTING_OPTIONS_FOR_START) {
                    game.control().enterState(SETTING_OPTIONS_FOR_START);
                }
                game.publishGameEvent(GameEvent.Type.CREDIT_ADDED);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.isPlaying()) {
                return false;
            }
            return game.control().state() == SETTING_OPTIONS_FOR_START
                || game.control().state() == INTRO
                || game.optGameLevel().isPresent() && game.level().isDemoLevel()
                || ui.context().coinMechanism().isEmpty();
        }
    };

    GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.voicePlayer().stop();
            ui.context().currentGame().control().enterState(GameState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return ui.context().coinMechanism().numCoins() > 0
                && (game.control().state() == INTRO || game.control().state() == SETTING_OPTIONS_FOR_START)
                && game.canStartNewGame();
        }
    };
}
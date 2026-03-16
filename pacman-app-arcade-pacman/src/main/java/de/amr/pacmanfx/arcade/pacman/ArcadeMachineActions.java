/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadeGameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;

import static de.amr.pacmanfx.arcade.pacman.model.ArcadeGameState.INTRO;
import static de.amr.pacmanfx.arcade.pacman.model.ArcadeGameState.SETTING_OPTIONS_FOR_START;

public interface ArcadeMachineActions {

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            final CoinMechanism slot = ui.gameContext().coinMechanism();
            final Game game = ui.gameContext().game();
            ui.voicePlayer().stopVoice();
            ui.soundManager().setEnabled(true);
            slot.insertCoin();
            game.control().enterState(SETTING_OPTIONS_FOR_START);
            game.publishGameEvent(new CreditAddedEvent(1));
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final CoinMechanism slot = ui.gameContext().coinMechanism();
            final State<Game> state = ui.gameContext().game().control().state();
            return slot.isEmpty() || (state == SETTING_OPTIONS_FOR_START && !slot.isFull());
        }
    };

    GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.voicePlayer().stopVoice();
            ui.gameContext().game().control().enterState(ArcadeGameState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final CoinMechanism slot = ui.gameContext().coinMechanism();
            final Game game = ui.gameContext().game();
            final State<Game> state = ui.gameContext().game().control().state();
            return !slot.isEmpty()
                && (state == INTRO || state == SETTING_OPTIONS_FOR_START)
                && game.canStartNewGame();
        }
    };
}
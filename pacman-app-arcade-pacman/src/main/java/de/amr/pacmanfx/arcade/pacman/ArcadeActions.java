/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState.INTRO;

public final class ArcadeActions {
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public static final GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            if (ui.context().coinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.soundManager().setEnabled(true);
                ui.context().coinMechanism().insertCoin();
                ui.context().currentGame().publishGameEvent(GameEvent.Type.CREDIT_ADDED);
            }
            ui.context().currentGame().changeState(Arcade_GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.context().currentGame().isPlaying()) {
                return false;
            }
            return ui.context().currentGameState() == Arcade_GameState.SETTING_OPTIONS_FOR_START
                || ui.context().currentGameState() == INTRO
                || ui.context().optGameLevel().isPresent() && ui.context().optGameLevel().get().isDemoLevel()
                || ui.context().coinMechanism().noCoin();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.context().currentGame().changeState(Arcade_GameState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            boolean isArcadeGame = StandardGameVariant.isArcadeGameName(ui.context().gameVariantName());
            return isArcadeGame
                && !ui.context().coinMechanism().noCoin()
                && (ui.context().currentGameState() == INTRO || ui.context().currentGameState() == Arcade_GameState.SETTING_OPTIONS_FOR_START)
                && ui.context().currentGame().canStartNewGame();
        }
    };

}
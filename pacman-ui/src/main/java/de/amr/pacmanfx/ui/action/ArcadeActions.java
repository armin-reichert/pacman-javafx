/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.GamePlayState;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;

import java.util.Set;

import static de.amr.pacmanfx.model.GamePlayState.INTRO;

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
                ui.context().eventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.context().currentGame().stateMachine().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.context().currentGame().isPlaying()) {
                return false;
            }
            return ui.context().currentGameState() == GamePlayState.SETTING_OPTIONS_FOR_START
                || ui.context().currentGameState() == INTRO
                || ui.context().optGameLevel().isPresent() && ui.context().optGameLevel().get().isDemoLevel()
                || ui.context().coinMechanism().isEmpty();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.context().currentGame().stateMachine().changeGameState(GamePlayState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            Set<String> arcadeGames = Set.of(
                StandardGameVariant.PACMAN.name(),
                StandardGameVariant.MS_PACMAN.name(),
                StandardGameVariant.PACMAN_XXL.name(),
                StandardGameVariant.MS_PACMAN_XXL.name()
            );
            return arcadeGames.contains(ui.context().gameBox().gameVariant())
                && !ui.context().coinMechanism().isEmpty()
                && (ui.context().currentGameState() == GamePlayState.INTRO || ui.context().currentGameState() == GamePlayState.SETTING_OPTIONS_FOR_START)
                && ui.context().currentGame().canStartNewGame();
        }
    };

}
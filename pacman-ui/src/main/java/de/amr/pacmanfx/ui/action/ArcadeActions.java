/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.GameState;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;

import java.util.Set;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static de.amr.pacmanfx.model.GameState.INTRO;

public final class ArcadeActions {
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public static final GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            if (THE_GAME_BOX.numCoins() < CoinMechanism.MAX_COINS) {
                ui.soundManager().setEnabled(true);
                THE_GAME_BOX.insertCoin();
                ui.context().eventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.context().currentGame().stateMachine().changeState(ui.context(), GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.context().currentGame().isPlaying()) {
                return false;
            }
            return ui.context().currentGameState() == GameState.SETTING_OPTIONS_FOR_START
                || ui.context().currentGameState() == INTRO
                || ui.context().optGameLevel().isPresent() && ui.context().optGameLevel().get().isDemoLevel()
                || THE_GAME_BOX.containsNoCoin();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.context().currentGame().stateMachine().changeState(ui.context(), GameState.STARTING_GAME_OR_LEVEL);
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
                && !THE_GAME_BOX.containsNoCoin()
                && (ui.context().currentGameState() == GameState.INTRO || ui.context().currentGameState() == GameState.SETTING_OPTIONS_FOR_START)
                && ui.context().currentGame().canStartNewGame();
        }
    };

}
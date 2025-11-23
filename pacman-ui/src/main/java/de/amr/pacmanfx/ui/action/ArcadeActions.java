/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;

import java.util.Set;

import static de.amr.pacmanfx.controller.PacManGamesState.INTRO;

public final class ArcadeActions {
    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    public static final GameAction ACTION_INSERT_COIN = new GameAction("INSERT_COIN") {
        @Override
        public void execute(GameUI ui) {
            if (ui.gameContext().coinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                ui.soundManager().setEnabled(true);
                ui.gameContext().coinMechanism().insertCoin();
                ui.gameContext().eventManager().publishEvent(GameEventType.CREDIT_ADDED);
            }
            ui.gameContext().playStateMachine().changeGameState(PacManGamesState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            if (ui.gameContext().game().isPlaying()) {
                return false;
            }
            return ui.gameContext().gameState() == PacManGamesState.SETTING_OPTIONS_FOR_START
                || ui.gameContext().gameState() == INTRO
                || ui.gameContext().optGameLevel().isPresent() && ui.gameContext().optGameLevel().get().isDemoLevel()
                || ui.gameContext().coinMechanism().isEmpty();
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction("START_GAME") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopVoice();
            ui.gameContext().playStateMachine().changeGameState(PacManGamesState.STARTING_GAME_OR_LEVEL);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            Set<String> arcadeGames = Set.of(
                StandardGameVariant.PACMAN.name(),
                StandardGameVariant.MS_PACMAN.name(),
                StandardGameVariant.PACMAN_XXL.name(),
                StandardGameVariant.MS_PACMAN_XXL.name()
            );
            return arcadeGames.contains(ui.gameContext().gameController().gameVariant())
                && !ui.gameContext().coinMechanism().isEmpty()
                && (ui.gameContext().gameState() == PacManGamesState.INTRO || ui.gameContext().gameState() == PacManGamesState.SETTING_OPTIONS_FOR_START)
                && ui.gameContext().game().canStartNewGame();
        }
    };

}
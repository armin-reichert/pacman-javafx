/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import org.tinylog.Logger;

public enum TengenGameActions implements GameAction {

    SELECT_NEXT_JOYPAD {
        @Override
        public void execute(GameContext context) {
            context.disableJoypad();
            context.nextJoypad();
            context.enableJoypad();
            Logger.info("Current joypad: {} ", context.joypad());
        }
    },

    TOGGLE_PAC_BOOSTER {
        @Override
        public void execute(GameContext context) {
            TengenMsPacManGame tengenGame = (TengenMsPacManGame) context.game();
            if (tengenGame.boosterMode() == BoosterMode.ACTIVATED_USING_KEY) {
                tengenGame.setBoosterActive(!tengenGame.isBoosterActive()); // toggle state
            }
        }
    },

    QUIT_DEMO_LEVEL {
        @Override
        public void execute(GameContext context) {
            if (context.game().isDemoLevel()) {
                context.gameController().changeState(GameState.WAITING_FOR_START);
            }
        }
    },

    SHOW_OPTIONS {
        @Override
        public void execute(GameContext context) {
            context.game().setPlaying(false);
            context.gameController().changeState(GameState.WAITING_FOR_START);
        }
    },

}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameActionProvider;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import org.tinylog.Logger;

public enum TengenGameActions implements GameAction {

    SELECT_NEXT_JOYPAD {
        @Override
        public void execute(GameContext context) {
            context.disableJoypad();
            context.nextJoypad();
            context.enableJoypad();
            Logger.info("Current joypad: {} ", context.joypadInput());
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

    START_PLAYING {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.gameController().changeState(GameState.STARTING_GAME);
        }
    };

    public static void bindDefaultJoypadActions(GameActionProvider actionProvider, JoypadKeyBinding binding) {
        actionProvider.bind(TengenGameActions.TOGGLE_PAC_BOOSTER, binding.key(NES.Joypad.A), binding.key(NES.Joypad.B));
        actionProvider.bind(GameActions2D.PLAYER_UP,    binding.key(NES.Joypad.UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  binding.key(NES.Joypad.DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  binding.key(NES.Joypad.LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, binding.key(NES.Joypad.RIGHT));
    }
}
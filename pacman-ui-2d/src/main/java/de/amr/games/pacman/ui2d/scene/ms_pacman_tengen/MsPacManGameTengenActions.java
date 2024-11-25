/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManGameTengen;
import de.amr.games.pacman.model.ms_pacman_tengen.PacBooster;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameActionProvider;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import org.tinylog.Logger;

public enum MsPacManGameTengenActions implements GameAction {

    SELECT_NEXT_JOYPAD {
        @Override
        public void execute(GameContext context) {
            context.disableJoypad();
            context.selectNextJoypadBinding();
            context.enableJoypad();
            Logger.info("Selected joypad: {} ", context.joypad());
        }
    },

    TOGGLE_PAC_BOOSTER {
        @Override
        public void execute(GameContext context) {
            MsPacManGameTengen game = (MsPacManGameTengen) context.game();
            if (game.pacBooster() == PacBooster.USE_A_OR_B) {
                game.activatePacBooster(!game.isBoosterActive());
            }
        }
    },

    QUIT_DEMO_LEVEL {
        @Override
        public void execute(GameContext context) {
            if (context.game().isDemoLevel()) {
                context.gameController().changeState(GameState.SETTING_OPTIONS);
            }
        }
    },

    SHOW_OPTIONS {
        @Override
        public void execute(GameContext context) {
            context.game().setPlaying(false);
            context.gameController().changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_PLAYING {
        @Override
        public void execute(GameContext context) {
            context.sound().stopAll();
            context.game().setPlaying(false);
            context.gameController().changeState(GameState.STARTING_GAME);
        }
    };

    public static void setDefaultJoypadBinding(GameActionProvider actionProvider, JoypadKeyBinding binding) {
        actionProvider.bind(MsPacManGameTengenActions.TOGGLE_PAC_BOOSTER,
            binding.key(NES.JoypadButton.BTN_A),
            binding.key(NES.JoypadButton.BTN_B));
        actionProvider.bind(GameActions2D.PLAYER_UP,    binding.key(NES.JoypadButton.BTN_UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  binding.key(NES.JoypadButton.BTN_DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  binding.key(NES.JoypadButton.BTN_LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, binding.key(NES.JoypadButton.BTN_RIGHT));
    }
}
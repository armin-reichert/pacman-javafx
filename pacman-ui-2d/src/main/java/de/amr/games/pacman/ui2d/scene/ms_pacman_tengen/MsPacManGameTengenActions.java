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
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
import org.tinylog.Logger;

public enum MsPacManGameTengenActions implements GameAction {

    SELECT_NEXT_JOYPAD {
        @Override
        public void execute(GameContext context) {
            context.disableJoypad();
            context.nextJoypadKeyBinding();
            context.enableJoypad();
            Logger.info("Current joypad: {} ", context.joypad());
        }
    },

    TOGGLE_PAC_BOOSTER {
        @Override
        public void execute(GameContext context) {
            MsPacManGameTengen tengenGame = (MsPacManGameTengen) context.game();
            if (tengenGame.pacBooster() == PacBooster.USE_A_OR_B) {
                tengenGame.activatePacBooster(!tengenGame.isBoosterActive()); // toggle state
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

    public static void bindDefaultJoypadActions(GameActionProvider actionProvider, JoypadKeyAdapter binding) {
        actionProvider.bind(MsPacManGameTengenActions.TOGGLE_PAC_BOOSTER, binding.keyCombination(NES.Joypad.A), binding.keyCombination(NES.Joypad.B));
        actionProvider.bind(GameActions2D.PLAYER_UP,    binding.keyCombination(NES.Joypad.UP));
        actionProvider.bind(GameActions2D.PLAYER_DOWN,  binding.keyCombination(NES.Joypad.DOWN));
        actionProvider.bind(GameActions2D.PLAYER_LEFT,  binding.keyCombination(NES.Joypad.LEFT));
        actionProvider.bind(GameActions2D.PLAYER_RIGHT, binding.keyCombination(NES.Joypad.RIGHT));
    }
}
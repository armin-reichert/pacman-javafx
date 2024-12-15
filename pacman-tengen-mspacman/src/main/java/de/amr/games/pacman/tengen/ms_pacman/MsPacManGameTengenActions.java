/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManGameTengen;
import de.amr.games.pacman.model.ms_pacman_tengen.PacBooster;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.action.GameAction;
import de.amr.games.pacman.ui.action.GameActionProvider;
import de.amr.games.pacman.ui.action.GameActions2D;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import org.tinylog.Logger;

import static de.amr.games.pacman.tengen.ms_pacman.GlobalProperties.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.GlobalProperties.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.games.pacman.ui.lib.Ufx.toggle;

public enum MsPacManGameTengenActions implements GameAction {

    SELECT_NEXT_JOYPAD {
        @Override
        public void execute(GameContext context) {
            context.disableJoypad();
            context.nextJoypadKeys();
            context.enableJoypad();
            Logger.info("Selected joypad: {} ", context.joypadKeys());
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
    },

    TOGGLE_DISPLAY_MODE {
        @Override
        public void execute(GameContext context) {
            SceneDisplayMode mode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();
            PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT : SceneDisplayMode.SCROLLING);
        }
    },

    TOGGLE_JOYPAD_BINDINGS_DISPLAYED {
        @Override
        public void execute(GameContext context) {
            toggle(PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED);
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
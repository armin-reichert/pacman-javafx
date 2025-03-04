/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.tengen.ms_pacman.scene.SceneDisplayMode;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import org.tinylog.Logger;

import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfig3D.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfig3D.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.games.pacman.uilib.Ufx.toggle;

public enum TengenMsPacMan_GameActions implements GameAction {

    SELECT_NEXT_JOYPAD_KEY_BINDING {
        @Override
        public void execute(GameContext context) {
            context.unregisterJoypadKeyBinding();
            context.selectNextJoypadKeyBinding();
            context.registerJoypadKeyBinding();
            Logger.info("Selected joypad: {} ", context.currentJoypadKeyBinding());
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
            TengenMsPacMan_GameModel game = context.game();
            if (game.pacBooster() == PacBooster.USE_A_OR_B) {
                game.activatePacBooster(!game.isBoosterActive());
            }
        }
    }
}
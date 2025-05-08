/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.uilib.Action;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Ufx.toggle;

public enum TengenMsPacMan_GameAction implements Action {

    QUIT_DEMO_LEVEL {
        @Override
        public void execute() {
            level().ifPresent(level -> THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS));
        }

        @Override
        public boolean isEnabled() {
            return level().isPresent() && level().get().isDemoLevel();
        }
    },

    SHOW_OPTIONS {
        @Override
        public void execute() {
            game().playingProperty().set(false);
            THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_GAME {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_PLAYING {
        @Override
        public void execute() {
            THE_SOUND.stopAll();
            game().playingProperty().set(false);
            THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
        }
    },

    TOGGLE_DISPLAY_MODE {
        @Override
        public void execute() {
            SceneDisplayMode mode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();
            PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT : SceneDisplayMode.SCROLLING);
        }
    },

    TOGGLE_JOYPAD_BINDINGS_DISPLAYED {
        @Override
        public void execute() {
            toggle(PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED);
        }
    },

    TOGGLE_PAC_BOOSTER {
        @Override
        public void execute() {
            var tengenGame = (TengenMsPacMan_GameModel) game();
            if (tengenGame.pacBooster() == PacBooster.USE_A_OR_B) {
                tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
            }
        }
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.uilib.Action;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Ufx.toggle;

public enum TengenMsPacMan_GameAction implements Action {

    QUIT_DEMO_LEVEL {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                if (level.isDemoLevel()) {
                    THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
                }
            });
        }
    },

    SHOW_OPTIONS {
        @Override
        public void execute() {
            THE_GAME_CONTROLLER.game().playingProperty().set(false);
            THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_PLAYING {
        @Override
        public void execute() {
            THE_SOUND.stopAll();
            THE_GAME_CONTROLLER.game().playingProperty().set(false);
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
            TengenMsPacMan_GameModel game = THE_GAME_CONTROLLER.game();
            if (game.pacBooster() == PacBooster.USE_A_OR_B) {
                game.activatePacBooster(!game.isBoosterActive());
            }
        }
    }
}
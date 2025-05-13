/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.uilib.Action;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theSound;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public enum TengenMsPacMan_GameAction implements Action {

    QUIT_DEMO_LEVEL {
        @Override
        public void execute() {
            optGameLevel().ifPresent(level -> theGameController().changeState(GameState.SETTING_OPTIONS));
        }

        @Override
        public boolean isEnabled() {
            return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
        }
    },

    SHOW_OPTIONS {
        @Override
        public void execute() {
            theGame().playingProperty().set(false);
            theGameController().changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_GAME {
        @Override
        public void execute() {
            theGameController().changeState(GameState.SETTING_OPTIONS);
        }
    },

    START_PLAYING {
        @Override
        public void execute() {
            theSound().stopAll();
            theGame().playingProperty().set(false);
            theGameController().changeState(GameState.STARTING_GAME);
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
            var tengenGame = (TengenMsPacMan_GameModel) theGame();
            if (tengenGame.pacBooster() == PacBooster.USE_A_OR_B) {
                tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
            }
        }
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.uilib.GameAction;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface TengenMsPacMan_GameActions {

    GameAction QUIT_DEMO_LEVEL = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled() {
            return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
        }

        @Override
        public String toString() {
            return "QuitDemoLevel";
        }
    };

    GameAction START_GAME = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public String toString() {
            return "StartGame";
        }
    };

    GameAction START_PLAYING = new GameAction() {
        @Override
        public void execute() {
            theSound().stopAll();
            theGame().playingProperty().set(false);
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public String toString() {
            return "StartPlaying";
        }
    };

    GameAction TOGGLE_DISPLAY_MODE = new GameAction() {
        @Override
        public void execute() {
            SceneDisplayMode mode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();
            PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled() {
            return theUIConfig().currentGameSceneIsPlayScene2D();
        }
    };

    GameAction TOGGLE_JOYPAD_BINDINGS_DISPLAYED = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED);
        }
    };

    GameAction TOGGLE_PAC_BOOSTER = new GameAction() {
        @Override
        public void execute() {
            var tengenGame = (TengenMsPacMan_GameModel) theGame();
            if (tengenGame.pacBooster() == PacBooster.USE_A_OR_B) {
                tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
            }
        }
    };
}
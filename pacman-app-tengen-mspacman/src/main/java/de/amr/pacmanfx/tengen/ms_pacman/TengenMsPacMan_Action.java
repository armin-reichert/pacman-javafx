/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.PacManGames_UI;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface TengenMsPacMan_Action {

    GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
        }

        @Override
        public String toString() {
            return "QuitDemoLevel";
        }
    };

    GameAction ACTION_START_GAME = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public String toString() {
            return "StartGame";
        }
    };

    GameAction ACTION_START_PLAYING = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theSound().stopAll();
            theGame().playingProperty().set(false);
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public String toString() {
            return "StartPlaying";
        }
    };

    GameAction ACTION_TOGGLE_DISPLAY_MODE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            SceneDisplayMode mode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();
            PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return ui.currentGameSceneIsPlayScene2D();
        }
    };

    GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED);
        }
    };

    GameAction ACTION_TOGGLE_PAC_BOOSTER = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            var tengenGame = (TengenMsPacMan_GameModel) theGame();
            if (tengenGame.pacBooster() == PacBooster.USE_A_OR_B) {
                tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
            }
        }
    };
}
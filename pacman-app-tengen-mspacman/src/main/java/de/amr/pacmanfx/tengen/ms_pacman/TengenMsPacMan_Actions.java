/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface TengenMsPacMan_Actions {

    GameAction ACTION_ENTER_START_SCREEN = new GameAction("ENTER_START_SCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().control().changeState(GameState.SETTING_OPTIONS_FOR_START);
        }
    };

    GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction("QUIT_DEMO_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().control().changeState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.context().currentGame().optGameLevel().isPresent() && ui.context().currentGame().level().isDemoLevel();
        }
    };

    GameAction ACTION_START_PLAYING = new GameAction("START_PLAYING") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            ui.soundManager().stopAll();
            game.setPlaying(false);
            game.control().changeState(GameState.STARTING_GAME_OR_LEVEL);
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new GameAction("TOGGLE_PLAY_SCENE_DISPLAY_MODE") {
        @Override
        public void execute(GameUI ui) {
            SceneDisplayMode mode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
            PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT
                : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D);
        }
    };

    GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new GameAction("TOGGLE_JOYPAD_BINDINGS_DISPLAYED") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_JOYPAD_BINDINGS_DISPLAYED);
        }
    };

    GameAction ACTION_TOGGLE_PAC_BOOSTER = new GameAction("TOGGLE_PAC_BOOSTER") {
        @Override
        public void execute(GameUI ui) {
            ui.context().currentGame().optGameLevel().ifPresent(gameLevel -> {
                var game = ui.context().<TengenMsPacMan_GameModel>currentGame();
                game.activatePacBooster(gameLevel.pac(), !game.isBoosterActive());
                if (game.isBoosterActive()) {
                    ui.showFlashMessage("Booster!");
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            var gameModel = ui.context().<TengenMsPacMan_GameModel>currentGame();
            return gameModel.pacBooster() == PacBooster.USE_A_OR_B && gameModel.optGameLevel().isPresent();
        }
    };
}

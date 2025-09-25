/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.SceneDisplayMode;
import de.amr.pacmanfx.ui.AbstractGameAction;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_2D;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface TengenMsPacMan_Actions {

    AbstractGameAction ACTION_ENTER_START_SCREEN = new AbstractGameAction("ENTER_START_SCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
        }
    };

    AbstractGameAction ACTION_QUIT_DEMO_LEVEL = new AbstractGameAction("QUIT_DEMO_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent() && ui.gameContext().gameLevel().isDemoLevel();
        }
    };

    AbstractGameAction ACTION_START_PLAYING = new AbstractGameAction("START_PLAYING") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopAll();
            ui.gameContext().game().setPlaying(false);
            ui.gameContext().gameController().changeGameState(GamePlayState.STARTING_GAME_OR_LEVEL);
        }
    };

    AbstractGameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new AbstractGameAction("TOGGLE_PLAY_SCENE_DISPLAY_MODE") {
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

    AbstractGameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new AbstractGameAction("TOGGLE_JOYPAD_BINDINGS_DISPLAYED") {
        @Override
        public void execute(GameUI ui) {
            toggle(PROPERTY_JOYPAD_BINDINGS_DISPLAYED);
        }
    };

    AbstractGameAction ACTION_TOGGLE_PAC_BOOSTER = new AbstractGameAction("TOGGLE_PAC_BOOSTER") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().optGameLevel().ifPresent(gameLevel -> {
                var game = ui.gameContext().<TengenMsPacMan_GameModel>game();
                game.activatePacBooster(gameLevel.pac(), !game.isBoosterActive());
                if (game.isBoosterActive()) {
                    ui.showFlashMessage("Booster!");
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            var gameModel = ui.gameContext().<TengenMsPacMan_GameModel>game();
            return gameModel.pacBooster() == PacBooster.USE_A_OR_B && gameModel.optGameLevel().isPresent();
        }
    };
}

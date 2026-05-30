/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode;
import de.amr.pacmanfx.ui.CommonSceneID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public interface TengenMsPacMan_Actions {

    GameAction ACTION_ENTER_START_SCREEN = new GameAction("enter_start_screen") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            game.flow().enterState(TengenMsPacMan_GameState.PREPARING_GAME_START);
        }
    };

    GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction("quit_demo_level") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            game.flow().enterState(TengenMsPacMan_GameState.PREPARING_GAME_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            return game.isDemoLevelRunning();
        }
    };

    GameAction ACTION_START_PLAYING = new GameAction("start_playing") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            game.flow().enterState(TengenMsPacMan_GameState.STARTING_GAME_OR_LEVEL);
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new GameAction("toggle_play_scene_display_mode") {
        @Override
        public void doAction(GameUI ui) {
            SceneDisplayMode mode = PROPERTY_PLAY_SCENE_DISPLAY_MODE.get();
            PROPERTY_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT
                : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.management().gameSceneManager().currentGameSceneHasID(ui, CommonSceneID.PLAY_SCENE_2D);
        }
    };

    GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new GameAction("toggle_joypad_bindings_displayed") {
        @Override
        public void doAction(GameUI ui) {
            toggleBooleanProperty(PROPERTY_JOYPAD_BINDINGS_DISPLAYED);
        }
    };

    GameAction ACTION_TOGGLE_PAC_BOOSTER = new GameAction("toggle_pac_booster") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            game.optGameLevel().ifPresent(gameLevel -> {
                final var tengenGame = (TengenMsPacMan_GameModel) game;
                tengenGame.activatePacBooster(gameLevel.entities().pac(), !tengenGame.isBoosterActive());
                if (tengenGame.isBoosterActive()) {
                    ui.showFlashMessage("Booster!"); //TODO localize
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final var tengenGame = ui.gameContext().<TengenMsPacMan_GameModel>game();
            return tengenGame.pacBoosterMode() == PacBooster.USE_A_OR_B && tengenGame.optGameLevel().isPresent();
        }
    };
}
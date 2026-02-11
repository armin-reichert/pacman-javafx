/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public interface TengenMsPacMan_Actions {

    GameAction ACTION_ENTER_START_SCREEN = new GameAction("ENTER_START_SCREEN") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            game.enterState(GameState.SETTING_OPTIONS_FOR_START);
        }
    };

    GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction("QUIT_DEMO_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            game.enterState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            return game.optGameLevel().isPresent() && game.level().isDemoLevel();
        }
    };

    GameAction ACTION_START_PLAYING = new GameAction("START_PLAYING") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            game.enterState(GameState.STARTING_GAME_OR_LEVEL);
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
            return ui.currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_2D);
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
            final Game game = ui.gameContext().currentGame();
            game.optGameLevel().ifPresent(gameLevel -> {
                final var tengenGame = (TengenMsPacMan_GameModel) game;
                tengenGame.activatePacBooster(gameLevel.pac(), !tengenGame.isBoosterActive());
                if (tengenGame.isBoosterActive()) {
                    ui.showFlashMessage("Booster!"); //TODO localize
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final var tengenGame = ui.gameContext().<TengenMsPacMan_GameModel>currentGame();
            return tengenGame.pacBooster() == PacBooster.USE_A_OR_B && tengenGame.optGameLevel().isPresent();
        }
    };
}
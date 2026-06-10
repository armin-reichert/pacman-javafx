/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.tengenmspacman.model.PacBooster;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;

import static de.amr.pacmanfx.uilib.Ufx.toggleBooleanProperty;

public final class TengenMsPacMan_Actions {

    private TengenMsPacMan_Actions() {}

    public static final GameAction ACTION_ENTER_START_SCREEN = new GameAction("enter_start_screen") {
        @Override
        public void doAction(Game game) {
            game.currentGameContext().flow().enterState(GameStateID.GAME_PREPARATION);
        }
    };

    public static final GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction("quit_demo_level") {
        @Override
        public void doAction(Game game) {
            game.currentGameContext().flow().enterState(GameStateID.GAME_PREPARATION);
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameModel gameModel = game.currentGameContext().model();
            return gameModel.isDemoLevelRunning();
        }
    };

    public static final GameAction ACTION_START_PLAYING = new GameAction("start_playing") {
        @Override
        public void doAction(Game game) {
            game.currentGameContext().flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    };

    public static final GameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new GameAction("toggle_play_scene_display_mode") {
        @Override
        public void doAction(Game game) {
            final var uiSettings = game.ui().extensions()
                .getExtension(TengenMsPacMan_UIConfig.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);

            final SceneDisplayMode mode = uiSettings.playSceneDisplayMode.get();
            uiSettings.playSceneDisplayMode.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT
                : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(Game game) {
            return game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D);
        }
    };

    public static final GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new GameAction("toggle_joypad_bindings_displayed") {
        @Override
        public void doAction(Game game) {
            final var uiSettings = game.ui().extensions()
                .getExtension(TengenMsPacMan_UIConfig.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);

            toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
        }
    };

    public static final GameAction ACTION_TOGGLE_PAC_BOOSTER = new GameAction("toggle_pac_booster") {
        @Override
        public void doAction(Game game) {
            game.currentGameContext().optCurrentLevel().ifPresent(gameLevel -> {
                final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.currentGameContext().model();
                tengenGame.activatePacBooster(gameLevel.entities().pac(), !tengenGame.isBoosterActive());
                if (tengenGame.isBoosterActive()) {
                    game.shortMessage("Booster!"); //TODO localize
                }
            });
        }

        @Override
        public boolean isEnabled(Game game) {
            final TengenMsPacMan_GameModel tengenGame = (TengenMsPacMan_GameModel) game.currentGameContext().model();
            return tengenGame.pacBoosterMode() == PacBooster.USE_A_OR_B && tengenGame.optGameLevel().isPresent();
        }
    };
}
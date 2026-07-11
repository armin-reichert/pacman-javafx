/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameStateID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import static java.util.Objects.requireNonNull;

public class GameSceneConfig extends AbstractGameSceneConfig {

    public GameSceneConfig(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    @Override
    protected GameScene createGameScene(Identifier sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> new TengenMsPacMan_BootScene(game);
            case CommonGameSceneID.INTRO_SCENE -> new TengenMsPacMan_IntroScene(game);
            case CommonGameSceneID.START_SCENE -> new TengenMsPacMan_OptionsScene(game);
            case TengenSceneID.HALL_OF_FAME -> new TengenMsPacMan_CreditsScene(game);
            case CommonGameSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D(game);
            case CommonGameSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D(game);
            case CommonGameSceneID.CUTSCENE_1 -> new TengenMsPacMan_CutScene1(game);
            case CommonGameSceneID.CUTSCENE_2 -> new TengenMsPacMan_CutScene2(game);
            case CommonGameSceneID.CUTSCENE_3 -> new TengenMsPacMan_CutScene3(game);
            case CommonGameSceneID.CUTSCENE_4 -> new TengenMsPacMan_CutScene4(game);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Identifier determineSceneID(GameContext context) {
        final State<GameContext> state = context.state();

        if (state instanceof CutScenesTestState testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }

        if (GameStateID.BOOT.identifies(state)) {
            return CommonGameSceneID.BOOT_SCENE;
        }
        if (GameStateID.GAME_LEVEL_INTERMISSION.identifies(state)) {
            return resolveCutSceneID(context);
        }
        if (GameStateID.GAME_INTRO.identifies(state)) {
            return CommonGameSceneID.INTRO_SCENE;
        }
        if (GameStateID.GAME_PREPARATION.identifies(state)) {
            return CommonGameSceneID.START_SCENE;
        }
        if (TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME.identifies(state)) {
            return TengenSceneID.HALL_OF_FAME;
        }
        return game.ui().viewModel().common3D.view3DEnabledProperty.get() ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

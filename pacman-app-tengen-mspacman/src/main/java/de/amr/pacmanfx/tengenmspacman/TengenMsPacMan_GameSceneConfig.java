/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameStateID;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public TengenMsPacMan_GameSceneConfig(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new TengenMsPacMan_BootScene(game);
            case CommonSceneID.INTRO_SCENE -> new TengenMsPacMan_IntroScene(game);
            case CommonSceneID.START_SCENE -> new TengenMsPacMan_OptionsScene(game);
            case TengenSceneID.HALL_OF_FAME -> new TengenMsPacMan_CreditsScene(game);
            case CommonSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D(game);
            case CommonSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D(game);
            case CommonSceneID.CUTSCENE_1 -> new TengenMsPacMan_CutScene1(game);
            case CommonSceneID.CUTSCENE_2 -> new TengenMsPacMan_CutScene2(game);
            case CommonSceneID.CUTSCENE_3 -> new TengenMsPacMan_CutScene3(game);
            case CommonSceneID.CUTSCENE_4 -> new TengenMsPacMan_CutScene4(game);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(GameContext gameContext) {
        final State<GameContext> state = gameContext.state();

        if (state instanceof CutScenesTestState testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }

        if (GameStateID.BOOT.identifies(state)) {
            return CommonSceneID.BOOT_SCENE;
        }
        if (GameStateID.GAME_LEVEL_INTERMISSION.identifies(state)) {
            return resolveCutSceneID(gameContext);
        }
        if (GameStateID.GAME_INTRO.identifies(state)) {
            return CommonSceneID.INTRO_SCENE;
        }
        if (GameStateID.GAME_PREPARATION.identifies(state)) {
            return CommonSceneID.START_SCENE;
        }
        if (TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME.identifies(state)) {
            return TengenSceneID.HALL_OF_FAME;
        }
        return game.ui().globals3D().property3DEnabled.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
    }
}

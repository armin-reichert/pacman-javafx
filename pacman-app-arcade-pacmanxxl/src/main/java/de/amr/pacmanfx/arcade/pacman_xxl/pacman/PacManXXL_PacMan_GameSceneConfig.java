/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;

import static java.util.Objects.requireNonNull;

class PacManXXL_PacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public PacManXXL_PacMan_GameSceneConfig(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    protected AbstractGameScene createGameScene(Identifier sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> new Arcade_BootScene2D(appContext);
            case CommonGameSceneID.INTRO_SCENE -> new ArcadePacMan_IntroScene(appContext);
            case CommonGameSceneID.START_SCENE -> new ArcadePacMan_StartScene(appContext);
            case CommonGameSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(appContext);
            case CommonGameSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(appContext);
            case CommonGameSceneID.CUTSCENE_1 -> new ArcadePacMan_CutScene1(appContext);
            case CommonGameSceneID.CUTSCENE_2 -> new ArcadePacMan_CutScene2(appContext);
            case CommonGameSceneID.CUTSCENE_3 -> new ArcadePacMan_CutScene3(appContext);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Identifier determineSceneID(GameContext gameContext) {
        final State<GameContext> state = gameContext.state();

        if (state instanceof CutScenesTestState testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }

        if (GameStateID.BOOT.identifies(state)) {
            return CommonGameSceneID.BOOT_SCENE;
        }
        if (GameStateID.GAME_LEVEL_INTERMISSION.identifies(state)) {
            return resolveCutSceneID(gameContext);
        }
        if (GameStateID.GAME_INTRO.identifies(state)) {
            return CommonGameSceneID.INTRO_SCENE;
        }
        if (GameStateID.GAME_PREPARATION.identifies(state)) {
            return CommonGameSceneID.START_SCENE;
        }
        return appContext.ui().viewModel().common3D.view3DEnabledProperty.get() ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

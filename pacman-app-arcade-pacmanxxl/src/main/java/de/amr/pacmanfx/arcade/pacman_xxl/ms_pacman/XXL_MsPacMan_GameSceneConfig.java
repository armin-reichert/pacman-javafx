/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.Named;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.vm.GameViewModel;

import static java.util.Objects.requireNonNull;

public class XXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public XXL_MsPacMan_GameSceneConfig() {}

    @Override
    protected AbstractGameScene createGameScene(GameAppContext appContext, Named sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> new Arcade_BootScene2D(appContext);
            case CommonGameSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene(appContext);
            case CommonGameSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene(appContext);
            case CommonGameSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(appContext);
            case CommonGameSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(appContext);
            case CommonGameSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1(appContext);
            case CommonGameSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2(appContext);
            case CommonGameSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3(appContext);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Named determineSceneID(GameViewModel viewModel, GameContext game) {
        final GameState state = game.state();

        if (state instanceof CutScenesTestState testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }

        if (CommonGameStateID.BOOT.hasSameNameAs(state)) {
            return CommonGameSceneID.BOOT_SCENE;
        }
        if (CommonGameStateID.GAME_LEVEL_INTERMISSION.hasSameNameAs(state)) {
            return resolveCutSceneID(game);
        }
        if (CommonGameStateID.GAME_INTRO.hasSameNameAs(state)) {
            return CommonGameSceneID.INTRO_SCENE;
        }
        if (CommonGameStateID.GAME_PREPARATION.hasSameNameAs(state)) {
            return CommonGameSceneID.START_SCENE;
        }
        return viewModel.common3D.view3DEnabledProperty.get() ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

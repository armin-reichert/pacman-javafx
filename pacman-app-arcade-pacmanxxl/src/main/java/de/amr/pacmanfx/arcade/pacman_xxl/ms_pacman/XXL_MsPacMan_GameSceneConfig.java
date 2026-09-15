/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.Named;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.cutscenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.cutscenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.cutscenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.introscene.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.startscene.ArcadeMsPacMan_StartScene;
import de.amr.pacmanfx.arcade.pacman.scenes.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene3D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import java.util.function.Function;

public class XXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public XXL_MsPacMan_GameSceneConfig() {}

    @Override
    protected Function<GameAppContext, GameScene> getGameSceneFactory(Named sceneID) {
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> Arcade_BootScene::new;
            case CommonGameSceneID.INTRO_SCENE -> ArcadeMsPacMan_IntroScene::new;
            case CommonGameSceneID.START_SCENE -> ArcadeMsPacMan_StartScene::new;
            case CommonGameSceneID.PLAY_SCENE_2D -> Arcade_PlayScene2D::new;
            case CommonGameSceneID.PLAY_SCENE_3D -> Arcade_PlayScene3D::new;
            case CommonGameSceneID.CUTSCENE_1 -> ArcadeMsPacMan_CutScene1::new;
            case CommonGameSceneID.CUTSCENE_2 -> ArcadeMsPacMan_CutScene2::new;
            case CommonGameSceneID.CUTSCENE_3 -> ArcadeMsPacMan_CutScene3::new;
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Named computeGameSceneID(GameContext game, boolean select3D) {
        final AbstractGameState state = game.state();

        if (state instanceof Test_CutScenesTestState testState) {
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
        return select3D ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

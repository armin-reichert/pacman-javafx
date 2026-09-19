/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.Named;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene.ArcadeMsPacMan_StartScene;
import de.amr.pacmanfx.arcade.pacman.gamescene.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.gamescene.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.gamescene.playscene.Arcade_PlayScene3D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import java.util.Map;
import java.util.function.Supplier;

public class ArcadeMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public ArcadeMsPacMan_GameSceneConfig() {}

    private static final Map<CommonGameSceneID, Supplier<GameScene>> FACTORY_MAP = Map.of(
        CommonGameSceneID.BOOT_SCENE   , Arcade_BootScene::new,
        CommonGameSceneID.INTRO_SCENE  , ArcadeMsPacMan_IntroScene::new,
        CommonGameSceneID.START_SCENE  , ArcadeMsPacMan_StartScene::new,
        CommonGameSceneID.PLAY_SCENE_2D, Arcade_PlayScene2D::new,
        CommonGameSceneID.PLAY_SCENE_3D, Arcade_PlayScene3D::new,
        CommonGameSceneID.CUTSCENE_1   , ArcadeMsPacMan_CutScene1::new,
        CommonGameSceneID.CUTSCENE_2   , ArcadeMsPacMan_CutScene2::new,
        CommonGameSceneID.CUTSCENE_3   , ArcadeMsPacMan_CutScene3::new
    );

    @Override
    protected Supplier<GameScene> getGameSceneFactory(Named sceneID) {
        return FACTORY_MAP.get((CommonGameSceneID) sceneID);
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

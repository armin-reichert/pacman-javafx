/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.Named;
import de.amr.pacmanfx.arcade.pacman.scenes.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.arcade.pacman.scenes.introscene.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene3D;
import de.amr.pacmanfx.arcade.pacman.scenes.startscene.ArcadePacMan_StartScene;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ArcadePacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private static final Map<CommonGameSceneID, Function<GameApp, GameScene>> FACTORY_MAP = new EnumMap<>(Map.of(
        CommonGameSceneID.BOOT_SCENE   , Arcade_BootScene::new,
        CommonGameSceneID.INTRO_SCENE  , ArcadePacMan_IntroScene::new,
        CommonGameSceneID.START_SCENE  , ArcadePacMan_StartScene::new,
        CommonGameSceneID.PLAY_SCENE_2D, Arcade_PlayScene2D::new,
        CommonGameSceneID.PLAY_SCENE_3D, Arcade_PlayScene3D::new,
        CommonGameSceneID.CUTSCENE_1   , ArcadePacMan_CutScene1::new,
        CommonGameSceneID.CUTSCENE_2   , ArcadePacMan_CutScene2::new,
        CommonGameSceneID.CUTSCENE_3   , ArcadePacMan_CutScene3::new
    ));

    public ArcadePacMan_GameSceneConfig() {}

    @Override
    protected Function<GameApp, GameScene> getGameSceneFactory(Named sceneID) {
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

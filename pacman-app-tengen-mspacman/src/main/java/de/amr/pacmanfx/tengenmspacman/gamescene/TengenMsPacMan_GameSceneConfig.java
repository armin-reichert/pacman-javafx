/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.gamescene.bootscene.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.creditsscene.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.playscene.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.tengenmspacman.gamescene.playscene.TengenMsPacMan_PlayScene3D;
import de.amr.pacmanfx.tengenmspacman.gamestate.TengenMsPacMan_GameStateID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public TengenMsPacMan_GameSceneConfig() {}

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    private static final Map<Named, Function<GameAppContext, GameScene>> FACTORY_MAP = Map.of(
        CommonGameSceneID.BOOT_SCENE    , TengenMsPacMan_BootScene::new,
        CommonGameSceneID.INTRO_SCENE   , TengenMsPacMan_IntroScene::new,
        CommonGameSceneID.START_SCENE   , TengenMsPacMan_OptionsScene::new,
        TengenSceneID.HALL_OF_FAME      , TengenMsPacMan_CreditsScene::new,
        CommonGameSceneID.PLAY_SCENE_2D , TengenMsPacMan_PlayScene2D::new,
        CommonGameSceneID.PLAY_SCENE_3D , TengenMsPacMan_PlayScene3D::new,
        CommonGameSceneID.CUTSCENE_1    , TengenMsPacMan_CutScene1::new,
        CommonGameSceneID.CUTSCENE_2    , TengenMsPacMan_CutScene2::new,
        CommonGameSceneID.CUTSCENE_3    , TengenMsPacMan_CutScene3::new,
        CommonGameSceneID.CUTSCENE_4    , TengenMsPacMan_CutScene4::new
    );

    @Override
    protected Function<GameAppContext, GameScene> getGameSceneFactory(Named sceneID) {
        return FACTORY_MAP.get(sceneID);
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
        if (TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME.hasSameNameAs(state)) {
            return TengenSceneID.HALL_OF_FAME;
        }
        return select3D ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

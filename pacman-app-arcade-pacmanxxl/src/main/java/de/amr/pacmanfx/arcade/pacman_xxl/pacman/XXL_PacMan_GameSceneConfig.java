/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.arcade.pacman.scenes.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.arcade.pacman.scenes.introscene.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene3D;
import de.amr.pacmanfx.arcade.pacman.scenes.startscene.ArcadePacMan_StartScene;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import static java.util.Objects.requireNonNull;

class XXL_PacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public XXL_PacMan_GameSceneConfig() {}

    @Override
    protected GameScene createGameScene(GameAppContext app, Named sceneID) {
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> new Arcade_BootScene(app);
            case CommonGameSceneID.INTRO_SCENE -> new ArcadePacMan_IntroScene(app);
            case CommonGameSceneID.START_SCENE -> new ArcadePacMan_StartScene(app);
            case CommonGameSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(app);
            case CommonGameSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(app);
            case CommonGameSceneID.CUTSCENE_1 -> new ArcadePacMan_CutScene1(app);
            case CommonGameSceneID.CUTSCENE_2 -> new ArcadePacMan_CutScene2(app);
            case CommonGameSceneID.CUTSCENE_3 -> new ArcadePacMan_CutScene3(app);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Named determineSceneID(GameContext game, boolean select3D) {
        final State<GameContext> state = game.state();

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

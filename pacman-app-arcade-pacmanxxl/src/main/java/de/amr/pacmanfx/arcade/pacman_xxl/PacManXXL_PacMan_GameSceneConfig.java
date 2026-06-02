/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static de.amr.pacmanfx.ui.AppConstants.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

class PacManXXL_PacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private final AppContext context;

    public PacManXXL_PacMan_GameSceneConfig(AppContext context) {
        this.context = context;
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(context);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D(context);
            case CommonSceneID.INTRO_SCENE -> new ArcadePacMan_IntroScene(context);
            case CommonSceneID.START_SCENE -> new ArcadePacMan_StartScene(context);
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(context);
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(context);
            case CommonSceneID.CUTSCENE_1 -> new ArcadePacMan_CutScene1(context);
            case CommonSceneID.CUTSCENE_2 -> new ArcadePacMan_CutScene2(context);
            case CommonSceneID.CUTSCENE_3 -> new ArcadePacMan_CutScene3(context);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(GameModel game) {
        final State<GameModel> state = game.flow().state();
        if (state.matchesByName(Arcade_GameState.BOOT.name())) {
            return CommonSceneID.BOOT_SCENE;
        }
        if (state.matchesByName(Arcade_GameState.INTERMISSION.name())) {
            return resolveCutSceneID(game);
        }
        if (state.matchesByName(Arcade_GameState.INTRO.name())) {
            return CommonSceneID.INTRO_SCENE;
        }
        if (state.matchesByName(Arcade_GameState.PREPARING_GAME_START.name())) {
            return CommonSceneID.START_SCENE;
        }
        if (state instanceof CutScenesTestState<?> testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }
        return PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
    }
}

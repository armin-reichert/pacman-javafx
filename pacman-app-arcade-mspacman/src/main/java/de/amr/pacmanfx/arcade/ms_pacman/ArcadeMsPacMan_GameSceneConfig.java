/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static de.amr.pacmanfx.ui.AppConstants.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

class ArcadeMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private final AppContext context;

    public ArcadeMsPacMan_GameSceneConfig(AppContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D(context);
            case CommonSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene(context);
            case CommonSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene(context);
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(context);
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(context);
            case CommonSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1(context);
            case CommonSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2(context);
            case CommonSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3(context);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(GameModel game) {
        final State<GameModel> state = game.flow().state();
        if (state.name().equals(Arcade_GameState.BOOT.name())) {
            return CommonSceneID.BOOT_SCENE;
        }
        if (state.name().equals(Arcade_GameState.GAME_LEVEL_INTERMISSION.name())) {
            return resolveCutSceneID(game);
        }
        if (state.name().equals(Arcade_GameState.GAME_INTRO.name())) {
            return CommonSceneID.INTRO_SCENE;
        }
        if (state.name().equals(Arcade_GameState.GAME_PREPARATION.name())) {
            return CommonSceneID.START_SCENE;
        }
        if (state instanceof CutScenesTestState<?> testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }
        return PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
    }
}

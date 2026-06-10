/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static de.amr.pacmanfx.ui.d3.Constants3D.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

class PacManXXL_PacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private final Game game;

    public PacManXXL_PacMan_GameSceneConfig(Game game) {
        this.game = game;
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(game);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D(game);
            case CommonSceneID.INTRO_SCENE -> new ArcadePacMan_IntroScene(game);
            case CommonSceneID.START_SCENE -> new ArcadePacMan_StartScene(game);
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(game);
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(game);
            case CommonSceneID.CUTSCENE_1 -> new ArcadePacMan_CutScene1(game);
            case CommonSceneID.CUTSCENE_2 -> new ArcadePacMan_CutScene2(game);
            case CommonSceneID.CUTSCENE_3 -> new ArcadePacMan_CutScene3(game);
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
        return PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
    }
}

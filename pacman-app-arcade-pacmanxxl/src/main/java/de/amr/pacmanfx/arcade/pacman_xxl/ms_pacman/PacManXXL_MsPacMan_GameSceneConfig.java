/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public PacManXXL_MsPacMan_GameSceneConfig(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(game);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D(game);
            case CommonSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene(game);
            case CommonSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene(game);
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(game);
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(game);
            case CommonSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1(game);
            case CommonSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2(game);
            case CommonSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3(game);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(GameContext gameContext) {
        final GameState state = gameContext.state();

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
        return game.ui().settings3D().d3EnabledProperty.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
    }
}

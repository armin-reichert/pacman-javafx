/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;

import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public PacManXXL_MsPacMan_GameSceneConfig(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    protected AbstractGameScene createGameScene(Identifier sceneID) {
        requireNonNull(game);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonGameSceneID.BOOT_SCENE -> new Arcade_BootScene2D(game);
            case CommonGameSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene(game);
            case CommonGameSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene(game);
            case CommonGameSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(game);
            case CommonGameSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(game);
            case CommonGameSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1(game);
            case CommonGameSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2(game);
            case CommonGameSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3(game);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected Identifier determineSceneID(GameContext gameContext) {
        final GameState state = gameContext.state();

        if (state instanceof CutScenesTestState testState) {
            return AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
        }

        if (GameStateID.BOOT.identifies(state)) {
            return CommonGameSceneID.BOOT_SCENE;
        }
        if (GameStateID.GAME_LEVEL_INTERMISSION.identifies(state)) {
            return resolveCutSceneID(gameContext);
        }
        if (GameStateID.GAME_INTRO.identifies(state)) {
            return CommonGameSceneID.INTRO_SCENE;
        }
        if (GameStateID.GAME_PREPARATION.identifies(state)) {
            return CommonGameSceneID.START_SCENE;
        }
        return game.ui().viewModel().common3D.view3DEnabledProperty.get() ? CommonGameSceneID.PLAY_SCENE_3D : CommonGameSceneID.PLAY_SCENE_2D;
    }
}

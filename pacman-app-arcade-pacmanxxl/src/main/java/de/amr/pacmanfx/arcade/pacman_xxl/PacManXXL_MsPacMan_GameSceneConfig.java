/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.AppContext;

import static de.amr.pacmanfx.ui.GameUI_Constants.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private final AppContext context;

    public PacManXXL_MsPacMan_GameSceneConfig(AppContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(context);
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
    protected SceneID determineSceneID(Game game) {
        return switch (game.flow().state()) {
            case Arcade_GameState.BOOT -> CommonSceneID.BOOT_SCENE;
            case Arcade_GameState.PREPARING_GAME_START -> CommonSceneID.START_SCENE;
            case Arcade_GameState.INTRO -> CommonSceneID.INTRO_SCENE;
            case Arcade_GameState.INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState<?> testState -> AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}

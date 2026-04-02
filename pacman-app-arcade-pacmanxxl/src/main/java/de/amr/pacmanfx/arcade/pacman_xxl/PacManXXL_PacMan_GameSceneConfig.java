/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

class PacManXXL_PacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public PacManXXL_PacMan_GameSceneConfig() {}

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D();
            case CommonSceneID.INTRO_SCENE -> new ArcadePacMan_IntroScene();
            case CommonSceneID.START_SCENE -> new ArcadePacMan_StartScene();
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D();
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D();
            case CommonSceneID.CUTSCENE_1 -> new ArcadePacMan_CutScene1();
            case CommonSceneID.CUTSCENE_2 -> new ArcadePacMan_CutScene2();
            case CommonSceneID.CUTSCENE_3 -> new ArcadePacMan_CutScene3();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(Game game) {
        return switch (game.control().state()) {
            case Arcade_GameState.BOOT -> CommonSceneID.BOOT_SCENE;
            case Arcade_GameState.SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case Arcade_GameState.INTRO -> CommonSceneID.INTRO_SCENE;
            case Arcade_GameState.INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}

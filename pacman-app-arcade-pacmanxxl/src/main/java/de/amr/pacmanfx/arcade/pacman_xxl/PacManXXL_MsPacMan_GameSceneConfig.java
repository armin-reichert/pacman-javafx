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
import de.amr.pacmanfx.ui.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public PacManXXL_MsPacMan_GameSceneConfig() {}

    @Override
    protected GameScene createGameScene(GameUI ui, SceneID sceneID) {
        requireNonNull(ui);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D(ui);
            case CommonSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene(ui);
            case CommonSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene(ui);
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D(ui);
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(ui);
            case CommonSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1(ui);
            case CommonSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2(ui);
            case CommonSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3(ui);
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
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}

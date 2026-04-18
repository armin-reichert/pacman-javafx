/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;

import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState.*;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    public TengenMsPacMan_GameSceneConfig() {}

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    @Override
    protected GameScene createGameScene(GameUI ui, SceneID sceneID) {
        requireNonNull(ui);
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new TengenMsPacMan_BootScene(ui);
            case CommonSceneID.INTRO_SCENE -> new TengenMsPacMan_IntroScene(ui);
            case CommonSceneID.START_SCENE -> new TengenMsPacMan_OptionsScene(ui);
            case TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME -> new TengenMsPacMan_CreditsScene(ui);
            case CommonSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D(ui);
            case CommonSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D(ui);
            case CommonSceneID.CUTSCENE_1 -> new TengenMsPacMan_CutScene1(ui);
            case CommonSceneID.CUTSCENE_2 -> new TengenMsPacMan_CutScene2(ui);
            case CommonSceneID.CUTSCENE_3 -> new TengenMsPacMan_CutScene3(ui);
            case CommonSceneID.CUTSCENE_4 -> new TengenMsPacMan_CutScene4(ui);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(Game game) {
        return switch (game.flow().state()) {
            case BOOT -> CommonSceneID.BOOT_SCENE;
            case PREPARING_GAME_START -> CommonSceneID.START_SCENE;
            case SHOWING_HALL_OF_FAME -> TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME;
            case INTRO -> CommonSceneID.INTRO_SCENE;
            case INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}

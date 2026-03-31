/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState.*;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameSceneConfig implements GameSceneConfig {

    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    public TengenMsPacMan_GameSceneConfig() {}

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        requireNonNull(game);
        final SceneID sceneID = determineSceneID(game);
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    private GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new TengenMsPacMan_BootScene();
            case CommonSceneID.INTRO_SCENE -> new TengenMsPacMan_IntroScene();
            case CommonSceneID.START_SCENE -> new TengenMsPacMan_OptionsScene();
            case TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME -> new TengenMsPacMan_CreditsScene();
            case CommonSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D();
            case CommonSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D();
            case CommonSceneID.CUTSCENE_1 -> new TengenMsPacMan_CutScene1();
            case CommonSceneID.CUTSCENE_2 -> new TengenMsPacMan_CutScene2();
            case CommonSceneID.CUTSCENE_3 -> new TengenMsPacMan_CutScene3();
            case CommonSceneID.CUTSCENE_4 -> new TengenMsPacMan_CutScene4();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    private SceneID determineSceneID(Game game) {
        return switch (game.control().state()) {
            case BOOT -> CommonSceneID.BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case SHOWING_HALL_OF_FAME -> TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME;
            case INTRO -> CommonSceneID.INTRO_SCENE;
            case INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.model.ArcadeGameState;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.d3.Factory3D;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_GameSceneConfig implements GameSceneConfig {

    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();
    private final Factory3D factory3D;

    public PacManXXL_MsPacMan_GameSceneConfig(Factory3D factory3D) {
        this.factory3D = requireNonNull(factory3D);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    private GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new Arcade_BootScene2D();
            case CommonSceneID.INTRO_SCENE -> new ArcadeMsPacMan_IntroScene();
            case CommonSceneID.START_SCENE -> new ArcadeMsPacMan_StartScene();
            case CommonSceneID.PLAY_SCENE_2D -> new Arcade_PlayScene2D();
            case CommonSceneID.PLAY_SCENE_3D -> new Arcade_PlayScene3D(factory3D);
            case CommonSceneID.CUTSCENE_1 -> new ArcadeMsPacMan_CutScene1();
            case CommonSceneID.CUTSCENE_2 -> new ArcadeMsPacMan_CutScene2();
            case CommonSceneID.CUTSCENE_3 -> new ArcadeMsPacMan_CutScene3();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return true;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        requireNonNull(game);
        final SceneID sceneID = determineSceneID(game);
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    private SceneID determineSceneID(Game game) {
        return switch (game.control().state()) {
            case ArcadeGameState.BOOT -> CommonSceneID.BOOT_SCENE;
            case ArcadeGameState.SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case ArcadeGameState.INTRO -> CommonSceneID.INTRO_SCENE;
            case ArcadeGameState.INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState testState -> GameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}

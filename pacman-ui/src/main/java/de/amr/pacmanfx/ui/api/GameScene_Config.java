/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;

import java.util.stream.Stream;

public interface GameScene_Config {

    String SCENE_ID_BOOT_SCENE_2D    = "BootScene2D";
    String SCENE_ID_INTRO_SCENE_2D   = "IntroScene2D";
    String SCENE_ID_START_SCENE_2D   = "StartScene2D";
    String SCENE_ID_CREDITS_SCENE_2D = "CreditsScene2D";
    String SCENE_ID_PLAY_SCENE_2D    = "PlayScene2D";
    String SCENE_ID_PLAY_SCENE_3D    = "PlayScene3D";

    static String sceneID_CutScene(int number) {
        return "CutScene_%d_2D".formatted(number);
    }

    void createGameScenes();

    Stream<GameScene> gameScenes();

    boolean gameSceneHasID(GameScene gameScene, String sceneID);

    GameScene selectGameScene(GameContext gameContext);
}

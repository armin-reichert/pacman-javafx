/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;

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

    /**
     * @param sceneBefore scene displayed before switching
     * @param sceneAfter scene displayed after switching
     * @return <code>23</code> if 2D -> 3D switch, <code>32</code> if 3D -> 2D switch</code>,
     *  <code>0</code> if scene before switch is not yet available
     */
    static byte identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> 32;
            case null, default -> 0; // may happen, it's ok
        };
    }


    void createGameScenes();

    Stream<GameScene> gameScenes();

    boolean gameSceneHasID(GameScene gameScene, String sceneID);

    GameScene selectGameScene(GameContext gameContext);

    boolean canvasDecorated(GameScene gameScene);
}

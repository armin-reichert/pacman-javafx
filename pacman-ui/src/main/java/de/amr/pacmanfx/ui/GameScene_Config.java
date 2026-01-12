/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;

import java.util.Optional;
import java.util.stream.Stream;

public interface GameScene_Config {

    interface SceneID {}

    enum CommonSceneID implements SceneID {
        BOOT_SCENE,
        INTRO_SCENE,
        START_SCENE,
        PLAY_SCENE_2D,
        PLAY_SCENE_3D,
        CUTSCENE_1,
        CUTSCENE_2,
        CUTSCENE_3,
        CUTSCENE_4
    }

    static SceneID cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonSceneID.CUTSCENE_1;
            case 2 -> CommonSceneID.CUTSCENE_2;
            case 3 -> CommonSceneID.CUTSCENE_3;
            case 4 -> CommonSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number" + n);
        };
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

    Stream<GameScene> gameScenes();

    boolean gameSceneHasID(GameScene gameScene, SceneID sceneID);

    Optional<GameScene> selectGameScene(Game game);

    boolean sceneDecorationRequested(GameScene gameScene);
}

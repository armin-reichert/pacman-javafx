/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.WorldMapColorScheme;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface GameUIConfig {
    Image appIcon();
    String assetNamespace();
    void createActorAnimations(GameLevel level);
    GameScene2D createPiPScene(Canvas canvas);
    GameRenderer createRenderer(Canvas canvas);
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    default boolean isGameCanvasDecorated() { return true; }
    GameScene selectGameScene(GameController gameController);
    GameSpriteSheet spriteSheet();
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);

    // 3D-only
    Node createLivesCounterShape(AssetStorage assets, double size);

    default boolean is2D3DPlaySceneSwitch(GameScene prevPlayScene, GameScene nextPlayScene) {
        if (prevPlayScene == null && nextPlayScene == null) {
            throw new IllegalStateException("WTF is going on here, old and new play scene are both NULL!");
        }
        if (prevPlayScene == null) {
            return false; // may happen
        }
        return gameSceneHasID(prevPlayScene, "PlayScene2D") && gameSceneHasID(nextPlayScene, "PlayScene3D")
            || gameSceneHasID(prevPlayScene, "PlayScene3D") && gameSceneHasID(nextPlayScene, "PlayScene2D");
    }
}
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
import de.amr.games.pacman.uilib.assets.AssetStorage;
import de.amr.games.pacman.uilib.assets.WorldMapColorScheme;
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
    <T extends GameSpriteSheet> T spriteSheet();
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);

    // 3D-only
    Node createLivesCounterShape(AssetStorage assets, double size);
}
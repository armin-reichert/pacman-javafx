/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.WorldMapColorScheme;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface GameUIConfiguration {
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    GameScene2D createPiPScene(GameContext context, Canvas canvas);
    GameSpriteSheet spriteSheet();
    GameRenderer createRenderer(AssetStorage assets, Canvas canvas);
    WorldMapColorScheme worldMapColoring(WorldMap worldMap);
    void createActorAnimations(GameLevel level);
    GameScene selectGameScene(GameContext context);
    String assetNamespace();
    default boolean isGameCanvasDecorated() { return true; }
    Image appIcon();

    // 3D-only
    Node createLivesCounterShape(AssetStorage assets, double size);
}
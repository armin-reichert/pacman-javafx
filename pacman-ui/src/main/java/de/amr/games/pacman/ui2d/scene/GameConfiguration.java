/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.uilib.WorldMapColoring;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public interface GameConfiguration {
    GameVariant gameVariant();
    void setGameScene(String id, GameScene gameScene);
    GameScene getGameScene(String id);
    default void initGameScenes(GameContext context) {
        gameScenes().forEach(gameScene -> gameScene.setGameContext(context));
    }
    Stream<GameScene> gameScenes();
    default boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        return getGameScene(sceneID) == gameScene;
    }
    GameScene2D createPiPScene(GameContext context, Canvas canvas);
    GameSpriteSheet spriteSheet();
    GameRenderer createRenderer(AssetStorage assets, Canvas canvas);
    WorldMapColoring worldMapColoring(WorldMap worldMap);
    void createActorAnimations(GameLevel level);
    GameScene selectGameScene(GameContext context);
    String assetKeyPrefix();
    default boolean isGameCanvasDecorated() { return true; }
}
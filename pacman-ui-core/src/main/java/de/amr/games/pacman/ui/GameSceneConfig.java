/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public interface GameSceneConfig {
    void set(String id, GameScene gameScene);
    GameScene get(String id);
    default void initGameScenes(GameContext context) {
        gameScenes().forEach(gameScene -> gameScene.setGameContext(context));
    }
    Stream<GameScene> gameScenes();
    default boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        return get(sceneID) == gameScene;
    }
    GameSpriteSheet spriteSheet();
    GameRenderer createRenderer(Canvas canvas);
    WorldMapColoring worldMapColoring(WorldMap worldMap);
    void createActorAnimations(GameLevel level);
    GameScene selectGameScene(GameContext context);
}
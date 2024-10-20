/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
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
    GameRenderer renderer();
    void createActorAnimations(GameModel game);
    GameScene selectGameScene(GameContext context);
}
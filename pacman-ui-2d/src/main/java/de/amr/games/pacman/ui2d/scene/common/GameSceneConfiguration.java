/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameSceneConfiguration {
    void set(String id, GameScene gameScene);
    GameScene get(String id);
    Stream<GameScene> gameScenes();
    default boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        return get(sceneID) == gameScene;
    }
    GameSpriteSheet spriteSheet();
    GameWorldRenderer renderer();
    void createActorAnimations(GameModel game);
    GameScene selectGameScene(GameContext context);
}
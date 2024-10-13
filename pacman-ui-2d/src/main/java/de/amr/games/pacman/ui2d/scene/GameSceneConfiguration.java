/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.AssetStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public abstract class GameSceneConfiguration {

    private final Map<GameSceneID, GameScene> sceneByID = new HashMap<>();

    public void set(GameSceneID id, GameScene gameScene) {
        sceneByID.put(id, gameScene);
    }

    public GameScene get(GameSceneID id) {
        return sceneByID.get(id);
    }

    public Stream<GameScene> gameScenes() {
        return sceneByID.values().stream();
    }

    public boolean gameSceneHasID(GameScene gameScene, GameSceneID sceneID) {
        return get(sceneID) == gameScene;
    }

    public abstract GameWorldRenderer createRenderer(AssetStorage assets);

    public abstract void createActorAnimations(GameModel game, GameSpriteSheet spriteSheet);

    public abstract GameScene selectGameScene(GameContext context);

    protected GameSceneID cutSceneID(int number) {
        return switch (number) {
            case 1 -> GameSceneID.CUT_SCENE_1;
            case 2 -> GameSceneID.CUT_SCENE_2;
            case 3 -> GameSceneID.CUT_SCENE_3;
            default -> null;
        };
    }
}
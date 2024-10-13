/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

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

    public abstract GameWorldRenderer createRenderer(AssetStorage assets);
}

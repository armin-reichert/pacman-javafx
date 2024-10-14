/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public abstract class GameSceneConfiguration {

    private final Map<String, GameScene> sceneByID = new HashMap<>();

    public void set(String id, GameScene gameScene) {
        sceneByID.put(id, gameScene);
    }

    public GameScene get(String id) {
        return sceneByID.get(id);
    }

    public Stream<GameScene> gameScenes() {
        return sceneByID.values().stream();
    }

    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        return get(sceneID) == gameScene;
    }

    public abstract GameSpriteSheet spriteSheet();

    public abstract GameWorldRenderer renderer();

    public abstract void createActorAnimations(GameModel game);

    public abstract GameScene selectGameScene(GameContext context);

    protected String cutSceneID(int number) {
        return "CutScene" + number;
    }
}
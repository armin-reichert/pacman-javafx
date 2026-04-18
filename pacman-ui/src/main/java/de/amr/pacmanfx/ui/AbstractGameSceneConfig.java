/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.Game;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGameSceneConfig implements GameSceneConfig {

    protected final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return true;
    }

    @Override
    public final Optional<GameScene> selectGameScene(GameUI ui, Game game) {
        requireNonNull(game);
        final SceneID sceneID = determineSceneID(game);
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, id -> createGameScene(ui, id));
        return Optional.of(gameScene);
    }

    @Override
    public final boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    protected abstract GameScene createGameScene(GameUI ui, SceneID sceneID);

    protected abstract SceneID determineSceneID(Game game);
}

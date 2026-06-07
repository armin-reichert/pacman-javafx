/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.app.AppContext;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGameSceneConfig implements GameSceneConfig {

    /**
     * Returns the {@link SceneID} for the numbered cutscene.
     *
     * @param n cutscene number (1–4)
     * @return the corresponding {@link CommonSceneID}
     * @throws IllegalArgumentException if the number is outside the valid range
     */
    public static SceneID cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonSceneID.CUTSCENE_1;
            case 2 -> CommonSceneID.CUTSCENE_2;
            case 3 -> CommonSceneID.CUTSCENE_3;
            case 4 -> CommonSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number " + n);
        };
    }

    protected final Map<SceneID, GameScene> scenesByID = new HashMap<>();

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public SceneID resolveCutSceneID(GameContext gameContext) {
        final Optional<GameLevel> optGameLevel = gameContext.model().optGameLevel();
        if (optGameLevel.isEmpty()) {
            throw new IllegalStateException("Cannot determine cut scene, no game level available");
        }
        final int cutSceneNumber = optGameLevel.get().cutSceneNumber();
        if (cutSceneNumber == 0) {
            throw new IllegalStateException("Cannot determine cut scene following level %d".formatted(optGameLevel.get().number()));
        }
        return AbstractGameSceneConfig.cutSceneID(cutSceneNumber);
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return true;
    }

    @Override
    public final Optional<GameScene> selectGameScene(AppContext appContext, GameModel game) {
        requireNonNull(game);
        final SceneID sceneID = determineSceneID(appContext.currentGameContext());
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public final boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    protected abstract GameScene createGameScene(SceneID sceneID);

    protected abstract SceneID determineSceneID(GameContext gameContext);
}

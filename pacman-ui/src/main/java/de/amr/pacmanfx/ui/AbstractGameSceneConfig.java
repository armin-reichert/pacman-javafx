/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
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

    /**
     * Identifies whether a scene switch represents a transition between 2D and 3D play modes.
     * <p>
     * This is used by the UI layer to trigger special animations or transitions when switching
     * between {@link GameScene2D} and {@link PlayScene3D}.
     *
     * @param sceneBefore the scene previously displayed (may be {@code null})
     * @param sceneAfter  the scene that will be displayed next (never {@code null})
     * @return
     *     <ul>
     *       <li>{@code 23} for a 2D → 3D switch</li>
     *       <li>{@code 32} for a 3D → 2D switch</li>
     *       <li>{@code 0} if no meaningful switch can be determined</li>
     *     </ul>
     * @throws IllegalStateException if both parameters are {@code null}
     */
    public static byte identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> 32;
            case null, default -> 0; // may happen, it's ok
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
    public SceneID resolveCutSceneID(Game game) {
        final Optional<GameLevel> optGameLevel = game.optGameLevel();
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

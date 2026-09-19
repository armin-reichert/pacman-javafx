/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGameSceneConfig implements GameVariantGameSceneConfig {

    public static Named cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonGameSceneID.CUTSCENE_1;
            case 2 -> CommonGameSceneID.CUTSCENE_2;
            case 3 -> CommonGameSceneID.CUTSCENE_3;
            case 4 -> CommonGameSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number " + n);
        };
    }

    protected final Map<Named, GameScene> scenesByID = new HashMap<>();

    protected AbstractGameSceneConfig() {}

    protected abstract Supplier<GameScene> getGameSceneFactory(Named sceneID);

    protected abstract Named computeGameSceneID(GameContext game, boolean select3D);

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public Named resolveCutSceneID(GameContext game) {
        final GameLevel level = game.session().level();
        final OptionalInt cutSceneNumber = game.playConfig().rules().cutSceneAfterLevel(level.number());
        if (cutSceneNumber.isEmpty()) {
            throw new IllegalStateException("Cannot determine cut scene following level %d".formatted(level.number()));
        }
        return AbstractGameSceneConfig.cutSceneID(cutSceneNumber.getAsInt());
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return true;
    }

    @Override
    public final Optional<GameScene> selectGameScene(GameContext game, boolean select3D) {
        requireNonNull(game);
        final Named sceneID = computeGameSceneID(game, select3D);
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, id -> getGameSceneFactory(id).get());
        return Optional.of(gameScene);
    }

    @Override
    public final boolean gameSceneHasID(GameScene gameScene, Named sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}

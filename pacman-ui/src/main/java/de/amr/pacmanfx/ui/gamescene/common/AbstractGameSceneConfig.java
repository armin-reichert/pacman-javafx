/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGameSceneConfig implements GameSceneConfig {

    public static Identifier cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonGameSceneID.CUTSCENE_1;
            case 2 -> CommonGameSceneID.CUTSCENE_2;
            case 3 -> CommonGameSceneID.CUTSCENE_3;
            case 4 -> CommonGameSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number " + n);
        };
    }

    protected final GameAppContext actionContext;
    protected final Map<Identifier, GameScene> scenesByID = new HashMap<>();

    public AbstractGameSceneConfig(GameAppContext actionContext) {
        this.actionContext = requireNonNull(actionContext);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public Identifier resolveCutSceneID(GameContext context) {
        final GameLevel level = context.model().assertLevel();
        final OptionalInt cutSceneNumber = context.model().rules().cutSceneNumberAfterLevel(level.number());
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
    public final Optional<GameScene> selectGameScene(GameAppContext actionContext, GameModel model) {
        requireNonNull(actionContext);
        final Identifier Identifier = determineSceneID(actionContext.currentGameContext());
        final GameScene gameScene = scenesByID.computeIfAbsent(Identifier, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public final boolean gameSceneHasID(GameScene gameScene, Identifier Identifier) {
        requireNonNull(gameScene);
        requireNonNull(Identifier);
        return scenesByID.get(Identifier) == gameScene;
    }

    protected abstract GameScene createGameScene(Identifier Identifier);

    protected abstract Identifier determineSceneID(GameContext context);
}

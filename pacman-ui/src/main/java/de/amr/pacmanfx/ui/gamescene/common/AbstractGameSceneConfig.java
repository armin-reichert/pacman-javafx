/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.vm.GameUISettingsVM;
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

    protected final Map<Identifier, GameScene> scenesByID = new HashMap<>();

    public AbstractGameSceneConfig() {}

    protected abstract GameScene createGameScene(GameAppContext appContext, Identifier Identifier);

    protected abstract Identifier determineSceneID(GameUISettingsVM viewModel, GameContext gameContext);

    @Override
    public void dispose() {
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
        scenesByID.clear();
    }

    @Override
    public Identifier resolveCutSceneID(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final OptionalInt cutSceneNumber = gameContext.model().rules().cutSceneAfterLevel(level.number());
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
    public final Optional<GameScene> selectGameScene(GameAppContext appContext, GameModel model) {
        requireNonNull(appContext);
        final Identifier Identifier = determineSceneID(appContext.ui().viewModel(), appContext.currentGameContext());
        final GameScene gameScene = scenesByID.computeIfAbsent(Identifier, id -> createGameScene(appContext, id));
        return Optional.of(gameScene);
    }

    @Override
    public final boolean gameSceneHasID(GameScene gameScene, Identifier Identifier) {
        requireNonNull(gameScene);
        requireNonNull(Identifier);
        return scenesByID.get(Identifier) == gameScene;
    }
}

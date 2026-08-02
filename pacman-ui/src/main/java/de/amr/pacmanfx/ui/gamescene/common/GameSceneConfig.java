/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.Optional;

/**
 * Defines the configuration and selection logic for all game scenes belonging to a specific
 * game variant or UI mode.
 */
public interface GameSceneConfig extends Disposable {

    boolean gameSceneHasID(GameScene gameScene, Named sceneID);

    Optional<GameScene> selectGameScene(GameAppContext appContext, GameModel model);

    Named resolveCutSceneID(GameContext gameContext);

    boolean sceneDecorationRequested(GameScene gameScene);
}

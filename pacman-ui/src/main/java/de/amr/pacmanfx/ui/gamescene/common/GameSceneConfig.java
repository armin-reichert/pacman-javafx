/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.game.Game;

import java.util.Optional;

/**
 * Defines the configuration and selection logic for all game scenes belonging to a specific
 * game variant or UI mode.
 */
public interface GameSceneConfig extends Disposable {

    boolean gameSceneHasID(AbstractGameScene gameScene, Identifier sceneID);

    Optional<AbstractGameScene> selectGameScene(Game game, GameModel gameModel);

    Identifier resolveCutSceneID(GameContext gameContext);

    boolean sceneDecorationRequested(AbstractGameScene gameScene);
}

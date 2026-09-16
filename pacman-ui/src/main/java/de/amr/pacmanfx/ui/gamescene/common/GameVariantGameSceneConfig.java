/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameApp;

import java.util.Optional;

/**
 * Defines the configuration and selection logic for all game scenes belonging to a specific
 * game variant or UI mode.
 */
public interface GameVariantGameSceneConfig extends Disposable {

    boolean gameSceneHasID(AbstractGameScene gameScene, Named sceneID);

    Optional<AbstractGameScene> selectGameScene(GameApp app, boolean select3D);

    Named resolveCutSceneID(GameContext game);

    boolean sceneDecorationRequested(AbstractGameScene gameScene);
}

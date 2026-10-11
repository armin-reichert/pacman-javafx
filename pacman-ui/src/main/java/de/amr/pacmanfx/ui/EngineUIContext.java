/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.engine.EngineContext;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewManager;

public interface EngineUIContext extends EngineContext {

    GameSceneManager gameSceneManager();

    GameViewManager viewManager();

    RenderManager renderManager();

    TranslationManager translationManager();

    GameUI ui();
}

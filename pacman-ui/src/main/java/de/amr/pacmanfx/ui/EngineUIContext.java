/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.engine.GameEngineContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;

public interface EngineUIContext extends GameEngineContext {

    GameSceneManager gameSceneManager();

    RenderManager renderManager();

    TranslationManager translationManager();

    GameUI ui();

}

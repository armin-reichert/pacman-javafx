/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.engine.GameEngineBaseContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;

public interface GameEngineContext extends GameEngineBaseContext {

    GameSceneManager gameSceneManager();

    RenderManager renderManager();

    TranslationManager translationManager();

    GameUI ui();

}

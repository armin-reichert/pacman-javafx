/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.engine.EngineCoreContext;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewManager;

public interface EngineContext extends EngineCoreContext {

    GameSceneManager gameSceneManager();

    GameViewManager viewManager();

    RenderManager renderManager();

    SpriteAnimationSystem spriteAnimationSystem();

    TranslationManager translationManager();

    GameUI ui();

    GameVariantUIConfig uiConfig(GameVariantRuntime runtime);
}

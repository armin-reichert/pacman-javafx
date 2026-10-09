/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.engine.config.GameVariantManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;

public interface GameActionExecutionContext {

    PacManGamesEngine engine();

    GameUI ui();

    TranslationManager translationManager();

    GameSceneManager gameSceneManager();

    GameContext currentGame();

    GameClock clock();

    SoundManager soundManager();

    GameVariantManager gameVariantManager();

    RenderManager renderManager();

    Input input();
}

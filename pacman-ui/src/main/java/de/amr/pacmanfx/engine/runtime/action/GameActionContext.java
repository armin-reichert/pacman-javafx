/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.config.GameVariantManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;

public interface GameActionContext {

    GameClock clock();

    GameContext currentGame();

    EngineLifecycle engineLife();

    GameSceneManager gameSceneManager();

    GameVariantManager gameVariantManager();

    Input input();

    RenderManager renderManager();

    SoundManager soundManager();

    TranslationManager translationManager();

    GameUI ui();

    DirectoryWatchdog watchdog();
}

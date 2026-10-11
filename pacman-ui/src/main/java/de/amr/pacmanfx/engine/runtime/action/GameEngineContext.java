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

import java.util.Optional;

public interface GameEngineContext {

    GameClock clock();

    Optional<GameContext> optCurrentGame();

    EngineLifecycle engineLife();

    GameSceneManager gameSceneManager();

    GameVariantManager gameVariantManager();

    Input input();

    SoundManager soundManager();

    DirectoryWatchdog watchdog();

    //TODO extract UI-specific part

    RenderManager renderManager();

    TranslationManager translationManager();

    GameUI ui();

}

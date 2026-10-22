/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;

import java.util.Optional;

public interface EngineContext {

    GameClock clock();

    Optional<GameContext> optCurrentGame();

    EngineLifecycle lifeCycle();

    GameVariantManager gameVariantManager();

    Input input();

    SoundManager soundManager();

    DirectoryWatchdog watchdog();
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.config.GameVariantManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.sound.SoundManager;

/**
 * The game "engine".
 */
public interface PacManGamesEngine {

    void exitGameVariant(GameVariantRuntime runtime);

    void enterGameVariant(GameVariantRuntime runtime);

    void newGameSession();

    void startGame();

    void suspendGame();

    void terminate();

    boolean runAction(GameAction gameAction);

    RenderManager renderManager();

    TranslationManager translationManager();

    SoundManager soundManager();

    PlayStation playStation();

    GameClock clock();

    GameVariantManager gameVariantManager();

    GameContext currentGame();

    Input input();

    DirectoryWatchdog watchdog();
}

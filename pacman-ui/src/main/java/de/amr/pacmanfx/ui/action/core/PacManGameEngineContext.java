/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.game.GameVariantManager;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.rendering.RenderManager;
import de.amr.pacmanfx.ui.sound.SoundManager;

/**
 * The game "engine".
 */
public interface PacManGameEngineContext {

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

    SpriteAnimationTimer spriteAnimationTimer();

    GameClock clock();

    GameVariantManager gameVariantManager();

    GameContext currentGame();

    GameUI ui();

    GameSceneManager gameSceneManager();

    Input input();

    DirectoryWatchdog watchdog();
}

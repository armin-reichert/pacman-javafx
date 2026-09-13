/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.game.GameLoop;
import de.amr.pacmanfx.game.GameVariantManager;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.RenderManager;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.input.Input;

public interface GameAppContext {

    void startGame();

    void suspendGame();

    void terminate();

    boolean runAction(GameAction gameAction);

    CommonGameActions commonActions();

    RenderManager renderManager();

    GameClock clock();

    GameVariantManager variantManager();

    default GameVariantPlayConfig currentGameVariantPlayConfig() {
        return variantManager().currentVariantConfig().playConfig();
    }

    default GameVariantUIConfig currentGameVariantUIConfig() {
        return variantManager().currentVariantConfig().uiConfig();
    }

    GameContext game();

    GameUI ui();

    GameSceneManager gameSceneManager();

    Input input();

    DirectoryWatchdog watchdog();
}

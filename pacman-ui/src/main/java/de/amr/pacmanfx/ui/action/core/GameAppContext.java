/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantManager;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.input.Input;

public interface GameAppContext {

    void startGame();

    void suspendGame();

    void terminate();

    GameContext game();

    boolean runAction(GameAction gameAction);

    CommonGameActions commonActions();

    GameVariantManager gameVariants();

    default GameVariantConfig currentGameVariantConfig() {
        return gameVariants().currentGameVariant().config();
    }

    default GameVariantUIConfig currentGameVariantUIConfig() {
        return gameVariants().currentGameVariant().uiConfig();
    }

    GameUI ui();

    Input input();

    GameClock clock();

    DirectoryWatchdog watchdog();
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.Identifier;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.game.GameLifecycle;
import de.amr.pacmanfx.game.GameVariantManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.input.Input;

public interface GameAppContext {

    GameLifecycle lifecycle();

    GameContext currentGameContext();

    CommonGameActions commonActions();

    GameVariantManager variants();

    GameUI ui();

    Input input();

    GameClock clock();

    DirectoryWatchdog watchdog();

    default <T> T getExtensionValue(Identifier id, Class<T> type) {
        return variants().currentVariant().getExtensionValue(this, id, type);
    }
}

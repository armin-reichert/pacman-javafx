/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action.core;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.game.GameVariantManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.input.Input;

public interface GameActionContext extends GameLifecycle {
    CoinMechanism coinMechanism();
    GameClock clock();
    GameContext gameContext();
    GameVariantManager variants();
    GameUI ui();
    Input input();
    CommonActions commonActions();
    default <T> T getExtensionValue(Identifier id, Class<T> type) {
        return variants().currentVariant().getExtensionValue(this, id, type);
    }
}

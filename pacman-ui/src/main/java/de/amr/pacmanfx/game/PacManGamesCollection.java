/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.core.GameLifecycle;

public interface PacManGamesCollection extends GameLifecycle {
    PacManGamesMachine machine();
    GameContext gameContext();
    GameUI ui();
    CommonActions commonActions();
    GameVariantManager variants();
    void setContextForCurrentVariant(GameContext context);
    void setUI(GameUI ui);
    void showGameVariant(GameVariantID variantID);
}

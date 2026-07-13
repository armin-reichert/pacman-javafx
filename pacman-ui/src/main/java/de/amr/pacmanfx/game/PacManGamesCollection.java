/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;

public interface PacManGamesCollection {
    PacManGamesMachine machine();
    GameContext currentGameContext();
    GameUI ui();
    CommonActions commonActions();
    GameVariantManager variants();
    void setGameContext(GameContext context);
    void setUI(GameUI ui);
    void showGameVariant(GameVariantID variantID);
}

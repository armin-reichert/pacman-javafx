/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;

public interface Game {

    PacManGamesMachine machine();

    GameUI ui();

    GameVariantManager variantManager();

    CommonActions actions();

    GameContext context();

    GameExtensions extensions();

    void setUI(GameUI ui);

    void showGameVariant(GameVariantID variantID);

    void startGamePlay();

    void suspendGamePlay();

    void terminate();
}

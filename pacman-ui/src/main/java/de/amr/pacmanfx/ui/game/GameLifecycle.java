/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameVariantID;
import javafx.stage.Stage;

public interface GameLifecycle {

    void stopGame();

    void startGame();

    void show(GameVariantID variantID, Stage stage);

    void terminate();
}

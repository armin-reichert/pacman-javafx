/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameVariantID;

public interface GameLifecycle {

    void stop();

    void start();

    void showUI(GameVariantID variantID);

    void terminate();
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

public interface GameLifecycle {
    void startGamePlay();
    void suspendGamePlay();
    void terminate();
}

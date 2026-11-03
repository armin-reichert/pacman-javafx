/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

public interface EngineLifecycle {

    void startSession();

    void endSession();

    void restartSession();

    void createNewSession();

    void terminate();
}

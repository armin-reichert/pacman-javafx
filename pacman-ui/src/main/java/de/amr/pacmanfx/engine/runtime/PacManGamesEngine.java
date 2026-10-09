/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime;

import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.PlayStation;

/**
 * The game "engine".
 */
public interface PacManGamesEngine extends EngineLifecycle {

    //TODO Move these into engine module too

    void exitGameVariant(GameVariantRuntime runtime);

    void enterGameVariant(GameVariantRuntime runtime);

    PlayStation playStation();
}

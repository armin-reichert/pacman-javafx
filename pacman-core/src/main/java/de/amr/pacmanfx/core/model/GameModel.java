/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;

public interface GameModel {

    void setInitialLifeCount(int initialLifeCount);
    int initialLifeCount();
    WorldMapSelector mapSelector();
    GameRules rules();
}

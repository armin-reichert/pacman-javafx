/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.levelCounter;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;

public class LevelCounter extends GameEntity {

    public LevelCounter() {
        setComponent(LevelCounterData.class, new LevelCounterData());
    }
}
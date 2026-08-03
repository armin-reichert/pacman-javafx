/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.pacmanfx.core.ecs.GameEntity;

public class LevelCounter extends GameEntity {

    public LevelCounter() {
        setComponent(LevelCounterData.class, new LevelCounterData());
    }
}
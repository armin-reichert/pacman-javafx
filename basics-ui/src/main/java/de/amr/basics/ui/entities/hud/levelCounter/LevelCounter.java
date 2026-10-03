/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud.levelCounter;

import de.amr.basics.ecs.GameEntity;

public class LevelCounter extends GameEntity {

    public LevelCounter() {
        setComponent(LevelCounterDataComp.class, new LevelCounterDataComp());
    }

    public LevelCounterDataComp data() {
        return assertComponent(LevelCounterDataComp.class);
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;

public class LevelCounter extends GameEntity {

    public LevelCounter() {
        setComp(LevelCounterData.class, new LevelCounterData());
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.OVERLAY));
    }

    public LevelCounterData data() {
        return reqComp(LevelCounterData.class);
    }
}
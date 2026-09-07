/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;

public class LevelCounter extends GameEntity implements Renderable {

    public LevelCounter() {
        setComp(LevelCounterData.class, new LevelCounterData());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }

    public LevelCounterData data() {
        return reqComp(LevelCounterData.class);
    }
}
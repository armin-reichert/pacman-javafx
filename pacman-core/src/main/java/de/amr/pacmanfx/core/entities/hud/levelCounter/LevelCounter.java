/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.hud.levelCounter;

import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

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
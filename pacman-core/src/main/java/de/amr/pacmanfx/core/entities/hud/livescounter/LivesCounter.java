/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.hud.livescounter;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.rendering.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public class LivesCounter extends GameEntity implements Renderable {

    public LivesCounter() {
        setComp(LivesCounterDataComp.class, new LivesCounterDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }

    public LivesCounterDataComp data() {
        return reqComp(LivesCounterDataComp.class);
    }
}

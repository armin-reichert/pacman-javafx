/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.livescounter.comp.LivesCounterDataComp;

public class LivesCounter extends GameEntity {

    public LivesCounter() {
        setComp(LivesCounterDataComp.class, new LivesCounterDataComp());
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.OVERLAY));
    }

    public LivesCounterDataComp data() {
        return reqComp(LivesCounterDataComp.class);
    }
}

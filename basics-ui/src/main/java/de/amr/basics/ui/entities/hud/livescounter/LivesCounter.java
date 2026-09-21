/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud.livescounter;

import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;

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

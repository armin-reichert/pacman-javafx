/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public class Door extends GameEntity implements Renderable {

    public Door() {
        setComp(DoorDataComp.class, new DoorDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.LEVEL;
    }
}

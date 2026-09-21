/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world.door;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

public class Door extends GameEntity implements Renderable {

    public Door() {
        setComp(DoorDataComp.class, new DoorDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.WORLD;
    }
}

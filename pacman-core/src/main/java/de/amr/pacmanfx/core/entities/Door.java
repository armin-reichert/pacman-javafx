/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.entities.door.comp.DoorDataComp;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class Door extends GameEntity implements Renderable {

    public Door() {
        setComp(DoorDataComp.class, new DoorDataComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.WORLD;
    }
}

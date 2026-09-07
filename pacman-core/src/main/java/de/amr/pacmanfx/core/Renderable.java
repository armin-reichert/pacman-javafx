/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

import java.util.Comparator;

public interface Renderable {

    Comparator<Renderable> RENDERING_ORDER = Comparator
        .comparingInt((Renderable r) -> r.layer().z())
        .thenComparingInt(Renderable::layerPriority);

    RenderingLayer layer();

    default int layerPriority() {
        return 0;
    }
}

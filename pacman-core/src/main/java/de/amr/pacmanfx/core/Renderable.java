/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public interface Renderable {

    RenderingLayer layer();

    default int zOrder() {
        return 0;
    }
}

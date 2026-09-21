/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.rendering;

public interface Renderable {

    RenderingLayer layer();

    default int z() {
        return 0;
    }
}

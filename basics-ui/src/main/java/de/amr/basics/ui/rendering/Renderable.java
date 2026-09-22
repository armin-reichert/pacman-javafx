/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.Vector2f;

public interface Renderable {

    RenderingLayer layer();

    default int z() {
        return 0;
    }

    default Vector2f offset() {
        return Vector2f.ZERO;
    }
}

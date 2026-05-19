/*
 * Copyright (c) 2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

public record ObjColor(float red, float green, float blue) {
    static ObjColor BLACK = new ObjColor(0, 0, 0);

    public boolean isBlack() {
        final double threshold = 1e-6;
        return red < threshold && green < threshold && blue < threshold;
    }
}

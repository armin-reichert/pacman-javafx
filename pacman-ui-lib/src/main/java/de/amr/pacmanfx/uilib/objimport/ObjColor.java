/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public record ObjColor(float red, float green, float blue) {
    static ObjColor BLACK = new ObjColor(0, 0, 0);
}

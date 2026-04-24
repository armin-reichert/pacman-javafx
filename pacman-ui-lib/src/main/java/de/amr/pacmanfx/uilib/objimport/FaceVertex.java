/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

class FaceVertex {
    public final int vIndex;
    public final int vtIndex;
    public final int vnIndex;

    public FaceVertex(int vIndex, int vtIndex, int vnIndex) {
        this.vIndex = vIndex;
        this.vtIndex = vtIndex;
        this.vnIndex = vnIndex;
    }
}

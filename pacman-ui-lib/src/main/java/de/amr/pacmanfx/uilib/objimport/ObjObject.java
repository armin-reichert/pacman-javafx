/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import java.util.ArrayList;
import java.util.List;

class ObjObject {
    public final String name;
    public final List<ObjGroup> groups = new ArrayList<>();

    public ObjObject(String name) {
        this.name = name;
    }
}

/*
 * Copyright (c) 2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.ArrayList;
import java.util.List;

public class ObjObject {
    public final String name;
    public final List<ObjGroup> groups = new ArrayList<>();

    public ObjObject(String name) {
        this.name = name;
    }
}

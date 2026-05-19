/*
 * Copyright (c) 2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.ArrayList;
import java.util.List;

public class ObjGroup {
    public final String name;
    public final List<ObjFace> faces = new ArrayList<>();

    public ObjGroup(String name) {
        this.name = name;
    }
}

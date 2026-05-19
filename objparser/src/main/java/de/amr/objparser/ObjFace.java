/*
 * Copyright (c) 2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.ArrayList;
import java.util.List;

public class ObjFace {
    public final List<ObjFaceVertex> vertices = new ArrayList<>();
    public final String materialName;
    public final Integer smoothingGroup;

    public ObjFace(String materialName, Integer smoothingGroup) {
        this.materialName = materialName;
        this.smoothingGroup = smoothingGroup;
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

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

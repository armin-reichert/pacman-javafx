/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjModel {
    final List<ObjVertex> vertices = new ArrayList<>();
    List<ObjTexCoord> texCoords = new ArrayList<>();
    final List<ObjNormal> normals = new ArrayList<>();

    final List<ObjObject> objects = new ArrayList<>();
    final Map<String, Map<String, ObjMaterial>> materialLibsMap = new HashMap<>();

    ObjObject currentObject;
    ObjGroup currentGroup;
    String currentMaterialName;
    Integer currentSmoothingGroup;
}

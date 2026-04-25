/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import java.util.*;

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

    public List<ObjVertex> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<ObjTexCoord> texCoords() {
        return Collections.unmodifiableList(texCoords);
    }

    public List<ObjObject> objects() {
        return Collections.unmodifiableList(objects);
    }

    public Map<String, Map<String, ObjMaterial>> materialLibsMap() {
        return Collections.unmodifiableMap(materialLibsMap);
    }
}

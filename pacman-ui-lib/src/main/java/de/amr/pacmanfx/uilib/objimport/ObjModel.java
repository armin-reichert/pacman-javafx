/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjModel {
    final List<Vertex> vertices = new ArrayList<>();
    List<TexCoord> texCoords = new ArrayList<>();
    final List<Normal> normals = new ArrayList<>();

    final List<ObjObject> objects = new ArrayList<>();
    final Map<String, Map<String, PhongMaterial>> materialLibsMap = new HashMap<>();

    ObjObject currentObject;
    ObjGroup currentGroup;
    String currentMaterialName;
    Integer currentSmoothingGroup;
}

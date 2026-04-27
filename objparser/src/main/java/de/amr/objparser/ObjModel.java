/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.*;

public class ObjModel {
    final Map<String, Map<String, ObjMaterial>> materialLibsMap = new HashMap<>();
    final List<ObjObject> objects = new ArrayList<>();
    final List<ObjVertex> vertices = new ArrayList<>();
    final List<ObjTexCoord> texCoords = new ArrayList<>();
    final List<ObjNormal> normals = new ArrayList<>();

    String source;
    String    url;
    ObjObject currentObject;
    ObjGroup  currentGroup;
    String    currentMaterialName;
    Integer   currentSmoothingGroup;

    public ObjModel() {
        source = "No source";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String source() {
        return source;
    }

    public String url() {
        return url;
    }

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

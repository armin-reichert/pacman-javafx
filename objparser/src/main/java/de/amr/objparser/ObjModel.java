/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.*;

public class ObjModel {
    final Map<String, Map<String, ObjMaterial>> materialLibsMap = new HashMap<>();
    final List<ObjObject> objects = new ArrayList<>(10);
    final List<ObjVertex> vertices = new ArrayList<>(1000);
    final List<ObjTexCoord> texCoords = new ArrayList<>(500);
    final List<ObjNormal> normals = new ArrayList<>(500);

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

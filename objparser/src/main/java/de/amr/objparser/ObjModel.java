/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.*;

public class ObjModel {
    Map<String, Map<String, ObjMaterial>> materialLibsMap = new HashMap<>();

    final List<ObjObject> objects;
    final List<ObjVertex> vertices;
    final List<ObjTexCoord> texCoords;
    final List<ObjNormal> normals;

    String    source = "";
    String    url;

    ObjObject currentObject;
    ObjGroup  currentGroup;
    String    currentMaterialName;
    Integer   currentSmoothingGroup;

    public ObjModel() {
        this(50);
    }

    public ObjModel(long lineCount) {
        objects = new ArrayList<>((int) Math.max(5, lineCount / 2000));
        vertices = new ArrayList<>((int)(lineCount / 3));
        texCoords = new ArrayList<>((int)(lineCount / 10));
        normals = new ArrayList<>((int)(lineCount / 10));
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

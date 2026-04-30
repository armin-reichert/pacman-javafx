/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjModel {

    // Geometry stored in fastutil primitive lists
    public final FloatArrayList vertices;   // x,y,z,x,y,z,...
    public final FloatArrayList texCoords;  // u,v,u,v,...
    public final FloatArrayList normals;    // nx,ny,nz,...

    // Object/group hierarchy (unchanged)
    public final List<ObjObject> objects = new ArrayList<>();

    // Material libraries
    public final Map<String, Map<String, ObjMaterial>> materialLibsMap = new HashMap<>();

    // Parsing state
    public ObjObject currentObject;
    public ObjGroup currentGroup;
    public String currentMaterialName;
    public Integer currentSmoothingGroup;

    // Metadata
    private String url;
    private String source;

    public ObjModel(ObjFileParser.ObjSizeInfo sizes) {

        // Pre-allocate exact sizes (fastutil)
        this.vertices  = new FloatArrayList((int) (sizes.vertexCount() * 3));
        this.texCoords = new FloatArrayList((int) (sizes.texCoordCount() * 2));
        this.normals   = new FloatArrayList((int) (sizes.normalCount() * 3));

        // Objects and faces are created during parsing
        this.currentObject = null;
        this.currentGroup = null;
        this.currentMaterialName = null;
        this.currentSmoothingGroup = null;
    }

    /* -------------------------------------------------------------
     * Metadata
     * ------------------------------------------------------------- */

    public void setUrl(String url) {
        this.url = url;
    }

    public String url() {
        return url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String source() {
        return source;
    }

    /* -------------------------------------------------------------
     * Convenience API
     * ------------------------------------------------------------- */

    public int vertexCount() {
        return vertices.size() / 3;
    }

    public int texCoordCount() {
        return texCoords.size() / 2;
    }

    public int normalCount() {
        return normals.size() / 3;
    }
}

/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.objimport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;

import java.net.URL;
import java.util.*;

public class ObjFileData {

    URL url;
    Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();
    List<Map<String, Material>> materialMapsList = new ArrayList<>();
    ObservableFloatArray vertexArray = FXCollections.observableFloatArray();
    ObservableFloatArray uvArray = FXCollections.observableFloatArray();
    ArrayList<Integer> faceList = new ArrayList<>();
    ArrayList<Integer> smoothingGroupList = new ArrayList<>();
    ObservableFloatArray normalsArray = FXCollections.observableFloatArray();
    ArrayList<Integer> faceNormalsList = new ArrayList<>();

    int vertexIndex(int v) {
        return (v < 0) ? v + vertexArray.size() / 3 : v - 1;
    }

    int uvIndex(int uv) {
        return (uv < 0) ? uv + uvArray.size() / 2 : uv - 1;
    }

    int normalIndex(int n) {
        return (n < 0) ? n + normalsArray.size() / 3 : n - 1;
    }

    /**
     * @return map of parsed triangle meshes with mesh name from OBJ file as key
     */
    public Map<String, TriangleMesh> meshMap() {
        return Collections.unmodifiableMap(triangleMeshMap);
    }

    /**
     * @return set of mesh names used in OBJ file
     */
    public Set<String> getMeshNames() {
        return triangleMeshMap.keySet();
    }

    /**
     * @param name mesh name
     * @return triangle mesh with given name or {@code null}
     */
    public TriangleMesh getTriangleMesh(String name) {
        return triangleMeshMap.get(name);
    }

    public List<Map<String, Material>> materialLibsList() {
        return materialMapsList;
    }
}

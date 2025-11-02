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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjFileData {

    public URL url;
    public Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();
    public List<Map<String, Material>> materialMapsList = new ArrayList<>();

    ObservableFloatArray vertexArray = FXCollections.observableFloatArray();
    ObservableFloatArray uvArray = FXCollections.observableFloatArray();
    ArrayList<Integer> facesList = new ArrayList<>();
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
}
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

/**
 * Parsed content from an OBJ file.
 */
public class ObjFileData {

    public ObjFileData(URL url) {
        this.url = Objects.requireNonNull(url);
    }

    public final URL url;
    public final Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();
    public final List<Map<String, Material>> materialMapsList = new ArrayList<>();

    final ObservableFloatArray vertexArray = FXCollections.observableFloatArray();
    final ObservableFloatArray uvArray = FXCollections.observableFloatArray();
    final ArrayList<Integer> facesList = new ArrayList<>();
    final ArrayList<Integer> smoothingGroupList = new ArrayList<>();
    final ObservableFloatArray normalsArray = FXCollections.observableFloatArray();
    final ArrayList<Integer> faceNormalsList = new ArrayList<>();

    final int vertexIndex(int v) {
        return (v < 0) ? v + vertexArray.size() / 3 : v - 1;
    }

    final int uvIndex(int uv) {
        return (uv < 0) ? uv + uvArray.size() / 2 : uv - 1;
    }

    final int normalIndex(int n) {
        return (n < 0) ? n + normalsArray.size() / 3 : n - 1;
    }
}
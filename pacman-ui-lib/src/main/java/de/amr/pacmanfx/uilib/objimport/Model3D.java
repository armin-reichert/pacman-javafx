/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import de.amr.pacmanfx.lib.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.Material;
import javafx.scene.shape.TriangleMesh;

import java.net.URL;
import java.util.*;

/**
 * Represents a 3D model loaded from a Wavefront OBJ file.
 * <p>
 * This class stores all intermediate data extracted from the OBJ file:
 * <ul>
 *   <li>vertex positions</li>
 *   <li>texture coordinates</li>
 *   <li>normals</li>
 *   <li>faces and smoothing groups</li>
 *   <li>materials and material groups</li>
 * </ul>
 * After parsing, the model provides one or more {@link TriangleMesh} instances,
 * each optionally associated with a material map.
 * <p>
 * The class does not perform the parsing itself; it only holds the parsed data.
 * A separate OBJ importer is expected to populate the fields.
 */
public class Model3D implements Disposable {

    /**
     * Creates a new model representation for the OBJ file located at the given URL.
     *
     * @param url the URL of the OBJ resource; must not be {@code null}
     */
    public Model3D(URL url) {
        this.url = Objects.requireNonNull(url);
    }

    /** The URL of the OBJ file this model was loaded from. */
    public final URL url;

    /**
     * Maps mesh names (OBJ object/group names) to their corresponding {@link TriangleMesh}.
     * <p>
     * OBJ files may contain multiple named objects; each becomes a separate mesh.
     */
    public final Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();

    /**
     * A list of material maps, one per mesh group.
     * <p>
     * Each entry maps material names to JavaFX {@link Material} instances.
     */
    public final List<Map<String, Material>> materialMapsList = new ArrayList<>();

    /** Flat array of vertex coordinates (x, y, z). */
    final ObservableFloatArray vertexArray = FXCollections.observableFloatArray();

    /** Flat array of texture coordinates (u, v). */
    final ObservableFloatArray uvArray = FXCollections.observableFloatArray();

    /** Face index list (vertex/uv/normal indices). */
    final ArrayList<Integer> facesList = new ArrayList<>();

    /** Smoothing group indices for each face. */
    final ArrayList<Integer> smoothingGroupList = new ArrayList<>();

    /** Flat array of vertex normals (nx, ny, nz). */
    final ObservableFloatArray normalsArray = FXCollections.observableFloatArray();

    /** Normal indices for each face. */
    final ArrayList<Integer> faceNormalsList = new ArrayList<>();

    /**
     * Converts an OBJ vertex index (1-based, negative allowed) into a 0-based index
     * into {@link #vertexArray}.
     *
     * @param v the OBJ vertex index
     * @return the resolved 0-based index
     */
    final int vertexIndex(int v) {
        return (v < 0) ? v + vertexArray.size() / 3 : v - 1;
    }

    /**
     * Converts an OBJ texture coordinate index (1-based, negative allowed)
     * into a 0-based index into {@link #uvArray}.
     *
     * @param uv the OBJ texture coordinate index
     * @return the resolved 0-based index
     */
    final int uvIndex(int uv) {
        return (uv < 0) ? uv + uvArray.size() / 2 : uv - 1;
    }

    /**
     * Converts an OBJ normal index (1-based, negative allowed)
     * into a 0-based index into {@link #normalsArray}.
     *
     * @param n the OBJ normal index
     * @return the resolved 0-based index
     */
    final int normalIndex(int n) {
        return (n < 0) ? n + normalsArray.size() / 3 : n - 1;
    }

    /**
     * Clears all stored model data.
     * <p>
     * After calling this method, the model becomes empty and cannot be used
     * unless repopulated by an importer.
     */
    @Override
    public void dispose() {
        triangleMeshMap.clear();
        materialMapsList.clear();
        vertexArray.clear();
        uvArray.clear();
        facesList.clear();
        smoothingGroupList.clear();
        normalsArray.clear();
        faceNormalsList.clear();
    }
}

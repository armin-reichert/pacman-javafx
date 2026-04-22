/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents a 3D model loaded from a Wavefront OBJ file.
 * <p>
 * This class stores all intermediate data extracted from the OBJ file:
 * <ul>
 *   <li>vertex positions</li>
 *   <li>texture coordinates</li>
 *   <li>normals</li>
 *   <li>faces and smoothing groups</li>
 * </ul>
 * After parsing, the model provides one or more {@link TriangleMesh} instances,
 * each optionally associated with a material map.
 * <p>
 * The class does not perform the parsing itself; it only holds the parsed data.
 * A separate OBJ importer is expected to populate the fields.
 */
public class Model3D {

    public static Model3D importObj(URL url, Charset charset) {
        final Model3D model3D = new Model3D(url);
        ObjFileParser.parse(url, charset).ifPresent(result -> {
            model3D.triangleMeshMap.putAll(result.meshMap());
            model3D.modelMaterialAssignments.putAll(result.modelMaterialAssignments());
        });
        return model3D;
    }

    public static Model3D importObj(URL url) {
        return importObj(url, StandardCharsets.UTF_8);
    }

    public static Model3D importObj(File file) throws MalformedURLException {
        return importObj(file.toURI().toURL());
    }

    /** The URL of the OBJ file this model was loaded from. */
    private final URL url;

    /**
     * Maps mesh names (OBJ object/group names) to their corresponding {@link TriangleMesh}.
     * <p>
     * OBJ files may contain multiple named objects; each becomes a separate mesh.
     */
    private final Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();

    private final Map<Mesh, PhongMaterial> modelMaterialAssignments = new HashMap<>();

    /**
     * Creates a new model representation for the OBJ file located at the given URL.
     *
     * @param url the URL of the OBJ resource; must not be {@code null}
     */
    public Model3D(URL url) {
        this.url = Objects.requireNonNull(url);
    }

    public URL url() {
        return url;
    }

    /**
     * Clears all stored model data.
     * <p>
     * After calling this method, the model becomes empty and cannot be used
     * unless repopulated by an importer.
     */
    public void dispose() {
        triangleMeshMap.clear();
    }

    public Map<String, TriangleMesh> meshMap() {
        return Collections.unmodifiableMap(triangleMeshMap);
    }

    public Optional<TriangleMesh> optMesh(String meshName) {
        requireNonNull(meshName);
        return Optional.ofNullable(triangleMeshMap.get(meshName));
    }

    public Mesh meshOrFail(String id) {
        return optMesh(id).orElseThrow(
            () -> new IllegalArgumentException("No mesh with ID '%s' exists in 3D model %s".formatted(id, url)));
    }

    public Optional<PhongMaterial> modelMaterialAssignment(Mesh mesh) {
        return Optional.ofNullable(modelMaterialAssignments.get(mesh));
    }
}

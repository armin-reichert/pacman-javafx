/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.IOException;
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

    public static Model3D importObjFile(URL url, Charset charset) throws IOException {
        return new ObjFileParser(url, charset).model3D();
    }

    public static Model3D importObjFile(URL url) throws Model3DException {
        try {
            final Model3D model3D = importObjFile(url, StandardCharsets.UTF_8);
            if (model3D == null) {
                throw new Model3DException("Could not load OBJ file");
            }
            for (TriangleMesh mesh : model3D.meshMap().values()) {
                try {
                    MeshHelper.validateTriangleMesh(mesh);
                } catch (AssertionError error) {
                    Logger.error("Invalid OBJ file data: {}, URL: '{}'", error.getMessage(), url);
                }
            }
            return model3D;
        } catch (IOException x) {
            throw new Model3DException("Could not load OBJ file", x);
        }
    }

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

    /** The URL of the OBJ file this model was loaded from. */
    final URL url;

    /**
     * Maps mesh names (OBJ object/group names) to their corresponding {@link TriangleMesh}.
     * <p>
     * OBJ files may contain multiple named objects; each becomes a separate mesh.
     */
    final Map<String, TriangleMesh> triangleMeshMap = new HashMap<>();

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
}

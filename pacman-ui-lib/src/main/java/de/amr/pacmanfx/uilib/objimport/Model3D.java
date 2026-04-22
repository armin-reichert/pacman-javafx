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
 */
public class Model3D {

    public static Model3D importObj(URL objFileURL, Charset charset) {
        final Model3D model3D = new Model3D(objFileURL);
        ObjFileParser.parse(objFileURL, charset).ifPresent(result -> {
            model3D.meshMap.putAll(result.meshMap());
            model3D.modelMaterialAssignments.putAll(result.modelMaterialAssignments());
        });
        return model3D;
    }

    public static Model3D importObj(URL objFileURL) {
        return importObj(objFileURL, StandardCharsets.UTF_8);
    }

    public static Model3D importObj(File objFile) throws MalformedURLException {
        return importObj(objFile.toURI().toURL());
    }

    private final URL url;

    private final Map<String, TriangleMesh> meshMap = new HashMap<>();

    private final Map<Mesh, PhongMaterial> modelMaterialAssignments = new HashMap<>();

    /**
     * Creates a new model representation for the OBJ file located at the given URL.
     *
     * @param url the URL of the OBJ resource; must not be {@code null}
     */
    public Model3D(URL url) {
        this.url = Objects.requireNonNull(url);
    }

    public void dispose() {
        meshMap.clear();
        modelMaterialAssignments.clear();
    }

    public URL url() {
        return url;
    }

    public Map<String, TriangleMesh> meshMap() {
        return Collections.unmodifiableMap(meshMap);
    }

    public Optional<TriangleMesh> optMesh(String meshName) {
        requireNonNull(meshName);
        return Optional.ofNullable(meshMap.get(meshName));
    }

    public Mesh meshOrFail(String meshName) {
        return optMesh(meshName).orElseThrow(
            () -> new IllegalArgumentException("No mesh with name '%s' exists in 3D model %s".formatted(meshName, url)));
    }

    public Optional<PhongMaterial> modelMaterialAssignment(Mesh mesh) {
        requireNonNull(mesh);
        return Optional.ofNullable(modelMaterialAssignments.get(mesh));
    }
}

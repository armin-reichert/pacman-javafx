/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;

public interface MeshHelper {

    static Mesh createScaledMesh(Mesh original, double scale) {
        if (!(original instanceof TriangleMesh mesh)) {
            throw new IllegalArgumentException("Only TriangleMesh supported");
        }

        final TriangleMesh copy = new TriangleMesh();
        copy.getTexCoords().addAll(mesh.getTexCoords());
        copy.getFaces().addAll(mesh.getFaces());
        copy.getFaceSmoothingGroups().addAll(mesh.getFaceSmoothingGroups());

        final float[] points = mesh.getPoints().toArray(null);
        for (int i = 0; i < points.length; i++) {
            points[i] *= (float) scale;
        }
        copy.getPoints().addAll(points);

        return copy;
    }

    static void validateTriangleMesh(TriangleMesh mesh) {
        final ObservableFloatArray points = mesh.getPoints();
        final ObservableFloatArray texCoords = mesh.getTexCoords();
        final ObservableFaceArray faces =  mesh.getFaces();

        final int numPoints = points.size() / mesh.getPointElementSize();
        if (numPoints == 0 || numPoints * mesh.getPointElementSize() != points.size()) {
            throw new AssertionError("Points array size is not correct: " + points.size());
        }

        final int numTexCoords = texCoords.size() / mesh.getTexCoordElementSize();
        if (numTexCoords == 0 || numTexCoords * mesh.getTexCoordElementSize() != texCoords.size()) {
            throw new AssertionError("Tex-Coords array size is not correct: " + points.size());
        }

        final int numFaces = faces.size() / mesh.getFaceElementSize();
        if (numFaces == 0 || numFaces * mesh.getFaceElementSize() != faces.size()) {
            throw new AssertionError("Faces array size is not correct: " + points.size());
        }
        if (numFaces != mesh.getFaceSmoothingGroups().size() && mesh.getFaceSmoothingGroups().size() > 0) {
            throw new AssertionError(
                "FaceSmoothingGroups array size is not correct: " + points.size() + ", numFaces = " + numFaces);
        }

        for (int i = 0; i < faces.size(); i += 2) {
            final int pointIndex = faces.get(i);
            if (pointIndex < 0 || pointIndex > numPoints) {
                throw new AssertionError("Incorrect point index: " + pointIndex + ", numPoints = " + numPoints);
            }
            final int texCoordIndex = faces.get(i + 1);
            if (texCoordIndex < 0 || texCoordIndex > numTexCoords) {
                throw new AssertionError("Incorrect texture coordinate index: " + texCoordIndex + ", numTexCoords = " + numTexCoords);
            }
        }
    }
}

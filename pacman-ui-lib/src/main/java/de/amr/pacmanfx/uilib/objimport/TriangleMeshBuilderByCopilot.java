/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.paint.PhongMaterial;

import java.util.*;

public class TriangleMeshBuilderByCopilot {

    private final ObjFileParserByCopilot.ObjModel model;
    private final Map<String, PhongMaterial> materials;

    public TriangleMeshBuilderByCopilot(
        ObjFileParserByCopilot.ObjModel model,
        Map<String, Map<String, PhongMaterial>> materialLibs) {

        this.model = model;

        // Flatten all material libraries into one map
        Map<String, PhongMaterial> flat = new HashMap<>();
        for (var lib : materialLibs.values()) {
            flat.putAll(lib);
        }
        this.materials = flat;
    }

    /**
     * Builds one MeshView per material.
     */
    public List<MeshView> buildMeshes() {
        Map<String, List<ObjFileParserByCopilot.ObjFace>> facesByMaterial = groupFacesByMaterial();

        List<MeshView> result = new ArrayList<>();

        for (var entry : facesByMaterial.entrySet()) {
            String materialName = entry.getKey();
            List<ObjFileParserByCopilot.ObjFace> faces = entry.getValue();

            TriangleMesh mesh = buildMeshForFaces(faces);
            MeshView view = new MeshView(mesh);

            PhongMaterial mat = materials.get(materialName);
            if (mat != null) {
                view.setMaterial(mat);
            }

            result.add(view);
        }

        return result;
    }

    /* -------------------------------------------------------------
     *  GROUP FACES BY MATERIAL
     * ------------------------------------------------------------- */

    private Map<String, List<ObjFileParserByCopilot.ObjFace>> groupFacesByMaterial() {
        Map<String, List<ObjFileParserByCopilot.ObjFace>> map = new HashMap<>();

        for (ObjFileParserByCopilot.ObjObject obj : model.objects) {
            for (ObjFileParserByCopilot.ObjGroup group : obj.groups) {
                for (ObjFileParserByCopilot.ObjFace face : group.faces) {
                    String mat = face.materialName;
                    map.computeIfAbsent(mat, k -> new ArrayList<>()).add(face);
                }
            }
        }

        return map;
    }

    /* -------------------------------------------------------------
     *  BUILD A TRIANGLEMESH FOR A SET OF FACES
     * ------------------------------------------------------------- */

    private TriangleMesh buildMeshForFaces(List<ObjFileParserByCopilot.ObjFace> faces) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> facesIdx = new ArrayList<>();

        // We need to deduplicate vertices because JavaFX TriangleMesh
        // does not allow arbitrary indexing like OBJ does.
        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (ObjFileParserByCopilot.ObjFace face : faces) {
            for (ObjFileParserByCopilot.FaceVertex fv : face.vertices) {

                VertexKey key = new VertexKey(fv.vIndex, fv.vtIndex, fv.vnIndex);

                int newIndex = vertexMap.computeIfAbsent(key, k -> {
                    // Add vertex position
                    ObjFileParserByCopilot.Vertex v = model.vertices.get(k.v);
                    points.add(v.x());
                    points.add(v.y());
                    points.add(v.z());

                    // Add UV (or dummy)
                    if (k.vt >= 0) {
                        ObjFileParserByCopilot.TexCoord tc = model.texCoords.get(k.vt);
                        texCoords.add(tc.u());
                        texCoords.add(1 - tc.v()); // Flip V for JavaFX
                    } else {
                        texCoords.add(0f);
                        texCoords.add(0f);
                    }

                    return (points.size() / 3) - 1;
                });

                // JavaFX face index format: vertexIndex, texCoordIndex
                facesIdx.add(newIndex);
                facesIdx.add(newIndex);
            }
        }

        mesh.getPoints().setAll(toFloatArray(points));
        mesh.getTexCoords().setAll(toFloatArray(texCoords));
        mesh.getFaces().setAll(toIntArray(facesIdx));

        return mesh;
    }

    /* -------------------------------------------------------------
     *  HELPERS
     * ------------------------------------------------------------- */

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    /**
     * A unique key for a vertex/uv/normal combination.
     */
    private record VertexKey(int v, int vt, int vn) {}
}

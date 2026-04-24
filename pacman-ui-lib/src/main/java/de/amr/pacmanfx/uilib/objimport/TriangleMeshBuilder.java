/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriangleMeshBuilder {

    private final ObjFileParser.ObjModel model;
    private final Map<String, PhongMaterial> materials;

    public TriangleMeshBuilder(
        ObjFileParser.ObjModel model,
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
    public Map<String, MeshView> buildMeshViewsByMaterial() {
        Map<String, List<ObjFileParser.ObjFace>> facesByMaterial = groupFacesByMaterial();
        Map<String, MeshView> meshViews = new HashMap<>();

        for (var entry : facesByMaterial.entrySet()) {
            String materialName = entry.getKey();
            List<ObjFileParser.ObjFace> faces = entry.getValue();

            TriangleMesh mesh = buildMeshForFaces(faces);
            MeshView view = new MeshView(mesh);

            PhongMaterial mat = materials.get(materialName);
            if (mat != null) {
                view.setMaterial(mat);
            }

            meshViews.put(materialName, view);
        }

        return meshViews;
    }

    /* -------------------------------------------------------------
     *  GROUP FACES BY MATERIAL
     * ------------------------------------------------------------- */

    private Map<String, List<ObjFileParser.ObjFace>> groupFacesByMaterial() {
        Map<String, List<ObjFileParser.ObjFace>> map = new HashMap<>();

        for (ObjFileParser.ObjObject obj : model.objects) {
            for (ObjFileParser.ObjGroup group : obj.groups) {
                for (ObjFileParser.ObjFace face : group.faces) {
                    String mat = face.materialName;
                    map.computeIfAbsent(mat, _ -> new ArrayList<>()).add(face);
                }
            }
        }

        return map;
    }

    /* -------------------------------------------------------------
     *  BUILD A TRIANGLEMESH FOR A SET OF FACES
     * ------------------------------------------------------------- */

    private TriangleMesh buildMeshForFaces(List<ObjFileParser.ObjFace> faces) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> facesIdx = new ArrayList<>();

        // We need to deduplicate vertices because JavaFX TriangleMesh
        // does not allow arbitrary indexing like OBJ does.
        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (ObjFileParser.ObjFace face : faces) {
            for (ObjFileParser.FaceVertex fv : face.vertices) {

                VertexKey key = new VertexKey(fv.vIndex, fv.vtIndex, fv.vnIndex);

                int newIndex = vertexMap.computeIfAbsent(key, k -> {
                    // Add vertex position
                    ObjFileParser.Vertex v = model.vertices.get(k.v);
                    points.add(v.x());
                    points.add(v.y());
                    points.add(v.z());

                    // Add UV (or dummy)
                    if (k.vt >= 0) {
                        ObjFileParser.TexCoord tc = model.texCoords.get(k.vt);
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

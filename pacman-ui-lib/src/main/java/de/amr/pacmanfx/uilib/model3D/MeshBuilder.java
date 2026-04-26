/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.objparser.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class MeshBuilder {

    public enum BuildMode {
        BY_GROUP,
        BY_OBJECT,
        BY_MATERIAL
    }

    private final ObjModel model;
    private final Map<String, PhongMaterial> materials;

    public MeshBuilder(ObjModel model) {
        this.model = requireNonNull(model);

        // Flatten material libraries
        materials = new HashMap<>();
        for (Map<String, ObjMaterial> lib : model.materialLibsMap().values()) {
            lib.forEach((name, material) -> materials.put(name, createPhongMaterial(material)));
        }
    }

    /* -------------------------------------------------------------
     *  PUBLIC API
     * ------------------------------------------------------------- */

    public static Map<String, MeshView> build(ObjModel objModel, BuildMode mode) {
        return new MeshBuilder(objModel).build(mode);
    }

    public Map<String, MeshView> build(BuildMode mode) {
        return switch (mode) {
            case BY_GROUP -> buildMeshViewsByGroup();
            case BY_OBJECT -> buildMeshViewsByObject();
            case BY_MATERIAL -> buildMeshViewsByMaterial();
        };
    }

    /**
     * Builds one MeshView per OBJ group.
     * Key format: "objectName.groupName"
     */
    public Map<String, MeshView> buildMeshViewsByGroup() {
        Map<String, MeshView> result = new LinkedHashMap<>();

        for (ObjObject obj : model.objects()) {
            for (ObjGroup group : obj.groups) {

                String key = obj.name + "." + group.name;

                MeshView mv = buildMeshViewForFaces(group.faces);
                mv.setId(key);

                result.put(key, mv);
            }
        }

        return result;
    }

    /**
     * Builds one MeshView per OBJ object.
     * All groups inside the object are merged.
     * Key format: "objectName"
     */
    public Map<String, MeshView> buildMeshViewsByObject() {
        Map<String, MeshView> result = new LinkedHashMap<>();

        for (ObjObject obj : model.objects()) {

            List<ObjFace> allFaces = new ArrayList<>();
            for (ObjGroup group : obj.groups) {
                allFaces.addAll(group.faces);
            }

            MeshView mv = buildMeshViewForFaces(allFaces);
            mv.setId(obj.name);

            result.put(obj.name, mv);
        }

        return result;
    }

    /**
     * Builds one MeshView per material.
     * Key format: "materialName"
     */
    public Map<String, MeshView> buildMeshViewsByMaterial() {
        Map<String, List<ObjFace>> facesByMaterial = new LinkedHashMap<>();

        for (ObjObject obj : model.objects()) {
            for (ObjGroup group : obj.groups) {
                for (ObjFace face : group.faces) {
                    facesByMaterial
                        .computeIfAbsent(face.materialName, _ -> new ArrayList<>())
                        .add(face);
                }
            }
        }

        Map<String, MeshView> result = new LinkedHashMap<>();

        for (var entry : facesByMaterial.entrySet()) {
            String matName = entry.getKey();
            List<ObjFace> faces = entry.getValue();

            MeshView mv = buildMeshViewForFaces(faces);
            mv.setId(matName);

            if (materials.containsKey(matName)) {
                mv.setMaterial(materials.get(matName));
            }

            result.put(matName, mv);
        }

        return result;
    }

    /* -------------------------------------------------------------
     *  MESH BUILDING
     * ------------------------------------------------------------- */

    private MeshView buildMeshViewForFaces(List<ObjFace> faces) {
        TriangleMesh mesh = buildMeshForFaces(faces);
        MeshView mv = new MeshView(mesh);

        // Assign material if all faces share one
        String mat = faces.getFirst().materialName;
        if (mat != null && materials.containsKey(mat)) {
            mv.setMaterial(materials.get(mat));
        }

        return mv;
    }

    private TriangleMesh buildMeshForFaces(List<ObjFace> faces) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> facesIdx = new ArrayList<>();
        List<Integer> smoothing = new ArrayList<>();

        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (ObjFace face : faces) {
            for (ObjFaceVertex fv : face.vertices) {

                VertexKey key = new VertexKey(fv.vIndex, fv.vtIndex, fv.vnIndex);

                int newIndex = vertexMap.computeIfAbsent(key, k -> {
                    ObjVertex v = model.vertices().get(k.v);
                    points.add(v.x());
                    points.add(v.y());
                    points.add(v.z());

                    if (k.vt >= 0) {
                        ObjTexCoord tc = model.texCoords().get(k.vt);
                        texCoords.add(tc.u());
                        texCoords.add(1 - tc.v());
                    } else {
                        texCoords.add(0f);
                        texCoords.add(0f);
                    }

                    return (points.size() / 3) - 1;
                });

                facesIdx.add(newIndex);
                facesIdx.add(newIndex);
            }

            int sg = face.smoothingGroup != null ? (1 << face.smoothingGroup) : 0;
            smoothing.add(sg);
        }

        mesh.getPoints().setAll(toFloatArray(points));
        mesh.getTexCoords().setAll(toFloatArray(texCoords));
        mesh.getFaces().setAll(toIntArray(facesIdx));
        mesh.getFaceSmoothingGroups().setAll(toIntArray(smoothing));

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

    private record VertexKey(int v, int vt, int vn) {}

    private static PhongMaterial createPhongMaterial(ObjMaterial objMaterial) {
        PhongMaterial phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseColor(fxColor(objMaterial.diffuseColor(), objMaterial.opacity()));
        phongMaterial.setSpecularColor(fxColor(objMaterial.specularColor(), objMaterial.opacity()));
        phongMaterial.setSpecularPower(objMaterial.specularExponent());
        return phongMaterial;
    }

    private static Color fxColor(ObjColor objColor, double opacity) {
        return Color.color(objColor.red(), objColor.green(), objColor.blue(), opacity);
    }
}

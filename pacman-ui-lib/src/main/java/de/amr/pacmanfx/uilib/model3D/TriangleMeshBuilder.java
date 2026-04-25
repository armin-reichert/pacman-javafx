/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.objimport.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.util.*;

/**
 * Builds one MeshView per OBJ group.
 * No JavaFX Group nodes are created.
 */
public class TriangleMeshBuilder {

    private final ObjModel model;
    private final Map<String, PhongMaterial> materials;

    public TriangleMeshBuilder(ObjModel model) {
        this.model = model;

        // Flatten material libraries
        Map<String, PhongMaterial> materialMap = new HashMap<>();
        for (Map<String, ObjMaterial> lib : model.materialLibsMap().values()) {
            lib.forEach((name, material) -> materialMap.put(name, createPhongMaterial(material)));
        }
        this.materials = materialMap;
    }

    /* -------------------------------------------------------------
     *  PUBLIC API
     * ------------------------------------------------------------- */

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
                    // Position
                    ObjVertex v = model.vertices().get(k.v);
                    points.add(v.x());
                    points.add(v.y());
                    points.add(v.z());

                    // UV
                    if (k.vt >= 0) {
                        ObjTexCoord tc = model.texCoords().get(k.vt);
                        texCoords.add(tc.u());
                        texCoords.add(1 - tc.v()); // JavaFX V-flip
                    } else {
                        texCoords.add(0f);
                        texCoords.add(0f);
                    }

                    return (points.size() / 3) - 1;
                });

                // JavaFX face format: vertexIndex, texCoordIndex
                facesIdx.add(newIndex);
                facesIdx.add(newIndex);
            }

            // Smoothing group → JavaFX bitmask
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

    // Note: Copilot says that for transparent colors to work, the mesh view must have:
    // meshView.setCullFace(CullFace.NONE);
    // meshView.setDrawMode(DrawMode.FILL);
    // meshView.setDepthTest(DepthTest.ENABLE);
    // meshView.setBlendMode(BlendMode.SRC_OVER);
    private static PhongMaterial createPhongMaterial(ObjMaterial objMaterial) {
        if (objMaterial.illum() != ObjMaterial.DEFAULT_ILLUMINATION) {
            Logger.warn("{}: Illumination value {} will be ignored", objMaterial.name(), objMaterial.illum());
        }
        if (!objMaterial.ka().equals(ObjMaterial.DEFAULT_AMBIENT_COLOR)) {
            Logger.warn("{}: Ambient Color value {} will be ignored", objMaterial.name(), objMaterial.ka());
        }
        if (!objMaterial.ke().equals(ObjMaterial.DEFAULT_EMISSIVE_COLOR)) {
            Logger.warn("{}: Emissive Color value {} will be ignored", objMaterial.name(), objMaterial.ke());
        }
        if (objMaterial.ni() != ObjMaterial.DEFAULT_REFRACTION_INDEX) {
            Logger.warn("{}: Refraction Index value {} will be ignored", objMaterial.name(), objMaterial.ni());
        }
        final var phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseColor(fxColor(objMaterial.kd(), objMaterial.d()));
        phongMaterial.setSpecularColor(fxColor(objMaterial.ks(), objMaterial.d()));
        phongMaterial.setSpecularPower(objMaterial.ns());
        return phongMaterial;
    }

    private static Color fxColor(ColorRGB colorRGB, double opacity) {
        return Color.color(colorRGB.red(), colorRGB.green(), colorRGB.blue(), opacity);
    }
}

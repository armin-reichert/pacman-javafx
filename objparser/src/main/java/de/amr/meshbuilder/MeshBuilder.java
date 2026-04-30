/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshbuilder;

import de.amr.objparser.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

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
        for (Map<String, ObjMaterial> lib : model.materialLibsMap.values()) {
            lib.forEach((name, material) -> materials.put(name, createPhongMaterial(material)));
        }
    }

    /* -------------------------------------------------------------
     *  PUBLIC API
     * ------------------------------------------------------------- */

    public static Map<String, MeshView> build(ObjModel objModel, BuildMode mode) {
        requireNonNull(objModel);
        requireNonNull(mode);
        return new MeshBuilder(objModel).build(mode);
    }

    public Map<String, MeshView> build(BuildMode mode) {
        requireNonNull(mode);
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

        for (ObjObject obj : model.objects) {
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

        for (ObjObject obj : model.objects) {

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

        for (ObjObject obj : model.objects) {
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

        if (!faces.isEmpty()) {
            // Assign material if all faces share one
            String mat = faces.getFirst().materialName;
            if (mat != null && materials.containsKey(mat)) {
                mv.setMaterial(materials.get(mat));
            }
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

                VertexKey key = new VertexKey(fv.vIndex(), fv.vtIndex(), fv.vnIndex());

                int newIndex = vertexMap.computeIfAbsent(key, k -> {

                    // --- READ VERTEX FROM FASTUTIL FLOAT ARRAY ---
                    int vIndex = k.v * 3;
                    float vx = model.vertices.getFloat(vIndex);
                    float vy = model.vertices.getFloat(vIndex + 1);
                    float vz = model.vertices.getFloat(vIndex + 2);

                    points.add(vx);
                    points.add(vy);
                    points.add(vz);

                    // --- READ TEXCOORD FROM FASTUTIL FLOAT ARRAY ---
                    if (k.vt >= 0) {
                        int tIndex = k.vt * 2;
                        float u = model.texCoords.getFloat(tIndex);
                        float v = model.texCoords.getFloat(tIndex + 1);

                        texCoords.add(u);
                        texCoords.add(1 - v); // JavaFX UV flip
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

    private PhongMaterial createPhongMaterial(ObjMaterial m) {
        PhongMaterial fx = new PhongMaterial();

        /* ---------------------------------------------------------
         * 1. Diffuse color + opacity
         * --------------------------------------------------------- */
        fx.setDiffuseColor(fxColor(m.Kd, m.d));

        /* ---------------------------------------------------------
         * 2. Specular color + shininess
         * --------------------------------------------------------- */
        fx.setSpecularColor(fxColor(m.Ks, 1.0));
        fx.setSpecularPower(m.Ns);

        /* ---------------------------------------------------------
         * 3. Emissive color (JavaFX 17+)
         * --------------------------------------------------------- */
        try {
            fx.getClass().getMethod("setSelfIlluminationMap", Image.class);
            fx.setSelfIlluminationMap(null); // default
            if (!m.Ke.isBlack()) {
                // JavaFX does not support emissive color directly,
                // but we can approximate by tinting diffuse color slightly.
                Color base = fx.getDiffuseColor();
                Color glow = Color.color(
                    clamp01(base.getRed()   + m.Ke.red()   * 0.2),
                    clamp01(base.getGreen() + m.Ke.green() * 0.2),
                    clamp01(base.getBlue()  + m.Ke.blue()  * 0.2),
                    base.getOpacity()
                );
                fx.setDiffuseColor(glow);
            }
        } catch (NoSuchMethodException ignore) {
            // Running on JavaFX < 17 → emissive unsupported
        }

        /* ---------------------------------------------------------
         * 4. Diffuse texture
         * --------------------------------------------------------- */
        if (m.map_Kd != null) {
            fx.setDiffuseMap(loadTexture(m.map_Kd));
        }

        /* ---------------------------------------------------------
         * 5. Specular texture
         * --------------------------------------------------------- */
        if (m.map_Ks != null) {
            fx.setSpecularMap(loadTexture(m.map_Ks));
        }

        /* ---------------------------------------------------------
         * 6. Bump / normal map
         * --------------------------------------------------------- */
        if (m.map_bump != null) {
            fx.setBumpMap(loadTexture(m.map_bump));
        }

        /* ---------------------------------------------------------
         * 7. Opacity map (map_d)
         * --------------------------------------------------------- */
        if (m.map_d != null) {
            fx.setDiffuseMap(loadTexture(m.map_d)); // JavaFX uses alpha channel
        }

        /* ---------------------------------------------------------
         * 8. Emissive texture (JavaFX 17+)
         * --------------------------------------------------------- */
        if (m.map_Ke != null) {
            try {
                fx.setSelfIlluminationMap(loadTexture(m.map_Ke));
            } catch (Throwable ignore) {
                // JavaFX < 17
            }
        }

        return fx;
    }

    private static Color fxColor(ObjColor c, double opacity) {
        return Color.color(c.red(), c.green(), c.blue(), opacity);
    }

    private static double clamp01(double v) {
        return Math.clamp(v, 0, 1);
    }

    private Image loadTexture(String filename) {
        try {
            String fileURL = replaceFileName(model.url(), filename);
            Logger.debug("Try to open texture file: " + fileURL);
            var texture = new Image(fileURL);
            Logger.debug("Texture loaded: {}", texture);
            return texture;
        } catch (Exception e) {
            Logger.warn("Failed to load texture '{}': {}", filename, e.getMessage());
            return null;
        }
    }

    private String replaceFileName(String url, String filename) {
        int lastSlash = url.lastIndexOf('/');
        return url.substring(0, lastSlash) + "/" + filename;
    }
}

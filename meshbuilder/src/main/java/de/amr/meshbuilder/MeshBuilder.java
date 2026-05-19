/*
 * Copyright (c) 2026 Armin Reichert (MIT License)
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
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Creates JavaFX mesh views and Phong materials from the parsed Wavefront file data.
 */
public class MeshBuilder {

    private static final Predicate<String> INCLUDE_ALL = _ -> true;

    /**
     * Mesh builder mode to specify for which entities the mesh views should be created.
     */
    public enum BuildMode {
        /** Build mesh views for OBJ groups */
        BY_GROUP,
        /** Build mesh views for OBJ objects */
        BY_OBJECT,
        /** Build mesh views for OBJ material usages */
        BY_MATERIAL
    }

    private final ObjModel model;
    private final Map<String, PhongMaterial> materials;

    /**
     * Creates a mesh builder.
     * @param model the OBJ data model
     */
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

    /**
     * Builds all mesh views and materials for the specified category of entities from the Wavefront OBJ file.
     *
     * @param objModel the data model of the OBJ file
     * @param mode the mesh creation mode (objects, groups or materials)
     * @return a map of mesh views for the given category
     */
    public static Map<String, MeshView> build(ObjModel objModel, BuildMode mode) {
        requireNonNull(objModel);
        requireNonNull(mode);
        final var meshBuilder = new MeshBuilder(objModel);
        return switch (mode) {
            case BY_GROUP -> meshBuilder.buildMeshViewsByGroup();
            case BY_OBJECT -> meshBuilder.buildMeshViewsByObject();
            case BY_MATERIAL -> meshBuilder.buildMeshViewsByMaterial();
        };
    }

    /**
     * @return unmodifiable map of Phong materials created from the parsed OBJ file
     */
    public Map<String, PhongMaterial> materials() {
        return Collections.unmodifiableMap(materials);
    }

    /**
     * Builds one MeshView per OBJ group.
     * <p>
     * Key format: "objectName.groupName".
     *
     * @param included determines for which IDs mesh views should be created.
     * @return map with mesh views for OBJ groups
     */
    public Map<String, MeshView> buildMeshViewsByGroup(Predicate<String> included) {
        requireNonNull(included);
        final Map<String, MeshView> result = new LinkedHashMap<>();
        for (ObjObject obj : model.objects) {
            for (ObjGroup group : obj.groups) {
                final String meshID = obj.name + "." + group.name;
                if (!included.test(meshID)) {
                    continue;
                }
                final MeshView meshView = buildMeshViewForFaces(group.faces);
                meshView.setId(meshID);
                result.put(meshID, meshView);
            }
        }

        return result;
    }

    /**
     * Builds one MeshView per OBJ group.
     * <p>
     * Key format: "objectName.groupName".
     *
     * @return map with mesh views for OBJ groups
     */
    public Map<String, MeshView> buildMeshViewsByGroup() {
        return buildMeshViewsByGroup(INCLUDE_ALL);
    }

    /**
     * Builds one MeshView per OBJ object.
     * All groups inside the object are merged.
     * <p>
     * Key format: "objectName".
     *
     * @param included determines for which IDs mesh views should be created.
     * @return map with mesh views for OBJ objects
     */
    public Map<String, MeshView> buildMeshViewsByObject(Predicate<String> included) {
        final Map<String, MeshView> result = new LinkedHashMap<>();
        for (ObjObject obj : model.objects) {
            final String meshID = obj.name;
            if (!included.test(meshID)) {
                continue;
            }
            final List<ObjFace> allFaces = new ArrayList<>();
            for (ObjGroup group : obj.groups) {
                allFaces.addAll(group.faces);
            }
            final MeshView meshView = buildMeshViewForFaces(allFaces);
            meshView.setId(meshID);
            result.put(meshID, meshView);
        }
        return result;
    }

    /**
     * Builds one MeshView per OBJ object.
     * All groups inside the object are merged.
     * <p>
     * Key format: "objectName". Spaces in names from OBJ file are replaced by underscores.
     *
     * @return map with mesh views for OBJ objects
     */
    public Map<String, MeshView> buildMeshViewsByObject() {
        return buildMeshViewsByObject(INCLUDE_ALL);
    }

    /**
     * Builds one MeshView per material.
     * Key format: "materialName"
     *
     * @param included determines for which IDs mesh views should be created.
     * @return map with mesh views for OBJ materials
     */
    public Map<String, MeshView> buildMeshViewsByMaterial(Predicate<String> included) {
        final Map<String, List<ObjFace>> facesByMaterial = new LinkedHashMap<>();
        for (ObjObject obj : model.objects) {
            for (ObjGroup group : obj.groups) {
                for (ObjFace face : group.faces) {
                    final String key = face.materialName;
                    facesByMaterial.computeIfAbsent(key, _ -> new ArrayList<>()).add(face);
                }
            }
        }
        final Map<String, MeshView> result = new LinkedHashMap<>();
        for (var entry : facesByMaterial.entrySet()) {
            final String materialName = entry.getKey();
            final List<ObjFace> faces = entry.getValue();
            if (!included.test(materialName)) {
                continue;
            }
            final MeshView meshView = buildMeshViewForFaces(faces);
            meshView.setId(materialName);
            meshView.setMaterial(materials.get(materialName));
            result.put(materialName, meshView);
        }
        return result;
    }

    /**
     * Builds one MeshView per material.
     * Key format: "materialName"
     *
     * @return map with mesh views for OBJ materials
     */
    public Map<String, MeshView> buildMeshViewsByMaterial() {
        return buildMeshViewsByMaterial(INCLUDE_ALL);
    }

    /* -------------------------------------------------------------
     *  MESH BUILDING
     * ------------------------------------------------------------- */

    private MeshView buildMeshViewForFaces(List<ObjFace> faces) {
        final TriangleMesh mesh = buildMeshForFaces(faces);
        MeshView meshView = new MeshView(mesh);

        if (!faces.isEmpty()) {
            // Assign material if all faces share one
            final String matName = faces.getFirst().materialName;
            if (materials.containsKey(matName)) {
                meshView.setMaterial(materials.get(matName));
            }
        }

        return meshView;
    }

    private TriangleMesh buildMeshForFaces(List<ObjFace> faces) {
        final TriangleMesh mesh = new TriangleMesh();

        final List<Float> points = new ArrayList<>();
        final List<Float> texCoords = new ArrayList<>();
        final List<Integer> facesIdx = new ArrayList<>();
        final List<Integer> smoothing = new ArrayList<>();

        final Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (ObjFace face : faces) {
            for (ObjFaceVertex fv : face.vertices) {

                final VertexKey key = new VertexKey(fv.vIndex(), fv.vtIndex(), fv.vnIndex());

                final int newIndex = vertexMap.computeIfAbsent(key, k -> {

                    // --- READ VERTEX FROM FLOAT ARRAY ---
                    final int vIndex = k.v * 3;
                    final float vx = model.vertices.get(vIndex);
                    final float vy = model.vertices.get(vIndex + 1);
                    final float vz = model.vertices.get(vIndex + 2);

                    points.add(vx);
                    points.add(vy);
                    points.add(vz);

                    // --- READ TEXCOORD FROM FLOAT ARRAY ---
                    if (k.vt >= 0) {
                        final int tIndex = k.vt * 2;
                        final float u = model.texCoords.get(tIndex);
                        final float v = model.texCoords.get(tIndex + 1);

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

            final int sg = face.smoothingGroup != null ? (1 << face.smoothingGroup) : 0;
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

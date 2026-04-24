package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.*;

/**
 * Builds one MeshView per OBJ group.
 * No JavaFX Group nodes are created.
 */
public class TriangleMeshBuilder {

    private final ObjFileParser.ObjModel model;
    private final Map<String, PhongMaterial> materials;

    public TriangleMeshBuilder(ObjFileParser.ObjModel model) {
        this.model = model;

        // Flatten material libraries
        Map<String, PhongMaterial> flat = new HashMap<>();
        for (var lib : model.materialLibsMap.values()) {
            flat.putAll(lib);
        }
        this.materials = flat;
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

        for (ObjFileParser.ObjObject obj : model.objects) {
            for (ObjFileParser.ObjGroup group : obj.groups) {

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

    private MeshView buildMeshViewForFaces(List<ObjFileParser.ObjFace> faces) {
        TriangleMesh mesh = buildMeshForFaces(faces);
        MeshView mv = new MeshView(mesh);

        // Assign material if all faces share one
        String mat = faces.getFirst().materialName;
        if (mat != null && materials.containsKey(mat)) {
            mv.setMaterial(materials.get(mat));
        }

        return mv;
    }

    private TriangleMesh buildMeshForFaces(List<ObjFileParser.ObjFace> faces) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> facesIdx = new ArrayList<>();
        List<Integer> smoothing = new ArrayList<>();

        Map<VertexKey, Integer> vertexMap = new HashMap<>();

        for (ObjFileParser.ObjFace face : faces) {
            for (ObjFileParser.FaceVertex fv : face.vertices) {

                VertexKey key = new VertexKey(fv.vIndex, fv.vtIndex, fv.vnIndex);

                int newIndex = vertexMap.computeIfAbsent(key, k -> {
                    // Position
                    ObjFileParser.Vertex v = model.vertices.get(k.v);
                    points.add(v.x());
                    points.add(v.y());
                    points.add(v.z());

                    // UV
                    if (k.vt >= 0) {
                        ObjFileParser.TexCoord tc = model.texCoords.get(k.vt);
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
}

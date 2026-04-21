/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static de.amr.pacmanfx.uilib.objimport.SmoothingGroups.computeSmoothingGroups;
import static java.util.Objects.requireNonNull;

/**
 * Parses Wavefront OBJ files. Not a full fledged implementation but just good enough for my purposes.
 *
 * <p>Code derived from the OBJ importer from the 3DViewer sample project (Oracle).
 *
 * @see <a href=
 * "https://github.com/teamfx/openjfx-10-dev-rt/tree/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers">3DViewer
 * Sample</a>
 */
public class ObjFileParser {

    private enum Keyword {
        FACE            ("f"),
        GROUP           ("g"),
        MATERIAL_LIB    ("mtllib"),
        OBJECT          ("o"),
        SMOOTHING_GROUP ("s"),
        USE_MATERIAL    ("usemtl"),
        VERTEX          ("v"),
        VERTEX_NORMAL   ("vn"),
        TEX_COORD       ("vt");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }
    }

    private static String[] splitBySpace(String line) {
        return line.trim().split("\\s+");
    }

    private static int[] toIntArray(List<Integer> list) {
        final int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
        // Perhaps more elegant, but not as performant:
        // return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private static float[] toFloatArray(ObservableFloatArray ofa) {
        final float[] arr = new float[ofa.size()];
        ofa.copyTo(0, arr, 0, ofa.size());
        return arr;
        // Perhaps more elegant, but not as performant:
        // return ofa.toArray(new float[ofa.size()]);
    }

    private static List<Integer> restOfList(ArrayList<Integer> list, int start) {
        return list.subList(start, list.size());
    }

    private boolean fullMatch(String line, Keyword cmd) {
        return line.equals(cmd.text);
    }

    private boolean prefixMatch(String line, Keyword cmd) {
        return line.startsWith(cmd.text + " ");
    }

    private static String restOf(String line, Keyword cmd) {
        return line.substring(cmd.text.length() + 1).trim();
    }

    // fields

    private final Map<String, TriangleMesh> meshMap = new HashMap<>();

    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    private int anonMeshNameCount = 0;
    private String meshName;

    private int currentSmoothingGroup = 0;

    /** Flat array of vertex coordinates (x, y, z). */
    private final ObservableFloatArray vertexArray = FXCollections.observableFloatArray();

    /** Flat array of texture coordinates (u, v). */
    private final ObservableFloatArray uvArray = FXCollections.observableFloatArray();

    /** Face index list (vertex/uv/normal indices). */
    private final ArrayList<Integer> facesList = new ArrayList<>();

    /** Smoothing group indices for each face. */
    private final ArrayList<Integer> smoothingGroupList = new ArrayList<>();

    /** Flat array of vertex normals (nx, ny, nz). */
    private final ObservableFloatArray normalsArray = FXCollections.observableFloatArray();

    /** Normal indices for each face. */
    private final ArrayList<Integer> faceNormalsList = new ArrayList<>();


    public ObjFileParser(URL url, Charset charset) throws IOException {
        requireNonNull(url);
        requireNonNull(charset);
        try (InputStream is = url.openStream()) {
            final var reader = new BufferedReader(new InputStreamReader(is, charset));
            parse(reader);
        }
    }

    public Map<String, TriangleMesh> meshMap() {
        return Collections.unmodifiableMap(meshMap);
    }

    private void commitPendingMesh() {
        TriangleMesh mesh = createTriangleMesh();
        if (mesh != null) {
            if (meshName == null) {
                meshName = nextAnonMeshName();
            }
            meshMap.put(meshName, mesh);
            Logger.info("Mesh '{}', vertices: {}, texture coordinates: {}, faces: {}, smoothing groups: {}",
                meshName,
                mesh.getPoints().size() / mesh.getPointElementSize(),
                mesh.getTexCoords().size() / mesh.getTexCoordElementSize(),
                mesh.getFaces().size() / mesh.getFaceElementSize(),
                mesh.getFaceSmoothingGroups().size());
        }
    }

    private String nextAnonMeshName() {
        return "default" + anonMeshNameCount++;
    }

    private void parse(BufferedReader reader) throws IOException {
        String statement;
        while ((statement = reader.readLine()) != null) {
            if (statement.isBlank() || statement.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (prefixMatch(statement, Keyword.FACE)) {
                parseFace(restOf(statement, Keyword.FACE));
            }
            else if (fullMatch(statement, Keyword.GROUP)) {
                commitPendingMesh();
                meshName = nextAnonMeshName();
            }
            else if (prefixMatch(statement, Keyword.GROUP)) {
                commitPendingMesh();
                meshName = restOf(statement, Keyword.GROUP);
            }
            else if (prefixMatch(statement, Keyword.MATERIAL_LIB)) {
                // we don't use material library definitions defined in the OBJ file
                final String libraryName = restOf(statement, Keyword.MATERIAL_LIB);
                Logger.info("Material library definition '{}' ignored", libraryName);
            }
            else if (fullMatch(statement, Keyword.OBJECT)) {
                commitPendingMesh();
                meshName = nextAnonMeshName();
            }
            else if (prefixMatch(statement, Keyword.OBJECT)) {
                commitPendingMesh();
                meshName = restOf(statement, Keyword.OBJECT);
            }
            else if (prefixMatch(statement, Keyword.SMOOTHING_GROUP)) {
                parseSmoothingGroup(restOf(statement, Keyword.SMOOTHING_GROUP));
            }
            else if (prefixMatch(statement, Keyword.USE_MATERIAL)) {
                commitPendingMesh();
                final String materialName = restOf(statement, Keyword.USE_MATERIAL);
                Logger.trace("Material usage '{}' ignored", materialName);
            }
            else if (prefixMatch(statement, Keyword.VERTEX)) {
                parseVertex(restOf(statement, Keyword.VERTEX));
            }
            else if (prefixMatch(statement, Keyword.VERTEX_NORMAL)) {
                parseVertexNormal(restOf(statement, Keyword.VERTEX_NORMAL));
            }
            else if (prefixMatch(statement, Keyword.TEX_COORD)) {
                parseTextureCoordinate(restOf(statement, Keyword.TEX_COORD));
            }
            else {
                Logger.warn("Line skipped: {} (no idea what it wants from me)", statement);
            }
        }
        commitPendingMesh();
    }

    /**
     * Face definition. Example:
     * <pre>f 723/564/3004 731/555/3034 732/554/3037 724/563/3007</pre>
     */
    private void parseFace(String argText) {
        String[] blocks = splitBySpace(argText);
        int[][] triplets = new int[blocks.length][];
        boolean uvProvided = true;
        boolean normalProvided = true;
        for (int i = 0; i < blocks.length; i++) {
            String[] points = blocks[i].split("/");
            if (points.length < 2) {
                uvProvided = false;
            }
            if (points.length < 3) {
                normalProvided = false;
            }
            triplets[i] = new int[points.length];
            for (int j = 0; j < points.length; j++) {
                if (points[j].isEmpty()) {
                    triplets[i][j] = 0;
                    if (j == 1) {
                        uvProvided = false;
                    }
                    if (j == 2) {
                        normalProvided = false;
                    }
                } else {
                    triplets[i][j] = Integer.parseInt(points[j]);
                }
            }
        }
        int v1 = vertexIndex(triplets[0][0]);
        int uv1 = -1;
        int n1 = -1;
        if (uvProvided) {
            uv1 = uvIndex(triplets[0][1]);
            if (uv1 < 0) {
                uvProvided = false;
            }
        }
        if (normalProvided) {
            n1 = normalIndex(triplets[0][2]);
            if (n1 < 0) {
                normalProvided = false;
            }
        }
        for (int i = 1; i < triplets.length - 1; i++) {
            int v2 = vertexIndex(triplets[i][0]);
            int v3 = vertexIndex(triplets[i + 1][0]);
            int uv2 = -1;
            int uv3 = -1;
            int n2 = -1;
            int n3 = -1;
            if (uvProvided) {
                uv2 = uvIndex(triplets[i][1]);
                uv3 = uvIndex(triplets[i + 1][1]);
            }
            if (normalProvided) {
                n2 = normalIndex(triplets[i][2]);
                n3 = normalIndex(triplets[i + 1][2]);
            }
            facesList.add(v1);
            facesList.add(uv1);
            facesList.add(v2);
            facesList.add(uv2);
            facesList.add(v3);
            facesList.add(uv3);
            faceNormalsList.add(n1);
            faceNormalsList.add(n2);
            faceNormalsList.add(n3);
            smoothingGroupList.add(currentSmoothingGroup);
        }
    }

    /**
     * Vertex definition. List of geometric vertices, with (x, y, z, [w]) coordinates, w is optional and defaults to 1.0.
     * <p>Example:
     * <pre>v 3.14441400 1.97608500 -4.85138200</pre>
     */
    private void parseVertex(String argsText) {
        String[] vertices = splitBySpace(argsText);
        float x = Float.parseFloat(vertices[0]);
        float y = Float.parseFloat(vertices[1]);
        float z = Float.parseFloat(vertices[2]);
        vertexArray.addAll(x, y, z);
    }

    /**
     * "vt ". Texture coordinates, u, [v], [w], floating point values between 0 and 1.
     * v, w are optional and default to 0.
     * <p>
     * Example:
     * <pre>vt 0.90625000 6.2500000e-2</pre>
     */
    private void parseTextureCoordinate(String argsText) {
        String[] coordinates = splitBySpace(argsText);
        float u = Float.parseFloat(coordinates[0]);
        float v = Float.parseFloat(coordinates[1]);
        uvArray.addAll(u, 1 - v);
    }

    /**
     * Smoothing group: "s <integer> ..." or "s off".
     * <p>Example:
     * <pre>s 5</pre>
     */
    private void parseSmoothingGroup(String argsText) {
        if (argsText.equals("off")) {
            currentSmoothingGroup = 0;
        } else {
            currentSmoothingGroup = Integer.parseInt(argsText);
        }
    }

    /**
     * Vertex normal: "vn float_value1 float_value2 float_value3"
     * <p>Example:</p>
     * <pre>vn -0.59190005 0.53777519 0.60037669</pre>
     */
    private void parseVertexNormal(String argsText) {
        String[] values = splitBySpace(argsText);
        float x = Float.parseFloat(values[0]);
        float y = Float.parseFloat(values[1]);
        float z = Float.parseFloat(values[2]);
        normalsArray.addAll(x, y, z);
    }

    private TriangleMesh createTriangleMesh() {
        if (facesStart >= facesList.size()) {
            // we're only interested in faces
            smoothingGroupsStart = smoothingGroupList.size();
            return null;
        }
        var vertexMap  = new HashMap<Integer, Integer>(vertexArray.size() / 2);
        var uvMap      = new HashMap<Integer, Integer>(uvArray.size() / 2);
        var normalsMap = new HashMap<Integer, Integer>(normalsArray.size() / 2);

        var vertices  = FXCollections.observableFloatArray();
        var texCoords = FXCollections.observableFloatArray();
        var normals   = FXCollections.observableFloatArray();

        boolean useNormals = true;

        for (int facesIndex = facesStart; facesIndex < facesList.size(); facesIndex += 2) {
            // First comes vertex index
            final int vertexIndex = facesList.get(facesIndex);
            if (!vertexMap.containsKey(vertexIndex)) {
                vertexMap.put(vertexIndex, vertices.size() / 3);
                vertices.addAll(
                    vertexArray.get(vertexIndex * 3),
                    vertexArray.get(vertexIndex * 3 + 1),
                    vertexArray.get(vertexIndex * 3 + 2)
                );
            }
            facesList.set(facesIndex, vertexMap.get(vertexIndex));

            // Second comes texture coordinate index
            final int texCoordIndex = facesList.get(facesIndex + 1);
            if (!uvMap.containsKey(texCoordIndex)) {
                uvMap.put(texCoordIndex, texCoords.size() / 2);
                if (texCoordIndex >= 0) {
                    texCoords.addAll(
                        uvArray.get(texCoordIndex * 2),
                        uvArray.get(texCoordIndex * 2 + 1)
                    );
                } else {
                    texCoords.addAll(0f, 0f);
                }
            }
            facesList.set(facesIndex + 1, uvMap.get(texCoordIndex));

            if (useNormals) {
                int normalsIndex = faceNormalsList.get(facesIndex / 2);
                if (!normalsMap.containsKey(normalsIndex)) {
                    normalsMap.put(normalsIndex, normals.size() / 3);
                    if (normalsIndex >= 0 && normals.size() >= (normalsIndex + 1) * 3) {
                        normals.addAll(
                            normalsArray.get(normalsIndex * 3),
                            normalsArray.get(normalsIndex * 3 + 1),
                            normalsArray.get(normalsIndex * 3 + 2)
                        );
                    } else {
                        useNormals = false;
                        normals.addAll(0f, 0f, 0f);
                    }
                }
                faceNormalsList.set(facesIndex / 2, normalsMap.get(normalsIndex));
            }
        }

        // Now create the triangle mesh from the parsed data:
        final var mesh = new TriangleMesh();
        mesh.getPoints().setAll(vertices);
        mesh.getTexCoords().setAll(texCoords);

        final int[] faces = toIntArray(restOfList(facesList, facesStart));
        mesh.getFaces().setAll(faces);

        final int[] smoothingGroups = useNormals
            ? computeSmoothingGroups(mesh, faces, toIntArray(restOfList(faceNormalsList, facesNormalStart)), toFloatArray(normals))
            : toIntArray(restOfList(smoothingGroupList, smoothingGroupsStart));
        mesh.getFaceSmoothingGroups().setAll(smoothingGroups);

        facesStart = facesList.size();
        facesNormalStart = faceNormalsList.size();
        smoothingGroupsStart = smoothingGroupList.size();

        return mesh;
    }

    /**
     * Converts an OBJ vertex index (1-based, negative allowed) into a 0-based index
     * into {@link #vertexArray}.
     *
     * @param v the OBJ vertex index
     * @return the resolved 0-based index
     */
    private int vertexIndex(int v) {
        return (v < 0) ? v + vertexArray.size() / 3 : v - 1;
    }

    /**
     * Converts an OBJ texture coordinate index (1-based, negative allowed)
     * into a 0-based index into {@link #uvArray}.
     *
     * @param uv the OBJ texture coordinate index
     * @return the resolved 0-based index
     */
    private int uvIndex(int uv) {
        return (uv < 0) ? uv + uvArray.size() / 2 : uv - 1;
    }

    /**
     * Converts an OBJ normal index (1-based, negative allowed)
     * into a 0-based index into {@link #normalsArray}.
     *
     * @param n the OBJ normal index
     * @return the resolved 0-based index
     */
    private int normalIndex(int n) {
        return (n < 0) ? n + normalsArray.size() / 3 : n - 1;
    }

}
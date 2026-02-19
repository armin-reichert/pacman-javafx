/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.amr.pacmanfx.uilib.objimport.SmoothingGroups.computeSmoothingGroups;
import static java.util.Objects.requireNonNull;

/**
 * Code is based on Oracle's OBJ importer from the 3DViewer sample project.
 *
 * @see <a href=
 * "https://github.com/teamfx/openjfx-10-dev-rt/tree/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers">3DViewer
 * Sample</a>
 */
public class ObjFileImporter {

    private static String[] splitBySpace(String line) {
        return line.trim().split("\\s+");
    }

    private static int[] toIntArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private static float[] toFloatArray(ObservableFloatArray observableFloatArray) {
        return observableFloatArray.toArray(new float[observableFloatArray.size()]);
    }

    private static List<Integer> restOf(ArrayList<Integer> list, int start) {
        return list.subList(start, list.size());
    }

    public static void validateTriangleMesh(TriangleMesh mesh) {
        int numPoints = mesh.getPoints().size() / mesh.getPointElementSize();
        int numTexCoords = mesh.getTexCoords().size() / mesh.getTexCoordElementSize();
        int numFaces = mesh.getFaces().size() / mesh.getFaceElementSize();
        if (numPoints == 0 || numPoints * mesh.getPointElementSize() != mesh.getPoints().size()) {
            throw new AssertionError("Points array size is not correct: " + mesh.getPoints().size());
        }
        if (numTexCoords == 0 || numTexCoords * mesh.getTexCoordElementSize() != mesh.getTexCoords().size()) {
            throw new AssertionError("Tex-Coords array size is not correct: " + mesh.getPoints().size());
        }
        if (numFaces == 0 || numFaces * mesh.getFaceElementSize() != mesh.getFaces().size()) {
            throw new AssertionError("Faces array size is not correct: " + mesh.getPoints().size());
        }
        if (numFaces != mesh.getFaceSmoothingGroups().size() && mesh.getFaceSmoothingGroups().size() > 0) {
            throw new AssertionError(
                "FaceSmoothingGroups array size is not correct: " + mesh.getPoints().size() + ", numFaces = " + numFaces);
        }
        ObservableFaceArray faces = mesh.getFaces();
        for (int i = 0; i < faces.size(); i += 2) {
            int pointIndex = faces.get(i);
            if (pointIndex < 0 || pointIndex > numPoints) {
                throw new AssertionError("Incorrect point index: " + pointIndex + ", numPoints = " + numPoints);
            }
            int texCoordIndex = faces.get(i + 1);
            if (texCoordIndex < 0 || texCoordIndex > numTexCoords) {
                throw new AssertionError("Incorrect texture coordinate index: " + texCoordIndex + ", numTexCoords = " + numTexCoords);
            }
        }
    }

    public static Model3D importObjFile(URL url, Charset charset) throws IOException {
        requireNonNull(url);
        requireNonNull(charset);
        try (final InputStream is = url.openStream()) {
            final var reader = new BufferedReader(new InputStreamReader(is, charset));
            final ObjFileImporter importer = new ObjFileImporter(url);
            importer.parse(reader);
            Logger.info("OBJ file parsed: {} vertices, {} uvs, {} faces, {} smoothing groups. URL={}",
                importer.data.vertexArray.size() / 3,
                importer.data.uvArray.size() / 2,
                importer.data.facesList.size() / 6,
                importer.data.smoothingGroupList.size(),
                url);
            return importer.data;
        }
    }

    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    private String meshName = "default";
    private int currentSmoothingGroup = 0;

    private final Model3D data;

    private ObjFileImporter(URL url) {
        data = new Model3D(url);
    }

    private void parse(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (line.startsWith("f ")) {
                parseFace(line.substring(2));
            }
            else if (line.startsWith("g ")) {
                commitCurrentMesh();
                meshName = line.substring(2);
            }
            else if (line.equals("g")) {
                commitCurrentMesh();
                meshName = "default";
            }
            else if (line.startsWith("mtllib ")) {
                parseMaterialLibs(line.substring(7));
            }
            else if (line.startsWith("o ")) {
                commitCurrentMesh();
                meshName = line.substring(2);
            }
            else if (line.startsWith("s ")) {
                parseSmoothingGroup(line.substring(2));
            }
            else if (line.startsWith("usemtl ")) {
                commitCurrentMesh();
                Logger.trace("usemtl '{}' command not supported", line.substring(7));
            }
            else if (line.startsWith("v ")) {
                parseVertex(line.substring(2));
            }
            else if (line.startsWith("vn ")) {
                parseVertexNormal(line.substring(3));
            }
            else if (line.startsWith("vt ")) {
                parseTextureCoordinate(line.substring(3));
            }
            else {
                Logger.warn("Line skipped: {} (no idea what it means)", line);
            }
        }
        commitCurrentMesh();
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
        int v1 = data.vertexIndex(triplets[0][0]);
        int uv1 = -1;
        int n1 = -1;
        if (uvProvided) {
            uv1 = data.uvIndex(triplets[0][1]);
            if (uv1 < 0) {
                uvProvided = false;
            }
        }
        if (normalProvided) {
            n1 = data.normalIndex(triplets[0][2]);
            if (n1 < 0) {
                normalProvided = false;
            }
        }
        for (int i = 1; i < triplets.length - 1; i++) {
            int v2 = data.vertexIndex(triplets[i][0]);
            int v3 = data.vertexIndex(triplets[i + 1][0]);
            int uv2 = -1;
            int uv3 = -1;
            int n2 = -1;
            int n3 = -1;
            if (uvProvided) {
                uv2 = data.uvIndex(triplets[i][1]);
                uv3 = data.uvIndex(triplets[i + 1][1]);
            }
            if (normalProvided) {
                n2 = data.normalIndex(triplets[i][2]);
                n3 = data.normalIndex(triplets[i + 1][2]);
            }
            data.facesList.addAll(List.of(v1, uv1, v2, uv2, v3, uv3));
            data.faceNormalsList.addAll(List.of(n1, n2, n3));
            data.smoothingGroupList.add(currentSmoothingGroup);
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
        data.vertexArray.addAll(x, y, z);
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
        data.uvArray.addAll(u, 1 - v);
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
     * Material libs: "mtllib filename.."
     * <p>Example:
     * <pre>mtllib pacman.mtl</pre>
     */
    private void parseMaterialLibs(String argsText) {
        String[] materialFileNames = splitBySpace(argsText);
        for (String filename : materialFileNames) {
            MaterialFileReader materialFileReader = new MaterialFileReader(filename, data.url.toExternalForm());
            data.materialMapsList.add(materialFileReader.getMaterialMap());
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
        data.normalsArray.addAll(x, y, z);
    }

    private void commitCurrentMesh() {
        if (facesStart >= data.facesList.size()) {
            // we're only interested in faces
            smoothingGroupsStart = data.smoothingGroupList.size();
            return;
        }
        var vertexMap      = new HashMap<Integer, Integer>(data.vertexArray.size() / 2);
        var uvMap          = new HashMap<Integer, Integer>(data.uvArray.size() / 2);
        var normalsMap     = new HashMap<Integer, Integer>(data.normalsArray.size() / 2);

        var verticesArray  = FXCollections.observableFloatArray();
        var texCoordsArray = FXCollections.observableFloatArray();
        var normalsArray   = FXCollections.observableFloatArray();

        boolean useNormals = true;

        for (int facesIndex = facesStart; facesIndex < data.facesList.size(); facesIndex += 2) {

            // First comes vertex index
            final int vertexIndex = data.facesList.get(facesIndex);
            if (!vertexMap.containsKey(vertexIndex)) {
                vertexMap.put(vertexIndex, verticesArray.size() / 3);
                verticesArray.addAll(
                    data.vertexArray.get(vertexIndex * 3),
                    data.vertexArray.get(vertexIndex * 3 + 1),
                    data.vertexArray.get(vertexIndex * 3 + 2)
                );
            }
            data.facesList.set(facesIndex, vertexMap.get(vertexIndex));

            // Second comes texture coordinate index
            final int texCoordIndex = data.facesList.get(facesIndex + 1);
            if (!uvMap.containsKey(texCoordIndex)) {
                uvMap.put(texCoordIndex, texCoordsArray.size() / 2);
                if (texCoordIndex >= 0) {
                    texCoordsArray.addAll(
                        data.uvArray.get(texCoordIndex * 2),
                        data.uvArray.get(texCoordIndex * 2 + 1)
                    );
                } else {
                    texCoordsArray.addAll(0f, 0f);
                }
            }
            data.facesList.set(facesIndex + 1, uvMap.get(texCoordIndex));

            if (useNormals) {
                int normalsIndex = data.faceNormalsList.get(facesIndex / 2);
                if (!normalsMap.containsKey(normalsIndex)) {
                    normalsMap.put(normalsIndex, normalsArray.size() / 3);
                    if (normalsIndex >= 0 && data.normalsArray.size() >= (normalsIndex + 1) * 3) {
                        normalsArray.addAll(
                            data.normalsArray.get(normalsIndex * 3),
                            data.normalsArray.get(normalsIndex * 3 + 1),
                            data.normalsArray.get(normalsIndex * 3 + 2)
                        );
                    } else {
                        useNormals = false;
                        normalsArray.addAll(0f, 0f, 0f);
                    }
                }
                data.faceNormalsList.set(facesIndex / 2, normalsMap.get(normalsIndex));
            }
        }

        // Now build the triangle mesh from the parsed data:

        final var mesh = new TriangleMesh();
        mesh.getPoints().setAll(verticesArray);
        mesh.getTexCoords().setAll(texCoordsArray);

        final int[] faces = toIntArray(restOf(data.facesList, facesStart));
        mesh.getFaces().setAll(faces);

        final int[] smoothingGroups = useNormals
            ? computeSmoothingGroups(mesh, faces, toIntArray(restOf(data.faceNormalsList, facesNormalStart)), toFloatArray(normalsArray))
            : toIntArray(restOf(data.smoothingGroupList, smoothingGroupsStart));
        mesh.getFaceSmoothingGroups().setAll(smoothingGroups);

        // try specified name, if already used, make unique name using serial number e.g. "my_mesh (3)"
        int serialNumber = 2;
        String unusedMeshName = meshName;
        while (data.triangleMeshMap.containsKey(unusedMeshName)) {
            Logger.info("Mesh name '{}' already exists", unusedMeshName);
            unusedMeshName = "%s (%d)".formatted(meshName, serialNumber);
            ++serialNumber;
        }
        data.triangleMeshMap.put(unusedMeshName, mesh);

        Logger.trace("Added mesh '{}', vertices: {}, texture coordinates: {}, faces: {}, smoothing groups: {}",
            meshName,
            mesh.getPoints().size() / mesh.getPointElementSize(),
            mesh.getTexCoords().size() / mesh.getTexCoordElementSize(),
            mesh.getFaces().size() / mesh.getFaceElementSize(),
            mesh.getFaceSmoothingGroups().size());

        facesStart = data.facesList.size();
        facesNormalStart = data.faceNormalsList.size();
        smoothingGroupsStart = data.smoothingGroupList.size();
    }
}
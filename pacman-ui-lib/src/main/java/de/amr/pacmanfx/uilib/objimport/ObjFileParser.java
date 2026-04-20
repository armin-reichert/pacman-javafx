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
public class ObjFileParser {

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

    private static List<Integer> restOf(ArrayList<Integer> list, int start) {
        return list.subList(start, list.size());
    }

    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    private String meshName = "default";
    private int currentSmoothingGroup = 0;

    private final Model3D model3D;

    public ObjFileParser(URL url, Charset charset) throws IOException {
        requireNonNull(url);
        requireNonNull(charset);
        model3D = new Model3D(url);
        try (InputStream is = url.openStream()) {
            final var reader = new BufferedReader(new InputStreamReader(is, charset));
            parse(reader);
            Logger.info("OBJ file parsed: {} vertices, {} uvs, {} faces, {} smoothing groups. URL={}",
                model3D.vertexArray.size() / 3,
                model3D.uvArray.size() / 2,
                model3D.facesList.size() / 6,
                model3D.smoothingGroupList.size(),
                url);
        }
    }

    public Model3D model3D() {
        return model3D;
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
        int v1 = model3D.vertexIndex(triplets[0][0]);
        int uv1 = -1;
        int n1 = -1;
        if (uvProvided) {
            uv1 = model3D.uvIndex(triplets[0][1]);
            if (uv1 < 0) {
                uvProvided = false;
            }
        }
        if (normalProvided) {
            n1 = model3D.normalIndex(triplets[0][2]);
            if (n1 < 0) {
                normalProvided = false;
            }
        }
        for (int i = 1; i < triplets.length - 1; i++) {
            int v2 = model3D.vertexIndex(triplets[i][0]);
            int v3 = model3D.vertexIndex(triplets[i + 1][0]);
            int uv2 = -1;
            int uv3 = -1;
            int n2 = -1;
            int n3 = -1;
            if (uvProvided) {
                uv2 = model3D.uvIndex(triplets[i][1]);
                uv3 = model3D.uvIndex(triplets[i + 1][1]);
            }
            if (normalProvided) {
                n2 = model3D.normalIndex(triplets[i][2]);
                n3 = model3D.normalIndex(triplets[i + 1][2]);
            }
            model3D.facesList.add(v1);
            model3D.facesList.add(uv1);
            model3D.facesList.add(v2);
            model3D.facesList.add(uv2);
            model3D.facesList.add(v3);
            model3D.facesList.add(uv3);
            model3D.faceNormalsList.add(n1);
            model3D.faceNormalsList.add(n2);
            model3D.faceNormalsList.add(n3);
            model3D.smoothingGroupList.add(currentSmoothingGroup);
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
        model3D.vertexArray.addAll(x, y, z);
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
        model3D.uvArray.addAll(u, 1 - v);
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
            MaterialFileReader materialFileReader = new MaterialFileReader(filename, model3D.url.toExternalForm());
            model3D.materialMapsList.add(materialFileReader.getMaterialMap());
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
        model3D.normalsArray.addAll(x, y, z);
    }

    private void commitCurrentMesh() {
        if (facesStart >= model3D.facesList.size()) {
            // we're only interested in faces
            smoothingGroupsStart = model3D.smoothingGroupList.size();
            return;
        }
        var vertexMap      = new HashMap<Integer, Integer>(model3D.vertexArray.size() / 2);
        var uvMap          = new HashMap<Integer, Integer>(model3D.uvArray.size() / 2);
        var normalsMap     = new HashMap<Integer, Integer>(model3D.normalsArray.size() / 2);

        var verticesArray  = FXCollections.observableFloatArray();
        var texCoordsArray = FXCollections.observableFloatArray();
        var normalsArray   = FXCollections.observableFloatArray();

        boolean useNormals = true;

        for (int facesIndex = facesStart; facesIndex < model3D.facesList.size(); facesIndex += 2) {

            // First comes vertex index
            final int vertexIndex = model3D.facesList.get(facesIndex);
            if (!vertexMap.containsKey(vertexIndex)) {
                vertexMap.put(vertexIndex, verticesArray.size() / 3);
                verticesArray.addAll(
                    model3D.vertexArray.get(vertexIndex * 3),
                    model3D.vertexArray.get(vertexIndex * 3 + 1),
                    model3D.vertexArray.get(vertexIndex * 3 + 2)
                );
            }
            model3D.facesList.set(facesIndex, vertexMap.get(vertexIndex));

            // Second comes texture coordinate index
            final int texCoordIndex = model3D.facesList.get(facesIndex + 1);
            if (!uvMap.containsKey(texCoordIndex)) {
                uvMap.put(texCoordIndex, texCoordsArray.size() / 2);
                if (texCoordIndex >= 0) {
                    texCoordsArray.addAll(
                        model3D.uvArray.get(texCoordIndex * 2),
                        model3D.uvArray.get(texCoordIndex * 2 + 1)
                    );
                } else {
                    texCoordsArray.addAll(0f, 0f);
                }
            }
            model3D.facesList.set(facesIndex + 1, uvMap.get(texCoordIndex));

            if (useNormals) {
                int normalsIndex = model3D.faceNormalsList.get(facesIndex / 2);
                if (!normalsMap.containsKey(normalsIndex)) {
                    normalsMap.put(normalsIndex, normalsArray.size() / 3);
                    if (normalsIndex >= 0 && model3D.normalsArray.size() >= (normalsIndex + 1) * 3) {
                        normalsArray.addAll(
                            model3D.normalsArray.get(normalsIndex * 3),
                            model3D.normalsArray.get(normalsIndex * 3 + 1),
                            model3D.normalsArray.get(normalsIndex * 3 + 2)
                        );
                    } else {
                        useNormals = false;
                        normalsArray.addAll(0f, 0f, 0f);
                    }
                }
                model3D.faceNormalsList.set(facesIndex / 2, normalsMap.get(normalsIndex));
            }
        }

        // Now build the triangle mesh from the parsed data:

        final var mesh = new TriangleMesh();
        mesh.getPoints().setAll(verticesArray);
        mesh.getTexCoords().setAll(texCoordsArray);

        final int[] faces = toIntArray(restOf(model3D.facesList, facesStart));
        mesh.getFaces().setAll(faces);

        final int[] smoothingGroups = useNormals
            ? computeSmoothingGroups(mesh, faces, toIntArray(restOf(model3D.faceNormalsList, facesNormalStart)), toFloatArray(normalsArray))
            : toIntArray(restOf(model3D.smoothingGroupList, smoothingGroupsStart));
        mesh.getFaceSmoothingGroups().setAll(smoothingGroups);

        // try specified name, if already used, make unique name using serial number e.g. "my_mesh (3)"
        int serialNumber = 2;
        String unusedMeshName = meshName;
        while (model3D.triangleMeshMap.containsKey(unusedMeshName)) {
            Logger.info("Mesh name '{}' already exists", unusedMeshName);
            unusedMeshName = "%s (%d)".formatted(meshName, serialNumber);
            ++serialNumber;
        }
        model3D.triangleMeshMap.put(unusedMeshName, mesh);

        Logger.trace("Added mesh '{}', vertices: {}, texture coordinates: {}, faces: {}, smoothing groups: {}",
            meshName,
            mesh.getPoints().size() / mesh.getPointElementSize(),
            mesh.getTexCoords().size() / mesh.getTexCoordElementSize(),
            mesh.getFaces().size() / mesh.getFaceElementSize(),
            mesh.getFaceSmoothingGroups().size());

        facesStart = model3D.facesList.size();
        facesNormalStart = model3D.faceNormalsList.size();
        smoothingGroupsStart = model3D.smoothingGroupList.size();
    }
}
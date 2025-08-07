/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.collections.FXCollections;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Derived from Oracle's OBJ importer from the 3DViewer sample project.
 *
 * @see <a href=
 * "https://github.com/teamfx/openjfx-10-dev-rt/tree/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers">3DViewer
 * Sample</a>
 */
public class ObjFileImporter {

    private static int[] toIntArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private static List<Integer> restList(ArrayList<Integer> list, int start) {
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

    public static ObjFileData importObjFile(URL url, Charset charset) {
        requireNonNull(url);
        requireNonNull(charset);
        ObjFileImporter importer = new ObjFileImporter(url);
        try (InputStream is = url.openStream()) {
            Instant start = Instant.now();
            var reader = new BufferedReader(new InputStreamReader(is, charset));
            importer.parse(reader);
            for (TriangleMesh mesh : importer.data.triangleMeshMap.values()) {
                validateTriangleMesh(mesh);
            }
            Duration duration = Duration.between(start, Instant.now());
            Logger.info("OBJ file parsed in {} milliseconds; '{}'", duration.toMillis(), url);
        }
        catch (IOException x) {
            Logger.error(x);
            Logger.error("Importing OBJ file '{}' failed!", url);
        }
        return importer.data;
    }

    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    private String meshName = "default";
    private int currentSmoothGroup = 0;

    private final ObjFileData data;

    private ObjFileImporter(URL url) {
        data = new ObjFileData();
        data.url = url;
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
                parseMaterialLib(line.substring(7));
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
        Logger.info("OBJ file parsed: {} vertices, {} uvs, {} faces, {} smoothing groups",
            data.vertexArray.size() / 3, data.uvArray.size() / 2, data.faceList.size() / 6, data.smoothingGroupList.size());
    }

    /*
     * Face: "f v1/v2/v3 ...."
     */
    private void parseFace(String argText) {
        String[] parts = argText.trim().split("\\s+");
        int[][] faceData = new int[parts.length][];
        boolean uvProvided = true;
        boolean normalProvided = true;
        for (int i = 0; i < parts.length; i++) {
            String[] vs = parts[i].split("/");
            if (vs.length < 2) {
                uvProvided = false;
            }
            if (vs.length < 3) {
                normalProvided = false;
            }
            faceData[i] = new int[vs.length];
            for (int j = 0; j < vs.length; j++) {
                if (vs[j].isEmpty()) {
                    faceData[i][j] = 0;
                    if (j == 1) {
                        uvProvided = false;
                    }
                    if (j == 2) {
                        normalProvided = false;
                    }
                } else {
                    faceData[i][j] = Integer.parseInt(vs[j]);
                }
            }
        }
        int v1 = vertexIndex(faceData[0][0]);
        int uv1 = -1;
        int n1 = -1;
        if (uvProvided) {
            uv1 = uvIndex(faceData[0][1]);
            if (uv1 < 0) {
                uvProvided = false;
            }
        }
        if (normalProvided) {
            n1 = normalIndex(faceData[0][2]);
            if (n1 < 0) {
                normalProvided = false;
            }
        }
        for (int i = 1; i < faceData.length - 1; i++) {
            int v2 = vertexIndex(faceData[i][0]);
            int v3 = vertexIndex(faceData[i + 1][0]);
            int uv2 = -1;
            int uv3 = -1;
            int n2 = -1;
            int n3 = -1;
            if (uvProvided) {
                uv2 = uvIndex(faceData[i][1]);
                uv3 = uvIndex(faceData[i + 1][1]);
            }
            if (normalProvided) {
                n2 = normalIndex(faceData[i][2]);
                n3 = normalIndex(faceData[i + 1][2]);
            }
            data.faceList.add(v1);
            data.faceList.add(uv1);
            data.faceList.add(v2);
            data.faceList.add(uv2);
            data.faceList.add(v3);
            data.faceList.add(uv3);
            data.faceNormalsList.add(n1);
            data.faceNormalsList.add(n2);
            data.faceNormalsList.add(n3);
            data.smoothingGroupList.add(currentSmoothGroup);
        }
    }

    /*
     * "v <x> <y> <z> (<w>)"
     *
     * List of geometric vertices, with (x, y, z, [w]) coordinates, w is optional and defaults to 1.0.
     */
    private void parseVertex(String argsText) {
        String[] parts = argsText.trim().split("\\s+");
        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        data.vertexArray.addAll(x, y, z);
    }

    /*
     * "vt ..."
     *
     * List of texture coordinates, in (u, [v, w]) coordinates, these will vary between 0 and 1. v, w are optional
     * and default to 0.
     */
    private void parseTextureCoordinate(String argsText) {
        String[] parts = argsText.trim().split("\\s+");
        float u = Float.parseFloat(parts[0]);
        float v = Float.parseFloat(parts[1]);
        data.uvArray.addAll(u, 1 - v);
    }

    /*
     * Smoothing group: "s <integer> ..." or "s off"
     */
    private void parseSmoothingGroup(String argsText) {
        if (argsText.equals("off")) {
            currentSmoothGroup = 0;
        } else {
            currentSmoothGroup = Integer.parseInt(argsText);
        }
    }

    /*
     * Material lib: "mtllib filename1 filename2 ..."
     */
    private void parseMaterialLib(String argsText) {
        String[] parts = argsText.trim().split("\\s+");
        for (String filename : parts) {
            MtlReader mtlReader = new MtlReader(filename, data.url.toExternalForm());
            data.materialLibsList.add(mtlReader.getMaterials());
        }
    }

    /*
     * Vertex normal: "vn ..."
     */
    private void parseVertexNormal(String argsText) {
        String[] parts = argsText.trim().split("\\s+");
        float x = Float.parseFloat(parts[0]);
        float y = Float.parseFloat(parts[1]);
        float z = Float.parseFloat(parts[2]);
        data.normalsArray.addAll(x, y, z);
    }

    private int vertexIndex(int v) {
        return (v < 0) ? v + data.vertexArray.size() / 3 : v - 1;
    }

    private int uvIndex(int uv) {
        return (uv < 0) ? uv + data.uvArray.size() / 2 : uv - 1;
    }

    private int normalIndex(int n) {
        return (n < 0) ? n + data.normalsArray.size() / 3 : n - 1;
    }

    private void commitCurrentMesh() {
        if (facesStart >= data.faceList.size()) {
            // we're only interested in faces
            smoothingGroupsStart = data.smoothingGroupList.size();
            return;
        }
        var vertexMap      = new HashMap<Integer, Integer>(data.vertexArray.size() / 2);
        var uvMap          = new HashMap<Integer, Integer>(data.uvArray.size() / 2);
        var normalMap      = new HashMap<Integer, Integer>(data.normalsArray.size() / 2);
        var newVertexArray = FXCollections.observableFloatArray();
        var newUVArray     = FXCollections.observableFloatArray();
        var newNormalArray = FXCollections.observableFloatArray();
        boolean useNormals = true;

        for (int i = facesStart; i < data.faceList.size(); i += 2) {
            int vi = data.faceList.get(i);
            Integer nvi = vertexMap.get(vi);
            if (nvi == null) {
                nvi = newVertexArray.size() / 3;
                vertexMap.put(vi, nvi);
                newVertexArray.addAll(
                    data.vertexArray.get(vi * 3),
                    data.vertexArray.get(vi * 3 + 1),
                    data.vertexArray.get(vi * 3 + 2)
                );
            }
            data.faceList.set(i, nvi);

            int uvi = data.faceList.get(i + 1);
            Integer nuvi = uvMap.get(uvi);
            if (nuvi == null) {
                nuvi = newUVArray.size() / 2;
                uvMap.put(uvi, nuvi);
                if (uvi >= 0) {
                    newUVArray.addAll(
                        data.uvArray.get(uvi * 2),
                        data.uvArray.get(uvi * 2 + 1)
                    );
                } else {
                    newUVArray.addAll(0f, 0f);
                }
            }
            data.faceList.set(i + 1, nuvi);

            if (useNormals) {
                int ni = data.faceNormalsList.get(i / 2);
                Integer nni = normalMap.get(ni);
                if (nni == null) {
                    nni = newNormalArray.size() / 3;
                    normalMap.put(ni, nni);
                    if (ni >= 0 && data.normalsArray.size() >= (ni + 1) * 3) {
                        newNormalArray.addAll(
                            data.normalsArray.get(ni * 3),
                            data.normalsArray.get(ni * 3 + 1),
                            data.normalsArray.get(ni * 3 + 2)
                        );
                    } else {
                        useNormals = false;
                        newNormalArray.addAll(0f, 0f, 0f);
                    }
                }
                data.faceNormalsList.set(i / 2, nni);
            }
        }

        final var mesh = new TriangleMesh();
        mesh.getPoints().setAll(newVertexArray);
        mesh.getTexCoords().setAll(newUVArray);

        List<Integer> facesSublist = restList(data.faceList, facesStart);
        mesh.getFaces().setAll(toIntArray(facesSublist));
        // Use normals if they are provided
        if (useNormals) {
            int[] flatFaces = toIntArray(facesSublist);
            int[] flatFaceNormals = toIntArray(restList(data.faceNormalsList, facesNormalStart));
            float[] normals = newNormalArray.toArray(new float[0]);
            int[] smGroups = SmoothingGroups.calcSmoothGroups(mesh, flatFaces, flatFaceNormals, normals);
            mesh.getFaceSmoothingGroups().setAll(smGroups);
        } else {
            mesh.getFaceSmoothingGroups().setAll(
                data.smoothingGroupList.subList(smoothingGroupsStart, data.smoothingGroupList.size()).stream().mapToInt(Integer::intValue).toArray());
        }

        // try specified name, if already used, make unique name using serial number e.g. "my_mesh (3)"
        int serialNumber = 2;
        String nextMeshName = meshName;
        while (data.triangleMeshMap.containsKey(nextMeshName)) {
            Logger.info("Mesh name '{}' already exists", nextMeshName);
            nextMeshName = "%s (%d)".formatted(meshName, serialNumber);
            ++serialNumber;
        }
        data.triangleMeshMap.put(nextMeshName, mesh);

        Logger.trace("Mesh '{}' added, vertices: {}, uvs: {}, faces: {}, smoothing groups: {}",
            meshName,
            mesh.getPoints().size() / mesh.getPointElementSize(),
            mesh.getTexCoords().size() / mesh.getTexCoordElementSize(),
            mesh.getFaces().size() / mesh.getFaceElementSize(),
            mesh.getFaceSmoothingGroups().size());

        facesStart = data.faceList.size();
        facesNormalStart = data.faceNormalsList.size();
        smoothingGroupsStart = data.smoothingGroupList.size();
    }
}
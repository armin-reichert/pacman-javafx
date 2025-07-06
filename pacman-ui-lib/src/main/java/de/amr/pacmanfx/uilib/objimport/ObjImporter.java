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
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Material;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Stripped-down version of Oracle's OBJ importer from the 3DViewer sample project.
 *
 * @see <a href=
 * "https://github.com/teamfx/openjfx-10-dev-rt/tree/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers">3DViewer
 * Sample</a>
 */
public class ObjImporter {

    public static void validateTree(Node node) {
        if (node instanceof MeshView meshView) {
            validateTriangleMesh((TriangleMesh) meshView.getMesh());
        } else if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                validateTree(child);
            }
        }
    }

    public static void validateTriangleMesh(TriangleMesh mesh) {
        int numPoints = mesh.getPoints().size() / mesh.getPointElementSize();
        int numTexCoords = mesh.getTexCoords().size() / mesh.getTexCoordElementSize();
        int numFaces = mesh.getFaces().size() / mesh.getFaceElementSize();
        if (numPoints == 0 || numPoints * mesh.getPointElementSize() != mesh.getPoints().size()) {
            throw new AssertionError("Points array size is not correct: " + mesh.getPoints().size());
        }
        if (numTexCoords == 0 || numTexCoords * mesh.getTexCoordElementSize() != mesh.getTexCoords().size()) {
            throw new AssertionError("TexCoords array size is not correct: " + mesh.getPoints().size());
        }
        if (numFaces == 0 || numFaces * mesh.getFaceElementSize() != mesh.getFaces().size()) {
            throw new AssertionError("Faces array size is not correct: " + mesh.getPoints().size());
        }
        if (numFaces != mesh.getFaceSmoothingGroups().size() && mesh.getFaceSmoothingGroups().size() > 0) {
            throw new AssertionError(
                "FaceSmoothingGroups array size is not correct: " + mesh.getPoints().size() + ", numFaces = " + numFaces);
        }
        ObservableIntegerArray faces = mesh.getFaces();
        for (int i = 0; i < faces.size(); i += 2) {
            int pIndex = faces.get(i);
            if (pIndex < 0 || pIndex > numPoints) {
                throw new AssertionError("Incorrect point index: " + pIndex + ", numPoints = " + numPoints);
            }
            int tcIndex = faces.get(i + 1);
            if (tcIndex < 0 || tcIndex > numTexCoords) {
                throw new AssertionError("Incorrect texCoord index: " + tcIndex + ", numTexCoords = " + numTexCoords);
            }
        }
    }

    private String objFileUrl;
    private final Map<String, TriangleMesh> meshMap = new HashMap<>();
    private final List<Map<String, Material>> materialLibsList = new ArrayList<>();
    private final ObservableFloatArray vertexArray = FXCollections.observableFloatArray();
    private final ObservableFloatArray uvArray = FXCollections.observableFloatArray();
    private final IntegerArrayList faceList = new IntegerArrayList();
    private final IntegerArrayList smoothingGroupList = new IntegerArrayList();
    private final ObservableFloatArray normals = FXCollections.observableFloatArray();
    private final IntegerArrayList faceNormals = new IntegerArrayList();
    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    public ObjImporter(String objFileUrl) throws IOException, URISyntaxException {
        this.objFileUrl = objFileUrl;
        parse(new URI(objFileUrl).toURL().openStream(), StandardCharsets.UTF_8);
    }

    public ObjImporter(InputStream inputStream) throws IOException {
        parse(inputStream, StandardCharsets.UTF_8);
    }

    public Set<String> getMeshNames() {
        return meshMap.keySet();
    }

    public TriangleMesh getTriangleMesh(String name) {
        return meshMap.get(name);
    }

    public List<Map<String, Material>> materialLibsList() {
        return materialLibsList;
    }

    private int vertexIndex(int v) {
        return (v < 0) ? v + vertexArray.size() / 3 : v - 1;
    }

    private int uvIndex(int uv) {
        return (uv < 0) ? uv + uvArray.size() / 2 : uv - 1;
    }

    private int normalIndex(int n) {
        return (n < 0) ? n + normals.size() / 3 : n - 1;
    }

    private void parse(InputStream inputStream, Charset charset) throws IOException {
        String currentLine;
        String currentName = "default";
        int currentSmoothGroup = 0;

        var reader = new BufferedReader(new InputStreamReader(inputStream, charset));
        while ((currentLine = reader.readLine()) != null) {
            try {
                if (currentLine.startsWith("o ")) {
                    // "o <objectname>"
                    String rest = currentLine.substring(2);
                    addMesh(currentName);
                    currentName = rest;
                    Logger.trace("Object name: {}", currentName);
                }

                else if (currentLine.startsWith("g ") || currentLine.equals("g")) {
                    // "g <group_name>" or "g" (default group)
                    boolean defaultGroup = currentLine.equals("g");
                    addMesh(currentName);
                    currentName = defaultGroup ? "default" : currentLine.substring(2);
                    Logger.trace("Group name: {}", currentName);
                }

                else if (currentLine.startsWith("v ")) {
                    /*
                     * "v <x> <y> <z> (<w>)"
                     *
                     * List of geometric vertices, with (x, y, z, [w]) coordinates, w is optional and defaults to 1.0.
                     */
                    String rest = currentLine.substring(2);
                    String[] parts = rest.trim().split("\\s+");
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float z = Float.parseFloat(parts[2]);
                    vertexArray.addAll(x, y, z);
                }

                else if (currentLine.startsWith("vt ")) {
                    /*
                     * "vt ..."
                     *
                     * List of texture coordinates, in (u, [v, w]) coordinates, these will vary between 0 and 1. v, w are optional
                     * and default to 0.
                     */
                    String rest = currentLine.substring(3);
                    String[] parts = rest.trim().split("\\s+");
                    float u = Float.parseFloat(parts[0]);
                    float v = Float.parseFloat(parts[1]);
                    uvArray.addAll(u, 1 - v);
                }

                else if (currentLine.startsWith("f ")) {
                    /*
                     * Face: "f v1/v2/v3 ...."
                     */
                    String rest = currentLine.substring(2);
                    String[] parts = rest.trim().split("\\s+");
                    int[][] data = new int[parts.length][];
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
                        data[i] = new int[vs.length];
                        for (int j = 0; j < vs.length; j++) {
                            if (vs[j].length() == 0) {
                                data[i][j] = 0;
                                if (j == 1) {
                                    uvProvided = false;
                                }
                                if (j == 2) {
                                    normalProvided = false;
                                }
                            } else {
                                data[i][j] = Integer.parseInt(vs[j]);
                            }
                        }
                    }
                    int v1 = vertexIndex(data[0][0]);
                    int uv1 = -1;
                    int n1 = -1;
                    if (uvProvided) {
                        uv1 = uvIndex(data[0][1]);
                        if (uv1 < 0) {
                            uvProvided = false;
                        }
                    }
                    if (normalProvided) {
                        n1 = normalIndex(data[0][2]);
                        if (n1 < 0) {
                            normalProvided = false;
                        }
                    }
                    for (int i = 1; i < data.length - 1; i++) {
                        int v2 = vertexIndex(data[i][0]);
                        int v3 = vertexIndex(data[i + 1][0]);
                        int uv2 = -1;
                        int uv3 = -1;
                        int n2 = -1;
                        int n3 = -1;
                        if (uvProvided) {
                            uv2 = uvIndex(data[i][1]);
                            uv3 = uvIndex(data[i + 1][1]);
                        }
                        if (normalProvided) {
                            n2 = normalIndex(data[i][2]);
                            n3 = normalIndex(data[i + 1][2]);
                        }
                        faceList.add(v1);
                        faceList.add(uv1);
                        faceList.add(v2);
                        faceList.add(uv2);
                        faceList.add(v3);
                        faceList.add(uv3);
                        faceNormals.add(n1);
                        faceNormals.add(n2);
                        faceNormals.add(n3);
                        smoothingGroupList.add(currentSmoothGroup);
                    }
                }

                else if (currentLine.startsWith("s ")) {
                    /*
                     * Smoothing group: "s <integer> ..." or "s off"
                     */
                    String rest = currentLine.substring(2);
                    if (rest.equals("off")) {
                        currentSmoothGroup = 0;
                    } else {
                        currentSmoothGroup = Integer.parseInt(rest);
                    }
                }

                else if (currentLine.startsWith("mtllib ")) {
                    /*
                     * Material lib: "mtllib filename1 filename2 ..."
                     */
                    String rest = currentLine.substring(7);
                    String[] parts = rest.trim().split("\\s+");
                    for (String filename : parts) {
                        MtlReader mtlReader = new MtlReader(filename, objFileUrl);
                        materialLibsList.add(mtlReader.getMaterials());
                    }
                }

                else if (currentLine.startsWith("usemtl ")) {
                    /*
                     * Use material: "usemtl ..."
                     */
                    addMesh(currentName);
                    // setting new material for next mesh
//					String materialName = line.substring("usemtl ".length());
//					for (Map<String, Material> mm : materialLibrary) {
//						Material m = mm.get(materialName);
//						if (m != null) {
//							material = m;
//							break;
//						}
//					}
                }

                else if (currentLine.isEmpty() || currentLine.startsWith("#")) {
                    /*
                     * Comment: "# ...."
                     */
                    Logger.trace("Empty or comment line, ignored");
                }

                else if (currentLine.startsWith("vn ")) {
                    /*
                     * Vertex normal.
                     */
                    String[] split = currentLine.substring(2).trim().split("\\s+");
                    float x = Float.parseFloat(split[0]);
                    float y = Float.parseFloat(split[1]);
                    float z = Float.parseFloat(split[2]);
                    normals.addAll(x, y, z);
                }

                else {
                    Logger.trace("Line skipped: {}", currentLine);
                }

            } catch (Exception ex) {
                Logger.error("Failed to parse line: {}", currentLine);
            }
        }
        addMesh(currentName);
        Logger.trace("Model loaded: {} vertices, {} uvs, {} faces, {} smoothing groups",
            vertexArray.size() / 3, uvArray.size() / 2, faceList.size() / 6, smoothingGroupList.size());
    }

    private void addMesh(String name) {
        if (facesStart >= faceList.size()) {
            // we're only interested in faces
            smoothingGroupsStart = smoothingGroupList.size();
            return;
        }
        var vertexMap = new HashMap<Integer, Integer>(vertexArray.size() / 2);
        var uvMap = new HashMap<Integer, Integer>(uvArray.size() / 2);
        var normalMap = new HashMap<Integer, Integer>(normals.size() / 2);
        var newVertexList = FXCollections.observableFloatArray();
        var newUVs = FXCollections.observableFloatArray();
        var newNormals = FXCollections.observableFloatArray();
        boolean useNormals = true;

        for (int i = facesStart; i < faceList.size(); i += 2) {
            int vi = faceList.get(i);
            Integer nvi = vertexMap.get(vi);
            if (nvi == null) {
                nvi = newVertexList.size() / 3;
                vertexMap.put(vi, nvi);
                newVertexList.addAll(vertexArray.get(vi * 3), vertexArray.get(vi * 3 + 1), vertexArray.get(vi * 3 + 2));
            }
            faceList.set(i, nvi);

            int uvi = faceList.get(i + 1);
            Integer nuvi = uvMap.get(uvi);
            if (nuvi == null) {
                nuvi = newUVs.size() / 2;
                uvMap.put(uvi, nuvi);
                if (uvi >= 0) {
                    newUVs.addAll(uvArray.get(uvi * 2), uvArray.get(uvi * 2 + 1));
                } else {
                    newUVs.addAll(0f, 0f);
                }
            }
            faceList.set(i + 1, nuvi);

            if (useNormals) {
                int ni = faceNormals.get(i / 2);
                Integer nni = normalMap.get(ni);
                if (nni == null) {
                    nni = newNormals.size() / 3;
                    normalMap.put(ni, nni);
                    if (ni >= 0 && normals.size() >= (ni + 1) * 3) {
                        newNormals.addAll(normals.get(ni * 3), normals.get(ni * 3 + 1), normals.get(ni * 3 + 2));
                    } else {
                        useNormals = false;
                        newNormals.addAll(0f, 0f, 0f);
                    }
                }
                faceNormals.set(i / 2, nni);
            }
        }

        var triangleMesh = new TriangleMesh();
        triangleMesh.getPoints().setAll(newVertexList);
        triangleMesh.getTexCoords().setAll(newUVs);
        triangleMesh.getFaces().setAll(((IntegerArrayList) faceList.subList(facesStart, faceList.size())).toIntArray());

        // Use normals if they are provided
        if (useNormals) {
            int[] newFaces = ((IntegerArrayList) faceList.subList(facesStart, faceList.size())).toIntArray();
            int[] newFaceNormals = ((IntegerArrayList) faceNormals.subList(facesNormalStart, faceNormals.size()))
                .toIntArray();
            int[] smGroups = SmoothingGroups.calcSmoothGroups(triangleMesh, newFaces, newFaceNormals,
                newNormals.toArray(new float[newNormals.size()]));
            triangleMesh.getFaceSmoothingGroups().setAll(smGroups);
        } else {
            triangleMesh.getFaceSmoothingGroups().setAll(
                ((IntegerArrayList) smoothingGroupList.subList(smoothingGroupsStart, smoothingGroupList.size())).toIntArray());
        }

        int keyIndex = 2;
        String keyBase = name;
        while (meshMap.get(name) != null) {
            name = keyBase + " (" + keyIndex++ + ")";
        }
        meshMap.put(name, triangleMesh);

        Logger.trace("Mesh '{}' added, vertices: {}, uvs: {}, faces: {}, smoothing groups: {}", name,
            triangleMesh.getPoints().size() / triangleMesh.getPointElementSize(),
            triangleMesh.getTexCoords().size() / triangleMesh.getTexCoordElementSize(), triangleMesh.getFaces().size() / triangleMesh.getFaceElementSize(),
            triangleMesh.getFaceSmoothingGroups().size());

        facesStart = faceList.size();
        facesNormalStart = faceNormals.size();
        smoothingGroupsStart = smoothingGroupList.size();
    }
}
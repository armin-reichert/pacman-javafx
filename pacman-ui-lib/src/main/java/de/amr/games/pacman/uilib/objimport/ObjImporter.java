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
package de.amr.games.pacman.uilib.objimport;

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
import java.util.*;


/**
 * Stripped-down version of Oracle's OBJ importer from the 3DViewer sample project.
 *
 * @author Armin Reichert
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
    private final Map<String, TriangleMesh> meshes = new HashMap<>();
    private final List<Map<String, Material>> materialLibrary = new ArrayList<>();
    private final ObservableFloatArray vertexes = FXCollections.observableFloatArray();
    private final ObservableFloatArray uvs = FXCollections.observableFloatArray();
    private final IntegerArrayList faces = new IntegerArrayList();
    private final IntegerArrayList smoothingGroups = new IntegerArrayList();
    private final ObservableFloatArray normals = FXCollections.observableFloatArray();
    private final IntegerArrayList faceNormals = new IntegerArrayList();
    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;

    public ObjImporter(String objFileUrl) throws IOException, URISyntaxException {
        this.objFileUrl = objFileUrl;
        parse(new URI(objFileUrl).toURL().openStream());
    }

    public ObjImporter(InputStream inputStream) throws IOException {
        parse(inputStream);
    }

    public Set<String> getMeshNames() {
        return meshes.keySet();
    }

    public TriangleMesh getMesh(String key) {
        return meshes.get(key);
    }

    public List<Map<String, Material>> materialLibrary() {
        return materialLibrary;
    }

    private int vertexIndex(int vertexIndex) {
        if (vertexIndex < 0) {
            return vertexIndex + vertexes.size() / 3;
        } else {
            return vertexIndex - 1;
        }
    }

    private int uvIndex(int uvIndex) {
        if (uvIndex < 0) {
            return uvIndex + uvs.size() / 2;
        } else {
            return uvIndex - 1;
        }
    }

    private int normalIndex(int normalIndex) {
        if (normalIndex < 0) {
            return normalIndex + normals.size() / 3;
        } else {
            return normalIndex - 1;
        }
    }

    private void parse(InputStream inputStream) throws IOException {
        var br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int currentSmoothGroup = 0;
        String key = "default";

        while ((line = br.readLine()) != null) {
            try {

                /*
                 * o <objectname>
                 */
                if (line.startsWith("o ")) {
                    addMesh(key);
                    key = line.substring(2);
                    Logger.trace("Object name: {}", key);
                }

                /*
                 * g <groupname>
                 */
                else if (line.startsWith("g ") || line.equals("g")) {
                    addMesh(key);
                    key = line.length() > 2 ? line.substring(2) : "default";
                    Logger.trace("Group name: {}", key);
                }

                /*
                 * v <x> <y> <z> (<w>)
                 *
                 * List of geometric vertices, with (x, y, z, [w]) coordinates, w is optional and defaults to 1.0.
                 */
                else if (line.startsWith("v ")) {
                    String[] split = line.substring(2).trim().split("\\s+");
                    float x = Float.parseFloat(split[0]);
                    float y = Float.parseFloat(split[1]);
                    float z = Float.parseFloat(split[2]);
                    vertexes.addAll(x, y, z);
                }

                /*
                 * vt
                 *
                 * List of texture coordinates, in (u, [v, w]) coordinates, these will vary between 0 and 1. v, w are optional
                 * and default to 0.
                 */
                else if (line.startsWith("vt ")) {
                    String[] split = line.substring(3).trim().split("\\s+");
                    float u = Float.parseFloat(split[0]);
                    float v = Float.parseFloat(split[1]);
                    uvs.addAll(u, 1 - v);
                }

                /*
                 * f v1 v2 v3 ....
                 *
                 * Face.
                 */
                else if (line.startsWith("f ")) {
                    String[] split = line.substring(2).trim().split("\\s+");
                    int[][] data = new int[split.length][];
                    boolean uvProvided = true;
                    boolean normalProvided = true;
                    for (int i = 0; i < split.length; i++) {
                        String[] split2 = split[i].split("/");
                        if (split2.length < 2) {
                            uvProvided = false;
                        }
                        if (split2.length < 3) {
                            normalProvided = false;
                        }
                        data[i] = new int[split2.length];
                        for (int j = 0; j < split2.length; j++) {
                            if (split2[j].length() == 0) {
                                data[i][j] = 0;
                                if (j == 1) {
                                    uvProvided = false;
                                }
                                if (j == 2) {
                                    normalProvided = false;
                                }
                            } else {
                                data[i][j] = Integer.parseInt(split2[j]);
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
                        faces.add(v1);
                        faces.add(uv1);
                        faces.add(v2);
                        faces.add(uv2);
                        faces.add(v3);
                        faces.add(uv3);
                        faceNormals.add(n1);
                        faceNormals.add(n2);
                        faceNormals.add(n3);
                        smoothingGroups.add(currentSmoothGroup);
                    }
                }

                /*
                 * Smoothing group s <integer>
                 */
                else if (line.startsWith("s ")) {
                    if (line.substring(2).equals("off")) {
                        currentSmoothGroup = 0;
                    } else {
                        currentSmoothGroup = Integer.parseInt(line.substring(2));
                    }
                }

                /*
                 * Material lib.
                 */
                else if (line.startsWith("mtllib ")) {
                    // setting materials lib
                    String[] split = line.substring("mtllib ".length()).trim().split("\\s+");
                    for (String filename : split) {
                        MtlReader mtlReader = new MtlReader(filename, objFileUrl);
                        materialLibrary.add(mtlReader.getMaterials());
                    }
                }

                /*
                 * Use material.
                 */
                else if (line.startsWith("usemtl ")) {
                    addMesh(key);
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

                /*
                 * Comment.
                 */
                else if (line.isEmpty() || line.startsWith("#")) {
                    // comments and empty lines are ignored
                }

                /*
                 * Vertex normal.
                 */
                else if (line.startsWith("vn ")) {
                    String[] split = line.substring(2).trim().split("\\s+");
                    float x = Float.parseFloat(split[0]);
                    float y = Float.parseFloat(split[1]);
                    float z = Float.parseFloat(split[2]);
                    normals.addAll(x, y, z);
                }

                /*
                 * Not implemented or not recognized.
                 */
                else {
                    Logger.trace("Line skipped: {}", line);
                }
            } catch (Exception ex) {
                Logger.error("Failed to parse line: {}", line);
            }
        }

        addMesh(key);

        Logger.trace("Model loaded: {} vertices, {} uvs, {} faces, {} smoothing groups", vertexes.size() / 3,
            uvs.size() / 2, faces.size() / 6, smoothingGroups.size());
    }

    private void addMesh(String key) {
        if (facesStart >= faces.size()) {
            // we're only interested in faces
            smoothingGroupsStart = smoothingGroups.size();
            return;
        }
        var vertexMap = new HashMap<Integer, Integer>(vertexes.size() / 2);
        var uvMap = new HashMap<Integer, Integer>(uvs.size() / 2);
        var normalMap = new HashMap<Integer, Integer>(normals.size() / 2);
        var newVertexes = FXCollections.observableFloatArray();
        var newUVs = FXCollections.observableFloatArray();
        var newNormals = FXCollections.observableFloatArray();
        boolean useNormals = true;

        for (int i = facesStart; i < faces.size(); i += 2) {
            int vi = faces.get(i);
            Integer nvi = vertexMap.get(vi);
            if (nvi == null) {
                nvi = newVertexes.size() / 3;
                vertexMap.put(vi, nvi);
                newVertexes.addAll(vertexes.get(vi * 3), vertexes.get(vi * 3 + 1), vertexes.get(vi * 3 + 2));
            }
            faces.set(i, nvi);

            int uvi = faces.get(i + 1);
            Integer nuvi = uvMap.get(uvi);
            if (nuvi == null) {
                nuvi = newUVs.size() / 2;
                uvMap.put(uvi, nuvi);
                if (uvi >= 0) {
                    newUVs.addAll(uvs.get(uvi * 2), uvs.get(uvi * 2 + 1));
                } else {
                    newUVs.addAll(0f, 0f);
                }
            }
            faces.set(i + 1, nuvi);

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

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(newVertexes);
        mesh.getTexCoords().setAll(newUVs);
        mesh.getFaces().setAll(((IntegerArrayList) faces.subList(facesStart, faces.size())).toIntArray());

        // Use normals if they are provided
        if (useNormals) {
            int[] newFaces = ((IntegerArrayList) faces.subList(facesStart, faces.size())).toIntArray();
            int[] newFaceNormals = ((IntegerArrayList) faceNormals.subList(facesNormalStart, faceNormals.size()))
                .toIntArray();
            int[] smGroups = SmoothingGroups.calcSmoothGroups(mesh, newFaces, newFaceNormals,
                newNormals.toArray(new float[newNormals.size()]));
            mesh.getFaceSmoothingGroups().setAll(smGroups);
        } else {
            mesh.getFaceSmoothingGroups().setAll(
                ((IntegerArrayList) smoothingGroups.subList(smoothingGroupsStart, smoothingGroups.size())).toIntArray());
        }

        int keyIndex = 2;
        String keyBase = key;
        while (meshes.get(key) != null) {
            key = keyBase + " (" + keyIndex++ + ")";
        }
        meshes.put(key, mesh);

        Logger.trace("Mesh '{}' added, vertices: {}, uvs: {}, faces: {}, smoothing groups: {}", key,
            mesh.getPoints().size() / mesh.getPointElementSize(),
            mesh.getTexCoords().size() / mesh.getTexCoordElementSize(), mesh.getFaces().size() / mesh.getFaceElementSize(),
            mesh.getFaceSmoothingGroups().size());

        facesStart = faces.size();
        facesNormalStart = faceNormals.size();
        smoothingGroupsStart = smoothingGroups.size();
    }
}
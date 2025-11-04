/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates.
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

import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.scene.shape.TriangleMesh;

import java.util.*;

/**
 * Util for converting Normals to Smoothing Groups
 */
public class SmoothingGroups {

    record Edge(int from, int to, int fromNormal, int toNormal) {

        public static Edge of(int from, int to, int fromNormal, int toNormal) {
            return new Edge(Math.min(from, to), Math.max(from, to), Math.min(fromNormal, toNormal), Math.max(fromNormal, toNormal));
        }

        public boolean isSmooth(Edge edge, float[] normals) {
            return (areNormalsEqual(getNormal(normals, fromNormal), getNormal(normals, edge.fromNormal))
                    && areNormalsEqual(getNormal(normals, toNormal), getNormal(normals, edge.toNormal))) ||
                    (areNormalsEqual(getNormal(normals, fromNormal), getNormal(normals, edge.toNormal))
                            && areNormalsEqual(getNormal(normals, toNormal), getNormal(normals, edge.fromNormal)));
        }
    }

    private static final float NORMAL_ANGLE = 0.9994f; // cos(2)

    private final BitSet visited;
    private final BitSet unvisited;
    private final Queue<Integer> q;

    private final int[][] faces;
    private final int[][] faceNormals;
    private final float[] normals;

    private Edge[][] faceEdges;

    public SmoothingGroups(int[][] faces, int[][] faceNormals, float[] normals) {
        this.faces = faces;
        this.faceNormals = faceNormals;
        this.normals = normals;
        visited = new BitSet(faces.length);
        unvisited = new BitSet(faces.length);
        unvisited.set(0, faces.length, true);
        q = new LinkedList<>();
    }

    // edge -> [faces]
    private List<Integer> getNextConnectedComponent(Map<Edge, List<Integer>> adjacentFaces) {
        int index = unvisited.previousSetBit(faces.length - 1);
        q.add(index);
        visited.set(index);
        unvisited.set(index, false);
        var res = new ArrayList<Integer>();
        while (!q.isEmpty()) {
            Integer faceIndex = q.remove();
            res.add(faceIndex);
            for (Edge edge : faceEdges[faceIndex]) {
                List<Integer> adjFaces = adjacentFaces.get(edge);
                if (adjFaces == null) {
                    continue;
                }
                Integer adjFaceIndex = adjFaces.get(adjFaces.get(0).equals(faceIndex) ? 1 : 0);
                if (!visited.get(adjFaceIndex)) {
                    q.add(adjFaceIndex);
                    visited.set(adjFaceIndex);
                    unvisited.set(adjFaceIndex, false);
                }
            }
        }
        return res;
    }

    private boolean hasNextConnectedComponent() {
        return !unvisited.isEmpty();
    }

    private void computeFaceEdges() {
        faceEdges = new Edge[faces.length][];
        for (int f = 0; f < faces.length; f++) {
            int[] face = faces[f];
            int[] faceNormal = faceNormals[f];
            int n = face.length / 2;
            faceEdges[f] = new Edge[n];
            int from = face[(n - 1) * 2];
            int fromNormal = faceNormal[n - 1];
            for (int i = 0; i < n; i++) {
                int to = face[i * 2];
                int toNormal = faceNormal[i];
                Edge edge = new Edge(from, to, fromNormal, toNormal);
                faceEdges[f][i] = edge;
                from = to;
                fromNormal = toNormal;
            }
        }
    }

    private Map<Edge, List<Integer>> getAdjacentFaces() {
        var adjacentFaces = new HashMap<Edge, List<Integer>>();
        for (int f = 0; f < faceEdges.length; f++) {
            for (Edge edge : faceEdges[f]) {
                if (!adjacentFaces.containsKey(edge)) {
                    adjacentFaces.put(edge, new ArrayList<Integer>());
                }
                adjacentFaces.get(edge).add(f);
            }
        }
        for (Iterator<Map.Entry<Edge, List<Integer>>> it = adjacentFaces.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Edge, List<Integer>> e = it.next();
            if (e.getValue().size() != 2) {
                // just skip them
                it.remove();
            }
        }
        return adjacentFaces;
    }

    private static Vector3f getNormal(float[] normals, int i) {
        return new Vector3f(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
    }

    private static boolean areNormalsEqual(Vector3f v1, Vector3f v2) {
        if (v1.x() == 1.0e20f || v1.y() == 1.0e20f || v1.z() == 1.0e20f || v2.x() == 1.0e20f || v2.y() == 1.0e20f
            || v2.z() == 1.0e20f) {
            return false;
        }
        Vector3f normal1 = new Vector3f(v1).normalized();
        Vector3f normal2 = new Vector3f(v2).normalized();
        return normal1.dot(normal2) >= NORMAL_ANGLE;
    }

    private Map<Edge, List<Integer>> getSmoothEdges(Map<Edge, List<Integer>> adjacentFaces) {
        var smoothEdges = new HashMap<Edge, List<Integer>>();
        for (int face = 0; face < faceEdges.length; face++) {
            for (Edge edge : faceEdges[face]) {
                List<Integer> adjFaces = adjacentFaces.get(edge);
                if (adjFaces == null || adjFaces.size() != 2) {
                    // could happen when we skip edges!
                    continue;
                }
                int adjFace = adjFaces.get(adjFaces.get(0) == face ? 1 : 0);
                Edge[] adjFaceEdges = faceEdges[adjFace];
                int adjEdgeInd = Arrays.asList(adjFaceEdges).indexOf(edge);
                if (adjEdgeInd == -1) {
                    System.out.println("Can't find edge " + edge + " in face " + adjFace);
                    System.out.println(Arrays.asList(adjFaceEdges));
                    continue;
                }
                Edge adjEdge = adjFaceEdges[adjEdgeInd];

                if (edge.isSmooth(adjEdge, normals)) {
                    if (!smoothEdges.containsKey(edge)) {
                        smoothEdges.put(edge, adjFaces);
                    }
                }
            }
        }
        return smoothEdges;
    }

    private List<List<Integer>> calcConnComponents(Map<Edge, List<Integer>> smoothEdges) {
        // System.out.println("smoothEdges = " + smoothEdges);
        List<List<Integer>> groups = new ArrayList<>();
        while (hasNextConnectedComponent()) {
            List<Integer> smoothGroup = getNextConnectedComponent(smoothEdges);
            groups.add(smoothGroup);
        }
        return groups;
    }

    private int[] generateSmGroups(List<List<Integer>> groups) {
        int[] smGroups = new int[faceNormals.length];
        int curGroup = 0;
        for (int i = 0; i < groups.size(); i++) {
            List<Integer> list = groups.get(i);
            if (list.size() == 1) {
                smGroups[list.get(0)] = 0;
            } else {
                for (int j = 0; j < list.size(); j++) {
                    Integer faceIndex = list.get(j);
                    smGroups[faceIndex] = 1 << curGroup;
                }
                if (curGroup++ == 31) {
                    curGroup = 0;
                }
            }
        }
        return smGroups;
    }

    private int[] computeSmoothingGroups() {
        computeFaceEdges();

        // edge -> [faces]
        Map<Edge, List<Integer>> adjacentFaces = getAdjacentFaces();

        // smooth edge -> [faces]
        Map<Edge, List<Integer>> smoothEdges = getSmoothEdges(adjacentFaces);

        // System.out.println("smoothEdges = " + smoothEdges);
        List<List<Integer>> groups = calcConnComponents(smoothEdges);

        return generateSmGroups(groups);
    }

    /**
     * Calculates smoothing groups for data formatted in PolygonMesh style
     *
     * @param faces       An array of faces, where each face consists of an array of vertex and uv indices
     * @param faceNormals An array of face normals, where each face normal consists of an array of normal indices
     * @param normals     The array of normals
     * @return An array of smooth groups, where the length of the array is the number of faces
     */
    public static int[] computeSmoothingGroups(int[][] faces, int[][] faceNormals, float[] normals) {
        SmoothingGroups smoothGroups = new SmoothingGroups(faces, faceNormals, normals);
        return smoothGroups.computeSmoothingGroups();
    }

    /**
     * Calculates smoothing groups for data formatted in TriangleMesh style
     *
     * @param flatFaces       An array of faces, where each triangle face is represented by 6 (vertex and uv) indices
     * @param flatFaceNormals An array of face normals, where each triangle face is represented by 3 normal indices
     * @param normals         The array of normals
     * @return An array of smooth groups, where the length of the array is the number of faces
     */
    public static int[] computeSmoothingGroups(TriangleMesh mesh, int[] flatFaces, int[] flatFaceNormals, float[] normals) {
        int faceElementSize = mesh.getFaceElementSize();
        int[][] faces = new int[flatFaces.length / faceElementSize][faceElementSize];
        for (int f = 0; f < faces.length; f++) {
            System.arraycopy(flatFaces, f * faceElementSize, faces[f], 0, faceElementSize);
        }
        int pointElementSize = mesh.getPointElementSize();
        int[][] faceNormals = new int[flatFaceNormals.length / pointElementSize][pointElementSize];
        for (int f = 0; f < faceNormals.length; f++) {
            System.arraycopy(flatFaceNormals, f * pointElementSize, faceNormals[f], 0, pointElementSize);
        }
        SmoothingGroups smoothGroups = new SmoothingGroups(faces, faceNormals, normals);
        return smoothGroups.computeSmoothingGroups();
    }
}
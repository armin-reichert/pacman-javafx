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
package de.amr.objimport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/** Obj file reader */
public class ObjImporter {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static float scale = 1;
	private static boolean flatXZ = false;

	public static void setScale(float scale) {
		ObjImporter.scale = scale;
	}

	public static void setFlatXZ(boolean flatXZ) {
		ObjImporter.flatXZ = flatXZ;
	}

	private Map<String, TriangleMesh> meshes = new HashMap<>();
	private Map<String, Material> materials = new HashMap<>();
	private List<Map<String, Material>> materialLibrary = new ArrayList<>();
	private String objFileUrl;
	private ObservableFloatArray vertexes = FXCollections.observableFloatArray();
	private ObservableFloatArray uvs = FXCollections.observableFloatArray();
	private IntegerArrayList faces = new IntegerArrayList();
	private IntegerArrayList smoothingGroups = new IntegerArrayList();
	private ObservableFloatArray normals = FXCollections.observableFloatArray();
	private IntegerArrayList faceNormals = new IntegerArrayList();
	private Material material = new PhongMaterial(Color.WHITE);
	private int facesStart = 0;
	private int facesNormalStart = 0;
	private int smoothingGroupsStart = 0;

	public ObjImporter(String objFileUrl) throws FileNotFoundException, IOException {
		this.objFileUrl = objFileUrl;
		LOG.info("Reading OBJ file from URL %s", objFileUrl);
		read(new URL(objFileUrl).openStream());
	}

	public ObjImporter(InputStream inputStream) throws IOException {
		read(inputStream);
	}

	public Set<String> getMeshes() {
		return meshes.keySet();
	}

	public TriangleMesh getMesh() {
		return meshes.values().iterator().next();
	}

	public TriangleMesh getMesh(String key) {
		return meshes.get(key);
	}

	public MeshView buildMeshView(String key) {
		MeshView meshView = new MeshView();
		meshView.setId(key);
		meshView.setMaterial(materials.get(key));
		meshView.setMesh(meshes.get(key));
		meshView.setCullFace(CullFace.NONE);
		return meshView;
	}

	public Material getMaterial() {
		return materials.values().iterator().next();
	}

	public Material getMaterial(String key) {
		return materials.get(key);
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

	private void read(InputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		int currentSmoothGroup = 0;
		String key = "default";
		while ((line = br.readLine()) != null) {
			try {
				if (line.startsWith("g ") || line.equals("g")) {
					addMesh(key);
					key = line.length() > 2 ? line.substring(2) : "default";
					LOG.trace("key = " + key);
				} else if (line.startsWith("v ")) {
					String[] split = line.substring(2).trim().split("\\s+");
					float x = Float.parseFloat(split[0]) * scale;
					float y = Float.parseFloat(split[1]) * scale;
					float z = Float.parseFloat(split[2]) * scale;
					// LOG.trace("x = " + x + ", y = " + y + ", z = " + z);
					vertexes.addAll(x, y, z);
					if (flatXZ) {
						uvs.addAll(x, z);
					}
				} else if (line.startsWith("vt ")) {
					String[] split = line.substring(3).trim().split("\\s+");
					float u = Float.parseFloat(split[0]);
					float v = Float.parseFloat(split[1]);

					// LOG.trace("u = " + u + ", v = " + v);

					uvs.addAll(u, 1 - v);
				} else if (line.startsWith("f ")) {
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
					if (uvProvided && !flatXZ) {
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
						if (uvProvided && !flatXZ) {
							uv2 = uvIndex(data[i][1]);
							uv3 = uvIndex(data[i + 1][1]);
						}
						if (normalProvided) {
							n2 = normalIndex(data[i][2]);
							n3 = normalIndex(data[i + 1][2]);
						}

						// LOG.trace("v1 = " + v1 + ", v2 = " + v2 + ", v3 = " + v3);
						// LOG.trace("uv1 = " + uv1 + ", uv2 = " + uv2 + ", uv3 = " + uv3);

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
				} else if (line.startsWith("s ")) {
					if (line.substring(2).equals("off")) {
						currentSmoothGroup = 0;
					} else {
						currentSmoothGroup = Integer.parseInt(line.substring(2));
					}
				} else if (line.startsWith("mtllib ")) {
					// setting materials lib
					String[] split = line.substring("mtllib ".length()).trim().split("\\s+");
					for (String filename : split) {
						MtlReader mtlReader = new MtlReader(filename, objFileUrl);
						materialLibrary.add(mtlReader.getMaterials());
					}
				} else if (line.startsWith("usemtl ")) {
					addMesh(key);
					// setting new material for next mesh
					String materialName = line.substring("usemtl ".length());
					for (Map<String, Material> mm : materialLibrary) {
						Material m = mm.get(materialName);
						if (m != null) {
							material = m;
							break;
						}
					}
				} else if (line.isEmpty() || line.startsWith("#")) {
					// comments and empty lines are ignored
				} else if (line.startsWith("vn ")) {
					String[] split = line.substring(2).trim().split("\\s+");
					float x = Float.parseFloat(split[0]);
					float y = Float.parseFloat(split[1]);
					float z = Float.parseFloat(split[2]);
					normals.addAll(x, y, z);
				} else {
					LOG.trace("line skipped: " + line);
				}
			} catch (Exception ex) {
				LOG.error("Failed to parse line: %s", line);
			}
		}
		addMesh(key);

		LOG.info("Loaded %d vertices, %d uvs, %d faces, %d smoothing groups", vertexes.size() / 3, uvs.size() / 2,
				faces.size() / 6, smoothingGroups.size());
	}

	private void addMesh(String key) {
		if (facesStart >= faces.size()) {
			// we're only interested in faces
			smoothingGroupsStart = smoothingGroups.size();
			return;
		}
		Map<Integer, Integer> vertexMap = new HashMap<>(vertexes.size() / 2);
		Map<Integer, Integer> uvMap = new HashMap<>(uvs.size() / 2);
		Map<Integer, Integer> normalMap = new HashMap<>(normals.size() / 2);
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
		materials.put(key, material);

		LOG.trace("Added mesh '" + key + "' of " + mesh.getPoints().size() / mesh.getPointElementSize() + " vertexes, "
				+ mesh.getTexCoords().size() / mesh.getTexCoordElementSize() + " uvs, "
				+ mesh.getFaces().size() / mesh.getFaceElementSize() + " faces, " + mesh.getFaceSmoothingGroups().size()
				+ " smoothing groups.");
		LOG.trace("material diffuse color = " + ((PhongMaterial) material).getDiffuseColor());
		LOG.trace("material diffuse map = " + ((PhongMaterial) material).getDiffuseMap());

		facesStart = faces.size();
		facesNormalStart = faceNormals.size();
		smoothingGroupsStart = smoothingGroups.size();
	}
}

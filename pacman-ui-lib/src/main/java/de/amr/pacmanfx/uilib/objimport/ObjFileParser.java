/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static de.amr.pacmanfx.uilib.objimport.SmoothingGroups.computeSmoothingGroups;
import static java.util.Objects.requireNonNull;

/**
 * Parses Wavefront OBJ files. Just good enough for my purposes. Some parts of the code still unclear to me.
 *
 * <p>Code has been derived from the OBJ importer in the 3DViewer sample project (Oracle). Parts rewritten using
 * Copilot.
 *
 * @see <a href=
 * "https://github.com/teamfx/openjfx-10-dev-rt/tree/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers">3DViewer
 * Sample</a>
 */
public class ObjFileParser {

    public record ObjFileParseResult(
        Map<String, TriangleMesh> meshMap,
        Map<Mesh, PhongMaterial> modelMaterialAssignments) {}

    public static Optional<ObjFileParseResult> parse(URL objFileURL, Charset charset) {
        try {
            final var parser = new ObjFileParser(objFileURL, charset);
            parser.meshMap.forEach((name, mesh) -> {
                try {
                    MeshHelper.validateTriangleMesh(mesh);
                } catch (AssertionError e) {
                    Logger.error(e, "Invalid mesh: {}, URL: '{}'", name, objFileURL);
                }
            });
            return Optional.of(new ObjFileParseResult(
                Collections.unmodifiableMap(parser.meshMap),
                Collections.unmodifiableMap(parser.modelMaterialAssignments)
            ));
        }
        catch (IOException x) {
            Logger.error(x, "OBJ file parsing failed");
            return Optional.empty();
        }
    }

    private enum Keyword {
        OBJECT            ("o"),
        GROUP             ("g"),
        MATERIAL_LIB      ("mtllib"),
        MATERIAL_USAGE    ("usemtl"),
        SMOOTHING_GROUP   ("s"),
        VERTEX            ("v"),
        VERTEX_NORMAL     ("vn"),
        TEX_COORD         ("vt"),
        FACE              ("f");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }
    }

    private static class MeshDefinition {
        final String name;
        String materialName;

        public MeshDefinition(String name) {
            this.name = name;
        }
    }

    // fields

    private final URL objFileURL;

    private final Map<String, TriangleMesh> meshMap = new HashMap<>();
    private final Map<String, Map<String, PhongMaterial>> materialLibsMap = new WeakHashMap<>();

    // If a material is assigned to an object/group with "usemtl" in the OBJ, it can be looked up here
    private final Map<Mesh, PhongMaterial> modelMaterialAssignments = new WeakHashMap<>();

    private int facesStart = 0;
    private int facesNormalStart = 0;
    private int smoothingGroupsStart = 0;
    private int currentSmoothingGroup = 0;

    private int lineNo;
    private int anonMeshNameCount = 0;

    private MeshDefinition currentMeshDef;

    /** Flat array of vertex coordinates (x, y, z). */
    private final ObservableFloatArray vertexArray = FXCollections.observableFloatArray();

    /** Flat array of vertex normals (nx, ny, nz). */
    private final ObservableFloatArray normalsArray = FXCollections.observableFloatArray();

    /** Flat array of texture coordinates (u, v). */
    private final ObservableFloatArray uvArray = FXCollections.observableFloatArray();

    /** Face index list (vertex/uv/normal indices). */
    private final List<Integer> facesList = new ArrayList<>();

    /** Normal indices for each face. */
    private final List<Integer> faceNormalsList = new ArrayList<>();

    /** Smoothing group indices for each face. */
    private final List<Integer> smoothingGroupList = new ArrayList<>();

    private ObjFileParser(URL objFileURL, Charset charset) throws IOException {
        this.objFileURL = requireNonNull(objFileURL);
        requireNonNull(charset);
        Logger.info("Parsing OBJ file {}", objFileURL);
        try (InputStream is = objFileURL.openStream()) {
            final var reader = new BufferedReader(new InputStreamReader(is, charset));
            parseMaterialLibraryDefinitions(reader);
        }
        try (InputStream is = objFileURL.openStream()) {
            final var reader = new BufferedReader(new InputStreamReader(is, charset));
            parseObjectsAndGroups(reader);
        }
    }

    // Private

    private void commitMesh() {
        TriangleMesh mesh = createTriangleMesh();
        if (mesh != null) {
            if (currentMeshDef == null) {
                currentMeshDef = new MeshDefinition(nextAnonMeshName());
            }
            meshMap.put(currentMeshDef.name, mesh);
            Logger.info("Mesh '{}', vertices: {}, texture coordinates: {}, faces: {}, smoothing groups: {}",
                currentMeshDef.name,
                mesh.getPoints().size() / mesh.getPointElementSize(),
                mesh.getTexCoords().size() / mesh.getTexCoordElementSize(),
                mesh.getFaces().size() / mesh.getFaceElementSize(),
                mesh.getFaceSmoothingGroups().size());
        }
    }

    // Search for material library definitions
    private void parseMaterialLibraryDefinitions(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (startsWith(line, Keyword.MATERIAL_LIB)) {
                final String libName = params(line, Keyword.MATERIAL_LIB);
                Logger.info("Material library definition found: '{}'", libName);
                if (materialLibsMap.containsKey(libName)) {
                    Logger.warn("Material library definition will be ignored (already defined): {}", libName);
                }
                else {
                    final Map<String, PhongMaterial> lib = parseMaterialLibraryFile(libName);
                    if (lib != null) {
                        Logger.info("Material library parsed: {}", libName);
                        materialLibsMap.put(libName, lib);
                    }
                    else Logger.error("Material library {} could not be parsed");
                }
            }
        }
    }

    private Map<String, PhongMaterial> parseMaterialLibraryFile(String libName) throws IOException {
        final int lastSlash = objFileURL.toExternalForm().lastIndexOf('/');
        if (lastSlash == -1) {
            Logger.error("OBJ file URL looks strange: {}", objFileURL);
            throw new RuntimeException();
        }
        final String libURL = objFileURL.toExternalForm().substring(0, lastSlash) + "/" + libName;
        Logger.info("Material library URL (derived from OBJ file URL): {}", libURL);

        try {
            final URI uri = new URI(libURL);
            try (InputStream is = uri.toURL().openStream()) {
                final var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                final MtlFileParser parser = new MtlFileParser();
                parser.parse(reader);
                return parser.materialMap();
            }
        } catch (URISyntaxException e) {
            Logger.error("Invalid material library URL: {}", libURL);
            throw new RuntimeException(e);
        }
    }

    private void parseObjectsAndGroups(BufferedReader reader) throws IOException {
        String line;
        lineNo = 0;
        while ((line = reader.readLine()) != null) {
            ++lineNo;

            if (line.isBlank() || line.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (fullMatch(line, Keyword.OBJECT)) {
                commitMesh();
                currentMeshDef = new MeshDefinition(nextAnonMeshName());
            }
            else if (startsWith(line, Keyword.OBJECT)) {
                commitMesh();
                currentMeshDef = new MeshDefinition(params(line, Keyword.OBJECT));
            }
            else if (fullMatch(line, Keyword.GROUP)) {
                commitMesh();
                currentMeshDef = new MeshDefinition(nextAnonMeshName());
            }
            else if (startsWith(line, Keyword.GROUP)) {
                commitMesh();
                currentMeshDef = new MeshDefinition(params(line, Keyword.GROUP));
            }
            else if (startsWith(line, Keyword.SMOOTHING_GROUP)) {
                parseSmoothingGroup(params(line, Keyword.SMOOTHING_GROUP));
            }
            else if (startsWith(line, Keyword.VERTEX)) {
                parseVertex(params(line, Keyword.VERTEX));
            }
            else if (startsWith(line, Keyword.VERTEX_NORMAL)) {
                parseVertexNormal(params(line, Keyword.VERTEX_NORMAL));
            }
            else if (startsWith(line, Keyword.TEX_COORD)) {
                parseTextureCoordinate(params(line, Keyword.TEX_COORD));
            }
            else if (startsWith(line, Keyword.FACE)) {
                parseFace(params(line, Keyword.FACE));
            }
            else if (startsWith(line, Keyword.MATERIAL_USAGE)) {
                final String materialName = params(line, Keyword.MATERIAL_USAGE);
                Logger.trace("Material usage '{}'", materialName);
                currentMeshDef.materialName = materialName;
            }
            else if (startsWith(line, Keyword.MATERIAL_LIB)) {
                // already processed in pass 1
                Logger.debug("Material library definition");
            }
            else {
                Logger.warn("Line skipped: {} (no idea what it wants from me)", line);
            }
        }
        commitMesh();
    }

    /**
     * Parses an OBJ face statement and triangulates it.
     * Supports all legal formats:
     *   f v
     *   f v/vt
     *   f v//vn
     *   f v/vt/vn
     *   and polygons with 3+ vertices.
     */
    private void parseFace(String argText) {

        // 1. Split the face into vertex blocks
        String[] blocks = splitBySpace(argText);

        // 2. Parse each block into (v, vt, vn) indices
        FaceVertex[] verts = new FaceVertex[blocks.length];
        boolean hasUV = true;
        boolean hasNormal = true;

        for (int i = 0; i < blocks.length; i++) {
            verts[i] = parseFaceVertex(blocks[i]);

            if (verts[i].vt == null) {
                hasUV = false;
            }
            if (verts[i].vn == null) {
                hasNormal = false;
            }
        }

        // 3. Convert OBJ indices to internal 0-based indices
        for (FaceVertex fv : verts) {
            fv.v = vertexIndex(fv.v);

            if (hasUV) {
                fv.vt = uvIndex(fv.vt);
                if (fv.vt < 0) hasUV = false;
            }

            if (hasNormal) {
                fv.vn = normalIndex(fv.vn);
                if (fv.vn < 0) hasNormal = false;
            }
        }

        // 4. Triangulate using a fan: (v0, v[i], v[i+1])
        for (int i = 1; i < verts.length - 1; i++) {
            FaceVertex v1 = verts[0];
            FaceVertex v2 = verts[i];
            FaceVertex v3 = verts[i + 1];

            // Vertex + UV indices
            facesList.add(v1.v);
            facesList.add(hasUV ? v1.vt : -1);

            facesList.add(v2.v);
            facesList.add(hasUV ? v2.vt : -1);

            facesList.add(v3.v);
            facesList.add(hasUV ? v3.vt : -1);

            // Normal indices
            faceNormalsList.add(hasNormal ? v1.vn : -1);
            faceNormalsList.add(hasNormal ? v2.vn : -1);
            faceNormalsList.add(hasNormal ? v3.vn : -1);

            // Smoothing group
            smoothingGroupList.add(currentSmoothingGroup);
        }
    }

    /** Helper record for a face vertex */
    private static class FaceVertex {
        Integer v;   // vertex index
        Integer vt;  // texture index (nullable)
        Integer vn;  // normal index (nullable)
    }

    /** Parses a single face vertex block like "3/4/5", "3//5", "3/4", or "3". */
    private FaceVertex parseFaceVertex(String block) {
        String[] parts = block.split("/", -1); // keep empty fields
        FaceVertex fv = new FaceVertex();

        fv.v = Integer.parseInt(parts[0]);

        if (parts.length > 1 && !parts[1].isEmpty()) {
            fv.vt = Integer.parseInt(parts[1]);
        }

        if (parts.length > 2 && !parts[2].isEmpty()) {
            fv.vn = Integer.parseInt(parts[2]);
        }

        return fv;
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

        if (currentMeshDef.materialName != null) {
            for (var materialLibName : materialLibsMap.keySet()) {
                final var materialLib = materialLibsMap.get(materialLibName);
                if (materialLib.containsKey(currentMeshDef.materialName)) {
                    final PhongMaterial material = materialLib.get(currentMeshDef.materialName);
                    modelMaterialAssignments.put(mesh, material);
                }
            }
        }

        facesStart = facesList.size();
        facesNormalStart = faceNormalsList.size();
        smoothingGroupsStart = smoothingGroupList.size();

        return mesh;
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

    private static List<Integer> restOfList(List<Integer> list, int start) {
        return list.subList(start, list.size());
    }

    private static boolean fullMatch(String line, Keyword keyword) {
        return line.equals(keyword.text);
    }

    private static boolean startsWith(String line, Keyword keyword) {
        return line.startsWith(keyword.text + " ");
    }

    private static String params(String line, Keyword keyword) {
        return line.substring(keyword.text.length() + 1).trim();
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

    private String nextAnonMeshName() {
        return "default" + anonMeshNameCount++;
    }
}
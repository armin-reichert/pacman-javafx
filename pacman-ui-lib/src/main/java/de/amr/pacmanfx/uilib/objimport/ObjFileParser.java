/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * OBJ file parser created by Copilot AI.
 */
public class ObjFileParser {

    /* -------------------------------------------------------------
     *  TOKENIZER
     * ------------------------------------------------------------- */

    public enum ObjKeyword {
        OBJECT            ("o"),
        GROUP             ("g"),
        MATERIAL_LIB      ("mtllib"),
        MATERIAL_USAGE    ("usemtl"),
        SMOOTHING_GROUP   ("s"),
        VERTEX            ("v"),
        VERTEX_NORMAL     ("vn"),
        TEX_COORD         ("vt"),
        FACE              ("f"),
        UNKNOWN           ("");

        private final String text;

        ObjKeyword(String text) {
            this.text = text;
        }

        static ObjKeyword fromText(String text) {
            for (ObjKeyword keyword : values()) {
                if (keyword.text.equals(text)) {
                    return keyword;
                }
            }
            return UNKNOWN;
        }
    }

    public record Token(ObjKeyword keyword, String args, int lineNo) {
        public Token(String keyword, String args, int lineNo) {
            this(ObjKeyword.fromText(keyword), args, lineNo);
        }
    }

    public static class Tokenizer {
        private final BufferedReader reader;
        private int lineNo = 0;

        public Tokenizer(BufferedReader reader) {
            this.reader = reader;
        }

        public Token next() throws IOException {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNo++;

                int hash = line.indexOf('#');
                if (hash >= 0) {
                    line = line.substring(0, hash);
                }

                line = line.strip();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+", 2);
                String keyword = parts[0];
                String args = parts.length > 1 ? parts[1].strip() : "";

                return new Token(keyword, args, lineNo);
            }

            return null;
        }
    }

    /* -------------------------------------------------------------
     *  OBJ MODEL STRUCTURE
     * ------------------------------------------------------------- */

    public static class ObjModel {
        public final List<Vertex>      vertices      = new ArrayList<>();
        public final List<TexCoord>    texCoords     = new ArrayList<>();
        public final List<Normal>      normals       = new ArrayList<>();

        public final List<ObjObject>   objects       = new ArrayList<>();
        public final List<ObjMaterialLib> materialLibs = new ArrayList<>();

        public ObjObject currentObject;
        public ObjGroup  currentGroup;
        public String    currentMaterialName;
        public Integer   currentSmoothingGroup;
    }

    public static class ObjMaterialLib {
        public final String fileName;
        public ObjMaterialLib(String fileName) { this.fileName = fileName; }
    }

    public static class ObjObject {
        public final String name;
        public final List<ObjGroup> groups = new ArrayList<>();
        public ObjObject(String name) { this.name = name; }
    }

    public static class ObjGroup {
        public final String name;
        public final List<ObjFace> faces = new ArrayList<>();
        public ObjGroup(String name) { this.name = name; }
    }

    public static class ObjFace {
        public final List<FaceVertex> vertices = new ArrayList<>();
        public final String  materialName;
        public final Integer smoothingGroup;

        public ObjFace(String materialName, Integer smoothingGroup) {
            this.materialName = materialName;
            this.smoothingGroup = smoothingGroup;
        }
    }

    public static class FaceVertex {
        public final int vIndex;
        public final int vtIndex;
        public final int vnIndex;

        public FaceVertex(int vIndex, int vtIndex, int vnIndex) {
            this.vIndex = vIndex;
            this.vtIndex = vtIndex;
            this.vnIndex = vnIndex;
        }
    }

    public record Vertex(float x, float y, float z) {}
    public record TexCoord(float u, float v) {}
    public record Normal(float x, float y, float z) {}

    /* -------------------------------------------------------------
     *  FIELDS
     * ------------------------------------------------------------- */

    private Tokenizer tokenizer;
    private final URL objFileURL;
    private int anonMeshNameCount = 0;
    private final ObjModel objModel;

    private final Map<String, Map<String, PhongMaterial>> materialLibsMap = new HashMap<>();

    /* -------------------------------------------------------------
     *  CONSTRUCTOR
     * ------------------------------------------------------------- */

    public ObjFileParser(URL objFileURL, Charset charset) throws IOException {
        this.objFileURL = requireNonNull(objFileURL);
        requireNonNull(charset);

        objModel = new ObjModel();
        try (InputStream is = objFileURL.openStream()) {
            parseMaterialLibraries(new BufferedReader(new InputStreamReader(is, charset)));
        }
        try (InputStream is = objFileURL.openStream()) {
            parseGeometry(new BufferedReader(new InputStreamReader(is, charset)));
        }
    }

    public ObjModel objModel() {
        return objModel;
    }

    public Map<String, Map<String, PhongMaterial>> materialLibsMap() {
        return materialLibsMap;
    }

    /* -------------------------------------------------------------
     *  MATERIAL LIBRARIES
     * ------------------------------------------------------------- */

    private void parseMaterialLibraries(BufferedReader reader) throws IOException {
        tokenizer = new Tokenizer(reader);

        for (Token token; (token = tokenizer.next()) != null; ) {
            if (token.keyword() == ObjKeyword.MATERIAL_LIB) {
                final String libName = token.args();
                Logger.info("Material library found: '{}'", libName);

                if (!materialLibsMap.containsKey(libName)) {
                    Map<String, PhongMaterial> lib = parseMaterialLibraryFile(libName);
                    if (lib != null) {
                        materialLibsMap.put(libName, lib);
                        Logger.info("Material library parsed: {}", libName);
                    }
                }
            }
        }
    }

    private Map<String, PhongMaterial> parseMaterialLibraryFile(String libName) throws IOException {
        final String objFileURLString = objFileURL.toExternalForm();
        final int endOfPath = objFileURLString.lastIndexOf('/');
        if (endOfPath == -1) {
            Logger.error("OBJ file URL has no path: {}", objFileURLString);
            throw new RuntimeException();
        }

        final String libURL = objFileURLString.substring(0, endOfPath) + "/" + libName;
        Logger.info("Material library URL: {}", libURL);

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

    /* -------------------------------------------------------------
     *  OBJ PARSING
     * ------------------------------------------------------------- */

    private void parseGeometry(BufferedReader reader) throws IOException {
        tokenizer = new Tokenizer(reader);
        Token token;

        while ((token = tokenizer.next()) != null) {
            switch (token.keyword()) {

                case OBJECT -> parseObject(token.args());
                case GROUP  -> parseGroup(token.args());
                case SMOOTHING_GROUP -> parseSmoothingGroup(token.args());
                case MATERIAL_USAGE  -> parseMaterialUsage(token.args());

                case VERTEX        -> objModel.vertices.add(parseVertex(token.args()));
                case TEX_COORD     -> objModel.texCoords.add(parseTexCoord(token.args()));
                case VERTEX_NORMAL -> objModel.normals.add(parseNormal(token.args()));

                case FACE -> parseFace(token.args());

                case MATERIAL_LIB -> Logger.debug("Material library definition ignored in 2nd pass");

                default -> Logger.warn("Unknown keyword '{}' at line {}",
                    token.keyword().text, token.lineNo());
            }
        }
    }

    /* -------------------------------------------------------------
     *  PARSING HELPERS
     * ------------------------------------------------------------- */

    private void parseObject(String name) {
        if (name.isEmpty()) {
            name = "Object." + nextAnonName();
        }

        ObjObject obj = new ObjObject(name);
        objModel.objects.add(obj);
        objModel.currentObject = obj;
        objModel.currentGroup = null;

        Logger.info("Object created: {}", name);
    }

    private void parseGroup(String name) {
        if (objModel.currentObject == null) {
            parseObject("Object." + nextAnonName());
        }

        if (name.isEmpty()) {
            name = "Group." + nextAnonName();
        }

        ObjGroup group = new ObjGroup(name);
        objModel.currentObject.groups.add(group);
        objModel.currentGroup = group;

        Logger.info("Group created: {}", name);
    }

    private void parseSmoothingGroup(String args) {
        if (args.equalsIgnoreCase("off") || args.equals("0")) {
            objModel.currentSmoothingGroup = null;
        } else {
            try {
                objModel.currentSmoothingGroup = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                Logger.error("Invalid smoothing group '{}'", args);
                objModel.currentSmoothingGroup = null;
            }
        }
    }

    private void parseMaterialUsage(String name) {
        objModel.currentMaterialName = name;
        Logger.info("Material usage: {}", name);
    }

    private Vertex parseVertex(String s) {
        String[] parts = splitBySpace(s, 3);
        return new Vertex(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1]),
            Float.parseFloat(parts[2])
        );
    }

    private TexCoord parseTexCoord(String s) {
        String[] parts = splitBySpace(s, 3);
        return new TexCoord(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1])
        );
    }

    private Normal parseNormal(String s) {
        String[] parts = splitBySpace(s, 3);
        return new Normal(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1]),
            Float.parseFloat(parts[2])
        );
    }

    private void parseFace(String args) {
        if (objModel.currentObject == null) {
            parseObject("Object." + nextAnonName());
        }
        if (objModel.currentGroup == null) {
            parseGroup("Group." + nextAnonName());
        }

        String[] refs = args.split("\\s+");
        List<FaceVertex> verts = new ArrayList<>();

        for (String ref : refs) {
            verts.add(parseFaceVertex(ref));
        }

        triangulateAndStoreFace(verts);
    }

    private FaceVertex parseFaceVertex(String ref) {
        String[] parts = ref.split("/");

        int v  = parseIndex(parts[0], objModel.vertices.size());
        int vt = parts.length > 1 && !parts[1].isEmpty()
            ? parseIndex(parts[1], objModel.texCoords.size())
            : -1;
        int vn = parts.length > 2 && !parts[2].isEmpty()
            ? parseIndex(parts[2], objModel.normals.size())
            : -1;

        return new FaceVertex(v, vt, vn);
    }

    private int parseIndex(String s, int size) {
        int idx = Integer.parseInt(s);
        if (idx < 0) {
            return size + idx;
        }
        return idx - 1;
    }

    private void triangulateAndStoreFace(List<FaceVertex> verts) {
        if (verts.size() < 3) {
            Logger.error("Invalid face with <3 vertices");
            return;
        }

        FaceVertex v0 = verts.get(0);

        for (int i = 1; i < verts.size() - 1; i++) {
            FaceVertex v1 = verts.get(i);
            FaceVertex v2 = verts.get(i + 1);

            ObjFace face = new ObjFace(
                objModel.currentMaterialName,
                objModel.currentSmoothingGroup
            );

            face.vertices.add(v0);
            face.vertices.add(v1);
            face.vertices.add(v2);

            objModel.currentGroup.faces.add(face);
        }
    }

    private String nextAnonName() {
        return "anon." + anonMeshNameCount++;
    }

    private static String[] splitBySpace(String text, int n) {
        return text.trim().split("\\s+", n);
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.objparser;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * OBJ file parser (optimized with pre-scan and fast tokenizers).
 */
public class ObjFileParser {

    public static final int SOURCE_LINES_LIMIT = 999;

    /* -------------------------------------------------------------
     *  SIZE INFO
     * ------------------------------------------------------------- */

    public record ObjSizeInfo(
        long vertexCount,
        long texCoordCount,
        long normalCount,
        long faceCount,
        long faceIndexCount
    ) {}

    private ObjSizeInfo computeObjSizes(InputStream in) throws IOException {
        long v = 0, vt = 0, vn = 0, f = 0, faceRefs = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                char c0 = line.charAt(0);
                switch (c0) {
                    case 'v' -> {
                        if (line.startsWith("v ")) v++;
                        else if (line.startsWith("vt ")) vt++;
                        else if (line.startsWith("vn ")) vn++;
                    }
                    case 'f' -> {
                        if (line.startsWith("f ")) {
                            f++;
                            faceRefs += countFaceReferences(line);
                        }
                    }
                }
            }
        }

        return new ObjSizeInfo(v, vt, vn, f, faceRefs);
    }

    private static int countFaceReferences(String line) {
        int count = 0;
        boolean inToken = false;
        for (int i = 2; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                if (inToken) {
                    count++;
                    inToken = false;
                }
            } else {
                inToken = true;
            }
        }
        if (inToken) count++;
        return count;
    }

    /* -------------------------------------------------------------
     *  FAST TOKENIZERS
     * ------------------------------------------------------------- */

    private static final class FastSpaceTokenizer {
        private String s;
        private int len;
        private int pos;

        public void reset(String s) {
            this.s = s;
            this.len = s.length();
            this.pos = 0;
        }

        public boolean hasNext() {
            skipSpaces();
            return pos < len;
        }

        public String next() {
            skipSpaces();
            int start = pos;
            while (pos < len && s.charAt(pos) != ' ') {
                pos++;
            }
            return s.substring(start, pos);
        }

        private void skipSpaces() {
            while (pos < len && s.charAt(pos) == ' ') {
                pos++;
            }
        }
    }

    private static final class FastFaceRefTokenizer {
        // out = {v, vt, vn}
        public void split(String ref, int[] out) {
            int len = ref.length();
            int part = 0;
            int start = 0;

            for (int i = 0; i < len; i++) {
                if (ref.charAt(i) == '/') {
                    out[part++] = parseInt(ref, start, i);
                    start = i + 1;
                }
            }
            out[part] = parseInt(ref, start, len);
        }

        private int parseInt(String s, int start, int end) {
            if (start == end) return Integer.MIN_VALUE; // empty
            int sign = 1;
            int i = start;
            if (s.charAt(i) == '-') {
                sign = -1;
                i++;
            }
            int val = 0;
            while (i < end) {
                val = val * 10 + (s.charAt(i) - '0');
                i++;
            }
            return val * sign;
        }
    }

    private final FastSpaceTokenizer spaceTok = new FastSpaceTokenizer();
    private final FastFaceRefTokenizer faceTok = new FastFaceRefTokenizer();
    private final int[] faceRef = new int[3];

    /* -------------------------------------------------------------
     *  TOKENIZER (line → keyword + args)
     * ------------------------------------------------------------- */

    public enum ObjKeyword {
        OBJECT("o"),
        GROUP("g"),
        MATERIAL_LIB("mtllib"),
        MATERIAL_USAGE("usemtl"),
        SMOOTHING_GROUP("s"),
        VERTEX("v"),
        VERTEX_NORMAL("vn"),
        TEX_COORD("vt"),
        FACE("f"),
        UNKNOWN("");

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

                // manual split into keyword + args (no regex)
                int len = line.length();
                int i = 0;
                while (i < len && !Character.isWhitespace(line.charAt(i))) {
                    i++;
                }
                String keyword = line.substring(0, i);
                while (i < len && Character.isWhitespace(line.charAt(i))) {
                    i++;
                }
                String args = i < len ? line.substring(i).strip() : "";

                return new Token(keyword, args, lineNo);
            }

            return null;
        }
    }

    /* -------------------------------------------------------------
     *  PARSER
     * ------------------------------------------------------------- */

    private final URL objFileURL;
    private final Charset charset;

    private int anonMeshNameCount = 0;

    public ObjFileParser(URL objFileURL, Charset charset) throws IOException {
        this.objFileURL = requireNonNull(objFileURL);
        this.charset = requireNonNull(charset);
    }

    public ObjModel parse() throws IOException {
        final var objModel = createEmptyModel(objFileURL, charset);

        try (InputStream stream = objFileURL.openStream();
             var reader = new BufferedReader(new InputStreamReader(stream, charset))) {

            Token token;
            final Tokenizer tokenizer = new Tokenizer(reader);
            while ((token = tokenizer.next()) != null) {
                switch (token.keyword()) {
                    case MATERIAL_LIB    -> parseMaterialLibraryRef(objModel, token.args());
                    case OBJECT          -> parseObject(objModel, token.args());
                    case GROUP           -> parseGroup(objModel, token.args());
                    case SMOOTHING_GROUP -> parseSmoothingGroup(objModel, token.args());
                    case MATERIAL_USAGE  -> parseMaterialUsage(objModel, token.args());

                    case VERTEX -> {
                        ObjVertex v = parseVertex(token.args());
                        objModel.vertices.add(v.x());
                        objModel.vertices.add(v.y());
                        objModel.vertices.add(v.z());
                    }

                    case TEX_COORD -> {
                        ObjTexCoord tc = parseTexCoord(token.args());
                        objModel.texCoords.add(tc.u());
                        objModel.texCoords.add(tc.v());
                    }

                    case VERTEX_NORMAL -> {
                        ObjNormal n = parseNormal(token.args());
                        objModel.normals.add(n.x());
                        objModel.normals.add(n.y());
                        objModel.normals.add(n.z());
                    }

                    case FACE -> parseFace(objModel, token.args());

                    default -> Logger.warn("Unknown keyword '{}' at line {}", token.keyword().name(), token.lineNo());
                }
            }
        }
        return objModel;
    }

    /* -------------------------------------------------------------
     *  MODEL CREATION WITH PRE-SCAN
     * ------------------------------------------------------------- */

    private ObjModel createEmptyModel(URL objFileURL, Charset charset) throws IOException {

        // First pass: count geometry
        ObjSizeInfo sizes;
        try (InputStream stream = objFileURL.openStream()) {
            sizes = computeObjSizes(stream);
        }

        // Count lines for metadata
        long lineCount;
        try (InputStream stream = objFileURL.openStream()) {
            lineCount = countLinesFast(stream);
        }

        // Collect first N lines for debugging
        try (InputStream stream = objFileURL.openStream();
             var reader = new BufferedReader(new InputStreamReader(stream, charset))) {

            final StringBuilder sb = new StringBuilder();
            int lineNo = 1;
            for (; lineNo <= SOURCE_LINES_LIMIT; ++lineNo) {
                final String line = reader.readLine();
                if (line == null) {
                    ObjModel model = new ObjModel(sizes);
                    model.setUrl(objFileURL.toExternalForm());
                    model.setSource(sb.toString());
                    return model;
                }
                sb.append(line).append("\n");
            }
            if (reader.readLine() != null) {
                sb.append("... of ").append(lineCount).append(" lines total");
            }

            ObjModel model = new ObjModel(sizes);
            model.setUrl(objFileURL.toExternalForm());
            model.setSource(sb.toString());
            return model;
        }
    }

    public static long countLinesFast(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        long count = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            for (int i = 0; i < n; i++) {
                if (buffer[i] == '\n') {
                    count++;
                }
            }
        }
        return count;
    }

    /* -------------------------------------------------------------
     *  MATERIAL LIBRARIES
     * ------------------------------------------------------------- */

    private void parseMaterialLibraryRef(ObjModel objModel, String libName) {
        if (objModel.materialLibsMap.containsKey(libName)) {
            return;
        }
        Logger.debug("Material library found: '{}'", libName);
        Map<String, ObjMaterial> lib = parseMaterialLibraryFile(libName);
        if (lib != null && !lib.isEmpty()) {
            objModel.materialLibsMap.put(libName, lib);
            Logger.debug("Material library parsed: {}", libName);
        }
    }

    private Map<String, ObjMaterial> parseMaterialLibraryFile(String libName) {
        final String objFileURLString = objFileURL.toExternalForm();
        final int endOfPath = objFileURLString.lastIndexOf('/');
        if (endOfPath == -1) {
            Logger.error("OBJ file URL has no path: {}", objFileURLString);
            throw new RuntimeException();
        }

        final String libURL = objFileURLString.substring(0, endOfPath) + "/" + libName;
        Logger.debug("Material library URL: {}", libURL);
        try {
            @SuppressWarnings("deprecation")
            final URL url = new URL(objFileURL, libName); // avoid URI issues with spaces
            final ObjMtlFileParser parser = new ObjMtlFileParser();
            try (InputStream stream = url.openStream()) {
                parser.parse(stream, charset);
                return parser.materialMap();
            }
        } catch (Exception x) {
            Logger.warn(x, "Material library parsing failed: URL={}", libURL);
            return Map.of();
        }
    }

    /* -------------------------------------------------------------
     *  PARSING HELPERS
     * ------------------------------------------------------------- */

    private void parseObject(ObjModel objModel, String name) {
        if (name.isEmpty()) {
            name = "Object." + nextAnonName();
        }

        ObjObject obj = new ObjObject(name);
        objModel.objects.add(obj);
        objModel.currentObject = obj;
        objModel.currentGroup = null;

        Logger.debug("Object created: {}", name);
    }

    private void parseGroup(ObjModel objModel, String name) {
        if (objModel.currentObject == null) {
            parseObject(objModel, "Object." + nextAnonName());
        }

        if (name.isEmpty()) {
            name = "Group." + nextAnonName();
        }

        ObjGroup group = new ObjGroup(name);
        objModel.currentObject.groups.add(group);
        objModel.currentGroup = group;

        Logger.debug("Group created: {}", name);
    }

    private void parseSmoothingGroup(ObjModel objModel, String args) {
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

    private void parseMaterialUsage(ObjModel objModel, String name) {
        objModel.currentMaterialName = name;
        Logger.debug("Material usage: {}", name);
    }

    private ObjVertex parseVertex(String s) {
        spaceTok.reset(s);
        float x = Float.parseFloat(spaceTok.next());
        float y = Float.parseFloat(spaceTok.next());
        float z = Float.parseFloat(spaceTok.next());
        return new ObjVertex(x, y, z);
    }

    private ObjTexCoord parseTexCoord(String s) {
        spaceTok.reset(s);
        float u = Float.parseFloat(spaceTok.next());
        float v = Float.parseFloat(spaceTok.next());
        return new ObjTexCoord(u, v);
    }

    private ObjNormal parseNormal(String s) {
        spaceTok.reset(s);
        float x = Float.parseFloat(spaceTok.next());
        float y = Float.parseFloat(spaceTok.next());
        float z = Float.parseFloat(spaceTok.next());
        return new ObjNormal(x, y, z);
    }

    private void parseFace(ObjModel objModel, String args) {
        if (objModel.currentObject == null) {
            parseObject(objModel, "Object." + nextAnonName());
        }
        if (objModel.currentGroup == null) {
            parseGroup(objModel, "Group." + nextAnonName());
        }

        List<ObjFaceVertex> verts = new ArrayList<>();

        spaceTok.reset(args);
        while (spaceTok.hasNext()) {
            String ref = spaceTok.next();
            verts.add(parseFaceVertex(objModel, ref));
        }

        triangulateAndStoreFace(objModel, verts);
    }

    private ObjFaceVertex parseFaceVertex(ObjModel objModel, String ref) {
        faceTok.split(ref, faceRef);

        int v  = parseIndex(faceRef[0], objModel.vertexCount());
        int vt = faceRef[1] == Integer.MIN_VALUE ? -1 : parseIndex(faceRef[1], objModel.texCoordCount());
        int vn = faceRef[2] == Integer.MIN_VALUE ? -1 : parseIndex(faceRef[2], objModel.normalCount());

        return new ObjFaceVertex(v, vt, vn);
    }

    private int parseIndex(String s, int size) {
        int idx = Integer.parseInt(s);
        if (idx < 0) {
            return size + idx;
        }
        return idx - 1;
    }

    private int parseIndex(int idx, int size) {
        if (idx < 0) {
            return size + idx;
        }
        return idx - 1;
    }

    private void triangulateAndStoreFace(ObjModel objModel, List<ObjFaceVertex> vertices) {
        if (vertices.size() < 3) {
            Logger.error("Invalid face with <3 vertices");
            return;
        }

        ObjFaceVertex v0 = vertices.get(0);

        for (int i = 1; i < vertices.size() - 1; i++) {
            ObjFaceVertex v1 = vertices.get(i);
            ObjFaceVertex v2 = vertices.get(i + 1);

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
        return "anon_" + anonMeshNameCount++;
    }
}

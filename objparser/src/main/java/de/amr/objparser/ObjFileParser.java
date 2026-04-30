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

import static java.util.Objects.requireNonNull;

/**
 * Fully optimized OBJ parser:
 * - Pre-scan for exact sizes
 * - Zero-allocation tokenizers
 * - Fast float parser
 * - Fastutil-compatible model
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
     *  FAST FLOAT PARSER
     * ------------------------------------------------------------- */

    private static final class FastFloat {
        public static float parse(String s, int start, int end) {
            boolean neg = false;
            int i = start;

            if (i < end) {
                char c = s.charAt(i);
                if (c == '-') { neg = true; i++; }
                else if (c == '+') i++;
            }

            double val = 0.0;

            while (i < end) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') break;
                val = val * 10 + (c - '0');
                i++;
            }

            if (i < end && s.charAt(i) == '.') {
                i++;
                double factor = 0.1;
                while (i < end) {
                    char c = s.charAt(i);
                    if (c < '0' || c > '9') break;
                    val += (c - '0') * factor;
                    factor *= 0.1;
                    i++;
                }
            }

            if (i < end && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                i++;
                boolean expNeg = false;
                if (i < end && s.charAt(i) == '-') { expNeg = true; i++; }
                else if (i < end && s.charAt(i) == '+') i++;

                int exp = 0;
                while (i < end) {
                    char c = s.charAt(i);
                    if (c < '0' || c > '9') break;
                    exp = exp * 10 + (c - '0');
                    i++;
                }

                val = val * Math.pow(10, expNeg ? -exp : exp);
            }

            return neg ? (float)-val : (float)val;
        }
    }

    /* -------------------------------------------------------------
     *  FAST TOKENIZERS
     * ------------------------------------------------------------- */

    private static final class FastSpaceTokenizer {
        private String s;
        private int len;
        private int pos;
        private int start;
        private int end;

        public void reset(String s) {
            this.s = s;
            this.len = s.length();
            this.pos = 0;
        }

        public boolean next() {
            while (pos < len && Character.isWhitespace(s.charAt(pos))) pos++;
            if (pos >= len) return false;

            start = pos;
            while (pos < len && !Character.isWhitespace(s.charAt(pos))) pos++;
            end = pos;
            return true;
        }

        public String source() { return s; }
        public int tokenStart() { return start; }
        public int tokenEnd() { return end; }
        public String token() { return s.substring(start, end); }
    }

    private static final class FastFaceRefTokenizer {
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
            if (start == end) return Integer.MIN_VALUE;
            int sign = 1;
            int i = start;
            if (s.charAt(i) == '-') { sign = -1; i++; }
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

        ObjKeyword(String text) { this.text = text; }

        static ObjKeyword fromText(String text) {
            for (ObjKeyword k : values()) if (k.text.equals(text)) return k;
            return UNKNOWN;
        }
    }

    public record Token(ObjKeyword keyword, String args, int lineNo) {}

    public static class Tokenizer {
        private final BufferedReader reader;
        private int lineNo = 0;

        public Tokenizer(BufferedReader reader) { this.reader = reader; }

        public Token next() throws IOException {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNo++;

                int hash = line.indexOf('#');
                if (hash >= 0) line = line.substring(0, hash);

                line = line.strip();
                if (line.isEmpty()) continue;

                int len = line.length();
                int i = 0;
                while (i < len && !Character.isWhitespace(line.charAt(i))) i++;
                String keyword = line.substring(0, i);

                while (i < len && Character.isWhitespace(line.charAt(i))) i++;
                String args = i < len ? line.substring(i) : "";

                return new Token(ObjKeyword.fromText(keyword), args, lineNo);
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

    public ObjFileParser(URL objFileURL, Charset charset) {
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

                    case VERTEX -> parseVertexInto(objModel, token.args());
                    case TEX_COORD -> parseTexCoordInto(objModel, token.args());
                    case VERTEX_NORMAL -> parseNormalInto(objModel, token.args());

                    case FACE -> parseFace(objModel, token.args());

                    default -> Logger.warn("Unknown keyword '{}' at line {}", token.keyword(), token.lineNo());
                }
            }
        }

        return objModel;
    }

    /* -------------------------------------------------------------
     *  MODEL CREATION WITH PRE-SCAN
     * ------------------------------------------------------------- */

    private ObjModel createEmptyModel(URL url, Charset charset) throws IOException {
        ObjSizeInfo sizes;
        try (InputStream stream = url.openStream()) {
            sizes = computeObjSizes(stream);
        }

        long lineCount;
        try (InputStream stream = url.openStream()) {
            lineCount = countLinesFast(stream);
        }

        try (InputStream stream = url.openStream();
             var reader = new BufferedReader(new InputStreamReader(stream, charset))) {

            final StringBuilder sb = new StringBuilder();
            int lineNo = 1;

            for (; lineNo <= SOURCE_LINES_LIMIT; lineNo++) {
                String line = reader.readLine();
                if (line == null) break;
                sb.append(line).append("\n");
            }

            ObjModel model = new ObjModel(sizes);
            model.setUrl(url.toExternalForm());
            model.setSource(sb.toString());
            return model;
        }
    }

    public static long countLinesFast(InputStream in) throws IOException {
        byte[] buf = new byte[8192];
        long count = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            for (int i = 0; i < n; i++) if (buf[i] == '\n') count++;
        }
        return count;
    }

    /* -------------------------------------------------------------
     *  MATERIAL LIBRARIES
     * ------------------------------------------------------------- */

    private void parseMaterialLibraryRef(ObjModel objModel, String libName) {
        if (objModel.materialLibsMap.containsKey(libName)) return;

        try {
            @SuppressWarnings("deprecation")
            URL url = new URL(objFileURL, libName);
            ObjMtlFileParser parser = new ObjMtlFileParser();
            try (InputStream stream = url.openStream()) {
                parser.parse(stream, charset);
                objModel.materialLibsMap.put(libName, parser.materialMap());
            }
        } catch (Exception x) {
            Logger.warn(x, "Material library parsing failed: {}", libName);
        }
    }

    /* -------------------------------------------------------------
     *  PARSING HELPERS
     * ------------------------------------------------------------- */

    private void parseObject(ObjModel model, String name) {
        if (name.isEmpty()) name = "Object." + nextAnonName();
        ObjObject obj = new ObjObject(name);
        model.objects.add(obj);
        model.currentObject = obj;
        model.currentGroup = null;
    }

    private void parseGroup(ObjModel model, String name) {
        if (model.currentObject == null) parseObject(model, "Object." + nextAnonName());
        if (name.isEmpty()) name = "Group." + nextAnonName();
        ObjGroup group = new ObjGroup(name);
        model.currentObject.groups.add(group);
        model.currentGroup = group;
    }

    private void parseSmoothingGroup(ObjModel model, String args) {
        if (args.equals("off") || args.equals("0")) {
            model.currentSmoothingGroup = null;
        } else {
            try {
                model.currentSmoothingGroup = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                model.currentSmoothingGroup = null;
            }
        }
    }

    private void parseMaterialUsage(ObjModel model, String name) {
        model.currentMaterialName = name;
    }

    /* -------------------------------------------------------------
     *  GEOMETRY PARSING (FAST FLOAT)
     * ------------------------------------------------------------- */

    private void parseVertexInto(ObjModel model, String s) {
        spaceTok.reset(s);

        spaceTok.next();
        float x = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        spaceTok.next();
        float y = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        spaceTok.next();
        float z = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        model.vertices.add(x);
        model.vertices.add(y);
        model.vertices.add(z);
    }

    private void parseTexCoordInto(ObjModel model, String s) {
        spaceTok.reset(s);

        spaceTok.next();
        float u = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        spaceTok.next();
        float v = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        model.texCoords.add(u);
        model.texCoords.add(v);
    }

    private void parseNormalInto(ObjModel model, String s) {
        spaceTok.reset(s);

        spaceTok.next();
        float x = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        spaceTok.next();
        float y = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        spaceTok.next();
        float z = FastFloat.parse(spaceTok.source(), spaceTok.tokenStart(), spaceTok.tokenEnd());

        model.normals.add(x);
        model.normals.add(y);
        model.normals.add(z);
    }

    /* -------------------------------------------------------------
     *  FACE PARSING
     * ------------------------------------------------------------- */

    private void parseFace(ObjModel model, String args) {
        if (model.currentObject == null) parseObject(model, "Object." + nextAnonName());
        if (model.currentGroup == null) parseGroup(model, "Group." + nextAnonName());

        List<ObjFaceVertex> verts = new ArrayList<>();

        spaceTok.reset(args);
        while (spaceTok.next()) {
            String ref = spaceTok.token();
            verts.add(parseFaceVertex(model, ref));
        }

        triangulate(model, verts);
    }

    private ObjFaceVertex parseFaceVertex(ObjModel model, String ref) {
        faceTok.split(ref, faceRef);

        int v  = parseIndex(faceRef[0], model.vertexCount());
        int vt = faceRef[1] == Integer.MIN_VALUE ? -1 : parseIndex(faceRef[1], model.texCoordCount());
        int vn = faceRef[2] == Integer.MIN_VALUE ? -1 : parseIndex(faceRef[2], model.normalCount());

        return new ObjFaceVertex(v, vt, vn);
    }

    private int parseIndex(int idx, int size) {
        return idx < 0 ? size + idx : idx - 1;
    }

    private void triangulate(ObjModel model, List<ObjFaceVertex> v) {
        if (v.size() < 3) return;

        ObjFaceVertex v0 = v.get(0);

        for (int i = 1; i < v.size() - 1; i++) {
            ObjFaceVertex v1 = v.get(i);
            ObjFaceVertex v2 = v.get(i + 1);

            ObjFace face = new ObjFace(model.currentMaterialName, model.currentSmoothingGroup);
            face.vertices.add(v0);
            face.vertices.add(v1);
            face.vertices.add(v2);

            model.currentGroup.faces.add(face);
        }
    }

    private String nextAnonName() {
        return "anon_" + anonMeshNameCount++;
    }
}

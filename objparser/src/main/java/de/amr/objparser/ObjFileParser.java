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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * OBJ file parser created by Copilot AI.
 */
public class ObjFileParser {

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

    private final URL objFileURL;
    private final Charset charset;

    private Tokenizer tokenizer;
    private int anonMeshNameCount = 0;

    public ObjFileParser(URL objFileURL, Charset charset) throws IOException {
        this.objFileURL = requireNonNull(objFileURL);
        this.charset = requireNonNull(charset);
    }

    public ObjModel parse() throws IOException {
        final var objModel = new ObjModel();
        try (InputStream stream = objFileURL.openConnection().getInputStream()) {
            final var reader = new BufferedReader(new InputStreamReader(stream, charset));
            String source = reader.lines().collect(Collectors.joining("\n"));
            objModel.setSource(source);
        }
        try (InputStream stream = objFileURL.openConnection().getInputStream()) {
            parseMaterialLibraries(objModel, stream);
        }
        try (InputStream stream = objFileURL.openConnection().getInputStream()) {
            parseGeometry(objModel, stream);
        }
        objModel.setUrl(objFileURL.toString());
        return objModel;
    }

    /* -------------------------------------------------------------
     *  MATERIAL LIBRARIES
     * ------------------------------------------------------------- */

    private void parseMaterialLibraries(ObjModel objModel, InputStream stream) throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(stream, charset));
        tokenizer = new Tokenizer(reader);
        for (Token token; (token = tokenizer.next()) != null; ) {
            if (token.keyword() == ObjKeyword.MATERIAL_LIB) {
                final String libName = token.args();
                Logger.debug("Material library found: '{}'", libName);
                if (!objModel.materialLibsMap.containsKey(libName)) {
                    Map<String, ObjMaterial> lib = parseMaterialLibraryFile(libName);
                    if (lib != null) {
                        objModel.materialLibsMap.put(libName, lib);
                        Logger.debug("Material library parsed: {}", libName);
                    }
                }
            }
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
            final URL url = new URL(objFileURL, libName); // use this instead of URI to avoid issues with spaces in name
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
     *  OBJ PARSING
     * ------------------------------------------------------------- */

    private void parseGeometry(ObjModel objModel, InputStream stream) throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(stream, charset));
        tokenizer = new Tokenizer(reader);
        Token token;

        while ((token = tokenizer.next()) != null) {
            switch (token.keyword()) {

                case OBJECT -> parseObject(objModel, token.args());
                case GROUP  -> parseGroup(objModel, token.args());
                case SMOOTHING_GROUP -> parseSmoothingGroup(objModel, token.args());
                case MATERIAL_USAGE  -> parseMaterialUsage(objModel, token.args());

                case VERTEX        -> objModel.vertices.add(parseVertex(token.args()));
                case TEX_COORD     -> objModel.texCoords.add(parseTexCoord(token.args()));
                case VERTEX_NORMAL -> objModel.normals.add(parseNormal(token.args()));

                case FACE -> parseFace(objModel, token.args());

                case MATERIAL_LIB -> Logger.debug("Material library definition ignored in 2nd pass");

                default -> Logger.warn("Unknown keyword '{}' at line {}",
                    token.keyword().text, token.lineNo());
            }
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
        String[] parts = splitBySpace(s, 3);
        return new ObjVertex(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1]),
            Float.parseFloat(parts[2])
        );
    }

    private ObjTexCoord parseTexCoord(String s) {
        String[] parts = splitBySpace(s, 3);
        return new ObjTexCoord(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1])
        );
    }

    private ObjNormal parseNormal(String s) {
        String[] parts = splitBySpace(s, 3);
        return new ObjNormal(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1]),
            Float.parseFloat(parts[2])
        );
    }

    private void parseFace(ObjModel objModel, String args) {
        if (objModel.currentObject == null) {
            parseObject(objModel, "Object." + nextAnonName());
        }
        if (objModel.currentGroup == null) {
            parseGroup(objModel, "Group." + nextAnonName());
        }

        String[] refs = args.split("\\s+");
        List<ObjFaceVertex> verts = new ArrayList<>();

        for (String ref : refs) {
            verts.add(parseFaceVertex(objModel, ref));
        }

        triangulateAndStoreFace(objModel, verts);
    }

    private ObjFaceVertex parseFaceVertex(ObjModel objModel, String ref) {
        String[] parts = ref.split("/", -1);

        int v  = parseIndex(parts[0], objModel.vertices.size());
        int vt = parts.length > 1 && !parts[1].isEmpty()
            ? parseIndex(parts[1], objModel.texCoords.size())
            : -1;
        int vn = parts.length > 2 && !parts[2].isEmpty()
            ? parseIndex(parts[2], objModel.normals.size())
            : -1;

        return new ObjFaceVertex(v, vt, vn);
    }

    private int parseIndex(String s, int size) {
        int idx = Integer.parseInt(s);
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

    private static String[] splitBySpace(String text, int n) {
        return text.trim().split("\\s+", n);
    }
}

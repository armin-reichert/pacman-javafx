/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fully extended Wavefront MTL parser supporting all material properties,
 * texture maps, and texture options.
 */
public class ObjMtlFileParser {

    /* -------------------------------------------------------------
     *  KEYWORDS
     * ------------------------------------------------------------- */

    public enum Keyword {
        NEW_MATERIAL       ("newmtl"),

        // Scalars
        OPACITY            ("d"),
        TRANSPARENCY       ("Tr"),
        ILLUMINATION       ("illum"),
        SPECULAR_POWER     ("Ns"),
        REFRACTION_INDEX   ("Ni"),

        // Colors
        AMBIENT_COLOR      ("Ka"),
        DIFFUSE_COLOR      ("Kd"),
        SPECULAR_COLOR     ("Ks"),
        EMISSIVE_COLOR     ("Ke"),
        TRANSMISSION_FILTER("Tf"),

        // Texture maps
        MAP_KA             ("map_Ka"),
        MAP_KD             ("map_Kd"),
        MAP_KS             ("map_Ks"),
        MAP_KE             ("map_Ke"),
        MAP_NS             ("map_Ns"),
        MAP_D              ("map_d"),
        MAP_TR             ("map_Tr"),
        MAP_BUMP           ("map_Bump"),
        BUMP               ("bump"),
        MAP_DISP           ("disp"),
        MAP_DECAL          ("decal"),
        MAP_REFL           ("map_refl"),
        REFL               ("refl"),

        UNKNOWN            ("");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }

        static Keyword fromText(String text) {
            for (Keyword k : values()) {
                if (k.text.equals(text)) return k;
            }
            return UNKNOWN;
        }
    }

    public record Token(Keyword keyword, String text, String args, int lineNo) {
        public Token(String text, String args, int lineNo) {
            this(Keyword.fromText(text), text, args, lineNo);
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
                if (hash >= 0) line = line.substring(0, hash);

                line = line.strip();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String keyword = parts[0];
                String args = parts.length > 1 ? parts[1].strip() : "";

                return new Token(keyword, args, lineNo);
            }

            return null;
        }
    }

    /* -------------------------------------------------------------
     *  STATE
     * ------------------------------------------------------------- */

    private final Map<String, ObjMaterial> materialMap = new LinkedHashMap<>();
    private ObjMaterial currentObjMaterial;
    private Tokenizer tokenizer;

    public Map<String, ObjMaterial> materialMap() {
        return Collections.unmodifiableMap(materialMap);
    }

    /* -------------------------------------------------------------
     *  PARSER ENTRY POINT
     * ------------------------------------------------------------- */

    public void parse(InputStream stream, Charset charset) throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(stream, charset));
        tokenizer = new Tokenizer(reader);

        Token token;
        while ((token = tokenizer.next()) != null) {
            switch (token.keyword) {

                case NEW_MATERIAL -> {
                    commitCurrentMaterial();
                    currentObjMaterial = new ObjMaterial(token.args);
                }

                /* --- Scalars --- */
                case SPECULAR_POWER -> currentObjMaterial.Ns = parseSpecularPower(token.args);
                case OPACITY        -> currentObjMaterial.d  = parseOpacity(token.args);
                case TRANSPARENCY   -> currentObjMaterial.d  = 1.0f - parseOpacity(token.args);
                case ILLUMINATION   -> currentObjMaterial.illum = parseIllumination(token.args);
                case REFRACTION_INDEX -> currentObjMaterial.Ni = parseRefractionIndex(token.args);

                /* --- Colors --- */
                case AMBIENT_COLOR  -> currentObjMaterial.Ka = parseColorRGB(token.args, ObjMaterial.DEFAULT_COLOR);
                case DIFFUSE_COLOR  -> currentObjMaterial.Kd = parseColorRGB(token.args, ObjMaterial.DEFAULT_COLOR);
                case SPECULAR_COLOR -> currentObjMaterial.Ks = parseColorRGB(token.args, ObjMaterial.DEFAULT_COLOR);
                case EMISSIVE_COLOR -> currentObjMaterial.Ke = parseColorRGB(token.args, ObjMaterial.DEFAULT_COLOR);
                case TRANSMISSION_FILTER -> currentObjMaterial.Tf = parseColorRGB(token.args, ObjMaterial.DEFAULT_COLOR);

                /* --- Texture maps --- */
                case MAP_KA   -> parseTextureMap(currentObjMaterial, "map_Ka", token.args);
                case MAP_KD   -> parseTextureMap(currentObjMaterial, "map_Kd", token.args);
                case MAP_KS   -> parseTextureMap(currentObjMaterial, "map_Ks", token.args);
                case MAP_KE   -> parseTextureMap(currentObjMaterial, "map_Ke", token.args);
                case MAP_NS   -> parseTextureMap(currentObjMaterial, "map_Ns", token.args);
                case MAP_D    -> parseTextureMap(currentObjMaterial, "map_d",  token.args);
                case MAP_TR   -> parseTextureMap(currentObjMaterial, "map_Tr", token.args);
                case MAP_BUMP -> parseTextureMap(currentObjMaterial, "map_Bump", token.args);
                case BUMP     -> parseTextureMap(currentObjMaterial, "bump", token.args);
                case MAP_DISP -> parseTextureMap(currentObjMaterial, "disp", token.args);
                case MAP_DECAL-> parseTextureMap(currentObjMaterial, "decal", token.args);
                case MAP_REFL -> parseTextureMap(currentObjMaterial, "map_refl", token.args);
                case REFL     -> parseTextureMap(currentObjMaterial, "refl", token.args);

                default -> Logger.warn("Unknown keyword '{}' at line {}", token.text, token.lineNo);
            }
        }

        commitCurrentMaterial();
    }

    /* -------------------------------------------------------------
     *  TEXTURE MAP PARSER
     * ------------------------------------------------------------- */

    private void parseTextureMap(ObjMaterial mat, String mapName, String args) {
        String[] parts = args.split("\\s+");
        int i = 0;

        Map<String, Object> opts = new LinkedHashMap<>();

        while (i < parts.length && parts[i].startsWith("-")) {
            String opt = parts[i];

            switch (opt) {
                case "-o" -> {
                    float u = Float.parseFloat(parts[++i]);
                    float v = Float.parseFloat(parts[++i]);
                    float w = Float.parseFloat(parts[++i]);
                    opts.put("o", new float[]{u, v, w});
                }
                case "-s" -> {
                    float u = Float.parseFloat(parts[++i]);
                    float v = Float.parseFloat(parts[++i]);
                    float w = Float.parseFloat(parts[++i]);
                    opts.put("s", new float[]{u, v, w});
                }
                case "-t" -> {
                    float u = Float.parseFloat(parts[++i]);
                    float v = Float.parseFloat(parts[++i]);
                    float w = Float.parseFloat(parts[++i]);
                    opts.put("t", new float[]{u, v, w});
                }
                case "-mm" -> {
                    float base = Float.parseFloat(parts[++i]);
                    float gain = Float.parseFloat(parts[++i]);
                    opts.put("mm", new float[]{base, gain});
                }
                case "-blendu", "-blendv", "-clamp", "-cc" -> {
                    String val = parts[++i];
                    opts.put(opt.substring(1), val.equalsIgnoreCase("on"));
                }
                case "-texres" -> {
                    int res = Integer.parseInt(parts[++i]);
                    opts.put("texres", res);
                }
                default -> Logger.warn("Unknown texture option '{}' in map '{}'", opt, mapName);
            }

            i++;
        }

        String filename = parts[i];

        switch (mapName) {
            case "map_Ka" -> mat.map_Ka = filename;
            case "map_Kd" -> mat.map_Kd = filename;
            case "map_Ks" -> mat.map_Ks = filename;
            case "map_Ke" -> mat.map_Ke = filename;
            case "map_Ns" -> mat.map_Ns = filename;
            case "map_d"  -> mat.map_d  = filename;
            case "map_Tr" -> mat.map_d  = filename;
            case "map_Bump", "bump" -> mat.map_bump = filename;
            case "disp" -> mat.disp = filename;
            case "decal" -> mat.decal = filename;
            case "map_refl", "refl" -> mat.map_refl = filename;
        }

        if (!opts.isEmpty()) {
            opts.forEach((k, v) -> mat.addTextureOption(mapName, k, v));
        }
    }

    /* -------------------------------------------------------------
     *  HELPERS
     * ------------------------------------------------------------- */

    private void commitCurrentMaterial() {
        if (currentObjMaterial != null) {
            ObjMaterial old = materialMap.put(currentObjMaterial.name(), currentObjMaterial);
            if (old != null) {
                Logger.warn("Material replaced: '{}'={}", currentObjMaterial.name(), old);
            } else {
                Logger.debug("Material added: '{}'={}", currentObjMaterial.name(), currentObjMaterial);
            }
            currentObjMaterial = null;
        }
    }

    private static float parseSpecularPower(String s) {
        float v = Float.parseFloat(s);
        return (v >= 0 && v <= 1000) ? v : ObjMaterial.DEFAULT_NS;
    }

    private static byte parseIllumination(String s) {
        int v = Integer.parseInt(s);
        return (v >= 0 && v <= 10) ? (byte) v : ObjMaterial.DEFAULT_ILLUM;
    }

    private static float parseOpacity(String s) {
        float v = Float.parseFloat(s);
        return (v >= 0 && v <= 1) ? v : ObjMaterial.DEFAULT_OPACITY;
    }

    private static float parseRefractionIndex(String s) {
        float v = Float.parseFloat(s);
        return (v >= 0.001f && v <= 10f) ? v : ObjMaterial.DEFAULT_NI;
    }

    private static ObjColor parseColorRGB(String s, ObjColor def) {
        String[] c = s.split("\\s+");
        if (c.length != 3) return def;

        float r = clamp01(Float.parseFloat(c[0]));
        float g = clamp01(Float.parseFloat(c[1]));
        float b = clamp01(Float.parseFloat(c[2]));

        return new ObjColor(r, g, b);
    }

    private static float clamp01(float v) {
        return Math.clamp(v, 0, 1);
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjMtlFileParser {

    /**
     * Wavefront MTL material parameters.
     *
     * <table border="1" cellpadding="4" cellspacing="0">
     *   <tr><th>Keyword</th><th>Meaning</th><th>Description</th></tr>
     *   <!-- Material name -->
     *   <tr>
     *     <td><code>newmtl</code></td>
     *     <td>Material name</td>
     *     <td>Begins a new material definition.</td>
     *   </tr>
     *   <!-- Illumination model -->
     *   <tr>
     *     <td><code>illum</code></td>
     *     <td>Illumination model</td>
     *     <td>Selects the lighting model (0–10). Controls shading, highlights, and reflection behavior.</td>
     *   </tr>
     *   <!-- Opacity / transparency -->
     *   <tr>
     *     <td><code>d</code></td>
     *     <td>Opacity (dissolve)</td>
     *     <td>Opacity factor (1.0 = fully opaque, 0.0 = fully transparent). Called “dissolve” in the original spec.</td>
     *   </tr>
     *   <tr>
     *     <td><code>Tr</code></td>
     *     <td>Transparency</td>
     *     <td>Transparency factor (1.0 = fully transparent). Equivalent to <code>1 - d</code>.</td>
     *   </tr>
     *   <!-- Optical properties -->
     *   <tr>
     *     <td><code>Ns</code></td>
     *     <td>Specular exponent</td>
     *     <td>Phong shininess value (0–1000). Higher values produce tighter, sharper highlights.</td>
     *   </tr>
     *   <tr>
     *     <td><code>Ni</code></td>
     *     <td>Optical density (index of refraction)</td>
     *     <td>Index of refraction (1.0 = air, 1.3–1.5 = glass, 2.0+ = dense materials).</td>
     *   </tr>
     *   <!-- Color components -->
     *   <tr>
     *     <td><code>Ka</code></td>
     *     <td>Ambient color</td>
     *     <td>RGB ambient reflectivity.</td>
     *   </tr>
     *   <tr>
     *     <td><code>Kd</code></td>
     *     <td>Diffuse color</td>
     *     <td>RGB diffuse reflectivity (base color).</td>
     *   </tr>
     *   <tr>
     *     <td><code>Ks</code></td>
     *     <td>Specular color</td>
     *     <td>RGB specular reflectivity (highlight color).</td>
     *   </tr>
     *   <tr>
     *     <td><code>Ke</code></td>
     *     <td>Emissive color</td>
     *     <td>RGB emissive light color (self‑illumination).</td>
     *   </tr>
     *   <!-- Transmission filter -->
     *   <tr>
     *     <td><code>Tf</code></td>
     *     <td>Transmission filter</td>
     *     <td>RGB filter applied to transmitted light (used for colored glass).</td>
     *   </tr>
     *   <!-- Texture maps -->
     *   <tr>
     *     <td><code>map_Ka</code></td>
     *     <td>Ambient texture</td>
     *     <td>Texture map for ambient color.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Kd</code></td>
     *     <td>Diffuse texture</td>
     *     <td>Texture map for diffuse color (albedo).</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Ks</code></td>
     *     <td>Specular texture</td>
     *     <td>Texture map for specular color.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Ke</code></td>
     *     <td>Emissive texture</td>
     *     <td>Texture map for emissive color.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Ns</code></td>
     *     <td>Specular exponent map</td>
     *     <td>Texture map controlling shininess per pixel.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_d</code></td>
     *     <td>Opacity map</td>
     *     <td>Texture map controlling transparency.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Tr</code></td>
     *     <td>Transparency map</td>
     *     <td>Equivalent to <code>map_d</code> but inverted.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_Bump</code>, <code>bump</code></td>
     *     <td>Bump map</td>
     *     <td>Height map used for bump mapping.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_disp</code></td>
     *     <td>Displacement map</td>
     *     <td>Height map for geometric displacement.</td>
     *   </tr>
     *   <tr>
     *     <td><code>map_refl</code></td>
     *     <td>Reflection map</td>
     *     <td>Environment reflection texture.</td>
     *   </tr>
     *   <!-- Reflection model -->
     *   <tr>
     *     <td><code>refl</code></td>
     *     <td>Reflection model</td>
     *     <td>Specifies reflection type (sphere, cube, etc.).</td>
     *   </tr>
     * </table>
     */
    public enum Keyword {
        NEW_MATERIAL       ("newmtl"),
        OPACITY            ("d"), // "d" = "dissolve"
        TRANSPARENCY       ("Tr"), // Tr = 1 - d
        ILLUMINATION       ("illum"), // not used by JavaFX
        AMBIENT_COLOR      ("Ka"), // not used by JavaFX
        DIFFUSE_COLOR      ("Kd"),
        EMISSIVE_COLOR     ("Ke"), // not used by JavaFX
        SPECULAR_COLOR     ("Ks"),
        REFRACTION_INDEX   ("Ni"), // not used by JavaFX
        SPECULAR_POWER     ("Ns"),
        UNKNOWN            ("");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }

        static Keyword fromText(String text) {
            for (Keyword keyword : values()) {
                if (keyword.text.equals(text)) {
                    return keyword;
                }
            }
            return UNKNOWN;
        }
    }

    public record Token(Keyword keyword, String args, int lineNo) {
        public Token(String keyword, String args, int lineNo) {
            this(Keyword.fromText(keyword), args, lineNo);
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

                // Remove inline comments
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

            return null; // EOF
        }
    }

    private final Map<String, ObjMaterial> materialMap = new LinkedHashMap<>();
    private ObjMaterial currentObjMaterial;

    private Tokenizer tokenizer;

    public Map<String, ObjMaterial> materialMap() {
        return Collections.unmodifiableMap(materialMap);
    }

    public void parse(BufferedReader reader) throws IOException {
        tokenizer = new Tokenizer(reader);
        Token token;

        while ((token = tokenizer.next()) != null) {
            switch (token.keyword) {
                case NEW_MATERIAL -> {
                    commitCurrentMaterial();
                    currentObjMaterial = new ObjMaterial(token.args);
                }
                case SPECULAR_POWER -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.ns = parseSpecularPower(token.args);
                    }
                }
                case OPACITY -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.d = parseOpacity(token.args);
                    }
                }
                case TRANSPARENCY -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.d = 1.0f - parseOpacity(token.args);
                    }
                }
                case ILLUMINATION -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.illum = parseIllumination(token.args);
                    }
                }
                case REFRACTION_INDEX ->  {
                    if (materialDefStarted()) {
                        currentObjMaterial.ni = parseRefractionIndex(token.args);
                    }
                }
                case AMBIENT_COLOR ->  {
                    if (materialDefStarted()) {
                        currentObjMaterial.ka = parseColorRGB(token.args, ObjMaterial.DEFAULT_AMBIENT_COLOR);
                    }
                }
                case DIFFUSE_COLOR ->  {
                    if (materialDefStarted()) {
                        currentObjMaterial.kd = parseColorRGB(token.args, ObjMaterial.DEFAULT_DIFFUSE_COLOR);
                    }
                }
                case EMISSIVE_COLOR -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.ke = parseColorRGB(token.args, ObjMaterial.DEFAULT_EMISSIVE_COLOR);
                    }
                }
                case SPECULAR_COLOR -> {
                    if (materialDefStarted()) {
                        currentObjMaterial.ks = parseColorRGB(token.args, ObjMaterial.DEFAULT_SPECULAR_COLOR);
                    }
                }
                default -> Logger.warn("Unknown keyword '{}' at line {}", token.keyword, token.lineNo);
            }
        }

        commitCurrentMaterial();
    }

    // Private

    private boolean materialDefStarted() {
        if (currentObjMaterial == null) {
            Logger.error("{}: No material definition has been started", tokenizer.lineNo);
            return false;
        }
        return true;
    }

    private void commitCurrentMaterial() {
        if (currentObjMaterial != null) {
            final ObjMaterial oldPhongMaterial = materialMap.put(currentObjMaterial.name, currentObjMaterial);
            if (oldPhongMaterial != null) {
                Logger.warn("Material replaced: '{}'={}", currentObjMaterial.name, oldPhongMaterial);
            } else {
                Logger.debug("Material added: '{}'={}", currentObjMaterial.name, currentObjMaterial);
            }
            currentObjMaterial = null;
        }
    }

    // float, 0..1000
    private static float parseSpecularPower(String s) {
        float value = Float.parseFloat(s);
        if (0 <= value && value <= 1000) {
            return value;
        }
        Logger.error("Specular Power Ns={} out-of-range 0..1000", value);
        return ObjMaterial.DEFAULT_SPECULAR_POWER;
    }

    // integer, 0..10
    private static byte parseIllumination(String s) {
        int value = Integer.parseInt(s);
        if (0 <= value && value <= 10) {
            return (byte) value;
        }
        Logger.error("Illumination illum={} out-of-range 0..10", value);
        return ObjMaterial.DEFAULT_ILLUMINATION;
    }

    // float, 0..1
    private static float parseOpacity(String s) {
        float value = Float.parseFloat(s);
        if (0 <= value && value <= 1) {
            return value;
        }
        Logger.error("Opacity d={} out-of-range 0..1", value);
        return ObjMaterial.DEFAULT_OPACITY;
    }

    // float, 0.001..10
    private static float parseRefractionIndex(String s) {
        float value = Float.parseFloat(s);
        if (0.001f <= value && value <= 10f) {
            return value;
        }
        Logger.error("Refraction Index Ni={} out of range 0.001..10", value);
        return ObjMaterial.DEFAULT_REFRACTION_INDEX;
    }

    // float 3-tuple, each 0..1
    private static ObjColor parseColorRGB(String s, ObjColor defaultColor) {
        final String[] comp = s.trim().split("\\s+", 3);
        if (comp.length == 3) {
            float r = (float) Math.clamp(Float.parseFloat(comp[0]), 0.0, 1.0);
            float g = (float) Math.clamp(Float.parseFloat(comp[1]), 0.0, 1.0);
            float b = (float) Math.clamp(Float.parseFloat(comp[2]), 0.0, 1.0);
            return new ObjColor(r, g, b);
        } else {
            Logger.error("Invalid color format: {}", s);
            return defaultColor;
        }
    }
}

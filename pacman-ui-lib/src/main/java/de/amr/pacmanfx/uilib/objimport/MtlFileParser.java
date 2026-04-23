/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MtlFileParser {

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
        ILLUMINATION       ("illum"),
        AMBIENT_COLOR      ("Ka"),
        DIFFUSE_COLOR      ("Kd"),
        EMISSIVE_COLOR     ("Ke"),
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

    public static class MtlTokenizer {
        private final BufferedReader reader;
        private int lineNo = 0;

        public MtlTokenizer(BufferedReader reader) {
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

    private static class ObjMaterial {
        static final float    DEFAULT_OPACITY = 1;
        static final byte     DEFAULT_ILLUMINATION = 2;
        static final ColorRGB DEFAULT_AMBIENT_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_DIFFUSE_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_EMISSIVE_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_SPECULAR_COLOR = ColorRGB.BLACK;
        static final float    DEFAULT_REFRACTION_INDEX = 1.0f;
        static final float    DEFAULT_SPECULAR_POWER = 10.0f;

        final String name;

        float d     = DEFAULT_OPACITY;
        byte illum  = DEFAULT_ILLUMINATION;
        ColorRGB ka = DEFAULT_AMBIENT_COLOR;
        ColorRGB kd = DEFAULT_DIFFUSE_COLOR;
        ColorRGB ks = DEFAULT_SPECULAR_COLOR;
        ColorRGB ke = DEFAULT_EMISSIVE_COLOR;
        float ni    = DEFAULT_REFRACTION_INDEX;
        float ns    = DEFAULT_SPECULAR_POWER;

        ObjMaterial(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "ObjMaterial{" +
                "name='" + name + '\'' +
                ", ns=" + ns +
                ", d=" + d +
                ", illum=" + illum +
                ", ni=" + ni +
                ", ka=" + ka +
                ", kd=" + kd +
                ", ks=" + ks +
                ", ke=" + ke +
                '}';
        }
    }

    private final Map<String, PhongMaterial> materialMap = new LinkedHashMap<>();
    private ObjMaterial currentMaterial;

    private MtlTokenizer tokenizer;

    public Map<String, PhongMaterial> materialMap() {
        return Collections.unmodifiableMap(materialMap);
    }

    public void parse(BufferedReader reader) throws IOException {
        tokenizer = new MtlTokenizer(reader);
        Token token;

        while ((token = tokenizer.next()) != null) {
            switch (token.keyword) {
                case NEW_MATERIAL -> {
                    commitCurrentMaterial();
                    currentMaterial = new ObjMaterial(token.args);
                }
                case SPECULAR_POWER -> {
                    if (materialStarted()) {
                        currentMaterial.ns = parseSpecularPower(token.args);
                    }
                }
                case OPACITY -> {
                    if (materialStarted()) {
                        currentMaterial.d = parseOpacity(token.args);
                    }
                }
                case ILLUMINATION -> {
                    if (materialStarted()) {
                        currentMaterial.illum = parseIllumination(token.args);
                    }
                }
                case REFRACTION_INDEX ->  {
                    if (materialStarted()) {
                        currentMaterial.ni = parseRefractionIndex(token.args);
                    }
                }
                case AMBIENT_COLOR ->  {
                    if (materialStarted()) {
                        currentMaterial.ka = parseColorRGB(token.args, ObjMaterial.DEFAULT_AMBIENT_COLOR);
                    }
                }
                case DIFFUSE_COLOR ->  {
                    if (materialStarted()) {
                        currentMaterial.kd = parseColorRGB(token.args, ObjMaterial.DEFAULT_DIFFUSE_COLOR);
                    }
                }
                case EMISSIVE_COLOR -> {
                    if (materialStarted()) {
                        currentMaterial.ke = parseColorRGB(token.args, ObjMaterial.DEFAULT_EMISSIVE_COLOR);
                    }
                }
                case SPECULAR_COLOR -> {
                    if (materialStarted()) {
                        currentMaterial.ks = parseColorRGB(token.args, ObjMaterial.DEFAULT_SPECULAR_COLOR);
                    }
                }
                default -> Logger.warn("Unknown keyword '{}' at line {}", token.keyword, token.lineNo);
            }
        }

        commitCurrentMaterial();
    }

    // Private

    private static Color fxColor(ColorRGB colorRGB) {
        return Color.color(colorRGB.red(), colorRGB.green(), colorRGB.blue());
    }

    private static PhongMaterial createPhongMaterial(ObjMaterial material) {
        final var phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseColor(fxColor(material.kd));
        phongMaterial.setSpecularColor(fxColor(material.ks));
        phongMaterial.setSpecularPower(material.ns);
        return phongMaterial;
    }

    private boolean materialStarted() {
        if (currentMaterial == null) {
            Logger.error("{}: No material definition has been started", tokenizer.lineNo);
            return false;
        }
        return true;
    }

    private void commitCurrentMaterial() {
        if (currentMaterial != null) {
            final PhongMaterial newPhongMaterial = createPhongMaterial(currentMaterial);
            final PhongMaterial oldPhongMaterial = materialMap.put(currentMaterial.name, newPhongMaterial);
            if (oldPhongMaterial != null) {
                Logger.warn("Material replaced: '{}'={}", currentMaterial.name, oldPhongMaterial);
            } else {
                Logger.info("Material added: '{}'={}", currentMaterial.name, newPhongMaterial);
            }
            currentMaterial = null;
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
    private static ColorRGB parseColorRGB(String s, ColorRGB defaultColor) {
        final String[] comp = s.trim().split("\\s+", 3);
        if (comp.length == 3) {
            float r = (float) Math.clamp(Float.parseFloat(comp[0]), 0.0, 1.0);
            float g = (float) Math.clamp(Float.parseFloat(comp[1]), 0.0, 1.0);
            float b = (float) Math.clamp(Float.parseFloat(comp[2]), 0.0, 1.0);
            return new ColorRGB(r, g, b);
        } else {
            Logger.error("Invalid color format: {}", s);
            return defaultColor;
        }
    }
}

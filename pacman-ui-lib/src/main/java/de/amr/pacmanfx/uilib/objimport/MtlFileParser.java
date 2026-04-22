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

    public enum Keyword {
        NEW_MATERIAL    ("newmtl"),
        SHININESS       ("Ns"),
        OPACITY         ("d"),
        ILLUMINATION    ("illum"),
        AMBIENT_COLOR   ("Ka"),
        DIFFUSE_COLOR   ("Kd"),
        EMISSIVE_COLOR  ("Ke"),
        SPECULAR_COLOR  ("Ks"),
        OPTICAL_DENSITY ("Ni"),
        UNKNOWN         ("");

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

    public static class MtlTokenizer {

        public static class Token {
            public final Keyword keyword;
            public final String args;
            public final int lineNo;

            public Token(String keywordText, String args, int lineNo) {
                this.keyword = Keyword.fromText(keywordText);
                this.args = args;
                this.lineNo = lineNo;
            }
        }

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

        static final float DEFAULT_SHININESS = 10.0f;
        static final float DEFAULT_OPACITY = 1;
        static final int DEFAULT_ILLUMINATION = 2;
        static final ColorRGB DEFAULT_AMBIENT_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_DIFFUSE_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_EMISSIVE_COLOR = ColorRGB.BLACK;
        static final ColorRGB DEFAULT_SPECULAR_COLOR = ColorRGB.BLACK;
        static final float DEFAULT_OPTICAL_DENSITY = 1.0f;

        final String name;

        float ns = DEFAULT_SHININESS;
        float d = DEFAULT_OPACITY;
        int illum = DEFAULT_ILLUMINATION;
        float ni = DEFAULT_OPTICAL_DENSITY;

        ColorRGB ka = DEFAULT_AMBIENT_COLOR;
        ColorRGB kd = DEFAULT_DIFFUSE_COLOR;
        ColorRGB ks = DEFAULT_SPECULAR_COLOR;
        ColorRGB ke = DEFAULT_EMISSIVE_COLOR;

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

    public Map<String, PhongMaterial> materialMap() {
        return Collections.unmodifiableMap(materialMap);
    }

    public void parse(BufferedReader reader) throws IOException {
        MtlTokenizer tokenizer = new MtlTokenizer(reader);
        MtlTokenizer.Token token;

        while ((token = tokenizer.next()) != null) {
            switch (token.keyword) {
                case NEW_MATERIAL -> {
                    commitCurrentMaterial();
                    currentMaterial = new ObjMaterial(token.args);
                }
                case SHININESS -> {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.ns = parseShininess(token.args, ObjMaterial.DEFAULT_SHININESS);
                    }
                }
                case OPACITY -> {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.d = parseOpacity(token.args, ObjMaterial.DEFAULT_OPACITY);
                    }
                }
                case ILLUMINATION -> {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.illum = parseIllumination(token.args, ObjMaterial.DEFAULT_ILLUMINATION);
                    }
                }
                case OPTICAL_DENSITY ->  {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.ni = parseOpticalDensity(token.args, ObjMaterial.DEFAULT_OPTICAL_DENSITY);
                    }
                }
                case AMBIENT_COLOR ->  {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.ka = parseColorRGB(token.args, ObjMaterial.DEFAULT_AMBIENT_COLOR);
                    }
                }
                case DIFFUSE_COLOR ->  {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.kd = parseColorRGB(token.args, ObjMaterial.DEFAULT_DIFFUSE_COLOR);
                    }
                }
                case EMISSIVE_COLOR -> {
                    if (assertCurrentMaterial(token.lineNo)) {
                        currentMaterial.ke = parseColorRGB(token.args, ObjMaterial.DEFAULT_EMISSIVE_COLOR);
                    }
                }
                case SPECULAR_COLOR -> {
                    if (assertCurrentMaterial(token.lineNo)) {
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

    private boolean assertCurrentMaterial(int lineNo) {
        if (currentMaterial == null) {
            Logger.error("{}: No material definition has been started", lineNo);
            return false;
        }
        return true;
    }

    private void commitCurrentMaterial() {
        if (currentMaterial != null) {
            final PhongMaterial oldValue = materialMap.put(currentMaterial.name, createPhongMaterial(currentMaterial));
            if (oldValue != null) {
                Logger.warn("Duplicate material found: {}. Overwrites previous material.", currentMaterial.name);
            }
            currentMaterial = null;
        }
    }

    // float, 0..1000
    private static float parseShininess(String s, float defaultValue) {
        float value = Float.parseFloat(s);
        if (0 <= value && value <= 1000) {
            return value;
        }
        Logger.error("Shininess Ns={} out-of-range 0..1000", value);
        return defaultValue;
    }

    // integer, 0..10
    private static int parseIllumination(String s, int defaultValue) {
        int value = Integer.parseInt(s);
        if (0 <= value && value <= 10) {
            return value;
        }
        Logger.error("Illumination illum={} out-of-range 0..10", value);
        return defaultValue;
    }

    // float, 0..1
    private static float parseOpacity(String s, float defaultValue) {
        float value = Float.parseFloat(s);
        if (0 <= value && value <= 1) {
            return value;
        }
        Logger.error("Opacity d={} out-of-range 0..1", value);
        return defaultValue;
    }

    // float, 0.001..10
    private static float parseOpticalDensity(String s, float defaultValue) {
        float value = Float.parseFloat(s);
        if (0.001f <= value && value <= 10f) {
            return value;
        }
        Logger.error("Optical density Ni={} out of range 0.001..10", value);
        return defaultValue;
    }

    // float 3-tuple, each 0..1
    private static ColorRGB parseColorRGB(String s, ColorRGB defaultValue) {
        String[] comp = s.trim().split("\\s+");
        if (comp.length == 3) {
            float r = (float) Math.clamp(Float.parseFloat(comp[0]), 0.0, 1.0);
            float g = (float) Math.clamp(Float.parseFloat(comp[1]), 0.0, 1.0);
            float b = (float) Math.clamp(Float.parseFloat(comp[2]), 0.0, 1.0);
            return new ColorRGB(r, g, b);
        }
        else {
            Logger.error("Invalid color format: {}", s);
            return defaultValue;
        }
    }
}

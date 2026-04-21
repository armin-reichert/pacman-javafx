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
        OPTICAL_DENSITY ("Ni");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }
    }

    private record RGB(double red, double green, double blue) {
        static RGB BLACK = new RGB(0.0, 0.0, 0.0);
    }

    private static class ObjMaterial {
        final String name;

        double ns = 0;
        double d = 1;
        int illum = 2;
        double ni = 1;

        RGB ka = RGB.BLACK;
        RGB kd = RGB.BLACK;
        RGB ks = RGB.BLACK;
        RGB ke = RGB.BLACK;

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
    private int lineNo = 1;

    public Map<String, PhongMaterial> materialMap() {
        return Collections.unmodifiableMap(materialMap);
    }

    public void parse(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                Logger.trace("Blank line, ignored");
            }
            else if (line.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (startsWith(line, Keyword.NEW_MATERIAL)) {
                commitCurrentMaterial();
                currentMaterial = new ObjMaterial(params(line, Keyword.NEW_MATERIAL));
            }
            else if (startsWith(line, Keyword.SHININESS)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.ns = parseShininess(params(line, Keyword.SHININESS), 10.0);
                }
            }
            else if (startsWith(line, Keyword.OPACITY)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.d = parseOpacity(params(line, Keyword.OPACITY), 1.0);
                }
            }
            else if (startsWith(line, Keyword.ILLUMINATION)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.illum = parseIllumination(params(line, Keyword.ILLUMINATION), 2);
                }
            }
            else if (startsWith(line, Keyword.OPTICAL_DENSITY)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.ni = Double.parseDouble(params(line, Keyword.OPTICAL_DENSITY));
                }
            }
            else if (startsWith(line, Keyword.AMBIENT_COLOR)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.ka = parseRGB(params(line, Keyword.AMBIENT_COLOR), RGB.BLACK);
                }
            }
            else if (startsWith(line, Keyword.DIFFUSE_COLOR)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.kd = parseRGB(params(line, Keyword.DIFFUSE_COLOR), RGB.BLACK);
                }
            }
            else if (startsWith(line, Keyword.EMISSIVE_COLOR)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.ke = parseRGB(params(line, Keyword.EMISSIVE_COLOR), RGB.BLACK);
                }
            }
            else if (startsWith(line, Keyword.SPECULAR_COLOR)) {
                if (assertCurrentMaterial()) {
                    currentMaterial.ks = parseRGB(params(line, Keyword.SPECULAR_COLOR), RGB.BLACK);
                }
            }
            ++lineNo;
        }
        commitCurrentMaterial();
        Logger.info("Found {} materials", materialMap.size());
        materialMap.forEach((name, material) -> Logger.info("{}: {}", name, material));
    }

    // Private

    private static Color fxColor(RGB rgb) {
        return Color.color(rgb.red, rgb.green, rgb.blue);
    }

    private static PhongMaterial createPhongMaterial(ObjMaterial material) {
        final var phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseColor(fxColor(material.kd));
        phongMaterial.setSpecularColor(fxColor(material.ks));
        phongMaterial.setSpecularPower(material.ns);
        return phongMaterial;
    }

    private boolean assertCurrentMaterial() {
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

    private static boolean startsWith(String line, MtlFileParser.Keyword keyword) {
        return line.startsWith(keyword.text + " ");
    }

    private static String params(String line, MtlFileParser.Keyword keyword) {
        return line.substring(keyword.text.length() + 1).strip();
    }

    // double, 0-1000
    private static double parseShininess(String s, double defaultValue) {
        double value = Double.parseDouble(s);
        if (0 <= value && value <= 1000) {
            return value;
        }
        Logger.error("Shininess value out-of-range 0..1000: {}", value);
        return defaultValue;
    }

    // integer, 0..10
    private static int parseIllumination(String s, int defaultValue) {
        int value = Integer.parseInt(s);
        if (0 <= value && value <= 10) {
            return value;
        }
        Logger.error("Illumination value out-of-range 0..10: {}", value);
        return defaultValue;
    }

    private static double parseOpacity(String s, double defaultValue) {
        double value = Double.parseDouble(s);
        if (0 <= value && value <= 1) {
            return value;
        }
        Logger.error("Opacity value out-of-range 0..1: {}", value);
        return defaultValue;
    }

    private static RGB parseRGB(String s, RGB defaultValue) {
        String[] comp = s.trim().split("\\s+");
        if (comp.length == 3) {
            double r = Math.clamp(Double.parseDouble(comp[0]), 0.0, 1.0);
            double g = Math.clamp(Double.parseDouble(comp[1]), 0.0, 1.0);
            double b = Math.clamp(Double.parseDouble(comp[2]), 0.0, 1.0);
            return new RGB(r, g, b);
        }
        else {
            Logger.error("Invalid RGB color format: {}", s);
            return defaultValue;
        }
    }
}

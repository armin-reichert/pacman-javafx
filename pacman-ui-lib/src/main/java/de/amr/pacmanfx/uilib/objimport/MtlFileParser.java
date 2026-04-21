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
        String name;
        double ns = 0;
        double d = 1;
        int illum = 2;
        double ni = 1;

        RGB ka = RGB.BLACK;
        RGB kd = RGB.BLACK;
        RGB ks = RGB.BLACK;
        RGB ke = RGB.BLACK;

        String mapKd;
        String mapKs;
        String mapD;
        String mapBump;


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
                ", mapKd='" + mapKd + '\'' +
                ", mapKs='" + mapKs + '\'' +
                ", mapD='" + mapD + '\'' +
                ", mapBump='" + mapBump + '\'' +
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
        String statement;
        while ((statement = reader.readLine()) != null) {
            if (statement.isBlank() || statement.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }
            else if (matches(statement, Keyword.NEW_MATERIAL)) {
                commitMaterial();
                String parameters = parameters(statement, Keyword.NEW_MATERIAL);
                currentMaterial = new ObjMaterial();
                currentMaterial.name = parameters.strip();
            }
            else if (matches(statement, Keyword.SHININESS)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.SHININESS);
                    currentMaterial.ns = parseShininess(parameters);
                }
            }
            else if (matches(statement, Keyword.OPACITY)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.OPACITY);
                    currentMaterial.d = parseOpacity(parameters, 1.0);
                }
            }
            else if (matches(statement, Keyword.ILLUMINATION)) {
                if (assertCurrentMaterial()) {
                    String params = parameters(statement, Keyword.ILLUMINATION);
                    currentMaterial.illum = parseIllumination(params, 2);
                }
            }
            else if (matches(statement, Keyword.OPTICAL_DENSITY)) {
                if (assertCurrentMaterial()) {
                    String params = parameters(statement, Keyword.OPTICAL_DENSITY);
                    currentMaterial.ni = Double.parseDouble(params);
                }
            }
            else if (matches(statement, Keyword.AMBIENT_COLOR)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.AMBIENT_COLOR);
                    currentMaterial.ka = parseRGB(parameters, RGB.BLACK);
                }
            }
            else if (matches(statement, Keyword.DIFFUSE_COLOR)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.DIFFUSE_COLOR);
                    currentMaterial.kd = parseRGB(parameters, RGB.BLACK);
                }
            }
            else if (matches(statement, Keyword.EMISSIVE_COLOR)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.EMISSIVE_COLOR);
                    currentMaterial.ke = parseRGB(parameters, RGB.BLACK);
                }
            }
            else if (matches(statement, Keyword.SPECULAR_COLOR)) {
                if (assertCurrentMaterial()) {
                    String parameters = parameters(statement, Keyword.SPECULAR_COLOR);
                    currentMaterial.ks = parseRGB(parameters, RGB.BLACK);
                }
            }
            ++lineNo;
        }
        commitMaterial();
        Logger.info("Found {} materials", materialMap.size());
        for (PhongMaterial material : materialMap.values()) {
            Logger.info(material);
        }
    }

    // Private

    private static Color fxColor(RGB rgb) {
        return Color.color(rgb.red, rgb.green, rgb.blue);
    }

    private static PhongMaterial createPhongMaterial(ObjMaterial material) {
        PhongMaterial phongMaterial = new PhongMaterial();
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

    private void commitMaterial() {
        if (currentMaterial != null) {
            final PhongMaterial oldValue = materialMap.put(currentMaterial.name, createPhongMaterial(currentMaterial));
            if (oldValue != null) {
                Logger.warn("Duplicate material found: {}. Overwrites previous material.", currentMaterial.name);
            }
            currentMaterial = null;
        }
    }

    private static boolean matches(String statement, MtlFileParser.Keyword keyword) {
        return statement.startsWith(keyword.text + " ");
    }

    private static String parameters(String statement, MtlFileParser.Keyword keyword) {
        return statement.substring(keyword.text.length() + 1).strip();
    }

    // double, 0-1000
    private double parseShininess(String s) {
        double value = Double.parseDouble(s);
        if (0 <= value && value <= 1000) {
            return value;
        }
        Logger.error("Shininess value out-of-range 0..1000: {}", value);
        return 500;
    }

    // integer, 0..10
    private int parseIllumination(String s, int defaultIllumination) {
        int value = Integer.parseInt(s);
        if (0 <= value && value <= 10) {
            return value;
        }
        Logger.error("Illumination value out-of-range 0..10: {}", value);
        return defaultIllumination;
    }

    private double parseOpacity(String s, double defaultOpacity) {
        double value = Double.parseDouble(s);
        if (0 <= value && value <= 1) {
            return value;
        }
        Logger.error("Opacity value out-of-range 0..1: {}", value);
        return defaultOpacity;
    }

    private RGB parseRGB(String s, RGB defaultRGB) {
        String[] comp = s.trim().split("\\s+");
        if (comp.length == 3) {
            double r = Double.parseDouble(comp[0]);
            double g = Double.parseDouble(comp[1]);
            double b = Double.parseDouble(comp[2]);
            return new RGB(r, g, b);
        }
        else {
            Logger.error("Invalid RGB color format: {}", s);
            return defaultRGB;
        }
    }
}

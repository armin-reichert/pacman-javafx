/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public class ObjMaterial {
    public static final float DEFAULT_OPACITY = 1;
    public static final byte DEFAULT_ILLUMINATION = 2;
    public static final ColorRGB DEFAULT_AMBIENT_COLOR = ColorRGB.BLACK;
    public static final ColorRGB DEFAULT_DIFFUSE_COLOR = ColorRGB.BLACK;
    public static final ColorRGB DEFAULT_EMISSIVE_COLOR = ColorRGB.BLACK;
    public static final ColorRGB DEFAULT_SPECULAR_COLOR = ColorRGB.BLACK;
    public static final float DEFAULT_REFRACTION_INDEX = 1.0f;
    public static final float DEFAULT_SPECULAR_POWER = 10.0f;

    final String name;

    float d = DEFAULT_OPACITY;
    byte illum = DEFAULT_ILLUMINATION;
    ColorRGB ka = DEFAULT_AMBIENT_COLOR;
    ColorRGB kd = DEFAULT_DIFFUSE_COLOR;
    ColorRGB ks = DEFAULT_SPECULAR_COLOR;
    ColorRGB ke = DEFAULT_EMISSIVE_COLOR;
    float ni = DEFAULT_REFRACTION_INDEX;
    float ns = DEFAULT_SPECULAR_POWER;

    ObjMaterial(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public float d() {
        return d;
    }

    public byte illum() {
        return illum;
    }

    public ColorRGB ka() {
        return ka;
    }

    public ColorRGB kd() {
        return kd;
    }

    public ColorRGB ks() {
        return ks;
    }

    public ColorRGB ke() {
        return ke;
    }

    public float ni() {
        return ni;
    }

    public float ns() {
        return ns;
    }

    @Override
    public String toString() {
        return "MaterialDef{" +
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

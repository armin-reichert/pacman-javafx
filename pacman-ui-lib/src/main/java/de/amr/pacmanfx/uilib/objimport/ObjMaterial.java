/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public class ObjMaterial {
    static final float DEFAULT_OPACITY = 1;
    static final byte DEFAULT_ILLUMINATION = 2;
    static final ColorRGB DEFAULT_AMBIENT_COLOR = ColorRGB.BLACK;
    static final ColorRGB DEFAULT_DIFFUSE_COLOR = ColorRGB.BLACK;
    static final ColorRGB DEFAULT_EMISSIVE_COLOR = ColorRGB.BLACK;
    static final ColorRGB DEFAULT_SPECULAR_COLOR = ColorRGB.BLACK;
    static final float DEFAULT_REFRACTION_INDEX = 1.0f;
    static final float DEFAULT_SPECULAR_POWER = 10.0f;

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

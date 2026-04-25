/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

/**
 * Wavefront material data.
 *
 * <table border="1" cellpadding="4" cellspacing="0">
 *   <tr><th>Keyword</th><th>Meaning</th><th>Description</th></tr>
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
 * </table>
 */
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

    public float opacity() {
        return d;
    }

    public byte illumination() {
        return illum;
    }

    public ColorRGB ambientColor() {
        return ka;
    }

    public ColorRGB diffuseColor() {
        return kd;
    }

    public ColorRGB specularColor() {
        return ks;
    }

    public ColorRGB emissiveColor() {
        return ke;
    }

    public float refractionIndex() {
        return ni;
    }

    public float specularExponent() {
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

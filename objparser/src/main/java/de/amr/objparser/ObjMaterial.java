/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.objparser;

import java.util.HashMap;
import java.util.Map;

/**
 * Wavefront MTL material definition with full specification support.
 */
public class ObjMaterial {

    // --- Defaults ---
    public static final float DEFAULT_OPACITY = 1f;
    public static final byte  DEFAULT_ILLUM   = 2;
    public static final float DEFAULT_NI      = 1.0f;
    public static final float DEFAULT_NS      = 10.0f;

    public static final ObjColor DEFAULT_COLOR = ObjColor.BLACK;

    // --- Identity ---
    private final String name;

    // --- Scalar properties ---
    public float d  = DEFAULT_OPACITY;  // dissolve (opacity)
    public float Tr = -1;               // transparency (optional)
    public float Ns = DEFAULT_NS;       // specular exponent
    public float Ni = DEFAULT_NI;       // index of refraction
    public byte illum = DEFAULT_ILLUM;  // illumination model

    // --- Color properties ---
    public ObjColor Ka = DEFAULT_COLOR; // ambient
    public ObjColor Kd = DEFAULT_COLOR; // diffuse
    public ObjColor Ks = DEFAULT_COLOR; // specular
    public ObjColor Ke = DEFAULT_COLOR; // emissive
    public ObjColor Tf = null;          // transmission filter (optional)

    // --- Texture maps (file names) ---
    public String map_Ka;   // ambient map
    public String map_Kd;   // diffuse map
    public String map_Ks;   // specular map
    public String map_Ke;   // emissive map
    public String map_Ns;   // specular exponent map
    public String map_d;    // opacity map
    public String map_bump; // bump map
    public String bump;     // alias for map_bump
    public String disp;     // displacement map
    public String decal;    // stencil/decal map
    public String map_Pr;   // roughness map
    public String map_Pm;   // metallic map
    public String map_Ps;   // sheen map
    public String map_refl; // reflection map

    // --- Texture options per map ---
    // Each map may have its own option set, so we store them in a map:
    // mapName → optionName → value
    public final Map<String, Map<String, Object>> textureOptions = new HashMap<>();

    public ObjMaterial(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    // --- Helpers for texture options ---
    public void addTextureOption(String mapName, String option, Object value) {
        textureOptions
            .computeIfAbsent(mapName, k -> new HashMap<>())
            .put(option, value);
    }

    public Map<String, Object> getTextureOptions(String mapName) {
        return textureOptions.getOrDefault(mapName, Map.of());
    }

    @Override
    public String toString() {
        return "ObjMaterial{" +
            "name='" + name + '\'' +
            ", illum=" + illum +
            ", d=" + d +
            ", Tr=" + Tr +
            ", Ns=" + Ns +
            ", Ni=" + Ni +
            ", Ka=" + Ka +
            ", Kd=" + Kd +
            ", Ks=" + Ks +
            ", Ke=" + Ke +
            ", Tf=" + Tf +
            ", map_Ka='" + map_Ka + '\'' +
            ", map_Kd='" + map_Kd + '\'' +
            ", map_Ks='" + map_Ks + '\'' +
            ", map_Ke='" + map_Ke + '\'' +
            ", map_Ns='" + map_Ns + '\'' +
            ", map_d='" + map_d + '\'' +
            ", map_bump='" + map_bump + '\'' +
            ", bump='" + bump + '\'' +
            ", disp='" + disp + '\'' +
            ", decal='" + decal + '\'' +
            ", map_Pr='" + map_Pr + '\'' +
            ", map_Pm='" + map_Pm + '\'' +
            ", map_Ps='" + map_Ps + '\'' +
            ", map_refl='" + map_refl + '\'' +
            ", textureOptions=" + textureOptions +
            '}';
    }
}

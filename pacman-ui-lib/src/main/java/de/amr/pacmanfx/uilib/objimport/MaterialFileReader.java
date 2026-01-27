/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Reader for OBJ file MTL material files.
 */
public class MaterialFileReader {

    private final String baseUrl;
    private final Map<String, Material> materials = new HashMap<>();
    private PhongMaterial material = new PhongMaterial();
    private boolean modified = false;

    public MaterialFileReader(String filename, String parentUrl) {
        baseUrl = parentUrl.substring(0, parentUrl.lastIndexOf('/') + 1);
        String fileUrl = baseUrl + filename;
        try {
            URL mtlUrl = new URI(fileUrl).toURL();
            Logger.trace("Reading material from URL {}", mtlUrl);
            read(mtlUrl.openStream());
        } catch (FileNotFoundException ex) {
            Logger.trace("No material file found for obj. [{}]", fileUrl);
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    public Map<String, Material> getMaterialMap() {
        return Collections.unmodifiableMap(materials);
    }

    private void read(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String name = "default";
        while ((line = br.readLine()) != null) {
            try {
                if (line.isEmpty() || line.startsWith("#")) {
                    Logger.trace("Skipped line {}", line);
                    // comments and empty lines are ignored
                } else if (line.startsWith("newmtl ")) {
                    addMaterial(name);
                    name = line.substring("newmtl ".length());
                } else if (line.startsWith("Kd ")) {
                    material.setDiffuseColor(readColor(line.substring(3)));
                    modified = true;
                } else if (line.startsWith("Ks ")) {
                    material.setSpecularColor(readColor(line.substring(3)));
                    modified = true;
                } else if (line.startsWith("Ns ")) {
                    material.setSpecularPower(Double.parseDouble(line.substring(3)));
                    modified = true;
                } else if (line.startsWith("map_Kd ")) {
                    material.setDiffuseColor(Color.WHITE);
                    material.setDiffuseMap(loadImage(line.substring("map_Kd ".length())));
//                    material.setSelfIlluminationMap(loadImage(line.substring("map_Kd ".length())));
//                    material.setSpecularColor(Color.WHITE);
                    modified = true;
                    // } else if (line.startsWith("illum ")) {
                    // int illumNo = Integer.parseInt(line.substring("illum ".length()));
                    /*
                     * 0 Color on and Ambient off 1 Color on and Ambient on 2 Highlight on 3 Reflection on and Ray trace on 4
                     * Transparency: Glass on Reflection: Ray trace on 5 Reflection: Fresnel on and Ray trace on 6 Transparency:
                     * Refraction on Reflection: Fresnel off and Ray trace on 7 Transparency: Refraction on Reflection: Fresnel on
                     * and Ray trace on 8 Reflection on and Ray trace off 9 Transparency: Glass on Reflection: Ray trace off 10
                     * Casts shadows onto invisible surfaces
                     */
                } else {
                    Logger.trace("Material file line ignored for name {}: {}", name, line);
                }
            } catch (Exception x) {
                Logger.error(x);
                Logger.error("Failed to parse line: {}", line);
            }
        }
        addMaterial(name);
    }

    private void addMaterial(String name) {
        if (modified) {
            if (!materials.containsKey(name)) {
                materials.put(name, material);
            } else {
                Logger.trace("Material already added. Ignoring {}", name);
            }
            material = new PhongMaterial(Color.WHITE);
        }
    }

    private Color readColor(String line) {
        String[] split = line.trim().split(" +");
        float red = Float.parseFloat(split[0]);
        float green = Float.parseFloat(split[1]);
        float blue = Float.parseFloat(split[2]);
        return Color.color(red, green, blue);
    }

    private Image loadImage(String filename) {
        filename = baseUrl + filename;
        Logger.trace("Loading source from {}", filename);
        return new Image(filename);
    }
}
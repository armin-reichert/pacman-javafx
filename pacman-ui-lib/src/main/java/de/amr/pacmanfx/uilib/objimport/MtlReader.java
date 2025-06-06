/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
public class MtlReader {

    private final String baseUrl;
    private final Map<String, Material> materials = new HashMap<>();
    private PhongMaterial material = new PhongMaterial();
    private boolean modified = false;

    public MtlReader(String filename, String parentUrl) {
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

    public Map<String, Material> getMaterials() {
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
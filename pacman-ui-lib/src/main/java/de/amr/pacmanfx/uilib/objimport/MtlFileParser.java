/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MtlFileParser {

    public enum Command {
        NEW_MATERIAL    ("newmtl"),
        SHININESS       ("Ns"),
        OPACITY         ("d"),
        ILLUMINATION    ("illum"),
        AMBIENT_COLOR   ("Ka"),
        DIFFUSE_COLOR   ("Kd"),
        EMISSIVE_COLOR  ("Ke"),
        SPECULAR_COLOR  ("Ks"),
        OPTICAL_DENSITY ("Ni"),
        COMMENT         ("#"),

        $("");

        private final String token;

        Command(String token) {
            this.token = token;
        }
    }

    private final Map<String, PhongMaterial> materialMap = new HashMap<>();

    private String line;

    public void parse(BufferedReader reader) throws IOException {
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#")) {
                Logger.trace("Blank or comment line, ignored");
            }

        }
    }

}

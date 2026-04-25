/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.objimport.MtlFileParser;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MtlFileParserTest {
    static final String[] FILE_PATHS = {
        "/de/amr/pacmanfx/uilib/model3D/ghost.mtl",
        "/de/amr/pacmanfx/uilib/model3D/pacman.mtl",
        "/de/amr/pacmanfx/uilib/model3D/pellet.mtl"
    };

    static void main() {
        for (String path : FILE_PATHS) {
            final URL url = MtlFileParser.class.getResource(path);
            if (url == null) {
                Logger.error("Invalid OBJ file path {}", path);
                return;
            }
            Logger.info("Parsing material file '{}'", path);
            final MtlFileParser parser = new MtlFileParser();
            try {
                try (InputStream is = url.openStream()) {
                    final var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    parser.parse(reader);
                }
            } catch (IOException x) {
                Logger.error(x, "Parsing error");
            }
        }
    }
}

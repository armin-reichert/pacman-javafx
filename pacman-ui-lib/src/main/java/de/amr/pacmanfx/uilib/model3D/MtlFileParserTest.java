/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.objparser.ObjMtlFileParser;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MtlFileParserTest {
    static final String[] FILE_PATHS = {
        "/de/amr/pacmanfx/uilib/model3D/ghost.mtl",
        "/de/amr/pacmanfx/uilib/model3D/pacman.mtl",
        "/de/amr/pacmanfx/uilib/model3D/pellet.mtl"
    };

    static void main() {
        int total = FILE_PATHS.length, passed = 0;
        for (String path : FILE_PATHS) {
            final URL url = MtlFileParserTest.class.getResource(path);
            if (url == null) {
                Logger.error("Invalid OBJ file path {}", path);
                return;
            }
            Logger.info("Parsing material file '{}'", path);
            final ObjMtlFileParser parser = new ObjMtlFileParser();
            try (InputStream stream = url.openStream()) {
                parser.parse(stream, StandardCharsets.UTF_8);
                ++passed;
            } catch (IOException x) {
                Logger.error(x, "Parsing error");
            }
        }
        Logger.info("{} of {} tests passed.", passed, total);
    }
}

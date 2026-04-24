/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.shape.MeshView;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ObjFileParser2Test {
    static final String[] FILE_PATHS = {
        "/de/amr/pacmanfx/uilib/model3D/ghost.obj",
//        "/de/amr/pacmanfx/uilib/model3D/pacman.obj",
//        "/de/amr/pacmanfx/uilib/model3D/pellet.obj"
    };

    static void main() {
        for (String path : FILE_PATHS) {
            final URL url = ObjFileParserByCopilot.class.getResource(path);
            if (url == null) {
                Logger.error("Invalid OBJ file URL {}", url);
                return;
            }
            try {
                final ObjFileParserByCopilot parser = new ObjFileParserByCopilot(url, StandardCharsets.UTF_8);
                TriangleMeshBuilder builder = new TriangleMeshBuilder(parser.objModel(), parser.materialLibsMap());
                List<MeshView> meshes = builder.buildMeshes();
                Logger.info("Constructed {} mesh views from OBJ model", meshes.size());
            } catch (IOException x) {
                Logger.error(x, "Parsing error");
            }
        }
    }
}

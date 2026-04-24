/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

import javafx.scene.shape.MeshView;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ObjFileParserTest {
    static final String[] FILE_PATHS = {
        "/de/amr/pacmanfx/uilib/model3D/ghost.obj",
//        "/de/amr/pacmanfx/uilib/model3D/pacman.obj",
//        "/de/amr/pacmanfx/uilib/model3D/pellet.obj"
    };

    static void main() {
        for (String path : FILE_PATHS) {
            final URL url = ObjFileParser.class.getResource(path);
            if (url == null) {
                Logger.error("Invalid OBJ file URL {}", url);
                return;
            }
            try {
                final ObjFileParser parser = new ObjFileParser(url, StandardCharsets.UTF_8);
                TriangleMeshBuilder builder = new TriangleMeshBuilder(parser.objModel(), parser.materialLibsMap());
                Map<String, MeshView> meshes = builder.buildMeshViewsByMaterial();
                Logger.info("Constructed {} mesh views from OBJ model: {}", meshes.size(), meshes);
            } catch (IOException x) {
                Logger.error(x, "Parsing error");
            }
        }
    }
}

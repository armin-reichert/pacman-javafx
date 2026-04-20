/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package experiments;

import de.amr.pacmanfx.uilib.objimport.Model3D;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class ObjModelLoaderTest {

    static void main(String[] args) {
        if (args.length > 0) {
            try {
                final File file = Paths.get(args[0]).toFile();
                final URL url = file.toURI().toURL();
                final Model3D model3D = Model3D.importObjFile(url);
                Logger.info(contentAsText(model3D, url));
            } catch (Exception x) {
                Logger.error(x);
            }
        } else {
            Logger.error("No model path program argument (e.g. 'model3D/ghost.obj') specified");
        }
    }

    public static String contentAsText(Model3D model3D, URL url) {
        var sb = new StringBuilder();
        sb.append("3D model loaded from URL ").append(url).append("\n");
        sb.append("\tMeshes:\n");
        for (var entry : model3D.meshMap().entrySet()) {
            sb.append("\t\t'%s': %s%n".formatted(entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
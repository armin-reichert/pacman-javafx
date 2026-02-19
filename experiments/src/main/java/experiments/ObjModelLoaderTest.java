/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package experiments;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.Model3D;
import de.amr.pacmanfx.uilib.objimport.ObjFileContent;
import javafx.scene.paint.Material;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Map;

public class ObjModelLoaderTest {

    static void main(String[] args) {
        if (args.length > 0) {
            ResourceManager rm = () -> ObjModelLoaderTest.class;
            var url = rm.url(args[0]);
            try {
                var model = Model3D.loadObjFile(url);
                Logger.info(contentAsText(model, url));
            } catch (Exception x) {
                Logger.error(x);
            }
        } else {
            Logger.error("No model path program argument (e.g. 'model3D/ghost.obj') specified");
        }
    }

    public static String contentAsText(ObjFileContent model, URL url) {
        var sb = new StringBuilder();
        sb.append("3D model loaded from URL ").append(url).append("\n");
        sb.append("\tMeshes:\n");
        for (var entry : Model3D.meshMap(model).entrySet()) {
            sb.append("\t\t'%s': %s%n".formatted(entry.getKey(), entry.getValue()));
        }
        sb.append("\tMaterials:\n");
        for (Map<String, Material> lib : Model3D.materialLibs(model)) {
            for (var entry : lib.entrySet()) {
                sb.append("\t\t'%s': %s%n".formatted(entry.getKey(), entry.getValue()));
            }
        }
        return sb.toString();
    }
}
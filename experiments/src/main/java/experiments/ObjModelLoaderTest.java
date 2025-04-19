/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package experiments;

import de.amr.games.pacman.uilib.assets.ResourceManager;
import de.amr.games.pacman.uilib.model3D.Model3D;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class ObjModelLoaderTest {

    public static void main(String[] args) {
        if (args.length > 0) {
            ResourceManager rm = () -> ObjModelLoaderTest.class;
            var url = rm.url(args[0]);
            try {
                var model = new Model3D(url);
                Logger.info(model.contentAsText(url));
            } catch (Exception x) {

            }
        } else {
            Logger.error("No model path program argument (e.g. 'model3D/ghost.obj') specified");
        }
    }
}
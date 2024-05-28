/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.model.Model3D;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class ObjModelLoaderTest {

    public static void main(String[] args) {
        if (args.length > 0) {
            ResourceManager rm = () -> PacManGames3dUI.class;
            var url = rm.url(args[0]);
            var model = new Model3D(url);
            Logger.info(model.contentAsText(url));
        } else {
            Logger.error("No model path program argument (e.g. 'model3D/ghost.obj') specified");
        }
    }
}
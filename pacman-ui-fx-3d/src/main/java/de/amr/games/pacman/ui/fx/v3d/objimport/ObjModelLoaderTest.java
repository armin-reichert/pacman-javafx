/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.objimport;

import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class ObjModelLoaderTest {

	public static void main(String[] args) {
		if (args.length > 0) {
			var app = new PacManGames3dApp();
			var url = app.url(args[0]);
			var model = new Model3D(url);
			System.out.println(model.contentReport());
		} else {
			Logger.error("Missing model file path (relative to asset folder)");
		}
	}
}
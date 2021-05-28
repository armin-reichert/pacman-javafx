package de.amr.games.pacman.ui.fx.model3D;

import static de.amr.games.pacman.lib.Logging.log;

public class Model3DTest {

	public static void main(String[] args) {
		GianmarcosPacManModel3D model = new GianmarcosPacManModel3D();
		log("MeshViews:");
		model.meshViewsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
		log("");
		log("Materials:");
		model.materialsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
		log("Pac-Man 3D model loaded successfully!");
	}
}
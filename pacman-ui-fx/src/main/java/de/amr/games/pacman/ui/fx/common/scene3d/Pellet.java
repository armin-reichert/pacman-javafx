package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Pellet {

	private final int radius = 1;
	private V2i tile;
	private Sphere sphere;

	public Pellet(V2i tile, PhongMaterial material) {
		this.tile = tile;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		sphere.setViewOrder(-tile.y * TS - 1);
	}

	public Node getNode() {
		return sphere;
	}

	public V2i getTile() {
		return tile;
	}

}
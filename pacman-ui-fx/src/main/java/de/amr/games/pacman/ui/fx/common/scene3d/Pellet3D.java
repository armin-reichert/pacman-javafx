package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Normal pellet.
 * 
 * @author Armin Reichert
 */
public class Pellet3D {

	private final int radius = 1;
	private final V2i tile;
	private final Sphere sphere;
	private final GameLevel gameLevel;

	public Pellet3D(V2i tile, PhongMaterial material, GameLevel gameLevel) {
		this.tile = tile;
		this.gameLevel = gameLevel;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		sphere.setViewOrder(-tile.y * TS - 1);
	}

	public void update() {
		sphere.setVisible(!gameLevel.isFoodRemoved(tile));
	}

	public Node getNode() {
		return sphere;
	}

	public V2i getTile() {
		return tile;
	}

}
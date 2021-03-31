package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Normal pellet.
 * 
 * @author Armin Reichert
 */
public class Pellet3D implements Supplier<Node> {

	private final int radius = 1;
	private final V2i tile;
	private final Sphere sphere;

	public Pellet3D(V2i tile, PhongMaterial material) {
		this.tile = tile;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		sphere.setViewOrder(-tile.y * TS - 1);
	}

	public void update(AbstractGameModel game) {
		sphere.setVisible(!game.currentLevel.isFoodRemoved(tile));
	}

	@Override
	public Node get() {
		return sphere;
	}

	public V2i getTile() {
		return tile;
	}
}
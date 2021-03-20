package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.common.Env;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Just another brick in the wall.
 * 
 * @author Armin Reichert
 */
public class Brick3D {

	private final V2i tile;
	private final Box block;

	public Brick3D(V2i tile, PhongMaterial material) {
		this.tile = tile;
		block = new Box(TS - 1, TS - 1, TS - 2);
		block.setMaterial(material);
		block.setTranslateX(tile.x * TS);
		block.setTranslateY(tile.y * TS);
		block.setViewOrder(-tile.y * TS);
		block.drawModeProperty().bind(Env.$drawMode);
	}

	public Node getNode() {
		return block;
	}

	public V2i getTile() {
		return tile;
	}
}
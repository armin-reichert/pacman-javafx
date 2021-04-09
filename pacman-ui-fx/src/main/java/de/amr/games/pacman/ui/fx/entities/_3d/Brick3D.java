package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Just another brick in the wall.
 * 
 * @author Armin Reichert
 */
public class Brick3D extends Box implements Supplier<Node> {

	public Brick3D(V2i tile, double w, double h, double d, PhongMaterial material) {
		this(tile.x * TS, tile.y * TS, w, h, d, material, tile);
	}

	public Brick3D(double x, double y, double w, double h, double d, PhongMaterial material, Object userData) {
		super(w, h, d);
		setMaterial(material);
		setTranslateX(x);
		setTranslateY(y);
		setUserData(userData);
		drawModeProperty().bind(Env.$drawMode);
	}

	@Override
	public Node get() {
		return this;
	}
}
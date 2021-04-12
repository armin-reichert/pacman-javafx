package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * 3D shape for a dead ghost.
 * 
 * @author Armin Reichert
 */
public class DeadGhost3D implements Supplier<Node> {

	private final Group root = new Group();
	private final Sphere[] pearls = new Sphere[3];

	public DeadGhost3D(Ghost ghost) {
		PhongMaterial skin = new PhongMaterial(GameRendering3D_Assets.getGhostColor(ghost.id));
		for (int i = 0; i < pearls.length; ++i) {
			pearls[i] = new Sphere(1);
			pearls[i].setMaterial(skin);
			pearls[i].setTranslateX(i * 3);
		}
		root.getChildren().addAll(pearls);
	}

	@Override
	public Node get() {
		return root;
	}
}
package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets.getGhostColor;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.model3D.JustAnotherModel3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

public class ColoredGhost3D implements Supplier<Node> {

	private final MeshView root;
	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;

	@Override
	public Node get() {
		return root;
	}

	public ColoredGhost3D(Ghost ghost) {
		normalSkin = new PhongMaterial(getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
		root = JustAnotherModel3D.IT.createGhost();
		root.setMaterial(normalSkin);
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
	}

	public void setBlue(boolean blue) {
		root.setMaterial(blue ? blueSkin : normalSkin);
	}

}

package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class ColoredGhost3D implements Supplier<Node> {

	private final Group root;
	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;

	@Override
	public Node get() {
		return root;
	}

	public ColoredGhost3D(Ghost ghost) {
		normalSkin = new PhongMaterial(Rendering2D_Assets.getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
//		root = JustAnotherModel3D.IT.createGhost();
		root = GianmarcosModel3D.IT.createGhost();
//		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		setBlue(false);
	}

	public void setBlue(boolean blue) {
		MeshView meshView = (MeshView) root.getChildren().get(0);
		meshView.setMaterial(blue ? blueSkin : normalSkin);
	}
}
package de.amr.games.pacman.ui.fx.entities._3d;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class ColoredGhost3D extends Group {

	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;

	public ColoredGhost3D(Ghost ghost) {
		normalSkin = new PhongMaterial(Rendering2D_Assets.getGhostColor(ghost.id));
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);
		getChildren().add(GianmarcosModel3D.IT.createGhost());
		setBlue(false);
	}

	public void setBlue(boolean blue) {
		Group ghost = (Group) getChildren().get(0);
		MeshView meshView = (MeshView) ghost.getChildren().get(0);
		meshView.setMaterial(blue ? blueSkin : normalSkin);
	}
}
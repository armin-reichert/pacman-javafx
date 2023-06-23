/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

/**
 * Part a ghosthouse door.
 * 
 * @author Armin Reichert
 */
public class DoorWing3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final ObjectProperty<PhongMaterial> barMaterialPy = new SimpleObjectProperty<>(this, "barMaterial",
			new PhongMaterial(Color.PINK));

	private final Group root = new Group();

	private PhongMaterial doorOpenMaterial;
	private PhongMaterial doorClosedMaterial;

	public DoorWing3D(Vector2i tile, Color color) {
		checkTileNotNull(tile);
		checkNotNull(color);

		doorClosedMaterial = ResourceManager.coloredMaterial(color); // TODO
		doorOpenMaterial = ResourceManager.coloredMaterial(ResourceManager.color(Color.gray(0.8), 0.1)); // TODO

		for (int i = 0; i < 2; ++i) {
			var vbar = new Cylinder(1, 8);
			vbar.materialProperty().bind(barMaterialPy);
			double x = tile.x() * 8 + i * 4 + 2;
			double y = tile.y() * 8 + 4;
			vbar.setTranslateX(x);
			vbar.setTranslateY(y);
			vbar.setTranslateZ(-4);
			vbar.setRotationAxis(Rotate.X_AXIS);
			vbar.setRotate(90);
			vbar.drawModeProperty().bind(drawModePy);
			root.getChildren().add(vbar);
		}
		var hbar = new Cylinder(0.5, 9);
		hbar.materialProperty().bind(barMaterialPy);
		hbar.setTranslateX(tile.x() * 8 + 4);
		hbar.setTranslateY(tile.y() * 8 + 4);
		hbar.setTranslateZ(-4);
		hbar.setRotationAxis(Rotate.Z_AXIS);
		hbar.setRotate(90);
		root.getChildren().add(hbar);
	}

	public Node getRoot() {
		return root;
	}

	public void setOpen(boolean open) {
		barMaterialPy.set(open ? doorOpenMaterial : doorClosedMaterial);
	}
}
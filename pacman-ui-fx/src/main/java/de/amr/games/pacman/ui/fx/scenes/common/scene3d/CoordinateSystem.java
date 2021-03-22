package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import de.amr.games.pacman.ui.fx.scenes.common.Env;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * TODO: fixme
 * 
 * @author Armin Reichert
 */
public class CoordinateSystem {

	private final Group root;

	public CoordinateSystem(int len) {
		Sphere origin = new Sphere(2);
		origin.setMaterial(new PhongMaterial(Color.BISQUE));

		Cylinder posX = createAxis(Color.RED.brighter(), len);
		posX.getTransforms().add(new Translate(len / 2, 0, 0));
		posX.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));

		Cylinder posY = createAxis(Color.GREEN.brighter(), len);
		posY.getTransforms().add(new Translate(0, len / 2, 0));

		Cylinder negZ = createAxis(Color.BLUE.brighter(), len);
		posY.getTransforms().add(new Translate(0, -len / 2, 0));
		negZ.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));

		root = new Group(origin, posX, posY, negZ);
		root.visibleProperty().bind(Env.$showAxes);

	}

	private Cylinder createAxis(Color color, double height) {
		Cylinder axis = new Cylinder(1, height);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}

	public Node getNode() {
		return root;
	}

}
package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.ui.fx.common.Env;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Pac-Man or Ms. Pac-Man 3D shape.
 * 
 * @author Armin Reichert
 */
public class Player {

	private Node root;

	public Player() {
		MeshView body = new MeshView(Assets.guyMeshTemplates.get("Sphere_Sphere.002_Material.001").getMesh());
		body.setMaterial(new PhongMaterial(Color.YELLOW));
		body.setDrawMode(Env.$drawMode.get());

		MeshView glasses = new MeshView(Assets.guyMeshTemplates.get("Sphere_Sphere.002_Material.002").getMesh());
		glasses.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));
		glasses.setDrawMode(Env.$drawMode.get());

		Translate centering = Assets.centerOverOrigin(body);
		glasses.getTransforms().add(centering);

		root = new Group(body, glasses);
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Assets.scale(root, TS);
	}

	public Node getNode() {
		return root;
	}
}
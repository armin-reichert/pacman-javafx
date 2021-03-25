package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.scenes.common.Env;
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
public class Player3D implements Supplier<Node> {

	private final Pac pac;
	private final Node root;

	public Player3D(Pac pac) {
		this.pac = pac;

		MeshView body = new MeshView(Assets3D.guyMeshTemplates.get("Sphere_Sphere.002_Material.001").getMesh());
		body.setMaterial(new PhongMaterial(Color.YELLOW));
		body.drawModeProperty().bind(Env.$drawMode);

		MeshView glasses = new MeshView(Assets3D.guyMeshTemplates.get("Sphere_Sphere.002_Material.002").getMesh());
		glasses.setMaterial(new PhongMaterial(Color.rgb(40, 40, 60)));
		glasses.drawModeProperty().bind(Env.$drawMode);

		Translate centering = Assets3D.centerOverOrigin(body);
		glasses.getTransforms().add(centering);

		root = new Group(body, glasses);
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Assets3D.scale(root, TS);
	}

	@Override
	public Node get() {
		return root;
	}

	public void update() {
		root.setVisible(pac.visible);
		root.setTranslateX(pac.position.x);
		root.setTranslateY(pac.position.y);
		root.setViewOrder(-pac.position.y - 2);
		root.setRotationAxis(Rotate.Y_AXIS);
		root.setRotate(90);
		switch (pac.dir) {
		case LEFT:
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(90);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(90);
			break;
		case RIGHT:
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(-90);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(-90);
			break;
		case UP:
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(0);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(180);
			break;
		case DOWN:
			root.setRotationAxis(Rotate.Y_AXIS);
			root.setRotate(0);
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(0);
			break;
		default:
			break;
		}
	}
}
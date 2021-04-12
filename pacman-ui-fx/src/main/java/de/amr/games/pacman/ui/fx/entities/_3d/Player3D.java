package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.model3D.JustAnotherModel3D;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * Pac-Man or Ms. Pac-Man 3D shape.
 * 
 * @author Armin Reichert
 */
public class Player3D implements Supplier<Node> {

	public final ReadOnlyBooleanProperty $visible = new ReadOnlyBooleanPropertyBase() {

		@Override
		public boolean get() {
			return pac.visible;
		}

		@Override
		public Object getBean() {
			return pac;
		}

		@Override
		public String getName() {
			return "visible";
		}
	};

	public final Pac pac;
	private final Node root;

	public Player3D(Pac pac) {
		this.pac = pac;

//		MeshView body = new MeshView(
//				GameRendering3D_Assets.guyMeshTemplates.get("Sphere_Sphere.002_Material.001").getMesh());
//		body.setMaterial(new PhongMaterial(Color.YELLOW));
//		body.drawModeProperty().bind(Env.$drawMode);
//
//		MeshView glasses = new MeshView(
//				GameRendering3D_Assets.guyMeshTemplates.get("Sphere_Sphere.002_Material.002").getMesh());
//		glasses.setMaterial(new PhongMaterial(Color.rgb(60, 60, 60)));
//		glasses.drawModeProperty().bind(Env.$drawMode);
//
//		Translate centering = GameRendering3D_Assets.centerOverOrigin(body);
//		glasses.getTransforms().add(centering);
//
//		root = new Group(body, glasses);
//		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
//		GameRendering3D_Assets.scale(root, TS);

		root = JustAnotherModel3D.IT.createPlayerMesh();
	}

	@Override
	public Node get() {
		return root;
	}

	public void update(PacManGameWorld world) {
		boolean inPortal = world.isPortal(pac.tile());
		root.setVisible(pac.visible && !inPortal);
		root.setTranslateX(pac.position.x);
		root.setTranslateY(pac.position.y);
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
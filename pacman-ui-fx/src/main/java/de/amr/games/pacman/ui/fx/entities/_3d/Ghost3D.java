package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D implements Supplier<Node> {

	private final Ghost ghost;
	private Group root;
	private Text bountyText;
	private PhongMaterial normalSkin;
	private PhongMaterial blueSkin;
	private MeshView meshView;

	@Override
	public Node get() {
		return root;
	}

	public Ghost3D(Ghost ghost) {
		this.ghost = ghost;

		normalSkin = GameRendering3D_Assets.ghostSkin(ghost.id);
		blueSkin = new PhongMaterial(Color.CORNFLOWERBLUE);

		meshView = GameRendering3D_Assets.createGhostMeshView(ghost.id);
		meshView.setMaterial(normalSkin);
		meshView.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Group coloredGhost = new Group(meshView);

		bountyText = new Text();
		bountyText.setText(String.valueOf(ghost.bounty));
		bountyText.setFont(Font.font("Sans", FontWeight.MEDIUM, 16));
		bountyText.setFill(Color.CYAN);
		Group bountyGhost = new Group(bountyText);

		Sphere[] pearls = new Sphere[3];
		for (int i = 0; i < pearls.length; ++i) {
			pearls[i] = new Sphere(1);
			pearls[i].setMaterial(normalSkin);
			pearls[i].setTranslateX(i * 3);
		}
		Group deadGhost = new Group(pearls);

		root = new Group(coloredGhost, bountyGhost, deadGhost);
		selectChild(0);
	}

	private void selectChild(int index) {
		for (int i = 0; i < 3; ++i) {
			root.getChildren().get(i).setVisible(i == index);
		}
	}

	public void update() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		root.setViewOrder(-(ghost.position.y + 5)); // TODO
		if (ghost.bounty > 0) {
			bountyText.setText(String.valueOf(ghost.bounty));
			root.setRotate(0);
			selectChild(1);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
			selectChild(2);
		} else if (ghost.is(GhostState.FRIGHTENED)) {
			meshView.setMaterial(blueSkin);
			turnTowardsMoveDirection();
			selectChild(0);
		} else {
			meshView.setMaterial(normalSkin);
			turnTowardsMoveDirection();
			selectChild(0);
		}
	}

	private void turnTowardsMoveDirection() {
		root.setRotationAxis(Rotate.Y_AXIS);
		root.setRotate(0);
		switch (ghost.dir) {
		case LEFT:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(180);
			break;
		case RIGHT:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(0);
			break;
		case UP:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(-90);
			break;
		case DOWN:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(90);
			break;
		default:
			break;
		}
	}
}
package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
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
import javafx.scene.transform.Translate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D implements Supplier<Node> {

	private final Ghost ghost;
	private Group root;
	private MeshView meshView;
	private Text bountyText;
	private Group pearlChain;

	public Ghost3D(Ghost ghost) {
		this.ghost = ghost;
		createMeshView();
		createBountyText();
		createPearlChain();
		root = new Group(meshView, bountyText, pearlChain);
		displayColored();
	}

	public void update() {
		if (ghost.bounty > 0) {
			displayAsBounty();
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			displayReturningHome();
		} else if (ghost.is(GhostState.FRIGHTENED)) {
			displayFrightened();
		} else {
			displayColored();
		}
	}

	private void createMeshView() {
		meshView = Assets3D.createGhostMeshView(ghost.id);
		meshView.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
	}

	private void createBountyText() {
		bountyText = new Text();
		bountyText.setText(String.valueOf(ghost.bounty));
		bountyText.setFont(Font.font("Sans", FontWeight.MEDIUM, 8));
		bountyText.setFill(Color.CYAN);
		bountyText.setTranslateZ(-1.5 * TS);
	}

	private void createPearlChain() {
		PhongMaterial skin = Assets3D.ghostSkin(ghost.id);
		Sphere[] pearls = new Sphere[3];
		for (int i = 0; i < pearls.length; ++i) {
			pearls[i] = new Sphere(1);
			pearls[i].setMaterial(skin);
			pearls[i].setTranslateX(i * 3);
		}
		pearlChain = new Group(pearls);
		pearlChain.getTransforms().add(new Translate(-3, 0, 0));
	}

	@Override
	public Node get() {
		return root;
	}

	public void displayColored() {
		setColor(Assets3D.ghostColor(ghost.id));
		selectChild(0);
		updateTransforms();
	}

	public void displayFrightened() {
		setColor(Color.CORNFLOWERBLUE);
		selectChild(0);
		updateTransforms();
	}

	public void displayAsBounty() {
		selectChild(1);
		bountyText.setText("" + ghost.bounty);
		updateTransforms();
	}

	public void displayReturningHome() {
		selectChild(2);
		pearlChain.setRotationAxis(Rotate.Z_AXIS);
		pearlChain.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		updateTransforms();
	}

	private void selectChild(int index) {
		for (int i = 0; i < 3; ++i) {
			root.getChildren().get(i).setVisible(i == index);
		}
	}

	private void setColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		meshView.setMaterial(material);
	}

	private void updateTransforms() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		root.setViewOrder(-(ghost.position.y + 5));
	}
}
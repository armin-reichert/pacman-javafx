package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
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
		root = GianmarcosModel3D.IT.createPacMan();
	}

	@Override
	public Node get() {
		return root;
	}

	private void turnToMoveDirection() {
		// now. Pac-Man looks to the LEFT
		root.setRotationAxis(Rotate.Z_AXIS);
		switch (pac.dir) {
		case LEFT:
			root.setRotate(0);
			break;
		case RIGHT:
			root.setRotate(180);
			break;
		case UP:
			root.setRotate(90);
			break;
		case DOWN:
			root.setRotate(270);
			break;
		default:
			break;
		}
	}

	public void update(PacManGameWorld world) {
		boolean inPortal = world.isPortal(pac.tile());
		root.setVisible(pac.visible && !inPortal);
		root.setTranslateX(pac.position.x);
		root.setTranslateY(pac.position.y);
		turnToMoveDirection();
	}
}
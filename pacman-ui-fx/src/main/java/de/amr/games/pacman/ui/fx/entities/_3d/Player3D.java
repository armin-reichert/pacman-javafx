package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.Pac;
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

	private static double lerp(double current, double target) {
		Logging.log("%.0f -> %.0f", current, target);
		return current + (target - current) * 0.25;
	}

	private void turnToMoveDirection() {
		root.setRotationAxis(Rotate.Z_AXIS);
		double currentRotate = root.getRotate();
		switch (pac.dir) {
		case LEFT:
			root.setRotate(lerp(currentRotate, 0));
			break;
		case RIGHT:
			root.setRotate(lerp(currentRotate, 180));
			break;
		case UP:
			root.setRotate(lerp(currentRotate, 90));
			break;
		case DOWN:
			root.setRotate(lerp(currentRotate, 270));
			break;
		default:
			break;
		}
	}

	public void update(AbstractGameModel game) {
		boolean inPortal = game.currentLevel.world.isPortal(pac.tile());
		root.setVisible(pac.visible && !inPortal);
		root.setTranslateX(pac.position.x);
		root.setTranslateY(pac.position.y);
		turnToMoveDirection();
	}
}
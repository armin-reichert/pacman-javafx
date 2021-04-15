package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
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

	public void update(AbstractGameModel game) {
		boolean inPortal = game.currentLevel.world.isPortal(pac.tile());
		root.setVisible(pac.visible && !inPortal);
		root.setTranslateX(pac.position.x);
		root.setTranslateY(pac.position.y);
		changeMoveDirection(rot(pac.prevDir()), rot(pac.dir()));
	}

	private int rot(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.UP ? 90 : dir == Direction.RIGHT ? 180 : 270;
	}

	private void changeMoveDirection(double from, double to) {
		root.setRotationAxis(Rotate.Z_AXIS);
		root.setRotate(to);
	}
}
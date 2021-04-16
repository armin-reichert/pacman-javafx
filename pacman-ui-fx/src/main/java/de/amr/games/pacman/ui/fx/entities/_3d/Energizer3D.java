package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * Animated energizer.
 * 
 * @author Armin Reichert
 */
public class Energizer3D extends Sphere implements Supplier<Node> {

	private final V2i tile;
	private final ScaleTransition pumping;

	public Energizer3D(V2i tile, PhongMaterial material) {
		super(3);
		this.tile = tile;
		setMaterial(material);
		setTranslateX(tile.x * TS);
		setTranslateY(tile.y * TS);
		pumping = new ScaleTransition(Duration.seconds(0.25), this);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Transition.INDEFINITE);
		pumping.setFromX(0);
		pumping.setFromY(0);
		pumping.setFromZ(0);
		pumping.setToX(1.1);
		pumping.setToY(1.1);
		pumping.setToZ(1.1);
	}

	@Override
	public Node get() {
		return this;
	}

	public void update(AbstractGameModel game) {
		setVisible(!game.currentLevel.isFoodRemoved(tile));
	}

	public V2i getTile() {
		return tile;
	}

	public void startPumping() {
		pumping.playFromStart();
	}

	public void stopPumping() {
		pumping.stop();
		setScaleX(1);
		setScaleY(1);
		setScaleZ(1);
	}
}
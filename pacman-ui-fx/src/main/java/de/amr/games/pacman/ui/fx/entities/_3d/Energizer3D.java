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
public class Energizer3D implements Supplier<Node> {

	private final int radius = 3;
	private final V2i tile;
	private final Sphere sphere;
	private final ScaleTransition pumping;

	public Energizer3D(V2i tile, PhongMaterial material) {
		this.tile = tile;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		pumping = new ScaleTransition(Duration.seconds(0.25), sphere);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Transition.INDEFINITE);
		pumping.setFromX(0);
		pumping.setFromY(0);
		pumping.setFromZ(0);
		pumping.setToX(1.25);
		pumping.setToY(1.25);
		pumping.setToZ(1.25);
	}

	public void update(AbstractGameModel game) {
		sphere.setVisible(!game.currentLevel.isFoodRemoved(tile));
	}

	@Override
	public Node get() {
		return sphere;
	}

	public V2i getTile() {
		return tile;
	}

	public void startPumping() {
		pumping.playFromStart();
	}

	public void stopPumping() {
		pumping.stop();
		sphere.setScaleX(1);
		sphere.setScaleY(1);
		sphere.setScaleZ(1);
	}
}
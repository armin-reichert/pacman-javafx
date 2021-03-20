package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
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
public class Energizer {

	private final int radius = 3;
	private V2i tile;
	private Sphere sphere;
	private ScaleTransition pumping;

	public Energizer(V2i tile, PhongMaterial material) {
		this.tile = tile;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		sphere.setUserData(tile);
		sphere.setViewOrder(-tile.y * TS - 1);
		createAnimation();
	}

	private void createAnimation() {
		pumping = new ScaleTransition(Duration.seconds(0.25), sphere);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Transition.INDEFINITE);
		pumping.setFromX(0);
		pumping.setFromY(0);
		pumping.setFromZ(0);
		pumping.setToX(1.5);
		pumping.setToY(1.5);
		pumping.setToZ(1.5);
	}

	public Node getNode() {
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
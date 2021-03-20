package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
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
public class Energizer3D {

	private final int radius = 3;
	private final V2i tile;
	private final Sphere sphere;
	private final GameLevel gameLevel;
	private final ScaleTransition pumping;

	public Energizer3D(V2i tile, PhongMaterial material, GameLevel gameLevel) {
		this.tile = tile;
		this.gameLevel = gameLevel;
		sphere = new Sphere(radius);
		sphere.setMaterial(material);
		sphere.setTranslateX(tile.x * TS);
		sphere.setTranslateY(tile.y * TS);
		sphere.setViewOrder(-tile.y * TS - 1);
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

	public void update() {
		sphere.setVisible(!gameLevel.isFoodRemoved(tile));
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
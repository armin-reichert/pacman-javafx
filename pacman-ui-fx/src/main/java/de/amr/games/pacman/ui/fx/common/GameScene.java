package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Optional;
import java.util.OptionalDouble;

import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import javafx.scene.Camera;
import javafx.scene.SubScene;

/**
 * Common interface of all game scenes.
 * 
 * @author Armin Reichert
 */
public interface GameScene {

	public static final int WIDTH_UNSCALED = 28 * TS;
	public static final int HEIGHT_UNSCALED = 36 * TS;
	public static final double ASPECT_RATIO = (double) WIDTH_UNSCALED / HEIGHT_UNSCALED;

	/**
	 * @return aspect ratio width / height
	 */
	OptionalDouble aspectRatio();

	Optional<PacManGameAnimations> animations();

	void start();

	void update();

	void end();

	void resize(double width, double height);

	SubScene getSubScene();

	Camera getCamera();

	default void initCamera() {
	}

	default void updateCamera() {
	}
}
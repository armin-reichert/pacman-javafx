package de.amr.games.pacman.ui.fx.scenes.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.DefaultPacManGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Common interface for all game scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public interface GameScene extends DefaultPacManGameEventHandler {

	public static final int UNSCALED_SCENE_WIDTH = 28 * TS;
	public static final int UNSCALED_SCENE_HEIGHT = 36 * TS;

	SubScene getSubScene();

	void init();

	void update();

	void end();

	PacManGameController getGameController();

	void setGameController(PacManGameController gameController);

	default GameModel game() {
		return getGameController() != null ? getGameController().game() : null;
	}

	OptionalDouble aspectRatio();

	void stretchTo(double width, double height);

	default void keepStretched(Scene parentScene) {
		if (aspectRatio().isPresent()) {
			double aspectRatio = aspectRatio().getAsDouble();
			parentScene.widthProperty().addListener((s, o, newParentWidth) -> {
				double maxHeight = Math.min(newParentWidth.doubleValue() / aspectRatio, parentScene.getHeight());
				double maxWidth = maxHeight * aspectRatio;
				stretchTo(maxWidth, maxHeight);
			});
			parentScene.heightProperty().addListener((s, o, newParentHeight) -> {
				double maxHeight = newParentHeight.doubleValue();
				double maxWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				stretchTo(maxWidth, maxHeight);
			});
		} else {
			getSubScene().widthProperty().bind(parentScene.widthProperty());
			getSubScene().heightProperty().bind(parentScene.heightProperty());
		}
	}
}
package de.amr.games.pacman.ui.fx.common;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

/**
 * Interface implemented by each scene.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	Optional<PacManGameModel> game();

	Scene getFXScene();

	GraphicsContext gc();

	void start();

	void end();

	void update();

	void render();

	Keyboard keyboard();

	Optional<PacManGameAnimation> animation();
}
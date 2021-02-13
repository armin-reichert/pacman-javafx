package de.amr.games.pacman.ui.fx.scene.common;

import java.util.Optional;

import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;

/**
 * Interface implemented by each scene.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	Scene getFXScene();

	void start();

	void end();

	void render();

	Keyboard keyboard();

	Optional<PacManGameAnimation> animation();
}
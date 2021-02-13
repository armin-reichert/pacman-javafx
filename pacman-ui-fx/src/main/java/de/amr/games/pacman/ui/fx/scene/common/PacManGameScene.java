package de.amr.games.pacman.ui.fx.scene.common;

import java.util.Optional;

import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;

public interface PacManGameScene {

	Scene getFXScene();

	void start();

	void end();

	void render();

	Keyboard keyboard();

	Optional<PacManGameAnimations> animations();
}
/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public class GameSceneChoice {

	public static final String KEY_2D = "2D";
	public static final String KEY_3D = "3D";

	private final Map<String, GameScene> choices = new HashMap<>(2);

	public static GameSceneChoice onlyScene2D(GameScene2D scene) {
		var choice = new GameSceneChoice();
		choice.setScene2D(scene);
		return choice;
	}

	public Optional<GameScene> scene2D() {
		return Optional.ofNullable(choices.get(KEY_2D));
	}

	public Optional<GameScene> scene3D() {
		return Optional.ofNullable(choices.get(KEY_3D));
	}

	public void setScene2D(GameScene scene) {
		choices.put(KEY_2D, scene);
	}

	public void setScene3D(GameScene scene) {
		choices.put(KEY_3D, scene);
	}
}
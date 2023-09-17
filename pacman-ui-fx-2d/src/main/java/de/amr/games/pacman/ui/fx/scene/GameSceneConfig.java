/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class GameSceneConfig {
	private final Map<String, GameScene> scenes = new HashMap<>();

	public GameSceneConfig() {
	}

	public GameSceneConfig(
			GameScene2D bootScene, GameScene2D introScene, GameScene2D creditScene,
			GameScene2D playScene, GameScene2D cut1, GameScene2D cut2, GameScene2D cut3) {
		scenes.put("boot",   bootScene);
		scenes.put("intro",  introScene);
		scenes.put("credit", creditScene);
		scenes.put("play",   playScene);
		scenes.put("cut1",   cut1);
		scenes.put("cut2",   cut2);
		scenes.put("cut3",   cut3);
	}

	public <T extends GameScene> T gameScene(String id) {
		return (T) scenes.get(id);
	}
}
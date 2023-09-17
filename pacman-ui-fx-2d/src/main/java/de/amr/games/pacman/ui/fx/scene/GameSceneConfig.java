/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

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
		setGameScene("boot",   bootScene);
		setGameScene("intro",  introScene);
		setGameScene("credit", creditScene);
		setGameScene("play",   playScene);
		setGameScene("cut1",   cut1);
		setGameScene("cut2",   cut2);
		setGameScene("cut3",   cut3);
	}

	public void setGameScene(String id, GameScene gameScene) {
		checkNotNull(id);
		checkNotNull(gameScene);
		scenes.put(id, gameScene);
	}

	public <T extends GameScene> T gameScene(String id) {
		return (T) scenes.get(id);
	}
}
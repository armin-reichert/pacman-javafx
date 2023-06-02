/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;

/**
 * @author Armin Reichert
 */
public class GameSceneConfiguration {

	private final GameScene2D bootScene;
	private final GameScene2D introScene;
	private final GameScene2D creditScene;
	private final GameScene2D playScene;
	private GameScene playScene3D;
	private final GameScene2D[] cutScenes;

	public GameSceneConfiguration(GameScene2D bootScene, GameScene2D introScene, GameScene2D creditScene,
			GameScene2D playScene, GameScene playScene3D, GameScene2D cut1, GameScene2D cut2, GameScene2D cut3) {
		this.bootScene = bootScene;
		this.introScene = introScene;
		this.creditScene = creditScene;
		this.playScene = playScene;
		this.playScene3D = playScene3D;
		this.cutScenes = new GameScene2D[] { cut1, cut2, cut3 };
	}

	public GameScene2D bootScene() {
		return bootScene;
	}

	public GameScene2D creditScene() {
		return creditScene;
	}

	public GameScene2D introScene() {
		return introScene;
	}

	public GameScene2D playScene() {
		return playScene;
	}

	public GameScene playScene3D() {
		return playScene3D;
	}

	public void setPlayScene3D(GameScene playScene3D) {
		this.playScene3D = playScene3D;
	}

	public GameScene2D cutScene(int cutSceneNumber) {
		return cutScenes[cutSceneNumber - 1];
	}
}
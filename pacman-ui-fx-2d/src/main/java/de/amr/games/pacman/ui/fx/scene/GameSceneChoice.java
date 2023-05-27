/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

/**
 * @author Armin Reichert
 */
public class GameSceneChoice {

	private GameScene scene2D;
	private GameScene scene3D;

	public GameSceneChoice(GameScene scene2D, GameScene scene3D) {
		this.scene2D = scene2D;
		this.scene3D = scene3D;
	}

	public GameSceneChoice(GameScene scene2D) {
		this.scene2D = scene2D;
	}

	public GameScene scene2D() {
		return scene2D;
	}

	public GameScene scene3D() {
		return scene3D;
	}

	public void setScene2D(GameScene scene2d) {
		scene2D = scene2d;
	}

	public void setScene3D(GameScene scene3d) {
		scene3D = scene3d;
	}

	public boolean includes(GameScene gameScene) {
		return gameScene == scene2D || gameScene == scene3D;
	}
}
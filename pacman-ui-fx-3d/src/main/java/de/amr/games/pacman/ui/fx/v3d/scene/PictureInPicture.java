/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(PacManGames3d.PIP_MIN_HEIGHT);

	public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);

	private final GameScene2D gameScene;

	public PictureInPicture() {
		gameScene = new PlayScene2D();
		gameScene.setSceneCanvasScaled(true);
		gameScene.setRoundedCorners(false);
		gameScene.fxSubScene().heightProperty().bind(heightPy);
		gameScene.fxSubScene().widthProperty().bind(heightPy.multiply(GameScene2D.ASPECT_RATIO));
		gameScene.fxSubScene().opacityProperty().bind(opacityPy);
		gameScene.fxSubScene().setVisible(false);
	}

	public Node root() {
		return gameScene.fxSubScene();
	}

	public void render() {
		gameScene.render();
	}

	public void update(GameScene master, boolean on) {
		if (master != null) {
			gameScene.setContext(master.context());
			gameScene.fxSubScene().setVisible(on && master.is3D());
		} else {
			gameScene.fxSubScene().setVisible(false);
		}
	}
}
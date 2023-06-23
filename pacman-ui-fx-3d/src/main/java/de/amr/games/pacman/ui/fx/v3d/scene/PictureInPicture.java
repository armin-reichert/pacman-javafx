/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(PacManGames3d.PIP_MIN_HEIGHT);
	public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);
	private final GameScene2D gameScene;

	public PictureInPicture(PacManGames2dUI ui) {
		var canvas = new Canvas(heightPy.get() * 28 / 36, heightPy.get());
		gameScene = new PlayScene2D(ui);
		gameScene.setCanvas(canvas);
		gameScene.root().opacityProperty().bind(opacityPy);
		gameScene.root().setVisible(false);
		heightPy.addListener((py, ov, nv) -> {
			gameScene.setScaling(nv.doubleValue() / GameScene2D.HEIGHT_UNSCALED);
		});
	}

	public Node root() {
		return gameScene.root();
	}

	public void render() {
		gameScene.render();
	}

	public void update(GameScene master, boolean on) {
		if (master != null) {
			gameScene.root().setVisible(on && master.is3D());
			gameScene.setScoreVisible(true);
			gameScene.setCreditVisible(false);
		} else {
			gameScene.root().setVisible(false);
		}
	}
}
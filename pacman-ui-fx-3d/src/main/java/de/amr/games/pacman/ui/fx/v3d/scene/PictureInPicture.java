/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dApp;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(PacManGames3dApp.PIP_MIN_HEIGHT);
	public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);
	private final PlayScene2D playScene2D;
	private PlayScene3D master;

	public PictureInPicture() {
		var canvas = new Canvas(heightPy.get() * 28 / 36, heightPy.get());
		playScene2D = new PlayScene2D();
		playScene2D.setCanvas(canvas);
		playScene2D.setScoreVisible(true);
		playScene2D.setCreditVisible(false);
		playScene2D.root().opacityProperty().bind(opacityPy);
		playScene2D.root().setVisible(false);
		heightPy.addListener((py, ov, nv) -> {
			double scaling = nv.doubleValue() / PacManGames2dApp.CANVAS_HEIGHT_UNSCALED;
			canvas.setWidth(PacManGames2dApp.CANVAS_WIDTH_UNSCALED * scaling);
			canvas.setHeight(PacManGames2dApp.CANVAS_HEIGHT_UNSCALED * scaling);
			playScene2D.setScaling(scaling);
		});
	}

	public void setMaster(PlayScene3D master) {
		Globals.checkNotNull(master);
		this.master = master;
		playScene2D.setContext(master.context());
	}

	public Node root() {
		return playScene2D.root();
	}

	public void render() {
		playScene2D.draw();
	}
}
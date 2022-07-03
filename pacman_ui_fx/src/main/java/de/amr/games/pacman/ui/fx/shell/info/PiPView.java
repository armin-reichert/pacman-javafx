/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

/**
 * Picture-In-Picture view. Displays 2D game scene when 3D game scene is active.
 * 
 * @author Armin Reichert
 */
public class PiPView extends StackPane {

	private final GameUI ui;
	private final Pane sceneContainer = new Pane();
	private final PlayScene2D playScene2D;
	private final Text hint = new Text();
	private double scale = 1.0;
	private double width = 28 * 8 * scale;
	private double height = 36 * 8 * scale;

	public PiPView(GameUI ui) {
		this.ui = ui;
		playScene2D = new PlayScene2D(false);
		var subScene = playScene2D.getFXSubScene();
		subScene.setFocusTraversable(false);
		subScene.setWidth(width);
		subScene.setHeight(height);
		playScene2D.getCanvas().widthProperty().bind(subScene.widthProperty());
		playScene2D.getCanvas().heightProperty().bind(subScene.heightProperty());
		playScene2D.getCanvas().getTransforms().setAll(new Scale(scale, scale));
		playScene2D.getOverlayCanvas().visibleProperty().unbind();
		playScene2D.getOverlayCanvas().setVisible(false);
		sceneContainer.setMinWidth(width);
		sceneContainer.setMinHeight(height);
		sceneContainer.getChildren().setAll(subScene);
		hint.setFont(Font.font("Sans", FontWeight.EXTRA_BOLD, 20.0));
		hint.setFill(Color.WHITE);
		hint.setText("3D play scene inactive");
		setBackground(Ufx.colorBackground(Color.BLACK));
		getChildren().setAll(sceneContainer, hint);
	}

	public void init() {
		if (ui.getCurrentGameScene() instanceof PlayScene3D) {
			playScene2D.setSceneContext(ui.getSceneContext());
			playScene2D.init();
		}
	}

	public void update() {
		if (ui.getCurrentGameScene() instanceof PlayScene3D) {
			playScene2D.update();
			sceneContainer.setVisible(true);
			hint.setVisible(false);
		} else {
			sceneContainer.setVisible(false);
			hint.setVisible(true);
		}
	}
}
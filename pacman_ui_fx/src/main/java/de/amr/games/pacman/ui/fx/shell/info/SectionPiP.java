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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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
 * Picture-In-Picture view.
 * 
 * @author Armin Reichert
 */
public class SectionPiP extends Section {

	private final StackPane root = new StackPane();
	private final Pane pipSceneContainer = new Pane();
	private final Text hint = new Text();
	private final double w = 260.0;
	private final double h = w * 1.35;
	private GameScene2D pipScene;

	public SectionPiP(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, gc, title, minLabelWidth, textColor, textFont, labelFont);
		content.add(root, 0, 0, 2, 1);
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		root.getChildren().setAll(pipSceneContainer, hint);
		pipSceneContainer.setMinWidth(w);
		pipSceneContainer.setMinHeight(h);
		hint.setFont(Font.font("Sans", FontWeight.EXTRA_BOLD, 20.0));
		hint.setFill(Color.WHITE);
		hint.setText("Nothing to see here");
	}

	@Override
	public void init() {
		if (ui.getCurrentGameScene() instanceof PlayScene3D) {
			pipScene = new PlayScene2D();
			var subScene = pipScene.getFXSubScene();
			subScene.setWidth(w);
			subScene.setHeight(h);
			pipScene.getCanvas().widthProperty().bind(subScene.widthProperty());
			pipScene.getCanvas().heightProperty().bind(subScene.heightProperty());
			pipScene.getCanvas().getTransforms().setAll(new Scale(1.2, 1.2));
			pipScene.setSceneContext(ui.getSceneContext());
			pipScene.init();
			pipSceneContainer.getChildren().setAll(subScene);
		}
	}

	@Override
	public void update() {
		if (pipScene != null && ui.getCurrentGameScene() instanceof PlayScene3D) {
			pipSceneContainer.setVisible(true);
			hint.setVisible(false);
			var canvas = pipScene.getCanvas();
			var g = canvas.getGraphicsContext2D();
			g.setFill(Color.BLACK);
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			pipScene.doRender(g);
		} else {
			pipSceneContainer.setVisible(false);
			hint.setVisible(true);
		}
	}
}
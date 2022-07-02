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
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;

/**
 * Picture-In-Picture view.
 * 
 * @author Armin Reichert
 */
public class SectionPiP extends Section {

	private StackPane embeddedSceneContainer = new StackPane();
	private GameScene2D embeddedScene;

	public SectionPiP(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, gc, title, minLabelWidth, textColor, textFont, labelFont);
		content.add(embeddedSceneContainer, 0, 0, 2, 1);
	}

	@Override
	public void init() {
		embeddedScene = new PlayScene2D();
		var subScene = embeddedScene.getFXSubScene();
		embeddedSceneContainer.getChildren().setAll(subScene);
		subScene.setWidth(260.0);
		subScene.setHeight(subScene.getWidth() * 1.35);
		embeddedScene.getCanvas().widthProperty().bind(subScene.widthProperty());
		embeddedScene.getCanvas().heightProperty().bind(subScene.heightProperty());
		var scale = 1.2;
		embeddedScene.getCanvas().getTransforms().setAll(new Scale(scale, scale));
		embeddedScene.setSceneContext(ui.createSceneContext());
		embeddedScene.init();
	}

	@Override
	public void update() {
		if (embeddedScene != null) {
			embeddedScene.update();
		}
	}
}
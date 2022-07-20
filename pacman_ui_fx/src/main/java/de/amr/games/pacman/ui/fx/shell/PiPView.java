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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Picture-In-Picture view. Displays an embedded 2D game scene.
 * 
 * @author Armin Reichert
 */
public class PiPView {

	private static final Image PLACEHOLDER = Ufx.image("graphics/stoerung.jpg");

	public final DoubleProperty sceneHeightPy = new SimpleDoubleProperty();

	private StackPane root = new StackPane();
	private GameScene2D gameScene;

	public PiPView() {
		root.setBackground(Ufx.colorBackground(Color.BLACK));
	}

	public PiPView(GameScene2D embeddedGameScene) {
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		setEmbeddedGameScene(embeddedGameScene);
	}

	public void setEmbeddedGameScene(GameScene2D gameScene) {
		this.gameScene = gameScene;
		gameScene.resize(sceneHeightPy.doubleValue());
		gameScene.getFXSubScene().setFocusTraversable(false);
		root.getChildren().setAll(gameScene.getFXSubScene());
		sceneHeightPy.addListener((x, y, h) -> gameScene.resize(h.doubleValue()));
	}

	public StackPane getRoot() {
		return root;
	}

	public GameScene2D getGameScene() {
		return gameScene;
	}

	public void refresh(SceneManager sceneManager) {
		if (gameScene != null) {
			sceneManager.updateSceneContext(gameScene);
			gameScene.init();
		}
	}

	public void drawContent(boolean drawIt) {
		if (gameScene != null) {
			var g = gameScene.getGameSceneCanvas().getGraphicsContext2D();
			var width = g.getCanvas().getWidth();
			var height = g.getCanvas().getHeight();
			g.setFill(Color.BLACK);
			g.fillRect(0, 0, width, height);
			if (drawIt) {
				var hudFont = Font.font("Monospaced", FontWeight.BOLD, Math.floor(10.0 * gameScene.getScaling()));
				gameScene.drawHUD(g, hudFont);
				gameScene.drawSceneContent(g);
			} else {
				g.drawImage(PLACEHOLDER, 0, 0, 224, 288);
			}
		}
	}
}
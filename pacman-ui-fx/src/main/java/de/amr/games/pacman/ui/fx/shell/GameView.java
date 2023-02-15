/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.Map;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class GameView {

	private static final Image APP_ICON_PACMAN = ResourceMgr.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = ResourceMgr.image("icons/mspacman.png");

	public static final double PIP_VIEW_MIN_HEIGHT = ArcadeWorld.SIZE_PX.y();
	public static final double PIP_VIEW_MAX_HEIGHT = ArcadeWorld.SIZE_PX.y() * 2;

	private final Stage stage;
	private final Scene scene;
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	/**
	 * Embedded 2D-view of the current play scene. Activated/deactivated by pressing key F2. Size and transparency can be
	 * controlled using the dashboard.
	 */
	private final PlayScene2D pipPlayScene = new PlayScene2D();
	private final Map<GameVariant, Rendering2D> rendererMap;

	public GameView(Stage stage, int width, int height, float zoom, boolean fullscreen,
			Map<GameVariant, Rendering2D> rendererMap) {
		if (width <= 0) {
			throw new IllegalArgumentException("Layout width must be positive but is: %d".formatted(width));
		}
		if (height <= 0) {
			throw new IllegalArgumentException("Layout height must be positive but is: %d".formatted(height));
		}
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive but is: %.2f".formatted(zoom));
		}

		this.rendererMap = rendererMap;

		this.stage = stage;
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setFullScreen(fullscreen);

		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(pipPlayScene.fxSubScene());
		var placeHolder = new Pane(); /* placeholder for current game scene */
		var root = new StackPane(placeHolder, flashMessageView, overlayPane);
		scene = new Scene(root, width * zoom, height * zoom);
		stage.setScene(scene);
	}

	public void show() {
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
	}

	public void update(GameVariant variant) {
		var paused = Env.Simulation.pausedPy.get() ? " (paused)" : "";
		switch (variant) {
		case MS_PACMAN -> {
			stage.setTitle("Ms. Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			stage.setTitle("Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalStateException();
		}
		var bgColor = Env.ThreeD.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		var sceneRoot = (Region) scene.getRoot();
		sceneRoot.setBackground(ResourceMgr.colorBackground(bgColor));
	}

	public void updatePipView(GameScene currentGameScene) {
		boolean visible = Env.PiP.visiblePy.get() && GameSceneManager.isPlayScene(currentGameScene);
		pipPlayScene().fxSubScene().setVisible(visible);
		if (visible) {
			pipPlayScene().setContext(currentGameScene.context());
			pipPlayScene().draw();
		}
	}

	public void embedGameScene(GameScene gameScene) {
		StackPane root = (StackPane) scene.getRoot();
		root.getChildren().set(0, gameScene.fxSubScene());
		gameScene.onEmbed(scene);
	}

	public Rendering2D renderer(GameVariant variant) {
		return rendererMap.get(variant);
	}

	public Stage stage() {
		return stage;
	}

	public Scene scene() {
		return scene;
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public PlayScene2D pipPlayScene() {
		return pipPlayScene;
	}
}
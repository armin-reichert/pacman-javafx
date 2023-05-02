/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.v3d.app;

import java.util.List;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameUI;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The play scene is available in 2D and 3D. All others scenes are 2D only.
 * 
 * <p>
 * TODO still more refactoring necessary
 * 
 * @author Armin Reichert
 */
public class GameUI3d extends GameUI {

	public static final float PIP_MIN_HEIGHT = TILES_Y * 8;
	public static final float PIP_MAX_HEIGHT = 2.5f * PIP_MIN_HEIGHT;

	private PlayScene2D pipGameScene;
	private Dashboard dashboard;

	public GameUI3d(final Stage stage, final Settings settings, GameController gameController,
			List<GameSceneChoice> msPacManScenes, List<GameSceneChoice> pacManScenes) {
		super(stage, settings, gameController, msPacManScenes, pacManScenes);
	}

	@Override
	public void doRender() {
		flashMessageView.update();
		currentGameScene.render();
		dashboard.update();
		pipGameScene.render();
	}

	@Override
	protected void createComponents() {
		super.createComponents();
		pipGameScene = new PlayScene2D(gameController);
		dashboard = new Dashboard();
		dashboard.populate(this);
	}

	@Override
	protected Scene createMainScene(float sizeX, float sizeY) {
		var scene = new Scene(root, sizeX, sizeY);
		scene.heightProperty().addListener((py, ov, nv) -> currentGameScene.onParentSceneResize(scene));
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToOptimalSize();
			}
		});
		var topLayer = new BorderPane();
		topLayer.setLeft(dashboard);
		topLayer.setRight(pipGameScene.fxSubScene());
		root.getChildren().add(new Label("Game scene comes here"));
		root.getChildren().add(flashMessageView);
		root.getChildren().add(topLayer);

		return scene;
	}

	@Override
	protected void updateMainView() {
		if (currentGameScene != null && currentGameScene.is3D()) {
			if (Env3d.d3_drawModePy.get() == DrawMode.LINE) {
				root.setBackground(AppRes.Manager.colorBackground(Color.BLACK));// TODO
			} else {
				root.setBackground(AppRes3d.Textures.backgroundForScene3D);
			}
		} else {
			root.setBackground(AppRes.Manager.colorBackground(Env.mainSceneBgColorPy.get()));// TODO
		}
		var paused = Env.simulationPausedPy.get();
		var dimensionMsg = AppRes.Texts.message(Env3d.d3_enabledPy.get() ? "threeD" : "twoD"); // TODO
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(AppRes.Texts.message(messageKey, dimensionMsg));// TODO
			stage.getIcons().setAll(AppRes.Graphics.MsPacManGame.icon);
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(AppRes.Texts.message(messageKey, dimensionMsg));// TOOD
			stage.getIcons().setAll(AppRes.Graphics.PacManGame.icon);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	/**
	 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
	 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
	 */
	private void updatePictureInPictureView() {
		boolean visible = Env3d.pipVisiblePy.get() && isPlayScene(currentGameScene);
		pipGameScene.fxSubScene().setVisible(visible);
		pipGameScene.context().setCreditVisible(false);
		pipGameScene.context().setScoreVisible(true);
		pipGameScene.context().setRendering2D(currentGameScene.context().rendering2D());
	}

	private boolean isPlayScene(GameScene gameScene) {
		return gameScene == scenes.get(GameVariant.PACMAN).get(INDEX_PLAY_SCENE).scene2D()
				|| gameScene == scenes.get(GameVariant.PACMAN).get(INDEX_PLAY_SCENE).scene3D()
				|| gameScene == scenes.get(GameVariant.MS_PACMAN).get(INDEX_PLAY_SCENE).scene2D()
				|| gameScene == scenes.get(GameVariant.MS_PACMAN).get(INDEX_PLAY_SCENE).scene3D();
	}

	@Override
	protected void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainView());

		dashboard.visibleProperty().bind(Env3d.dashboardVisiblePy);
		Env3d.pipVisiblePy.addListener((py, oldVal, newVal) -> updatePictureInPictureView());
		Env3d.pipSceneHeightPy.addListener((py, oldVal, newVal) -> pipGameScene.resize(newVal.doubleValue()));
		pipGameScene.fxSubScene().opacityProperty().bind(Env3d.pipOpacityPy);

		Env3d.d3_drawModePy.addListener((py, oldVal, newVal) -> updateMainView());
		Env3d.d3_enabledPy.addListener((py, oldVal, newVal) -> updateMainView());
		Env3d.d3_enabledPy.set(true);
		Env3d.d3_perspectivePy.set(Perspective.NEAR_PLAYER);
	}

	@Override
	public void updateGameScene(boolean reload) {
		var matching = sceneSelectionMatchingCurrentGameState();
		var use3D = Env3d.d3_enabledPy.get();
		var nextGameScene = (use3D && matching.scene3D() != null) ? matching.scene3D() : matching.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updatePictureInPictureView(); // TODO
		updateMainView();
	}

	@Override
	protected void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.USE_3D)) {
			toggleUse3DScene();
		} else if (Keyboard.pressed(Keys.DASHBOARD) || Keyboard.pressed(Keys.DASHBOARD2)) {
			Actions3d.toggleDashboardVisible();
		} else if (Keyboard.pressed(Keys.PIP_VIEW)) {
			Actions3d.togglePipViewVisible();
		}
		super.handleKeyboardInput();
	}

	public void toggleUse3DScene() {
		Ufx.toggle(Env3d.d3_enabledPy);
		if (findGameScene(3).isPresent()) {
			updateGameScene(true);
			currentGameScene().onSceneVariantSwitch();
		} else {
			Actions.showFlashMessage(AppRes.Texts.message(Env3d.d3_enabledPy.get() ? "use_3D_scene" : "use_2D_scene"));// TODO
		}
	}

	public Dashboard dashboard() {
		return dashboard;
	}
}
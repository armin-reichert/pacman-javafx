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

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.GameUI2d;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a {@link PlayScene2D 2D} and a {@link PlayScene3D 3D} version. All
 * others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class GameUI3d extends GameUI2d {

	public static final float PIP_MIN_HEIGHT = TS * TILES_Y;
	public static final float PIP_MAX_HEIGHT = 2.5f * PIP_MIN_HEIGHT;

	private PlayScene2D pipGameScene;
	private Dashboard dashboard;

	public GameUI3d(Stage stage, Settings settings) {
		super(stage, settings);
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	@Override
	public void doRender() {
		flashMessageView.update();
		currentGameScene.render();
		dashboard.update();
		pipGameScene.render();
	}

	@Override
	protected void createPacManSceneChoices() {
		super.createPacManSceneChoices();
		var choices = sceneChoicesMap.get(GameVariant.PACMAN);
		var playScene2D = choices.get(INDEX_PLAY_SCENE).scene2D();
		choices.set(INDEX_PLAY_SCENE, new GameSceneChoice(playScene2D, new PlayScene3D(gameController)));
	}

	@Override
	protected void createMsPacManSceneChoices() {
		super.createMsPacManSceneChoices();
		var choices = sceneChoicesMap.get(GameVariant.MS_PACMAN);
		var playScene2D = choices.get(INDEX_PLAY_SCENE).scene2D();
		choices.set(INDEX_PLAY_SCENE, new GameSceneChoice(playScene2D, new PlayScene3D(gameController)));
	}

	@Override
	protected void createMainSceneLayout() {
		pipGameScene = new PlayScene2D(gameController);
		dashboard = new Dashboard(this);
		var dashboardLayer = new BorderPane();
		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pipGameScene.fxSubScene());
		mainSceneRoot.getChildren().add(new Label("Game scene comes here"));
		mainSceneRoot.getChildren().add(flashMessageView);
		mainSceneRoot.getChildren().add(dashboardLayer);
	}

	@Override
	public void updateContextSensitiveHelp() {
	}

	@Override
	protected void updateStage() {
		updatePictureInPictureView();
		if (currentGameScene != null && currentGameScene.is3D()) {
			if (Game3d.d3_drawModePy.get() == DrawMode.LINE) {
				mainSceneRoot.setBackground(ResourceManager.colorBackground(Color.BLACK));
			} else {
				mainSceneRoot.setBackground(Game3d.resources.backgroundForScene3D);
			}
		} else {
			mainSceneRoot.setBackground(Game2d.RES.imageBackground("graphics/pacman_wallpaper_gray.png"));
		}
		var paused = pausedPy.get();
		var dimensionMsg = fmtMessage(Game3d.resources.messages, Game3d.d3_enabledPy.get() ? "threeD" : "twoD"); // TODO
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(fmtMessage(Game3d.resources.messages, messageKey, dimensionMsg));
			stage.getIcons().setAll(Game2d.resources.graphics.msPacMan().icon);
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(fmtMessage(Game3d.resources.messages, messageKey, dimensionMsg));
			stage.getIcons().setAll(Game2d.resources.graphics.pacMan().icon);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	@Override
	protected void initProperties(Settings settings) {
		super.initProperties(settings);

		dashboard.visibleProperty().bind(Game3d.dashboardVisiblePy);
		Game3d.pipVisiblePy.addListener((py, oldVal, newVal) -> updatePictureInPictureView());
		Game3d.pipHeightPy.addListener((py, oldVal, newVal) -> pipGameScene.resize(newVal.doubleValue()));
		pipGameScene.fxSubScene().opacityProperty().bind(Game3d.pipOpacityPy);

		Game3d.d3_drawModePy.addListener((py, oldVal, newVal) -> updateStage());
		Game3d.d3_enabledPy.addListener((py, oldVal, newVal) -> updateStage());
		Game3d.d3_enabledPy.set(true);
		Game3d.d3_perspectivePy.set(Perspective.NEAR_PLAYER);
	}

	@Override
	protected GameScene chooseGameScene(GameSceneChoice choice) {
		var use3D = Game3d.d3_enabledPy.get();
		return (use3D && choice.scene3D() != null) ? choice.scene3D() : choice.scene2D();
	}

	@Override
	protected void handleKeyboardInput() {
		super.handleKeyboardInput();
		if (Keyboard.pressed(Game3d.Keys.TOGGLE_2D_3D)) {
			toggleUse3DScene();
		} else if (Keyboard.pressed(Game3d.Keys.DASHBOARD) || Keyboard.pressed(Game3d.Keys.DASHBOARD2)) {
			Game3d.actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Game3d.Keys.PIP_VIEW)) {
			Game3d.actions.togglePipVisibility();
		}
	}

	public void toggleUse3DScene() {
		Ufx.toggle(Game3d.d3_enabledPy);
		if (findGameScene(3).isPresent()) {
			updateGameScene(true);
			currentGameScene().onSceneVariantSwitch();
		} else {
			var message = fmtMessage(Game3d.resources.messages, Game3d.d3_enabledPy.get() ? "use_3D_scene" : "use_2D_scene");
			Game2d.actions.showFlashMessage(message);
		}
	}

	private void updatePictureInPictureView() {
		boolean visible = Game3d.pipVisiblePy.get() && isPlayScene(currentGameScene);
		pipGameScene.fxSubScene().setVisible(visible);
		pipGameScene.context().setCreditVisible(false);
		pipGameScene.context().setScoreVisible(true);
		pipGameScene.context().setRendering2D(currentGameScene.context().rendering2D());
	}

	private boolean isPlayScene(GameScene gameScene) {
		return sceneChoicesMap.get(GameVariant.PACMAN).get(INDEX_PLAY_SCENE).includes(gameScene)
				|| sceneChoicesMap.get(GameVariant.MS_PACMAN).get(INDEX_PLAY_SCENE).includes(gameScene);
	}
}
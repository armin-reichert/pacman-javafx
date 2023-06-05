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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a 2D and a 3D version. All others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI {

	private PictureInPicture pip;
	private Dashboard dashboard;

	@Override
	public void onRender() {
		flashMessageView.update();
		currentGameScene.render();
		dashboard.update();
		pip.render();
	}

	@Override
	protected void configureGameScenes() {
		super.configureGameScenes();
		gameSceneConfigMsPacMan.setPlayScene3D(new PlayScene3D());
		gameSceneConfigPacMan.setPlayScene3D(new PlayScene3D());
	}

	@Override
	protected void createMainScene(Stage stage, Settings settings) {
		pip = new PictureInPicture();

		dashboard = new Dashboard(this);
		dashboard.setVisible(false);
		dashboard.init();

		var dashboardLayer = new BorderPane();
		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pip.root());

		mainSceneRoot = new StackPane();
		mainSceneRoot.getChildren().add(flashMessageView);
		mainSceneRoot.getChildren().add(dashboardLayer);

		var mainScene = new Scene(mainSceneRoot, settings.zoom * 28 * 8, settings.zoom * 36 * 8, Color.BLACK);
		stage.setScene(mainScene);
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene();
			}
		});

		addStartPage(settings.variant);
		updateStage();
	}

	@Override
	protected void configurePacSteering() {
		// Steering with unmodified or with CONTROL+cursor key
		keyboardPlayerSteering = new KeyboardSteering();
		keyboardPlayerSteering.define(Direction.UP, KeyCode.UP, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.DOWN, KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.LEFT, KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);

		gameController.setManualPacSteering(keyboardPlayerSteering);
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyboardPlayerSteering);
	}

	@Override
	protected void configureBindings(Settings settings) {
		super.configureBindings(settings);

		pip.opacityPy.bind(PacManGames3d.PY_PIP_OPACITY);
		pip.heightPy.bind(PacManGames3d.PY_PIP_HEIGHT);

		PacManGames3d.PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PacManGames3d.PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
		PacManGames3d.PY_3D_PERSPECTIVE.set(Perspective.NEAR_PLAYER);
	}

	@Override
	protected void updateStage() {
		pip.update(currentGameScene, PacManGames3d.PY_PIP_ON.get());
		if (currentGameScene != null && currentGameScene.is3D()) {
			if (PacManGames3d.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				mainSceneRoot.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				mainSceneRoot.setBackground(theme.background("model3D.wallpaper"));
			}
		} else {
			mainSceneRoot.setBackground(theme.background("wallpaper.background"));
		}
		var paused = clock != null ? clock().isPaused() : false;
		var dimensionMsg = fmtMessage(PacManGames3d.TEXTS, PacManGames3d.PY_3D_ENABLED.get() ? "threeD" : "twoD"); // TODO
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, messageKey, dimensionMsg));
			stage.getIcons().setAll(theme.image("mspacman.icon"));
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, messageKey, dimensionMsg));
			stage.getIcons().setAll(theme.image("pacman.icon"));
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var scene = super.sceneMatchingCurrentGameState();
		var config = gameVariant() == GameVariant.MS_PACMAN ? gameSceneConfigMsPacMan : gameSceneConfigPacMan;
		if (PacManGames3d.PY_3D_ENABLED.get() && scene == config.playScene()) {
			scene = config.playScene3D();
		}
		return scene;
	}

	@Override
	protected void handleKeyboardInput() {
		super.handleKeyboardInput();
		if (Keyboard.pressed(PacManGames3d.KEY_TOGGLE_2D_3D)) {
			toggle2D3D();
		} else if (Keyboard.anyPressed(PacManGames3d.KEY_TOGGLE_DASHBOARD, PacManGames3d.KEY_TOGGLE_DASHBOARD_2)) {
			toggleDashboardVisible();
		} else if (Keyboard.pressed(PacManGames3d.KEY_TOGGLE_PIP_VIEW)) {
			togglePipOn();
		}
	}

	@Override
	public void showHelp() {
		if (!currentGameScene.is3D()) {
			super.showHelp();
		}
	}

	public void toggle2D3D() {
		Ufx.toggle(PacManGames3d.PY_3D_ENABLED);
		updateOrReloadGameScene(true);
		currentGameScene().onSceneVariantSwitch();
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	// --- Actions ---

	public void togglePipOn() {
		Ufx.toggle(PacManGames3d.PY_PIP_ON);
		pip.update(currentGameScene, PacManGames3d.PY_PIP_ON.get());
		var message = fmtMessage(PacManGames3d.TEXTS, PacManGames3d.PY_PIP_ON.get() ? "pip_on" : "pip_off");
		showFlashMessage(message);
	}

	public void toggleDashboardVisible() {
		dashboard().setVisible(!dashboard().isVisible());
	}

	public void selectNextPerspective() {
		var next = PacManGames3d.PY_3D_PERSPECTIVE.get().next();
		PacManGames3d.PY_3D_PERSPECTIVE.set(next);
		String perspectiveName = fmtMessage(PacManGames3d.TEXTS, next.name());
		showFlashMessage(fmtMessage(PacManGames3d.TEXTS, "camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prev = PacManGames3d.PY_3D_PERSPECTIVE.get().prev();
		PacManGames3d.PY_3D_PERSPECTIVE.set(prev);
		String perspectiveName = fmtMessage(PacManGames3d.TEXTS, prev.name());
		showFlashMessage(fmtMessage(PacManGames3d.TEXTS, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3d.PY_3D_DRAW_MODE
				.set(PacManGames3d.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}
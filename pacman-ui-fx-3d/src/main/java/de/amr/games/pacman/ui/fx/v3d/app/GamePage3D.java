/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.app.GamePage;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;

/**
 * @author Armin Reichert
 */
class GamePage3D extends GamePage {

	private final BorderPane dashboardLayer = new BorderPane();
	private final PictureInPicture pip;
	private final Dashboard dashboard;

	public GamePage3D(PacManGames3dUI ui, Theme theme) {
		super(ui, theme);

		pip = new PictureInPicture((PlayScene3D) ui.sceneConfig().playScene3D());
		pip.opacityPy.bind(PacManGames3dApp.PY_PIP_OPACITY);
		pip.heightPy.bind(PacManGames3dApp.PY_PIP_HEIGHT);

		dashboard = new Dashboard(ui);
		dashboard.setVisible(false);

		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pip.root());
	}

	public PictureInPicture getPip() {
		return pip;
	}

	@Override
	public void setGameScene(GameScene gameScene) {
		if (gameScene.is3D()) {
			layers.getChildren().set(GAME_SCENE_LAYER, gameScene.root());
			// Assume PlayScene3D is the only 3D scene
			layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
			layers.requestFocus();
			helpButton.setVisible(false);
			updateBackground(gameScene);
		} else {
			layers.getChildren().set(GAME_SCENE_LAYER, gameSceneLayer);
			super.setGameScene(gameScene);
		}
		updateTopLayer();
	}

	public void updateBackground(GameScene gameScene) {
		if (gameScene.is3D()) {
			if (PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				layers.setBackground(ui.theme().background("model3D.wallpaper"));
			}
		} else {
			gameSceneLayer.setBackground(ui.theme().background("wallpaper.background"));
		}
	}

	@Override
	public void render() {
		super.render();
		dashboard.update();
		pip.root().setVisible(isPiPOn() && ui.currentGameScene().is3D());
		pip.render();
	}

	@Override
	protected void handleKeyboardInput() {
		var ui3D = (PacManGames3dUI) ui;
		super.handleKeyboardInput();
		if (Keyboard.pressed(PacManGames3dApp.KEY_TOGGLE_2D_3D)) {
			ui3D.toggle2D3D();
		} else if (Keyboard.anyPressed(PacManGames3dApp.KEY_TOGGLE_DASHBOARD, PacManGames3dApp.KEY_TOGGLE_DASHBOARD_2)) {
			toggleDashboardVisible();
		} else if (Keyboard.pressed(PacManGames3dApp.KEY_TOGGLE_PIP_VIEW)) {
			togglePipVisible();
		}
	}

	/**
	 * @return if the picture-in-picture view is enabled, it might be invisible nevertheless!
	 */
	private boolean isPiPOn() {
		return PacManGames3dApp.PY_PIP_ON.get();
	}

	private void togglePipVisible() {
		Ufx.toggle(PacManGames3dApp.PY_PIP_ON);
		var message = message(PacManGames3dApp.TEXTS, isPiPOn() ? "pip_on" : "pip_off");
		ui.showFlashMessage(message);
		updateTopLayer();
	}

	private void toggleDashboardVisible() {
		dashboard.setVisible(!dashboard.isVisible());
		updateTopLayer();
	}

	private void updateTopLayer() {
		layers.getChildren().remove(dashboardLayer);
		if (dashboard.isVisible() || isPiPOn()) {
			layers.getChildren().add(dashboardLayer);
		}
		layers.requestFocus();
	}

	protected void updateHelpButton() {
		if (ui.currentGameScene() != null && ui.currentGameScene().is3D()) {
			helpButton.setVisible(false);
		} else {
			super.updateHelpButton();
		}
	}

}
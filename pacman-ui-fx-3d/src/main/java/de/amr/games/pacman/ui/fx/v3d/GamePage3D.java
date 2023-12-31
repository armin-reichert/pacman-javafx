/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.GamePage;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.time.LocalTime;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.message;


/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

	private final BorderPane topLayer; // contains dashboard and picture-in-picture view
	private final PictureInPicture pip;
	private final Dashboard dashboard;
	private final GamePageContextMenu contextMenu;

	public GamePage3D(PacManGames3dUI ui, Theme theme) {
		super(ui, theme);

		pip = new PictureInPicture();
		pip.opacityPy.bind(PacManGames3dApp.PY_PIP_OPACITY);
		pip.heightPy.bind(PacManGames3dApp.PY_PIP_HEIGHT);
		ui.gameScenePy.addListener((obj, ov, newGameScene) -> {
			if (newGameScene == ui.sceneConfig().get("play3D")) {
				pip.setGameSceneContext(newGameScene.context());
			}
		});

		dashboard = new Dashboard(ui);
		dashboard.setVisible(false);

		topLayer = new BorderPane();
		topLayer.setLeft(dashboard);
		topLayer.setRight(pip.root());

		contextMenu = new GamePageContextMenu();
	}

	@Override
	public PacManGames3dUI ui() {
		return (PacManGames3dUI) ui;
	}

	public void openContextMenu(Node node, double x, double y) {
		contextMenu.hide();
		ui().currentGameScene().ifPresent(gameScene -> contextMenu.rebuild(this, gameScene));
		contextMenu.show(node, x, y);
	}

	public void closeContextMenu() {
		contextMenu.hide();
	}

	@Override
	protected void onGameSceneChanged(GameScene newGameScene) {
		//TODO this code is too difficult to understand, simplify
		if (isCurrentGameScene3D()) {
			if (newGameScene == ui().sceneConfig().get("play3D")) {
				// Note: event handler is removed again in super.onGameSceneChanged() call
				layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
			}
			layers.getChildren().set(GAME_SCENE_LAYER, newGameScene.root());
			layers.requestFocus();
			helpButton.setVisible(false);
		} else {
			layers.getChildren().set(GAME_SCENE_LAYER, gameSceneLayer);
			super.onGameSceneChanged(newGameScene);
		}
		closeContextMenu();
		updateBackground();
		updateTopLayer();
	}

	public void updateBackground() {
		if (isCurrentGameScene3D()) {
			if (PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				var hour = LocalTime.now().getHour();
				var isNight = hour >= 20 || hour <= 5;
				var wallpaper = isNight? "model3D.wallpaper.night" : "model3D.wallpaper";
				layers.setBackground(ui().theme().background(wallpaper));
			}
		} else {
			gameSceneLayer.setBackground(ui().theme().background("wallpaper.background"));
		}
	}

	@Override
	public void render() {
		super.render();
		contextMenu.updateState(this);
		dashboard.update();
		pip.root().setVisible(isPictureInPictureActive() && isCurrentGameScene3D());
		pip.render();
	}

	@Override
	protected void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames3dApp.KEY_TOGGLE_2D_3D)) {
			ui().toggle2D3D();
		} else if (Keyboard.anyPressed(PacManGames3dApp.KEYS_TOGGLE_DASHBOARD)) {
			toggleDashboardVisible();
		} else if (Keyboard.pressed(PacManGames3dApp.KEY_TOGGLE_PIP_VIEW)) {
			togglePipVisible();
		} else {
			super.handleKeyboardInput();
		}
	}

	/**
	 * @return if the picture-in-picture view is activated (can be invisible nevertheless!)
	 */
	public boolean isPictureInPictureActive() {
		return PacManGames3dApp.PY_PIP_ON.get();
	}

	private boolean isCurrentGameScene3D() {
		return ui().currentGameScene().isPresent() && ui().currentGameScene().get().is3D();
	}

	public void togglePipVisible() {
		Ufx.toggle(PacManGames3dApp.PY_PIP_ON);
		var message = message(isPictureInPictureActive() ? "pip_on" : "pip_off");
		ui().showFlashMessage(message);
		updateTopLayer();
	}

	public void toggleDashboardVisible() {
		dashboard.setVisible(!dashboard.isVisible());
		updateTopLayer();
	}

	private void updateTopLayer() {
		layers.getChildren().remove(topLayer);
		if (dashboard.isVisible() || isPictureInPictureActive()) {
			layers.getChildren().add(topLayer);
		}
		layers.requestFocus();
	}

	protected void updateHelpButton() {
		if (isCurrentGameScene3D()) {
			helpButton.setVisible(false);
		} else {
			super.updateHelpButton();
		}
	}
}
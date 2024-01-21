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
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.time.LocalTime;

import static de.amr.games.pacman.ui.fx.PacManGames2dApp.PY_SHOW_DEBUG_INFO;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

	private final BorderPane topLayer; // contains dashboard and picture-in-picture view
	private final PictureInPicture pip;
	private final Dashboard dashboard;
	private final GamePageContextMenu contextMenu;

	public GamePage3D(GameSceneContext sceneContext, ActionHandler3D actionHandler) {
		super(sceneContext, actionHandler);

		pip = new PictureInPicture();
		pip.gameScene().setContext(sceneContext);
		pip.opacityPy.bind(PY_PIP_OPACITY);
		pip.heightPy.bind(PY_PIP_HEIGHT);
		PY_PIP_ON.addListener((py, ov, nv) -> updateTopLayer());

		dashboard = new Dashboard(sceneContext.theme());
		dashboard.setVisible(false);

		contextMenu = new GamePageContextMenu();

		topLayer = new BorderPane();
		topLayer.setLeft(dashboard);
		topLayer.setRight(pip.root());
	}

	public GamePageContextMenu contextMenu() {
		return contextMenu;
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	@Override
	public void onGameSceneChanged(GameScene newGameScene) {
		//TODO this code is too difficult to understand, simplify
		if (isCurrentGameScene3D()) {
			if (newGameScene == sceneContext.sceneConfig().get("play3D")) {
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
		contextMenu.hide();
		updateBackground();
		updateTopLayer();
		updateDebugBorders();
	}

	public void updateBackground() {
		if (isCurrentGameScene3D()) {
			if (PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				var hour = LocalTime.now().getHour();
				var isNight = hour >= 20 || hour <= 5;
				var wallpaper = isNight? "model3D.wallpaper.night" : "model3D.wallpaper";
				layers.setBackground(sceneContext.theme().background(wallpaper));
			}
		} else {
			gameSceneLayer.setBackground(sceneContext.theme().background("wallpaper.background"));
		}
	}

	@Override
	public void render() {
		super.render();
		contextMenu.updateState();
		dashboard.update();
		pip.root().setVisible(PY_PIP_ON.get() && isCurrentGameScene3D());
		pip.render();
	}

	@Override
	protected void handleKeyboardInput() {
		var actionHandler3D = (ActionHandler3D) actionHandler;
		if (Keyboard.pressed(KEY_TOGGLE_2D_3D)) {
			actionHandler3D.toggle2D3D();
		} else if (Keyboard.pressed(KEYS_TOGGLE_DASHBOARD)) {
			toggleDashboardVisible();
		} else if (Keyboard.pressed(KEY_TOGGLE_PIP_VIEW)) {
			actionHandler3D.togglePipVisible();
		} else {
			super.handleKeyboardInput();
		}
	}

	@Override
	protected void updateDebugBorders()  {
		if (PY_SHOW_DEBUG_INFO.get() && !isCurrentGameScene3D()) {
			layers.setBorder(ResourceManager.border(Color.RED, 3));
			gameSceneLayer.setBorder(ResourceManager.border(Color.YELLOW, 3));
			popupLayer.setBorder(ResourceManager.border(Color.GREENYELLOW, 3));
		} else {
			layers.setBorder(null);
			gameSceneLayer.setBorder(null);
			popupLayer.setBorder(null);
		}
	}


	private boolean isCurrentGameScene3D() {
		return sceneContext.currentGameScene().isPresent() && sceneContext.currentGameScene().get() instanceof PlayScene3D;
	}

	public void toggleDashboardVisible() {
		dashboard.setVisible(!dashboard.isVisible());
		updateTopLayer();
	}

	public void updateTopLayer() {
		layers.getChildren().remove(topLayer);
		if (dashboard.isVisible() || PY_PIP_ON.get()) {
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
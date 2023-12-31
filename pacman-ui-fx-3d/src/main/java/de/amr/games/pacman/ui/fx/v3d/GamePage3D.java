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
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

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

	private class GamePageContextMenu extends ContextMenu {
		private CheckMenuItem autopilotItem;
		private CheckMenuItem immunityItem;
		private CheckMenuItem pipItem;
		private ToggleGroup perspectivesToggleGroup;

		public void rebuild(GameScene gameScene) {
			getItems().clear();
			getItems().add(createTitleItem(message("scene_display")));
			if (gameScene instanceof PlayScene2D) {
				var item = new MenuItem(message("use_3D_scene"));
				item.setOnAction(e -> ui().toggle2D3D());
				getItems().add(item);
			}
			else if (gameScene instanceof PlayScene3D) {
				var item = new MenuItem(message("use_2D_scene"));
				item.setOnAction(e -> ui().toggle2D3D());
				getItems().add(item);
				pipItem = new CheckMenuItem(message("pip"));
				pipItem.setOnAction(e -> togglePipVisible());
				getItems().add(pipItem);
				getItems().add(createTitleItem(message("select_perspective")));
				perspectivesToggleGroup = new ToggleGroup();
				for (var p : Perspective.values()) {
					var rmi = new RadioMenuItem(message(p.name()));
					rmi.setUserData(p);
					rmi.setToggleGroup(perspectivesToggleGroup);
					getItems().add(rmi);
				}
				perspectivesToggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
					if (nv != null) {
						// Note: These are the used data set for the radio menu item!
						PacManGames3dApp.PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
					}
				});
			}
			getItems().add(createTitleItem(message("pacman")));
			autopilotItem = new CheckMenuItem(message("autopilot"));
			autopilotItem.setOnAction(e -> ui.toggleAutopilot());
			getItems().add(autopilotItem);
			immunityItem = new CheckMenuItem(message("immunity"));
			immunityItem.setOnAction(e -> ui.toggleImmunity());
			getItems().add(immunityItem);
		}

		private MenuItem createTitleItem(String title) {
			var text = new Text(title);
			text.setFont(Font.font("Sans", FontWeight.BOLD, 14));
			return new CustomMenuItem(text);
		}

		public void updateState() {
			if (perspectivesToggleGroup != null) {
				for (var toggle : perspectivesToggleGroup.getToggles()) {
					toggle.setSelected(PacManGames3dApp.PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
				}
			}
			if (pipItem != null) {
				pipItem.setSelected(isPictureInPictureActive());
			}
			if (autopilotItem != null) {
				autopilotItem.setSelected(GameController.it().isAutoControlled());
			}
			if (immunityItem != null) {
				immunityItem.setSelected(GameController.it().isImmune());
			}
		}
	}

	public GamePage3D(PacManGames3dUI ui, Theme theme) {
		super(ui, theme);

		pip = new PictureInPicture();
		pip.opacityPy.bind(PacManGames3dApp.PY_PIP_OPACITY);
		pip.heightPy.bind(PacManGames3dApp.PY_PIP_HEIGHT);

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
		ui().currentGameScene().ifPresent(gameScene -> contextMenu.rebuild(gameScene));
		contextMenu.show(node, x, y);
	}

	public void closeContextMenu() {
		contextMenu.hide();
	}

	public PictureInPicture pip() {
		return pip;
	}

	@Override
	public void onGameSceneChanged() {
		//TODO this code is too difficult to understand, simplify
		if (isCurrentGameScene3D()) {
			var gameScene3D = ui().currentGameScene().get();
			if (gameScene3D == ui().sceneConfig().get("play3D")) {
				// Note: event handler is removed again in super.onGameSceneChanged() call
				layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
			}
			layers.getChildren().set(GAME_SCENE_LAYER, gameScene3D.root());
			layers.requestFocus();
			helpButton.setVisible(false);
		} else {
			layers.getChildren().set(GAME_SCENE_LAYER, gameSceneLayer);
			super.onGameSceneChanged();
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
		contextMenu.updateState();
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
	private boolean isPictureInPictureActive() {
		return PacManGames3dApp.PY_PIP_ON.get();
	}

	private boolean isCurrentGameScene3D() {
		return ui().currentGameScene().isPresent() && ui().currentGameScene().get().is3D();
	}

	private void togglePipVisible() {
		Ufx.toggle(PacManGames3dApp.PY_PIP_ON);
		var message = message(isPictureInPictureActive() ? "pip_on" : "pip_off");
		ui().showFlashMessage(message);
		updateTopLayer();
	}

	private void toggleDashboardVisible() {
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
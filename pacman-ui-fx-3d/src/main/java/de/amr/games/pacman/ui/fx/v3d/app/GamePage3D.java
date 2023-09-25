/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.GamePage;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

	private final BorderPane dashboardLayer = new BorderPane();
	private final PictureInPicture pip;
	private final Dashboard dashboard;

	private GamePageContextMenu contextMenu;

	private class GamePageContextMenu extends ContextMenu {
		private final CheckMenuItem autopilotItem;
		private final CheckMenuItem immunityItem;
		private CheckMenuItem pipItem;
		private final ToggleGroup perspectivesToggleGroup = new ToggleGroup();

		public GamePageContextMenu() {
			ui.currentScene().ifPresent(gameScene -> {
				getItems().add(createTitleItem(tt("scene_display")));
				if (gameScene instanceof PlayScene2D) {
					var item = new MenuItem(tt("use_3D_scene"));
					item.setOnAction(e -> ui().toggle2D3D());
					getItems().add(item);
				}
				else if (gameScene instanceof PlayScene3D) {
					var item = new MenuItem(tt("use_2D_scene"));
					item.setOnAction(e -> ui().toggle2D3D());
					getItems().add(item);
					pipItem = new CheckMenuItem(tt("pip"));
					pipItem.setOnAction(e -> togglePipVisible());
					getItems().add(pipItem);
					getItems().add(createTitleItem(tt("select_perspective")));
					for (var p : Perspective.values()) {
						var rmi = new RadioMenuItem(message(PacManGames3dApp.TEXTS, p.name()));
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
			});
			getItems().add(createTitleItem(tt("pacman")));
			autopilotItem = new CheckMenuItem(tt("autopilot"));
			autopilotItem.setOnAction(e -> ui.toggleAutopilot());
			getItems().add(autopilotItem);
			immunityItem = new CheckMenuItem(tt("immunity"));
			immunityItem.setOnAction(e -> ui.toggleImmunity());
			getItems().add(immunityItem);
		}

		private String tt(String key) {
			return message(PacManGames3dApp.TEXTS, key);
		}

		private MenuItem createTitleItem(String title) {
			var text = new Text(title);
			text.setFont(Font.font("Sans", FontWeight.BOLD, 14));
			return new CustomMenuItem(text);
		}

		public void updateState() {
			//TODO should be done via binding or something:
			for (var toggle : perspectivesToggleGroup.getToggles()) {
				toggle.setSelected(PacManGames3dApp.PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
			}
			if (pipItem != null) {
				pipItem.setSelected(isPictureInPictureActive());
			}
			autopilotItem.setSelected(GameController.it().isAutoControlled());
			immunityItem.setSelected(GameController.it().isImmune());
		}
	}

	public GamePage3D(PacManGames3dUI ui, Theme theme) {
		super(ui, theme);

		pip = new PictureInPicture();
		pip.opacityPy.bind(PacManGames3dApp.PY_PIP_OPACITY);
		pip.heightPy.bind(PacManGames3dApp.PY_PIP_HEIGHT);

		dashboard = new Dashboard(ui);
		dashboard.setVisible(false);

		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pip.root());

		contextMenu = new GamePageContextMenu();
	}

	@Override
	public PacManGames3dUI ui() {
		return (PacManGames3dUI) ui;
	}

	public void openContextMenu(Node node, double x, double y) {
		contextMenu.hide();
		contextMenu = new GamePageContextMenu();
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
		var gameScene = ui.currentScene().get();
		if (gameScene.is3D()) {
			layers.getChildren().set(GAME_SCENE_LAYER, gameScene.root());
			// Assume PlayScene3D is the only 3D scene
			layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
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
		ui.currentScene().ifPresent(gameScene -> {
			if (gameScene.is3D()) {
				if (PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
					layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
				} else {
					layers.setBackground(ui().theme().background("model3D.wallpaper"));
				}
			} else {
				gameSceneLayer.setBackground(ui().theme().background("wallpaper.background"));
			}
		});
	}

	@Override
	public void render() {
		super.render();
		contextMenu.updateState();
		dashboard.update();
		var pipVisible = isPictureInPictureActive() && ui().currentScene().isPresent() && ui().currentScene().get().is3D();
		pip.root().setVisible(pipVisible);
		pip.render();
	}

	@Override
	protected void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames3dApp.KEY_TOGGLE_2D_3D)) {
			ui().toggle2D3D();
		} else if (Keyboard.anyPressed(PacManGames3dApp.KEY_TOGGLE_DASHBOARD, PacManGames3dApp.KEY_TOGGLE_DASHBOARD_2)) {
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

	private void togglePipVisible() {
		Ufx.toggle(PacManGames3dApp.PY_PIP_ON);
		var message = message(PacManGames3dApp.TEXTS, isPictureInPictureActive() ? "pip_on" : "pip_off");
		ui().showFlashMessage(message);
		updateTopLayer();
	}

	private void toggleDashboardVisible() {
		dashboard.setVisible(!dashboard.isVisible());
		updateTopLayer();
	}

	private void updateTopLayer() {
		layers.getChildren().remove(dashboardLayer);
		if (dashboard.isVisible() || isPictureInPictureActive()) {
			layers.getChildren().add(dashboardLayer);
		}
		layers.requestFocus();
	}

	protected void updateHelpButton() {
		if (ui().currentScene().isPresent() && ui().currentScene().get().is3D()) {
			helpButton.setVisible(false);
		} else {
			super.updateHelpButton();
		}
	}
}
/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.GamePage;
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
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

	private class GamePageContextMenu extends ContextMenu {
		private CheckMenuItem autopilotItem;
		private CheckMenuItem immunityItem;
		private CheckMenuItem pipItem;
		private ToggleGroup   perspectiveMenuToggleGroup = new ToggleGroup();

		public GamePageContextMenu() {
			createSceneDisplayEntries();
			createPacManMenuEntries();
		}

		private void createSceneDisplayEntries() {
			MenuItem item = createTitleItem(message(PacManGames3dApp.TEXTS,"scene_display"));
			getItems().add(item);
			if (ui.currentGameScene() instanceof PlayScene2D) {
				item = new MenuItem(message(PacManGames3dApp.TEXTS,"use_3D_scene"));
				item.setOnAction(e -> ((PacManGames3dUI) ui).toggle2D3D());
				getItems().add(item);
			}
			else if (ui.currentGameScene() instanceof PlayScene3D) {
				item = new MenuItem(message(PacManGames3dApp.TEXTS,"use_2D_scene"));
				item.setOnAction(e -> ((PacManGames3dUI) ui).toggle2D3D());
				getItems().add(item);
				pipItem = new CheckMenuItem(message(PacManGames3dApp.TEXTS,"pip"));
				pipItem.setOnAction(e -> togglePipVisible());
				getItems().add(pipItem);
				createPerspectiveMenuEntries();
			}
		}

		private void createPerspectiveMenuEntries() {
			getItems().add(createTitleItem(message(PacManGames3dApp.TEXTS,"select_perspective")));
			for (var p : Perspective.values()) {
				var item = new RadioMenuItem(message(PacManGames3dApp.TEXTS, p.name()));
				item.setUserData(p);
				item.setToggleGroup(perspectiveMenuToggleGroup);
				getItems().add(item);
			}
			perspectiveMenuToggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
				if (nv != null) {
					PacManGames3dApp.PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
				}
			});
		}

		private void createPacManMenuEntries() {
			getItems().add(createTitleItem("Pac-Man"));
			autopilotItem = new CheckMenuItem("Autopilot");
			autopilotItem.setOnAction(e -> ui.toggleAutopilot());
			getItems().add(autopilotItem);
			immunityItem = new CheckMenuItem("Immunity");
			immunityItem.setOnAction(e -> ui.toggleImmunity());
			getItems().add(immunityItem);
		}

		private MenuItem createTitleItem(String title) {
			var text = new Text(title);
			text.setFont(Font.font("Sans", FontWeight.BOLD, 14));
			return new CustomMenuItem(text);
		}

		public void updateState() {
			//TODO should be done via binding or something:
			for (var toggle : perspectiveMenuToggleGroup.getToggles()) {
				toggle.setSelected(PacManGames3dApp.PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
			}
			autopilotItem.setSelected(GameController.it().isAutoControlled());
			immunityItem.setSelected(GameController.it().isImmune());
			if (pipItem != null) {
				pipItem.setSelected(isPiPOn());
			}
		}
	}

	private GamePageContextMenu contextMenu;

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
		ui.mainScene().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.getButton() == MouseButton.SECONDARY &&
					(ui.currentGameScene() instanceof PlayScene2D || ui.currentGameScene() instanceof PlayScene3D)) {
				if (contextMenu != null) {
					contextMenu.hide();
				}
				contextMenu = new GamePageContextMenu();
				contextMenu.show(ui.mainScene().getRoot(), e.getScreenX(), e.getScreenY());
			} else {
				contextMenu.hide();
			}
		});
	}

	public PictureInPicture getPip() {
		return pip;
	}

	@Override
	public void onGameSceneChanged() {
		if (ui.currentGameScene().is3D()) {
			layers.getChildren().set(GAME_SCENE_LAYER, ui.currentGameScene().root());
			// Assume PlayScene3D is the only 3D scene
			layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
			layers.requestFocus();
			helpButton.setVisible(false);
			updateBackground(ui.currentGameScene());
		} else {
			layers.getChildren().set(GAME_SCENE_LAYER, gameSceneLayer);
			super.onGameSceneChanged();
		}
		if (contextMenu != null) {
			contextMenu.hide();
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
		contextMenu.updateState();
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
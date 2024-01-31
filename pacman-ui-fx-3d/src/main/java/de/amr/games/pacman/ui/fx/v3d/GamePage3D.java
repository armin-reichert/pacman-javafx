/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.ui.fx.GamePage;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.dashboard.*;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

	private final BorderPane dashboardLayer; // contains dashboard and picture-in-picture view
	private final PictureInPicture pip;
	private final Pane dashboard;
	private final GamePageContextMenu contextMenu;
	private final List<InfoBox> infoBoxes = new ArrayList<>();

	public GamePage3D(Scene parentScene, GameSceneContext sceneContext, double width, double height) {
		super(sceneContext, width, height);

		pip = createPictureInPicture();
		contextMenu = createContextMenu(parentScene);
		dashboard = createDashboard();

		dashboardLayer = new BorderPane();
		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pip.root());
		layers.getChildren().add(dashboardLayer);

		canvasLayer.setBackground(sceneContext.theme().background("wallpaper.background"));

		PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateBackground3D());
		PY_3D_NIGHT_MODE.addListener((py, ov, nv) -> updateBackground3D());
		PY_PIP_ON.addListener((py, ov, nv) -> updateDashboardLayer());
		dashboard.visibleProperty().addListener((py, ov, nv) -> updateDashboardLayer());
	}

	private GamePageContextMenu createContextMenu(Scene parentScene) {
		var menu = new GamePageContextMenu(sceneContext);
		parentScene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			menu.hide();
			if (e.getButton() == MouseButton.SECONDARY) {
				sceneContext.currentGameScene().ifPresent(gameScene -> {
					if (gameScene == sceneContext.sceneConfig().get("play") ||
						gameScene == sceneContext.sceneConfig().get("play3D")) {
						menu.rebuild(gameScene);
						menu.show(parentScene.getRoot(), e.getScreenX(), e.getScreenY());
					}
				});
			}
		});
		return menu;
	}

	private PictureInPicture createPictureInPicture() {
		var pip = new PictureInPicture(sceneContext);
		pip.opacityPy.bind(PY_PIP_OPACITY);
		pip.heightPy.bind(PY_PIP_HEIGHT);
		return pip;
	}

	private VBox createDashboard() {
		var db = new VBox();
		infoBoxes.add(new InfoBoxGeneral(sceneContext.theme(), sceneContext.tt("infobox.general.title")));
		infoBoxes.add(new InfoBoxGameControl(sceneContext.theme(), sceneContext.tt("infobox.game_control.title")));
		infoBoxes.add(new InfoBox3D(sceneContext.theme(), sceneContext.tt("infobox.3D_settings.title")));
		infoBoxes.add(new InfoBoxGameInfo(sceneContext.theme(), sceneContext.tt("infobox.game_info.title")));
		infoBoxes.add(new InfoBoxGhostsInfo(sceneContext.theme(), sceneContext.tt("infobox.ghosts_info.title")));
		infoBoxes.add(new InfoBoxKeys(sceneContext.theme(), sceneContext.tt("infobox.keyboard_shortcuts.title")));
		infoBoxes.add(new InfoBoxAbout(sceneContext.theme(), sceneContext.tt("infobox.about.title")));
		infoBoxes.forEach(infoBox -> {
			db.getChildren().add(infoBox.getRoot());
			infoBox.init(sceneContext);
		});
		db.setVisible(false);
		return db;
	}

	@Override
	public void onGameSceneChanged(GameScene newGameScene) {
		contextMenu.hide();
		if (isCurrentGameScene3D()) {
			helpIcon.setVisible(false);
			showDebugBorders(false);
			updateBackground3D();
			updateDashboardLayer();
			if (newGameScene == sceneContext.sceneConfig().get("play3D")) {
				// Note: event handler is removed again in super.onGameSceneChanged() call
				layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) sceneContext.gameController().getManualPacSteering());
			}
			layers.getChildren().set(0, newGameScene.root());
		} else {
			layers.getChildren().set(0, canvasLayer);
			super.onGameSceneChanged(newGameScene);
		}
	}

	private void updateDashboardLayer() {
		dashboardLayer.setVisible(dashboard.isVisible() || PY_PIP_ON.get());
		layers.requestFocus();
	}

	private void updateBackground3D() {
		if (isCurrentGameScene3D()) {
			if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				var wallpaperKey = PY_3D_NIGHT_MODE.get() ? "model3D.wallpaper.night" : "model3D.wallpaper";
				layers.setBackground(sceneContext.theme().background(wallpaperKey));
			}
		}
	}

	@Override
	public void render() {
		super.render();
		contextMenu.updateState();
		infoBoxes.forEach(InfoBox::update);
		pip.root().setVisible(PY_PIP_ON.get() && isCurrentGameScene3D());
		pip.draw();
	}

	@Override
	protected void handleKeyboardInput() {
		var actionHandler3D = (ActionHandler3D) sceneContext.actionHandler();
		if (Keyboard.pressed(KEY_TOGGLE_2D_3D)) {
			actionHandler3D.toggle2D3D();
		} else if (Keyboard.pressed(KEYS_TOGGLE_DASHBOARD)) {
			dashboard.setVisible(!dashboard.isVisible());
		} else if (Keyboard.pressed(KEY_TOGGLE_PIP_VIEW)) {
			actionHandler3D.togglePipVisible();
		} else {
			super.handleKeyboardInput();
		}
	}

	@Override
	protected void showDebugBorders(boolean on)  {
		super.showDebugBorders(on && !isCurrentGameScene3D());
	}

	@Override
	protected boolean isHelpIconVisible() {
		return !isCurrentGameScene3D() && super.isHelpIconVisible();
	}

	private boolean isCurrentGameScene3D() {
		return sceneContext.currentGameScene().isPresent()
			&& sceneContext.currentGameScene().get() instanceof PlayScene3D;
	}
}
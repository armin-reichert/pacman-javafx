/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class GamePage {

	protected final PacManGames2dUI ui;
	protected final StackPane root;
	protected boolean canvasScaled = true;

	public GamePage(PacManGames2dUI ui) {
		this.ui = ui;
		root = new StackPane(new Region()); // placeholder
		root.setBackground(ui.theme().background("wallpaper.background"));
		root.setOnKeyPressed(this::handleKeyPressed);
		root.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene(ui.stage);
			}
		});
	}

	public StackPane root() {
		return root;
	}

	public void update() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().update();
		}
	}

	public void render() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().render();
		}
	}

	public void setGameScene(GameScene gameScene) {
		root.getChildren().set(0, gameScene.sceneContainer());
		if (gameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) gameScene;
			scene2D.setCanvasScaled(canvasScaled);
			// to draw rounded canvas corners, background color must be set
			scene2D.setWallpaperColor(ui.theme().color("wallpaper.color"));
		}
	}

	protected void resizeStageToFitCurrentGameScene(Stage stage) {
		if (ui.currentGameScene() != null && !ui.currentGameScene().is3D() && !stage.isFullScreen()) {
			stage.setWidth(ui.currentGameScene().sceneContainer().getWidth() + 16); // don't ask me why
		}
	}

	protected void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().handleKeyboardInput();
		}
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_SHOW_HELP)) {
			ui.showHelp();
		} else if (Keyboard.pressed(PacManGames2d.KEY_AUTOPILOT)) {
			ui.toggleAutopilot();
		} else if (Keyboard.pressed(PacManGames2d.KEY_BOOT)) {
			if (ui.gameController().state() != GameState.BOOT) {
				ui.reboot();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_DEBUG_INFO)) {
			Ufx.toggle(PacManGames2d.PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(PacManGames2d.KEY_IMMUNITIY)) {
			ui.toggleImmunity();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE)) {
			ui.togglePaused();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE_STEP) || Keyboard.pressed(PacManGames2d.KEY_SINGLE_STEP)) {
			ui.oneSimulationStep();
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEN_STEPS)) {
			ui.tenSimulationSteps();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_FASTER)) {
			ui.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_SLOWER)) {
			ui.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_NORMAL)) {
			ui.resetSimulationSpeed();
		} else if (Keyboard.pressed(PacManGames2d.KEY_QUIT)) {
			if (ui.gameController.state() != GameState.BOOT && ui.gameController.state() != GameState.INTRO) {
				ui.restartIntro();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEST_LEVELS)) {
			ui.startLevelTestMode();
		} else if (Keyboard.pressed(PacManGames2d.KEY_FULLSCREEN)) {
			ui.stage.setFullScreen(true);
		} else if (Keyboard.pressed(PacManGames2d.KEY_CANVAS_SCALED)) {
			if (ui.currentGameScene instanceof GameScene2D) {
				toggleCanvasScaled((GameScene2D) ui.currentGameScene);
			}
		}
	}

	private void toggleCanvasScaled(GameScene2D gameScene2D) {
		canvasScaled = !canvasScaled;
		gameScene2D.setCanvasScaled(canvasScaled);
		ui.showFlashMessage(canvasScaled ? "Canvas SCALED" : "Canvas UNSCALED");
	}
}

/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class GamePage {

	private static final int FRAME_THICKNESS = 15;

	protected final PacManGames2dUI ui;
	protected final StackPane root;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final BorderPane scene2DBackPanel;
	protected final BorderPane scene2DEmbedder;
	protected final ImageView helpButton;
	protected boolean canvasScaled = false;

	public GamePage(PacManGames2dUI ui) {
		this.ui = ui;

		scene2DEmbedder = new BorderPane();
		scene2DEmbedder.setMaxSize(1, 1);
		scene2DEmbedder.setScaleX(0.9);
		scene2DEmbedder.setScaleY(0.9);

		var borderStyle = new BorderStroke( //
				Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, //
				BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, //
				new CornerRadii(10), new BorderWidths(FRAME_THICKNESS), null);

		scene2DEmbedder.setBorder(new Border(borderStyle));
		scene2DEmbedder.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && e.getY() < scene2DEmbedder.getHeight() * 0.5) {
				resizeStageToFitCurrentGameScene(ui.stage);
			}
		});

		scene2DBackPanel = new BorderPane();
		scene2DBackPanel.setBackground(ResourceManager.coloredBackground(Color.BLACK));
		scene2DBackPanel.setPadding(new Insets(2, 15, 2, 15));

		helpButton = new ImageView();
		StackPane.setAlignment(helpButton, Pos.BOTTOM_CENTER);
		helpButton.setTranslateY(FRAME_THICKNESS);
		helpButton.setFitWidth(FRAME_THICKNESS);
		helpButton.setFitHeight(FRAME_THICKNESS);
		helpButton.setSmooth(true);
		helpButton.setCursor(Cursor.HAND);
		helpButton.setOnMouseClicked(e -> {
			if (e.getClickCount() == 1 && e.getButton() == MouseButton.PRIMARY) {
				ui.showHelp();
			}
		});

		root = new StackPane();
		root.setBackground(ui.theme().background("wallpaper.background"));
		root.setOnKeyPressed(this::handleKeyPressed);

		var stack = new StackPane(scene2DBackPanel, helpButton);
		stack.setBackground(ResourceManager.coloredBackground(Color.BLACK));
		scene2DEmbedder.setCenter(stack);

		root.getChildren().setAll(scene2DEmbedder, flashMessageView);
	}

	private boolean isHelpAvailable(GameScene gameScene) {
		var state = gameScene.context().gameController().state();
		if (state == GameState.BOOT || state == GameState.INTERMISSION || state == GameState.INTERMISSION_TEST) {
			return false;
		}
		return true;
	}

	private void updateHelpButton(GameScene gameScene) {
		if (isHelpAvailable(gameScene)) {
			boolean msPacManGame = gameScene.context().gameVariant() == GameVariant.MS_PACMAN;
			helpButton.setImage(ui.theme().image(msPacManGame ? "mspacman.helpButton.icon" : "pacman.helpButton.icon"));
			helpButton.setVisible(true);
		} else {
			helpButton.setVisible(false); // or gray out etc.
		}
	}

	public void setGameScene(GameScene gameScene) {
		if (gameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) gameScene;
			scene2D.setCanvasScaled(canvasScaled);
			scene2D.setRoundedCorners(false);
			scene2DBackPanel.setCenter(scene2D.root());
			root.getChildren().set(0, scene2DEmbedder);
		} else {
			root.getChildren().set(0, gameScene.root());
		}
		boolean playScene = false;
		if (ui.gameVariant() == GameVariant.MS_PACMAN) {
			playScene = gameScene == ui.configMsPacMan.playScene() || gameScene == ui.configMsPacMan.playScene3D();
		} else {
			playScene = gameScene == ui.configPacMan.playScene() || gameScene == ui.configPacMan.playScene3D();
		}
		if (playScene) {
			root.addEventHandler(KeyEvent.KEY_PRESSED, ui.keyboardPlayerSteering);
		} else {
			root.removeEventHandler(KeyEvent.KEY_PRESSED, ui.keyboardPlayerSteering);
		}
		updateHelpButton(gameScene);
		root.requestFocus();
	}

	public Pane root() {
		return root;
	}

	public Node helpButton() {
		return helpButton;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
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
		flashMessageView.update();
	}

	public void setBackground(Background background) {
		root.setBackground(background);
	}

	public void addLayer(Node layer) {
		root.getChildren().add(layer);
	}

	protected void resizeStageToFitCurrentGameScene(Stage stage) {
		if (ui.currentGameScene() != null && !ui.currentGameScene().is3D() && !stage.isFullScreen()) {
			stage.setWidth(ui.currentGameScene().root().getWidth() + 16); // don't ask me why
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
			toggleCanvasScaled(ui.currentGameScene);
		}
	}

	private void toggleCanvasScaled(GameScene gameScene) {
		if (gameScene instanceof GameScene2D) {
			canvasScaled = !canvasScaled;
			((GameScene2D) gameScene).setCanvasScaled(canvasScaled);
			ui.showFlashMessage(canvasScaled ? "Canvas SCALED" : "Canvas UNSCALED");
		}
	}
}

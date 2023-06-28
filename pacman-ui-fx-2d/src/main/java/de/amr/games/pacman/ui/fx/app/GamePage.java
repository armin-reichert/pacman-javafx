/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpButton;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenu;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenuFactory;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class GamePage {

	protected static final double MIN_SCALING = 0.7;
	protected static final int GAME_SCENE_LAYER = 0;
	protected static final Duration MENU_FADING_DELAY = Duration.seconds(1.5);

	protected final PacManGames2dUI ui;
	protected final Theme theme;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final StackPane layers = new StackPane();
	protected final BorderPane gameSceneLayer = new BorderPane();
	protected final BorderPane canvasContainer = new BorderPane();
	protected final Canvas canvas = new Canvas();
	protected final Pane popupLayer = new Pane();
	protected final HelpMenuFactory helpMenuFactory = new HelpMenuFactory();
	protected final HelpMenu helpMenu = new HelpMenu();
	protected final HelpButton helpButton = new HelpButton();
	protected final Signature signature = new Signature();

	protected GameScene2D gameScene2D;
	protected double scaling = 1.0;

	public GamePage(PacManGames2dUI ui, Theme theme) {
		this.ui = ui;
		this.theme = theme;

		gameSceneLayer.setBackground(theme.background("wallpaper.background"));
		gameSceneLayer.setCenter(canvasContainer);

		canvasContainer.setBackground(ResourceManager.coloredBackground(Color.BLACK));
		canvasContainer.setCenter(canvas);
		canvasContainer.heightProperty().addListener((py, ov, nv) -> resize(scaling, false));

		helpButton.setOnMouseClicked(e -> {
			e.consume();
			showHelpMenu();
		});

		layers.getChildren().addAll(gameSceneLayer, popupLayer, flashMessageView);
		popupLayer.getChildren().addAll(helpButton, signature.root(), helpMenu);

		layers.setOnKeyPressed(this::handleKeyPressed);
		//popupLayer.setOnMouseClicked(this::handleMouseClick);
		//new PacMouseSteering(this, popupLayer, () -> ui.game().level().map(GameLevel::pac).orElse(null));

		PacManGames2d.PY_SHOW_DEBUG_INFO.addListener((py, ov, debug) -> {
			layers.setBorder(debug ? ResourceManager.border(Color.RED, 3) : null);
			gameSceneLayer.setBorder(debug ? ResourceManager.border(Color.YELLOW, 3) : null);
			popupLayer.setBorder(debug ? ResourceManager.border(Color.GREENYELLOW, 3) : null);
		});
	}

	protected void updateHelpButton() {
		String key = ui.game().variant() == GameVariant.MS_PACMAN ? "mspacman.helpButton.icon" : "pacman.helpButton.icon";
		helpButton.setImage(theme.image(key), Math.ceil(10 * scaling));
		helpButton.setTranslateX(popupLayer.getWidth() - 20 * scaling);
		helpButton.setTranslateY(8 * scaling);
		helpButton.setVisible(ui.sceneConfig().bootScene() != gameScene2D);
	}

	protected void showHelpMenu() {
		helpMenuFactory.setFont(theme.font("font.monospaced", Math.max(6, 14 * scaling)));
		helpMenuFactory.currentHelpMenuContent().ifPresent(content -> {
			helpMenu.setTranslateX(10 * scaling);
			helpMenu.setTranslateY(30 * scaling);
			helpMenu.setContent(content);
			helpMenu.show(MENU_FADING_DELAY);
		});
	}

	public void resize(double scaling, boolean always) {
		if (scaling < MIN_SCALING) {
			Logger.info("Cannot scale to {}, minimum scaling is {}", scaling, MIN_SCALING);
			return;
		}

		updateHelpButton();
		updateSignatureSizeAndPosition();
		if (gameScene2D != null) {
			gameScene2D.setScaling(scaling);
		}

		if (this.scaling == scaling && !always) {
			return;
		}
		this.scaling = scaling;

		double w = Math.round( (PacManGames2d.CANVAS_WIDTH_UNSCALED  + 25) * scaling );
		double h = Math.round( (PacManGames2d.CANVAS_HEIGHT_UNSCALED + 15) * scaling );

		canvas.setWidth(PacManGames2d.CANVAS_WIDTH_UNSCALED * scaling);
		canvas.setHeight(PacManGames2d.CANVAS_HEIGHT_UNSCALED * scaling);

		canvasContainer.setMinSize (w, h);
		canvasContainer.setPrefSize(w, h);
		canvasContainer.setMaxSize (w, h);

		var roundedRect = new Rectangle(w, h);
		var arc = 35;
		roundedRect.setArcWidth (arc * scaling);
		roundedRect.setArcHeight(arc * scaling);
		canvasContainer.setClip(roundedRect);

		double borderWidth  = Math.max(5, Math.ceil(h / 60));
		double cornerRadius = Math.ceil(15 * scaling);
		canvasContainer.setBorder(ResourceManager.roundedBorder(ArcadeTheme.PALE, cornerRadius, borderWidth));

		popupLayer.setMinSize (w, h);
		popupLayer.setPrefSize(w, h);
		popupLayer.setMaxSize (w, h);

		Logger.info("Game page resized: scaling: {} height: {} border: {}", scaling, h, borderWidth);
	}

	public void setGameScene(GameScene gameScene) {
		gameScene2D = (GameScene2D) gameScene;
		gameScene2D.setCanvas(canvas);
		resize(scaling, true);
		if (gameScene == ui.sceneConfig().playScene()) {
			layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
		} else {
			layers.removeEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) GameController.it().getManualPacSteering());
		}
		if (gameScene == ui.sceneConfig().introScene()) {
			signature.showAfterSeconds(3);
		} else {
			signature.hide();
		}
		//layers.requestFocus();
	}

	protected void updateSignatureSizeAndPosition() {
		signature.setMadeByFont(Font.font("Helvetica", Math.floor(10 * scaling)));
		signature.setNameFont(theme.font("font.handwriting", Math.floor(12 * scaling)));
		if (ui.game().variant() == GameVariant.MS_PACMAN) {
			signature.root().setTranslateX(50 * scaling);
			signature.root().setTranslateY(40 * scaling);
		} else {
			signature.root().setTranslateX(50 * scaling);
			signature.root().setTranslateY(28 * scaling);
		}
	}

	public Pane root() {
		return layers;
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
			showHelpMenu();
		} else if (Keyboard.pressed(PacManGames2d.KEY_AUTOPILOT)) {
			ui.toggleAutopilot();
		} else if (Keyboard.pressed(PacManGames2d.KEY_BOOT)) {
			if (GameController.it().state() != GameState.BOOT) {
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
			if (GameController.it().state() != GameState.BOOT && GameController.it().state() != GameState.INTRO) {
				ui.restartIntro();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEST_LEVELS)) {
			ui.startLevelTestMode();
		} else if (Keyboard.pressed(PacManGames2d.KEY_FULLSCREEN)) {
			ui.stage.setFullScreen(true);
		}
	}
}
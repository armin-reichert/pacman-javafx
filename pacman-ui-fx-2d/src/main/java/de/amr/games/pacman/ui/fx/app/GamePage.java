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
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpButton;
import de.amr.games.pacman.ui.fx.util.FadingPane;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.oneOf;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

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
	protected final FadingPane helpMenu = new FadingPane();
	protected final HelpButton helpButton = new HelpButton();
	protected final Signature signature = new Signature();

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

		// debug border decoration
		layers.borderProperty().bind(debugBorderBinding(Color.RED, 3));
		gameSceneLayer.borderProperty().bind(debugBorderBinding(Color.YELLOW, 3));
		popupLayer.borderProperty().bind(debugBorderBinding(Color.GREENYELLOW, 3));
	}

	protected ObjectBinding<Border> debugBorderBinding(Color color, double width) {
		return Bindings.createObjectBinding(() -> PacManGames2dApp.PY_SHOW_DEBUG_INFO.get()
				&& ui.currentScene().isPresent() && !ui.currentScene().get().is3D() ?
				ResourceManager.border(color, width) : null, PacManGames2dApp.PY_SHOW_DEBUG_INFO);
	}

	protected void updateHelpButton() {
		String key = ui.game().variant() == GameVariant.MS_PACMAN ? "mspacman.helpButton.icon" : "pacman.helpButton.icon";
		helpButton.setImage(theme.image(key), Math.ceil(10 * scaling));
		helpButton.setTranslateX(popupLayer.getWidth() - 20 * scaling);
		helpButton.setTranslateY(8 * scaling);
		helpButton.setVisible(ui.currentScene().isPresent() && ui.currentScene().get() != ui.sceneConfig().get("boot"));
	}

	protected void showHelpMenu() {
		currentHelpMenuContent().ifPresent(content -> {
			helpMenu.setTranslateX(10 * scaling);
			helpMenu.setTranslateY(30 * scaling);
			helpMenu.setContent(content);
			helpMenu.show(MENU_FADING_DELAY);
		});
	}

	@Override
	public void setSize(double width, double height) {
		double s = 0.9 * height / PacManGames2dApp.CANVAS_HEIGHT_UNSCALED;
		if (s * PacManGames2dApp.CANVAS_WIDTH_UNSCALED > 0.8 * width) {
			s = 0.8 * width / PacManGames2dApp.CANVAS_WIDTH_UNSCALED;
		}
		s = Math.floor(s * 10) / 10; // round scaling factor to first decimal digit
		resize(s, false);
	}

	public void resize(double scaling, boolean always) {
		if (scaling < MIN_SCALING) {
			Logger.info("Cannot scale to {}, minimum scaling is {}", scaling, MIN_SCALING);
			return;
		}

		//TODO check if this has to be done also when scaling value did not change
		updateHelpButton();
		updateSignatureSizeAndPosition();
		ui.currentScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.setScaling(scaling);
			}
		});

		if (this.scaling == scaling && !always) {
			return;
		}
		this.scaling = scaling;

		double w = Math.round( (PacManGames2dApp.CANVAS_WIDTH_UNSCALED  + 25) * scaling );
		double h = Math.round( (PacManGames2dApp.CANVAS_HEIGHT_UNSCALED + 15) * scaling );

		canvas.setWidth(PacManGames2dApp.CANVAS_WIDTH_UNSCALED * scaling);
		canvas.setHeight(PacManGames2dApp.CANVAS_HEIGHT_UNSCALED * scaling);

		canvasContainer.setMinSize (w, h);
		canvasContainer.setPrefSize(w, h);
		canvasContainer.setMaxSize (w, h);

		var roundedRect = new Rectangle(w, h);
		roundedRect.setArcWidth (26 * scaling);
		roundedRect.setArcHeight(26 * scaling);
		canvasContainer.setClip(roundedRect);

		double borderWidth  = Math.max(5, Math.ceil(h / 55));
		double cornerRadius = Math.ceil(10 * scaling);
		var roundedBorder = ResourceManager.roundedBorder(ArcadePalette.PALE, cornerRadius, borderWidth);
		canvasContainer.setBorder(roundedBorder);

		popupLayer.setMinSize (w, h);
		popupLayer.setPrefSize(w, h);
		popupLayer.setMaxSize (w, h);

		Logger.trace("Game page resized: scaling: {}, canvas size: {000} x {000} px, border: {0} px", scaling,
				canvas.getWidth(), canvas.getHeight(), borderWidth);
	}

	public void onGameSceneChanged() {
		var config = ui.sceneConfig();
		var currentGameScene = ui.currentScene().get();
		// if play scene gets active/inactive, add/remove key handler
		if (GameController.it().getManualPacSteering() instanceof KeyboardSteering keyboardSteering) {
			if (currentGameScene == config.get("play")) {
				layers.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			} else {
				layers.removeEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			}
		}
		// if intro scene gets active/inactive, show/hide signature
		if (currentGameScene == config.get("intro")) {
			signature.showAfterSeconds(3);
		} else {
			signature.hide();
		}
		if (currentGameScene instanceof GameScene2D gameScene2D) {
			gameScene2D.setCanvas(canvas);
		}
		resize(scaling, true);
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

	@Override
	public Pane root() {
		return layers;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public PacManGames2dUI ui() {
		return ui;
	}

	public void render() {
		ui.currentScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.draw();
			}
		});
		flashMessageView.update();
		popupLayer.setVisible(true);
	}

	protected void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		ui.currentScene().ifPresent(GameScene::handleKeyboardInput);
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		var gameState = GameController.it().state();
		if (Keyboard.pressed(PacManGames2dApp.KEY_AUTOPILOT)) {
			ui.toggleAutopilot();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_BOOT)) {
			if (gameState != GameState.BOOT) {
				ui.reboot();
			}
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_DEBUG_INFO)) {
			Ufx.toggle(PacManGames2dApp.PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_FULLSCREEN)) {
			ui.stage.setFullScreen(true);
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_IMMUNITY)) {
			ui.toggleImmunity();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_SHOW_HELP)) {
			showHelpMenu();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_PAUSE)) {
			ui.togglePaused();
		} else if (Keyboard.anyPressed(PacManGames2dApp.KEY_PAUSE_STEP, PacManGames2dApp.KEY_SINGLE_STEP)) {
			ui.oneSimulationStep();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_TEN_STEPS)) {
			ui.tenSimulationSteps();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_SIMULATION_FASTER)) {
			ui.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_SIMULATION_SLOWER)) {
			ui.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_SIMULATION_NORMAL)) {
			ui.resetSimulationSpeed();
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_QUIT)) {
			if (gameState != GameState.BOOT && gameState != GameState.INTRO) {
				ui.restartIntro();
			}
		} else if (Keyboard.pressed(PacManGames2dApp.KEY_TEST_LEVELS)) {
			ui.startLevelTestMode();
		}
	}

	// Menu stuff

	private static class Menu {
		private final List<Node> column0 = new ArrayList<>();
		private final List<Node> column1 = new ArrayList<>();
		private Font font =	Font.font("Sans", 24);

		public void addRow(Node node0, Node node1) {
			column0.add(node0);
			column1.add(node1);
		}

		public int size() {
			return column0.size();
		}

		public void setFont(Font font) {
			this.font = font;
		}

		private Label label(String s) {
			var label = new Label(s);
			label.setTextFill(Color.gray(0.9));
			label.setFont(font);
			return label;
		}

		private Text text(String s, Color color) {
			var text = new Text(s);
			text.setFill(color);
			text.setFont(font);
			return text;
		}

		private Text text(String s) {
			return text(s, Color.YELLOW);
		}

		private void addEntry(String rbKey, String kbKey) {
			addRow(label(tt(rbKey)), text("[" + kbKey + "]"));
		}

		private Pane createPane() {
			var grid = new GridPane();
			grid.setHgap(20);
			grid.setVgap(10);
			for (int row = 0; row < column0.size(); ++row) {
				grid.add(column0.get(row), 0, row);
				grid.add(column1.get(row), 1, row);
			}
			int rowIndex = size();
			if (GameController.it().isAutoControlled()) {
				var text = text(tt("help.autopilot_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex++);
			}
			if (GameController.it().isImmune()) {
				var text = text(tt("help.immunity_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex++);
			}

			var pane = new BorderPane(grid);
			pane.setPadding(new Insets(10));
			var bgColor = GameController.it().game().variant() == GameVariant.MS_PACMAN
					? Color.rgb(255, 0, 0, 0.8)
					: Color.rgb(33, 33, 255, 0.8);
			pane.setBackground(ResourceManager.coloredRoundedBackground(bgColor, 10));
			return pane;
		}
	}

	private static String tt(String key) {
		return PacManGames2dApp.TEXTS.getString(key);
	}

	private Optional<Pane> currentHelpMenuContent() {
		var gameState = GameController.it().state();
		var game = GameController.it().game();
		var menuFont = theme.font("font.monospaced", Math.max(6, 14 * scaling));
		if (gameState == GameState.INTRO) {
			return Optional.of(menuIntro(menuFont));
		}
		if (gameState == GameState.CREDIT) {
			return Optional.of(menuCredit(menuFont));
		}
		if (game.level().isPresent()
				&& oneOf(gameState, GameState.READY, GameState.HUNTING, GameState.PACMAN_DYING, GameState.GHOST_DYING)) {
			return game.level().get().isDemoLevel()
					? Optional.of(menuDemoLevel(menuFont))
					: Optional.of(menuPlaying(menuFont));
		}
		return Optional.empty();
	}

	private Pane menuIntro(Font menuFont) {
		var menu = new Menu();
		menu.setFont(menuFont);
		if (GameController.it().hasCredit()) {
			menu.addEntry("help.start_game", "1");
		}
		menu.addEntry("help.add_credit", "5");
		menu.addEntry(GameController.it().game().variant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman",
				"V");
		return menu.createPane();
	}

	private Pane menuCredit(Font menuFont) {
		var menu = new Menu();
		menu.setFont(menuFont);
		if (GameController.it().hasCredit()) {
			menu.addEntry("help.start_game", "1");
		}
		menu.addEntry("help.add_credit", "5");
		menu.addEntry("help.show_intro", "Q");
		return menu.createPane();
	}

	private Pane menuPlaying(Font menuFont) {
		var menu = new Menu();
		menu.setFont(menuFont);
		menu.addEntry("help.move_left",  tt("help.cursor_left"));
		menu.addEntry("help.move_right", tt("help.cursor_right"));
		menu.addEntry("help.move_up",    tt("help.cursor_up"));
		menu.addEntry("help.move_down",  tt("help.cursor_down"));
		menu.addEntry("help.show_intro", "Q");
		return menu.createPane();
	}

	private Pane menuDemoLevel(Font menuFont) {
		var menu = new Menu();
		menu.setFont(menuFont);
		menu.addEntry("help.add_credit", "5");
		menu.addEntry("help.show_intro", "Q");
		return menu.createPane();
	}
}
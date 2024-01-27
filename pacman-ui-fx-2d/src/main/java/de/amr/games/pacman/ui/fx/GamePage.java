/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpButton;
import de.amr.games.pacman.ui.fx.util.FadingPane;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
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
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.oneOf;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage implements Page {

	protected static final double MIN_SCALING = 0.7;
	protected static final Duration MENU_FADING_DELAY = Duration.seconds(1.5);

	protected final GameSceneContext sceneContext;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final StackPane layers = new StackPane();
	protected final BorderPane gameSceneLayer = new BorderPane();
	protected final BorderPane canvasContainer = new BorderPane();
	protected final Canvas canvas = new Canvas();
	protected final Pane popupLayer = new Pane();
	protected final FadingPane helpMenu = new FadingPane();
	protected final HelpButton helpButton = new HelpButton();
	protected final Signature signature = new Signature("Remake (2023) by ", "Armin Reichert");

	protected double scaling = 1.0;

	protected List<ResourceBundle> messageBundles;

	public GamePage(GameSceneContext sceneContext, List<ResourceBundle> messageBundles) {
		this.sceneContext = sceneContext;
		this.messageBundles = messageBundles;

		gameSceneLayer.setBackground(sceneContext.theme().background("wallpaper.background"));
		gameSceneLayer.setCenter(canvasContainer);

		canvasContainer.setBackground(ResourceManager.coloredBackground(sceneContext.theme().color("canvas.background")));
		canvasContainer.setCenter(canvas);
		canvasContainer.heightProperty().addListener((py, ov, nv) -> scalePage(scaling, false));

		helpButton.setOnMouseClicked(e -> {
			e.consume();
			showHelpMenu();
		});

		layers.getChildren().addAll(gameSceneLayer, popupLayer, flashMessageView);
		popupLayer.getChildren().addAll(helpButton, signature.root(), helpMenu);

		layers.setOnKeyPressed(this::handleKeyPressed);

		PY_SHOW_DEBUG_INFO.addListener((py, ov, nv) -> updateDebugBorders());
	}

	@Override
	public Pane root() {
		return layers;
	}

	protected void updateHelpButton() {
		String key = sceneContext.gameVariant() == GameVariant.MS_PACMAN 
			? "mspacman.helpButton.icon" 
			: "pacman.helpButton.icon";
		helpButton.setImage(sceneContext.theme().image(key), Math.ceil(10 * scaling));
		helpButton.setTranslateX(popupLayer.getWidth() - 20 * scaling);
		helpButton.setTranslateY(8 * scaling);
		helpButton.setVisible(sceneContext.currentGameScene().isPresent()
			&& sceneContext.currentGameScene().get() != sceneContext.sceneConfig().get("boot"));
	}

	@Override
	public void setSize(double width, double height) {
		double s = 0.9 * height / CANVAS_HEIGHT_UNSCALED;
		if (s * CANVAS_WIDTH_UNSCALED > 0.8 * width) {
			s = 0.8 * width / CANVAS_WIDTH_UNSCALED;
		}
		s = Math.floor(s * 10) / 10; // round scaling factor to first decimal digit
		scalePage(s, false);
	}

	public void scalePage(double scaling, boolean always) {
		if (scaling < MIN_SCALING) {
			Logger.info("Cannot scale to {}, minimum scaling is {}", scaling, MIN_SCALING);
			return;
		}

		//TODO check if this has to be done also when scaling value did not change
		updateHelpButton();
		updateSignatureSizeAndPosition();
		sceneContext.currentGameScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.setScaling(scaling);
			}
		});

		if (this.scaling == scaling && !always) {
			return;
		}
		this.scaling = scaling;

		double w = Math.round( (CANVAS_WIDTH_UNSCALED  + 25) * scaling );
		double h = Math.round( (CANVAS_HEIGHT_UNSCALED + 15) * scaling );

		canvas.setWidth(CANVAS_WIDTH_UNSCALED * scaling);
		canvas.setHeight(CANVAS_HEIGHT_UNSCALED * scaling);

		var roundedRect = new Rectangle(w, h);
		roundedRect.setArcWidth (26 * scaling);
		roundedRect.setArcHeight(26 * scaling);
		canvasContainer.setClip(roundedRect);

		double borderWidth  = Math.max(5, Math.ceil(h / 55));
		double cornerRadius = Math.ceil(10 * scaling);
		var roundedBorder = ResourceManager.roundedBorder(
			sceneContext.theme().color("palette.pale"), cornerRadius, borderWidth);
		canvasContainer.setBorder(roundedBorder);

		setSizes(canvasContainer, w, h);
		setSizes(popupLayer, w, h);

		Logger.trace("Game page resized: scaling: {}, canvas size: {000} x {000} px, border: {0} px", scaling,
				canvas.getWidth(), canvas.getHeight(), borderWidth);
	}

	private void setSizes(Region region, double width, double height) {
		region.setMinSize(width, height);
		region.setMaxSize(width, height);
		region.setPrefSize(width, height);
	}

	public void onGameSceneChanged(GameScene newGameScene) {
		var config = sceneContext.sceneConfig();
		// if play scene gets active/inactive, add/remove key handler
		if (sceneContext.gameController().getManualPacSteering() instanceof KeyboardSteering keyboardSteering) {
			if (newGameScene == config.get("play")) {
				layers.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			} else {
				layers.removeEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			}
		}
		// if intro scene gets active/inactive, show/hide signature
		if (newGameScene == config.get("intro")) {
			signature.showAfterSeconds(3);
		} else {
			signature.hide();
		}
		if (newGameScene instanceof GameScene2D gameScene2D) {
			gameScene2D.setCanvas(canvas);
		}
		scalePage(scaling, true);

		updateDebugBorders();
	}

	protected void updateDebugBorders()  {
		if (PY_SHOW_DEBUG_INFO.get()) {
			layers.setBorder(ResourceManager.border(Color.RED, 3));
			gameSceneLayer.setBorder(ResourceManager.border(Color.YELLOW, 3));
			popupLayer.setBorder(ResourceManager.border(Color.GREENYELLOW, 3));
		} else {
			layers.setBorder(null);
			gameSceneLayer.setBorder(null);
			popupLayer.setBorder(null);
		}
	}

	protected void updateSignatureSizeAndPosition() {
		signature.getText(0).setFont(Font.font("Helvetica", Math.floor(10 * scaling)));
		signature.getText(1).setFont((sceneContext.theme().font("font.handwriting", Math.floor(12 * scaling))));
		if (sceneContext.game().variant() == GameVariant.MS_PACMAN) {
			signature.root().setTranslateX(50 * scaling);
			signature.root().setTranslateY(40 * scaling);
		} else {
			signature.root().setTranslateX(50 * scaling);
			signature.root().setTranslateY(28 * scaling);
		}
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public void render() {
		sceneContext.currentGameScene().ifPresent(gameScene -> {
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
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		var actionHandler = sceneContext.actionHandler();
		var gameState = sceneContext.gameState();
		if (Keyboard.pressed(KEY_AUTOPILOT)) {
			actionHandler.toggleAutopilot();
		} else if (Keyboard.pressed(KEY_BOOT)) {
			if (gameState != GameState.BOOT) {
				actionHandler.reboot();
			}
		} else if (Keyboard.pressed(KEY_DEBUG_INFO)) {
			Ufx.toggle(PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(KEY_FULLSCREEN)) {
			actionHandler.setFullScreen(true);
		} else if (Keyboard.pressed(KEY_IMMUNITY)) {
			actionHandler.toggleImmunity();
		} else if (Keyboard.pressed(KEY_SHOW_HELP)) {
			showHelpMenu();
		} else if (Keyboard.pressed(KEY_PAUSE)) {
			actionHandler.togglePaused();
		} else if (Keyboard.pressed(KEYS_SINGLE_STEP)) {
			actionHandler.oneSimulationStep();
		} else if (Keyboard.pressed(KEY_TEN_STEPS)) {
			actionHandler.tenSimulationSteps();
		} else if (Keyboard.pressed(KEY_SIMULATION_FASTER)) {
			actionHandler.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(KEY_SIMULATION_SLOWER)) {
			actionHandler.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(KEY_SIMULATION_NORMAL)) {
			actionHandler.resetSimulationSpeed();
		} else if (Keyboard.pressed(KEY_QUIT)) {
			if (gameState != GameState.BOOT && gameState != GameState.INTRO) {
				actionHandler.restartIntro();
			}
		} else if (Keyboard.pressed(KEY_TEST_LEVELS)) {
			actionHandler.startLevelTestMode();
		} else {
			sceneContext.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
		}
	}

	protected String tt(String key, Object... args) {
		var text = ResourceManager.message(messageBundles, key, args);
		return text != null ? text : "<" + key + ">";
	}


	// Menu stuff

	private class Menu {
		private final List<Node> column0 = new ArrayList<>();
		private final List<Node> column1 = new ArrayList<>();
		private final Font font;

		public Menu(Font font) {
			checkNotNull(font);
			this.font = font;
		}

		public void addRow(Node node0, Node node1) {
			column0.add(node0);
			column1.add(node1);
		}

		public int size() {
			return column0.size();
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
			if (sceneContext.gameController().isAutoControlled()) {
				var text = text(tt("help.autopilot_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex);
				++rowIndex;
			}
			if (sceneContext.gameController().isImmune()) {
				var text = text(tt("help.immunity_on"), Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex);
				++rowIndex;
			}

			var pane = new BorderPane(grid);
			pane.setPadding(new Insets(10));
			var bgColor = sceneContext.gameVariant() == GameVariant.MS_PACMAN
					? Color.rgb(255, 0, 0, 0.8)
					: Color.rgb(33, 33, 255, 0.8);
			pane.setBackground(ResourceManager.coloredRoundedBackground(bgColor, 10));
			return pane;
		}
	}

	protected void showHelpMenu() {
		currentHelpMenu().ifPresent(menu -> {
			helpMenu.setTranslateX(10 * scaling);
			helpMenu.setTranslateY(30 * scaling);
			helpMenu.setContent(menu.createPane());
			helpMenu.show(MENU_FADING_DELAY);
		});
	}

	private Font createMenuFont() {
		return sceneContext.theme().font("font.monospaced", Math.max(6, 14 * scaling));
	}

	private Optional<Menu> currentHelpMenu() {
		var gameState = sceneContext.gameState();
		if (gameState == GameState.INTRO) {
			return Optional.of(createIntroMenu());
		}
		if (gameState == GameState.CREDIT) {
			return Optional.of(createCreditMenu());
		}
		if (sceneContext.gameLevel().isPresent()
				&& oneOf(gameState, GameState.READY, GameState.HUNTING, GameState.PACMAN_DYING, GameState.GHOST_DYING)) {
			return sceneContext.gameLevel().get().isDemoLevel()
					? Optional.of(createDemoLevelMenu())
					: Optional.of(createPlayingMenu());
		}
		return Optional.empty();
	}

	private Menu createIntroMenu() {
		var menu = new Menu(createMenuFont());
		if (sceneContext.gameController().hasCredit()) {
			menu.addEntry("help.start_game", "1");
		}
		menu.addEntry("help.add_credit", "5");
		menu.addEntry(sceneContext.gameVariant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman", "V");
		return menu;
	}

	private Menu createCreditMenu() {
		var menu = new Menu(createMenuFont());
		if (sceneContext.gameController().hasCredit()) {
			menu.addEntry("help.start_game", "1");
		}
		menu.addEntry("help.add_credit", "5");
		menu.addEntry("help.show_intro", "Q");
		return menu;
	}

	private Menu createPlayingMenu() {
		var menu = new Menu(createMenuFont());
		menu.addEntry("help.move_left",  tt("help.cursor_left"));
		menu.addEntry("help.move_right", tt("help.cursor_right"));
		menu.addEntry("help.move_up",    tt("help.cursor_up"));
		menu.addEntry("help.move_down",  tt("help.cursor_down"));
		menu.addEntry("help.show_intro", "Q");
		return menu;
	}

	private Menu createDemoLevelMenu() {
		var menu = new Menu(createMenuFont());
		menu.addEntry("help.add_credit", "5");
		menu.addEntry("help.show_intro", "Q");
		return menu;
	}
}
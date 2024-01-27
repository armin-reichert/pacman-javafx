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
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.oneOf;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage extends CanvasContainer implements Page {

	protected static final Duration MENU_FADING_DELAY = Duration.seconds(1.5);

	protected final GameSceneContext sceneContext;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final Pane popupLayer = new Pane();
	protected final FadingPane helpMenu = new FadingPane();
	protected final HelpButton helpButton = new HelpButton();
	protected final Signature signature = new Signature("Remake (2023) by ", "Armin Reichert");

	public GamePage(GameSceneContext sceneContext, double width, double height) {
		this.sceneContext = sceneContext;
		helpButton.setOnMouseClicked(e -> {
			e.consume();
			showHelpMenu();
		});
		popupLayer.getChildren().addAll(helpButton, signature.root(), helpMenu);
		layers.getChildren().addAll(popupLayer, flashMessageView);
		layers.setOnKeyPressed(this::handleKeyPressed);
		PY_SHOW_DEBUG_INFO.addListener((py, ov, nv) -> showDebugBorders(nv));
		setSize(width, height);
	}

	protected void updateHelpButton(double newScaling) {
		String key = sceneContext.gameVariant() == GameVariant.MS_PACMAN 
			? "mspacman.helpButton.icon" 
			: "pacman.helpButton.icon";
		helpButton.setImage(sceneContext.theme().image(key), Math.ceil(10 * newScaling));
		helpButton.setTranslateX(popupLayer.getWidth() - 20 * newScaling);
		helpButton.setTranslateY(8 * newScaling);
		helpButton.setVisible(sceneContext.currentGameScene().isPresent()
			&& sceneContext.currentGameScene().get() != sceneContext.sceneConfig().get("boot"));
	}

	protected void scalePage(double newScaling, boolean always) {
		//TODO move into hook method?
		updateSignatureSizeAndPosition();
		sceneContext.currentGameScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.setScaling(newScaling);
			}
		});
		updateHelpButton(newScaling); // TODO doesn't work correctly
		super.scalePage(newScaling, always);
		setSizes(popupLayer, canvasContainer.getWidth(), canvasContainer.getHeight());
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

		showDebugBorders(PY_SHOW_DEBUG_INFO.get());
	}

	protected void showDebugBorders(boolean on)  {
		if (on) {
			layers.setBorder(ResourceManager.border(Color.RED, 3));
			canvasLayer.setBorder(ResourceManager.border(Color.YELLOW, 3));
			popupLayer.setBorder(ResourceManager.border(Color.GREENYELLOW, 3));
		} else {
			layers.setBorder(null);
			canvasLayer.setBorder(null);
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
		var text = ResourceManager.message(sceneContext.messageBundles(), key, args);
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
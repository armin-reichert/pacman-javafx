/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.shell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneManager;
import de.amr.games.pacman.ui.fx.shell.info.Dashboard;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * JavaFX UI for Pac-Man / Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. The intro scenes and intermission scenes are all 2D.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventAdapter {

	private static final Logger logger = LogManager.getFormatterLogger();

	private static final Image APP_ICON = Ufx.image("icons/pacman.png");

	public final GameLoop gameLoop = new GameLoop(60);

	private final GameController gameController;
	private final KeyboardPacSteering pacController;
	private final Stage stage;
	private final Scene mainScene;
	private final SceneManager sceneManager;
	private final StackPane sceneRoot = new StackPane();
	private final StackPane gameScenePlaceholder = new StackPane();
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final PiPView pipView = new PiPView();

	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage stage, double width, double height) {
		GameEvents.addEventListener(this);
		Actions.init(gameController, this);
		this.gameController = gameController;
		this.stage = stage;
		this.pacController = new KeyboardPacSteering(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
		gameController.setPacSteering(pacController);

		mainScene = new Scene(sceneRoot, width, height);
		logger.info("Main scene created. Size: %.0f x %.0f", mainScene.getWidth(), mainScene.getHeight());

		sceneManager = new SceneManager(gameController);
		sceneManager.allGameScenes()
				.forEach(gameScene -> gameScene.setResizeBehavior(mainScene.widthProperty(), mainScene.heightProperty()));

		createLayout();
		initKeyboardHandling();
		updateCurrentGameScene(true);

		stage.setOnCloseRequest(e -> gameLoop.stop());
		stage.setScene(mainScene);
		stage.setMinHeight(328);
		stage.setMinWidth(241);
		stage.getIcons().add(APP_ICON);
		stage.centerOnScreen();
		stage.show();
	}

	public void startGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.updateAndRender();
		});
		gameLoop.setRenderTask(this::render);
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
		gameLoop.start();
	}

	private void createLayout() {
		dashboard.build(this, gameController);
		pipView.sceneHeightPy.bind(Env.pipSceneHeightPy);
		pipView.visibleProperty().bind(Env.pipVisiblePy);
		pipView.opacityProperty().bind(Env.pipOpacityPy);
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView));
		sceneRoot.getChildren().addAll(gameScenePlaceholder, overlayPane, flashMessageView);
		updateBackground();
		Env.drawMode3DPy.addListener((x, y, z) -> updateBackground());
		Env.bgColorPy.addListener((x, y, z) -> updateBackground());
	}

	private void initKeyboardHandling() {
		mainScene.setOnKeyPressed(Keyboard::processEvent);
		Keyboard.addHandler(this::onKeyPressed);
		Keyboard.addHandler(pacController::onKeyPressed);
		Keyboard.addHandler(() -> currentGameScene.onKeyPressed());
	}

	private void updateBackground() {
		var mode = Env.drawMode3DPy.get();
		var bgColor = Env.bgColorPy.get();
		gameScenePlaceholder.setBackground(Ufx.colorBackground(mode == DrawMode.FILL ? bgColor : Color.BLACK));
	}

	public SceneManager getSceneManager() {
		return sceneManager;
	}

	public Stage getStage() {
		return stage;
	}

	public Scene getMainScene() {
		return mainScene;
	}

	public FlashMessageView getFlashMessageView() {
		return flashMessageView;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public PiPView getPipView() {
		return pipView;
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	@Override
	public void onGameEvent(GameEvent event) {
		GameEventAdapter.super.onGameEvent(event);
		// game scenes are not directly registered as game event handlers
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateCurrentGameScene(false);
	}

	@Override
	public void onUIForceUpdate(GameEvent e) {
		updateCurrentGameScene(true);
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		flashMessageView.update();
		dashboard.update();
		pipView.drawContent(currentGameScene instanceof PlayScene2D || currentGameScene instanceof PlayScene3D);
	}

	private void updateCurrentGameScene(boolean forcedUpdate) {
		int dimension = Env.use3DScenePy.get() ? SceneManager.SCENE_3D : SceneManager.SCENE_2D;
		GameScene newGameScene = sceneManager.findGameScene(dimension).orElse(null);
		if (newGameScene == null) {
			throw new IllegalStateException("No game scene found. Game state: %s".formatted(gameController.state()));
		}
		if (newGameScene == currentGameScene && !forcedUpdate) {
			return; // keep game scene
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		sceneManager.updateSceneContext(newGameScene);
		newGameScene.init();
		newGameScene.resize(mainScene.getHeight());
		gameScenePlaceholder.getChildren().setAll(newGameScene.getFXSubScene());
		logger.info("Current scene changed from %s to %s", currentGameScene, newGameScene);
		currentGameScene = newGameScene;

		// picture-in-picture view
		sceneManager.updateSceneContext(pipView.getPlayScene2D());
		pipView.getPlayScene2D().init();

		// does not really belong here, but...
		stage.setTitle(gameController.game().variant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
	}

	void on2D3DChange() {
		updateCurrentGameScene(true);
		if (currentGameScene instanceof PlayScene2D playScene2D) {
			playScene2D.onSwitchFrom3D();
		} else if (currentGameScene instanceof PlayScene3D playScene3D) {
			playScene3D.onSwitchFrom2D();
		}
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Keyboard.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.D)) {
			Env.toggle(Env.debugUIPy);
		} else if (Keyboard.pressed(Keyboard.CTRL, KeyCode.I)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.I)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.M)) {
			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(KeyCode.P)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Keyboard.SHIFT, KeyCode.P)) {
			Actions.singleStep();
		} else if (Keyboard.pressed(KeyCode.Q)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.DIGIT3)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(KeyCode.F1)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(KeyCode.F2)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
	}
}
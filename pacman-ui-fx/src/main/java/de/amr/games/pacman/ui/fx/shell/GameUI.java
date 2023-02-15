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
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.sound.common.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * JavaFX UI for Pac-Man and Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. All others scenes are 2D only.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventListener {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final GameRenderer RENDERER_MS_PACMAN = new MsPacManGameRenderer();
	private static final GameRenderer RENDERER_PACMAN = new PacManGameRenderer();

	private static GameRenderer renderer(GameModel game) {
		return switch (game.variant()) {
		case MS_PACMAN -> RENDERER_MS_PACMAN;
		case PACMAN -> RENDERER_PACMAN;
		default -> throw new IllegalStateException();
		};
	}

	public class Simulation extends GameLoop {

		public Simulation(int fps) {
			super(fps);
		}

		@Override
		public void doUpdate() {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		}

		@Override
		public void doRender() {
			gameView.flashMessageView().update();
			gameView.dashboard().update();
			gameView.updatePipView(currentGameScene);
		}
	}

	private final Settings settings;
	private final Simulation simulation = new Simulation(GameModel.FPS);
	private final GameController gameController;
	private final GameView gameView;
	private final SoundHandler soundHandler = new SoundHandler();

	private GameScene currentGameScene;

	public GameUI(Stage primaryStage, Settings settings) {
		this.settings = settings;
		gameController = new GameController(settings.variant);

		gameView = new GameView(primaryStage, ArcadeWorld.SIZE_PX.x(), ArcadeWorld.SIZE_PX.y(), settings.zoom,
				settings.fullScreen);

		// keyboard steering of Pac-Man
		var defaultPacSteering = new KeyboardSteering(Keys.PAC_UP, Keys.PAC_DOWN, Keys.PAC_LEFT, Keys.PAC_RIGHT);
		gameController.setManualPacSteering(defaultPacSteering);

		gameView.scene().heightProperty()
				.addListener((heightPy, oldHeight, newHeight) -> currentGameScene.resizeToHeight(newHeight.floatValue()));
		gameView.scene().addEventHandler(KeyEvent.KEY_PRESSED, defaultPacSteering::onKeyPressed);
		gameView.scene().setOnKeyPressed(e -> {
			if (Keyboard.accept(e))
				onKeyPressed();
		});

		initEnv();

		LOG.info("Created game UI, Application settings: %s", settings);
	}

	private void initEnv() {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> gameView.update(gameController.game().variant()));
		Env.ThreeD.drawModePy.addListener((py, oldVal, newVal) -> gameView.update(gameController.game().variant()));
		Env.ThreeD.enabledPy.set(settings.use3D);
		Env.ThreeD.perspectivePy.set(settings.perspective);

		Env.PiP.sceneHeightPy
				.addListener((py, oldVal, newVal) -> gameView.pipPlayScene().resizeToHeight(newVal.doubleValue()));
		gameView.pipPlayScene().fxSubScene().opacityProperty().bind(Env.PiP.opacityPy);

		Env.Simulation.pausedPy.addListener((py, oldVal, newVal) -> gameView.update(gameController.game().variant()));
		simulation.pausedPy.bind(Env.Simulation.pausedPy);
		simulation.targetFrameratePy.bind(Env.Simulation.targetFrameratePy);
		simulation.measuredPy.bind(Env.Simulation.timeMeasuredPy);
	}

	public void start() {
		if (simulation.isRunning()) {
			LOG.info("Game has already been started");
			return;
		}
		GameEvents.addListener(this);
		Actions.setUI(this);
		gameView.dashboard().init(this);
		gameController.boot(); // after booting, current game scene is initialized
		gameView.show();

		Ufx.afterSeconds(1.0, Actions::playHelpVoiceMessage).play();

		simulation.start();
		LOG.info("Game started. Game loop target frame rate: %d", simulation.targetFrameratePy.get());
		LOG.info("Window size: %.0f x %.0f, 3D: %s, perspective: %s".formatted(gameView.stage().getWidth(),
				gameView.stage().getHeight(), U.onOff(Env.ThreeD.enabledPy.get()), Env.ThreeD.perspectivePy.get()));
	}

	public void stop() {
		simulation.stop();
		LOG.info("Game stopped");
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean reload) {
		var use3D = Env.ThreeD.enabledPy.get();
		var variants = GameSceneManager.getSceneVariantsMatchingGameState(gameController);
		var nextGameScene = (use3D && variants.scene3D() != null) ? variants.scene3D() : variants.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		gameView.update(gameController.game().variant());
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		nextGameScene.setContext(new GameSceneContext(gameController, renderer(gameController.game())));
		nextGameScene.init();
		currentGameScene = nextGameScene;
		gameView.embedGameScene(currentGameScene);
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Keys.AUTOPILOT)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keys.BOOT)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Keys.DEBUG_INFO)) {
			Ufx.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Keys.IMMUNITIY)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Keys.MUTE)) {
//			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(Keys.PAUSE)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Keys.PAUSE_STEP) || Keyboard.pressed(Keys.SINGLE_STEP)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Keys.TEN_STEPS)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(Keys.QUIT)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Keys.TEST_LEVELS)) {
			Actions.toggleLevelTestMode();
		} else if (Keyboard.pressed(Keys.USE_3D)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(Keys.DASHBOARD)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Keys.PIP_VIEW)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(Keys.FULLSCREEN)) {
			gameView.stage().setFullScreen(true);
		}
		currentGameScene.onKeyPressed();
	}

	@Override
	public void onGameEvent(GameEvent event) {
		LOG.trace("Event received: %s", event);
		// call event specific handler
		GameEventListener.super.onGameEvent(event);
		if (currentGameScene != null) {
			currentGameScene.onGameEvent(event);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene(false);
	}

	@Override
	public void onUnspecifiedChange(GameEvent e) {
		updateGameScene(true);
	}

	@Override
	public void onLevelStarting(GameEvent e) {
		gameController.game().level().ifPresent(level -> {
			var r = currentGameScene.context().r2D();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			level.world().addAnimation(ArcadeWorld.FLASHING, r.createMazeFlashingAnimation());
			LOG.trace("Created level animations for level #%d", level.number());
		});
		updateGameScene(true);
	}

	@Override
	public void onSoundEvent(SoundEvent event) {
		soundHandler.onSoundEvent(event);
	}

	public GameController gameController() {
		return gameController;
	}

	public GameView gameView() {
		return gameView;
	}

	public Simulation simulation() {
		return simulation;
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}
}
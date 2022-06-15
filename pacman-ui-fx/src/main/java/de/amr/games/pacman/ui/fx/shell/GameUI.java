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

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_CreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_CreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.shell.info.Dashboard;
import de.amr.games.pacman.ui.fx.sound.MsPacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private final GameScene[][] scenes_PacMan = {
		//@formatter:off
		{ new PacMan_IntroScene(),         null },
		{ new PacMan_CreditScene(),        null },
		{ new PacMan_IntermissionScene1(), null },
		{ new PacMan_IntermissionScene2(), null },
		{ new PacMan_IntermissionScene3(), null },
		{ new PlayScene2D(),               new PlayScene3D() },
		//@formatter:on
	};

	private final GameScene[][] scenes_MsPacMan = {
		//@formatter:off
		{ new MsPacMan_IntroScene(),         null },
		{ new MsPacMan_CreditScene(),        null },
		{ new MsPacMan_IntermissionScene1(), null },
		{ new MsPacMan_IntermissionScene2(), null },
		{ new MsPacMan_IntermissionScene3(), null },
		{ new PlayScene2D(),                 new PlayScene3D() },
		//@formatter:on
	};

	private final GameController gameController;
	private final Stage stage;
	private final Scene scene;
	private final StackPane sceneRoot;
	private final Dashboard dashboard;
	private final FlashMessageView flashMessageView;

	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage stage, double width, double height) {
		this.gameController = gameController;
		this.stage = stage;

		GameEvents.addEventListener(this);

		// UI has 3 layers. From bottom to top: game scene, dashboard, flash message view.
		flashMessageView = new FlashMessageView();
		dashboard = new Dashboard(this, gameController);
		sceneRoot = new StackPane(new Region() /* placeholder for game scene */, dashboard, flashMessageView);
		Env.$drawMode3D.addListener((x, y, z) -> sceneRoot.setBackground(computeMainSceneBackground()));

		scene = new Scene(sceneRoot, width, height);
		allGameScenes().forEach(gameScene -> gameScene.setParent(scene));
		log("Main scene created. Size: %.0f x %.0f", scene.getWidth(), scene.getHeight());

		var pacSteering = new KeyboardPacSteering(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
		gameController.setPacSteering(pacSteering);
		gameController.game(GameVariant.MS_PACMAN).setSounds(new MsPacManGameSounds());
		gameController.game(GameVariant.PACMAN).setSounds(new PacManGameSounds());

		// Keyboard input handling
		scene.setOnKeyPressed(Keyboard::processEvent);
		Keyboard.addHandler(this::onKeyPressed);
		Keyboard.addHandler(() -> pacSteering.onKeyPressed());
		Keyboard.addHandler(() -> currentGameScene.onKeyPressed());

		updateCurrentGameScene(gameController.state(), true);

		stage.setMinHeight(328);
		stage.setMinWidth(241);
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> GameLoop.get().stop());
		stage.setTitle(gameController.game().variant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
		stage.setScene(scene);
		stage.centerOnScreen();
		stage.show();
	}

	private Stream<GameScene> allGameScenes() {
		return Stream.concat(Stream.of(scenes_MsPacMan), Stream.of(scenes_PacMan)).flatMap(Stream::of)
				.filter(Objects::nonNull);
	}

	public double getWidth() {
		return stage.getWidth();
	}

	public double getHeight() {
		return stage.getHeight();
	}

	public double getMainSceneWidth() {
		return scene.getWidth();
	}

	public double getMainSceneHeight() {
		return scene.getHeight();
	}

	public FlashMessageView getFlashMessageView() {
		return flashMessageView;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	/**
	 * Returns the game scene that fits the current game state.
	 *
	 * @param game      the game model (Pac-Man or Ms. Pac-Man)
	 * @param gameState the current game state
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public GameScene findGameScene(GameModel game, GameState gameState, int dimension) {
		var scenes = switch (game.variant) {
		case MS_PACMAN -> scenes_MsPacMan;
		case PACMAN -> scenes_PacMan;
		};
		var sceneIndex = switch (gameState) {
		case INTRO -> 0;
		case CREDIT -> 1;
		case INTERMISSION -> 1 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 1 + game.intermissionTestNumber;
		default -> 5;
		};
		var gameScene = scenes[sceneIndex][dimension];
		return gameScene != null ? gameScene : scenes[sceneIndex][SCENE_2D]; // use 2D as default
	}

	/**
	 * Called on every tick (if simulation is not paused). Game scene is updated *and* rendered such that when simulation
	 * is paused it gets redrawn nevertheless
	 */
	public void update() {
		gameController.update();
		currentGameScene.update();
	}

	@Override
	public void onGameEvent(GameEvent event) {
		GameEventAdapter.super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateCurrentGameScene(e.newGameState, false);
	}

	@Override
	public void onUIForceUpdate(GameEvent e) {
		updateCurrentGameScene(gameController.state(), true);
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		flashMessageView.update();
		dashboard.update();
	}

	public void setFullScreen(boolean fullscreen) {
		stage.setFullScreen(fullscreen);
	}

	public void toggle3D() {
		Env.toggle(Env.$3D);
		var game = gameController.game();
		var state = gameController.state();
		if (findGameScene(game, state, SCENE_2D) != findGameScene(game, state, SCENE_3D)) {
			updateCurrentGameScene(state, true);
			if (currentGameScene instanceof PlayScene2D) {
				((PlayScene2D) currentGameScene).onSwitchFrom3D();
			} else if (currentGameScene instanceof PlayScene3D) {
				((PlayScene3D) currentGameScene).onSwitchFrom2D();
			}
		}
	}

	private void updateCurrentGameScene(GameState gameState, boolean forcedSceneUpdate) {
		var dim = Env.$3D.get() ? SCENE_3D : SCENE_2D;
		var newGameScene = findGameScene(gameController.game(), gameController.state(), dim);
		if (newGameScene == null) {
			throw new IllegalStateException("No fitting game scene found for game state " + gameState);
		}
		if (newGameScene == currentGameScene && !forcedSceneUpdate) {
			return;
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		// just here for simplicity
		stage.setTitle(gameController.game().variant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
		log("Current scene changed from %s to %s", currentGameScene, newGameScene);
		currentGameScene = newGameScene;
		embedGameScene(currentGameScene, sceneRoot);
		currentGameScene.setSceneContext(gameController);
		currentGameScene.init();
	}

	private void embedGameScene(GameScene gameScene, StackPane parent) {
		parent.getChildren().set(0, gameScene.getFXSubScene());
		gameScene.resize(parent.getHeight());
		parent.setBackground(computeMainSceneBackground());
	}

	private Background computeMainSceneBackground() {
		if (!currentGameScene.is3D()) {
			return U.colorBackground(Color.CORNFLOWERBLUE);
		}
		return Env.$drawMode3D.get() == DrawMode.LINE ? U.colorBackground(Color.BLACK) : Wallpapers.get().random();
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Keyboard.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.D)) {
			Env.toggle(Env.$debugUI);
		} else if (Keyboard.pressed(Keyboard.CTRL, KeyCode.I)) {
			Actions.toggleInfoPanelsVisible();
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
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
	}
}
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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
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
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.shell.info.InfoLayer;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
public class GameUI extends GameEventAdapter {

	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private final GameController gameController;
	private final Stage stage;
	private final StackPane sceneRoot;
	private final InfoLayer infoLayer;
	private GameScene currentGameScene;

	private final GameScene[][] scenes_MrPacMan = {
		//@formatter:off
		{ new PacMan_IntroScene(), null },
		{ new PacMan_CreditScene(), null },
		{ new PacMan_IntermissionScene1(), null },
		{ new PacMan_IntermissionScene2(), null },
		{ new PacMan_IntermissionScene3(), null },
		{ new PlayScene2D(), new PlayScene3D() },
		//@formatter:on
	};

	private final GameScene[][] scenes_MsPacMan = {
		//@formatter:off
		{ new MsPacMan_IntroScene(), null },
		{ new MsPacMan_CreditScene(), null },
		{ new MsPacMan_IntermissionScene1(), null },
		{ new MsPacMan_IntermissionScene2(), null },
		{ new MsPacMan_IntermissionScene3(), null },
		{ new PlayScene2D(), new PlayScene3D() },
		//@formatter:on
	};

	public GameUI(GameController gameController, Stage stage, double width, double height) {
		this.gameController = gameController;
		this.stage = stage;
		this.infoLayer = new InfoLayer(this, gameController);

		GlobalActions.gameController = gameController;
		GlobalActions.ui = this;

		// first child is placeholder for subscene assigned to current game scene
		sceneRoot = new StackPane(new Region(), FlashMessageView.get(), infoLayer);

		var mainScene = new Scene(sceneRoot, width, height);
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(this::handleMouseClicked);
		mainScene.setOnMouseMoved(this::handleMouseMoved);
		log("Main scene created. Size: %.0f x %.0f", mainScene.getWidth(), mainScene.getHeight());

		Env.$drawMode3D.addListener((x, y, z) -> updateBackground(currentGameScene));

		defineResizingBehavior(mainScene, scenes_MsPacMan);
		defineResizingBehavior(mainScene, scenes_MrPacMan);
		updateGameScene(gameController.state(), true);

		stage.setScene(mainScene);
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> GameLoop.get().stop());
		stage.centerOnScreen();
		stage.show();
	}

	public void setFullScreen(boolean fullscreen) {
		stage.setFullScreen(fullscreen);
	}

	public double getWidth() {
		return stage.getWidth();
	}

	public double getHeight() {
		return stage.getHeight();
	}

	public double getMainSceneWidth() {
		return stage.getScene().getWidth();
	}

	public double getMainSceneHeight() {
		return stage.getScene().getHeight();
	}

	public InfoLayer getInfoLayer() {
		return infoLayer;
	}

	/**
	 * Defines the resizing behavior of the game scenes. 2D scenes adapt to the parent scene height keeping their aspect
	 * ratio. 3D scenes adapt to the parent scene size.
	 * 
	 * @param parent parent scene (main scene)
	 * @param scenes game scenes
	 */
	public void defineResizingBehavior(Scene parent, GameScene[][] scenes) {
		for (int sceneIndex = 0; sceneIndex < scenes.length; ++sceneIndex) {
			var scene2D = scenes[sceneIndex][SCENE_2D];
			parent.heightProperty().addListener(($height, oldHeight, newHeight) -> scene2D.resize(newHeight.doubleValue()));
			var scene3D = scenes[sceneIndex][SCENE_3D];
			if (scene3D != null) {
				scene3D.getFXSubScene().widthProperty().bind(parent.widthProperty());
				scene3D.getFXSubScene().heightProperty().bind(parent.heightProperty());
			}
		}
	}

	/**
	 * Returns the scene that fits the current game state.
	 *
	 * @param game      the game model (Pac-Man or Ms. Pac-Man)
	 * @param gameState the current game state
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public GameScene getFittingScene(GameModel game, GameState gameState, int dimension) {
		var scenes = switch (game.variant) {
		case MS_PACMAN -> scenes_MsPacMan;
		case PACMAN -> scenes_MrPacMan;
		};
		var sceneIndex = switch (gameState) {
		case INTRO -> 0;
		case CREDIT -> 1;
		case INTERMISSION -> 1 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 1 + game.intermissionTestNumber;
		default -> 5;
		};
		if (scenes[sceneIndex][dimension] == null) { // no 3D version exists, use 2D
			return scenes[sceneIndex][SCENE_2D];
		}
		return scenes[sceneIndex][dimension];
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	/**
	 * Called on every tick (if simulation is not paused).
	 */
	public void update() {
		gameController.update();
		// game scene is updated *and* rendered such that when simulation is paused it gets redrawn nevertheless
		currentGameScene.update();
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		FlashMessageView.get().update();
		infoLayer.update();
		stage.setTitle(gameController.game().variant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onUIForceUpdate(GameEvent e) {
		updateGameScene(gameController.state(), true);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene(e.newGameState, false);
	}

	private int selectedDimension() {
		return Env.$3D.get() ? SCENE_3D : SCENE_2D;
	}

	void updateGameScene(GameState gameState, boolean forced) {
		var fittingGameScene = getFittingScene(gameController.game(), gameController.state(), selectedDimension());
		if (fittingGameScene == null) {
			throw new IllegalStateException("No scene found for game state " + gameState);
		}
		if (fittingGameScene != currentGameScene || forced) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			fittingGameScene.resize(sceneRoot.getHeight());
			updateBackground(fittingGameScene);
			sceneRoot.getChildren().set(0, fittingGameScene.getFXSubScene());
			fittingGameScene.setSceneContext(gameController);
			fittingGameScene.init();
			log("Current scene changed from %s to %s", currentGameScene, fittingGameScene);
			currentGameScene = fittingGameScene;
		}
	}

	private void updateBackground(GameScene gameScene) {
		var bg = gameScene.is3D() //
				? Env.$drawMode3D.get() == DrawMode.LINE ? U.colorBackground(Color.BLACK) : Wallpapers.get().random()
				: U.colorBackground(Color.CORNFLOWERBLUE);
		sceneRoot.setBackground(bg);
	}

	private void handleMouseClicked(MouseEvent e) {
		currentGameScene.getFXSubScene().requestFocus();
	}

	private void handleKeyPressed(KeyEvent e) {
		// ALT + key
		if (Key.pressed(e, Key.ALT, KeyCode.A)) {
			GlobalActions.toggleAutopilot();
		} else if (Key.pressed(e, Key.ALT, KeyCode.E)) {
			gameController.cheatEatAllPellets();
		} else if (Key.pressed(e, Key.ALT, KeyCode.D)) {
			Env.toggle(Env.$debugUI);
		} else if (Key.pressed(e, Key.ALT, KeyCode.I)) {
			GlobalActions.toggleImmunity();
		} else if (Key.pressed(e, Key.ALT, KeyCode.L)) {
			GlobalActions.addLives(3);
		} else if (Key.pressed(e, Key.ALT, KeyCode.M)) {
			GlobalActions.toggleSoundMuted();
		} else if (Key.pressed(e, Key.ALT, KeyCode.N)) {
			gameController.cheatEnterNextLevel();
		} else if (Key.pressed(e, Key.ALT, KeyCode.X)) {
			gameController.cheatKillAllEatableGhosts();
		} else if (Key.pressed(e, Key.ALT, KeyCode.Z)) {
			GlobalActions.startIntermissionScenesTest();
		} else if (Key.pressed(e, Key.ALT, KeyCode.LEFT)) {
			GlobalActions.changePerspective(-1);
		} else if (Key.pressed(e, Key.ALT, KeyCode.RIGHT)) {
			GlobalActions.changePerspective(+1);
		} else if (Key.pressed(e, Key.ALT, KeyCode.DIGIT3)) {
			GlobalActions.toggleUse3DScene();
		}

		else if (Key.pressed(e, Key.CTRL, KeyCode.I)) {
			GlobalActions.toggleInfoPanelsVisible();
		}

		else if (Key.pressed(e, KeyCode.Q)) {
			GlobalActions.quitCurrentScene();
		} else if (Key.pressed(e, KeyCode.V)) {
			GlobalActions.selectNextGameVariant();
		} else if (Key.pressed(e, KeyCode.F11)) {
			stage.setFullScreen(true);
		}
		currentGameScene.onKeyPressed(e.getCode());
	}

	// Begin test area ---

	private String lastPicked = "";

	private void handleMouseMoved(MouseEvent e) {
		identifyNode(e.getPickResult().getIntersectedNode());
	}

	private void identifyNode(Node node) {
		if (node != null) {
			String s = String.format("%s", node);
			Object info = node.getUserData();
			if (info instanceof Pac3D) {
				Pac3D pac3D = (Pac3D) info;
				s = pac3D.identifyNode(node);
			} else if (info instanceof Ghost3D) {
				Ghost3D ghost3D = (Ghost3D) info;
				s = ghost3D.identifyNode(node);
			}
			if (!lastPicked.equals(s)) {
				log(s);
				lastPicked = s;
			}
		}
	}

	// End test area ---

}
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
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.controller.common.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.common.event.GameEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.shell.info.InfoLayer;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Node;
import javafx.scene.Scene;
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
public class GameUI extends DefaultGameEventHandler {

	public static final V2i GAME_SIZE = new V2i(28 * TS, 36 * TS);
	public static final int MIN_FRAMERATE = 5, MAX_FRAMERATE = 120;

	public final GameController gc;
	public final Stage stage;

	private final StackPane sceneRoot;
	private final GameScenes gameScenes;
	private final InfoLayer infoLayer;
	private GameScene currentGameScene;

	public GameUI(GameController gc, Stage stage, double width, double height) {
		this.gc = gc;
		this.stage = stage;
		this.infoLayer = new InfoLayer(this);

		gc.games().forEach(game -> game.addEventListener(this));

		// first child is placeholder for subscene assigned to current game scene
		sceneRoot = new StackPane(new Region(), FlashMessageView.get(), infoLayer);
		Env.$drawMode3D.addListener(($drawMode, _old, _new) -> updateBackground(currentGameScene));

		var scene = new Scene(sceneRoot, width, height);
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.setOnMouseClicked(this::handleMouseClicked);
		scene.setOnMouseMoved(this::handleMouseMoved);

		gameScenes = new GameScenes(scene, gc, GAME_SIZE);
		selectGameScene();

		stage.setScene(scene);
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> GameLoop.get().stop());
		stage.centerOnScreen();
		stage.show();
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	/**
	 * Called on every tick (if simulation is not paused).
	 */
	public void update() {
		gc.update();
		// game scene is updated *and* rendered such that when simulation is paused it gets redrawn nevertheless
		currentGameScene.update();
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		FlashMessageView.get().update();
		infoLayer.update();
		stage.setTitle(gc.gameVariant() == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		selectGameScene();
		currentGameScene.onGameEvent(event);
	}

	private void selectGameScene() {
		var newGameScene = gameScenes.getScene(Env.$3D.get() ? GameScenes.SCENE_3D : GameScenes.SCENE_2D);
		if (newGameScene != currentGameScene) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			newGameScene.resize(sceneRoot.getHeight());
			updateBackground(newGameScene);
			sceneRoot.getChildren().set(0, newGameScene.getFXSubScene());
			newGameScene.setContext();
			newGameScene.init();
			log("Game scene is now '%s'", newGameScene.getClass());
			currentGameScene = newGameScene;
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

	@SuppressWarnings("incomplete-switch")
	private void handleKeyPressed(KeyEvent e) {
		if (e.isAltDown()) {
			switch (e.getCode()) {
			case A -> toggleAutopilot();
			case E -> gc.game().cheatEatAllPellets();
			case I -> toggleImmunity();
			case L -> addLives(3);
			case M -> toggleSoundMuted();
			case N -> enterNextLevel();
			case Q -> quitCurrentGameScene();
			case V -> toggleGameVariant();
			case X -> gc.cheatKillAllPossibleGhosts();
			case Z -> startIntermissionScenesTest();
			case LEFT -> changePerspective(-1);
			case RIGHT -> changePerspective(1);
			case DIGIT3 -> toggleUse3DScene();
			case DIGIT5 -> addCredit();
			}
		}

		else if (e.isControlDown()) {
			switch (e.getCode()) {
			case I -> toggleInfoPanelsVisible();
			}
		}

		else {
			switch (e.getCode()) {
			case SPACE -> gc.requestGame();
			case F11 -> stage.setFullScreen(true);
			}
		}
	}

	public void addCredit() {
		gc.credit++;
		SoundManager.get().play(GameSound.CREDIT);
	}

	public void changePerspective(int delta) {
		if (currentGameScene.is3D()) {
			Env.$perspective.set(Env.perspectiveShifted(delta));
			String perspectiveName = Env.message(Env.$perspective.get().name());
			showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
		}
	}

	public void quitCurrentGameScene() {
		currentGameScene.end();
		SoundManager.get().stopAll();
		gc.reset(GameState.INTRO);
	}

	public void enterNextLevel() {
		if (gc.game().running) {
			gc.changeState(GameState.LEVEL_COMPLETE);
		}
	}

	public void enterLevel(int levelNumber) {
		if (gc.game().levelNumber == levelNumber) {
			return;
		}
		SoundManager.get().stopAll();
		if (levelNumber == 1) {
			gc.game().reset();
			gc.changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > gc.game().levelNumber ? gc.game().levelNumber + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				gc.game().setLevel(n);
			}
			gc.changeState(GameState.LEVEL_STARTING);
		}
	}

	public void addLives(int lives) {
		if (gc.game().running) {
			gc.game().player.lives += lives;
			showFlashMessage(1, "You have %d lives", gc.game().player.lives);
		}
	}

	public void startIntermissionScenesTest() {
		if (gc.state() == GameState.INTRO) {
			gc.startIntermissionTest();
		}
	}

	public void toggleInfoPanelsVisible() {
		Env.toggle(infoLayer.visibleProperty());
	}

	public void togglePaused() {
		Env.toggle(Env.$paused);
		showFlashMessage(1, Env.$paused.get() ? "Paused" : "Resumed");
		log(Env.$paused.get() ? "Simulation paused." : "Simulation resumed.");
	}

	public void toggleGameVariant() {
		if (!gc.game().running) {
			gc.selectGameVariant(gc.gameVariant().succ());
		}
	}

	public void toggleAutopilot() {
		gc.game().player.autoMoving = !gc.game().player.autoMoving;
		String message = Env.message(gc.game().player.autoMoving ? "autopilot_on" : "autopilot_off");
		showFlashMessage(1, message);
	}

	public void toggleImmunity() {
		gc.game().player.immune = !gc.game().player.immune;
		String message = Env.message(gc.game().player.immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(1, message);
	}

	public void toggleUse3DScene() {
		Env.toggle(Env.$3D);
		if (gameScenes.getScene(GameScenes.SCENE_2D) != gameScenes.getScene(GameScenes.SCENE_3D)) {
			selectGameScene();
			if (currentGameScene instanceof PlayScene2D) {
				((PlayScene2D) currentGameScene).onSwitchFrom3DScene();
			} else if (currentGameScene instanceof PlayScene3D) {
				((PlayScene3D) currentGameScene).onSwitchFrom2DScene();
			}
		}
		showFlashMessage(1, Env.message(Env.$3D.get() ? "use_3D_scene" : "use_2D_scene"));
	}

	public void toggleDrawMode() {
		Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public void toggleSoundMuted() {
		if (SoundManager.get().isMuted()) {
			if (!gc.game().attractMode) {
				SoundManager.get().setMuted(false);
			}
		} else {
			SoundManager.get().setMuted(true);
		}
	}
}
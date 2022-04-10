/*
MIT License

Copyright (c) 2021 Armin Reichert

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
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;

import java.util.Random;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.shell.info.InfoLayer;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class GameUI extends DefaultGameEventHandler {

	public static final int MIN_FRAMERATE = 10, MAX_FRAMERATE = 120;

	public final GameController gc;
	public final Stage stage;

	private final StackPane mainSceneRoot;
	private final GameScenes gameScenes;
	private final InfoLayer infoLayer;
	private final Background[] wallpapers = { //
			U.imageBackground("/common/wallpapers/beach.jpg"), //
			U.imageBackground("/common/wallpapers/space.jpg"), //
			U.imageBackground("/common/wallpapers/easter_island.jpg"), //
	};
	private int wallpaperIndex;
	private GameScene currentGameScene;

	public GameUI(GameController gc, Stage stage, double width, double height) {
		this.gc = gc;
		this.stage = stage;
		this.infoLayer = new InfoLayer(this);

		// first child will be updated by subscene assigned to current game scene
		mainSceneRoot = new StackPane(new Region(), FlashMessageView.get(), infoLayer);
		var mainScene = new Scene(mainSceneRoot, width, height);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameLoop.get().stop());
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setScene(mainScene);

		gc.addGameEventListener(this);
		Env.$drawMode3D.addListener(
				($drawMode, oldDrawMode, newDrawMode) -> mainSceneRoot.setBackground(getBackground(currentGameScene)));

		SoundManager.get().selectGameVariant(gc.gameVariant);
		gameScenes = new GameScenes(mainScene, gc, GianmarcosModel3D.get());
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public void show(boolean fullscreen) {
		selectGameScene();
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.show();
	}

	/**
	 * Called on every tick (if simulation is not paused).
	 */
	public void update() {
		gc.updateState();
		// game scene is updated *and* rendered such that when simulation is paused it gets redrawn nevertheless
		currentGameScene.update();
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		FlashMessageView.get().update();
		infoLayer.update();
		stage.setTitle(gc.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
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
			switch (gc.gameVariant) {
			case MS_PACMAN -> newGameScene.setContext(gc.game, Rendering2D_MsPacMan.get());
			case PACMAN -> newGameScene.setContext(gc.game, Rendering2D_PacMan.get());
			default -> throw new IllegalStateException();
			}
			newGameScene.resizeFXSubScene(stage.getScene().getHeight());
			mainSceneRoot.setBackground(getBackground(newGameScene));
			mainSceneRoot.getChildren().set(0, newGameScene.getFXSubScene());
			SoundManager.get().stopAll();
			SoundManager.get().selectGameVariant(gc.gameVariant);
			currentGameScene = newGameScene;
			currentGameScene.init();
			log("Game scene is now '%s'", currentGameScene.getClass());
		}
	}

	public Background nextWallpaper() {
		int currentWallpaperIndex = wallpaperIndex;
		do {
			wallpaperIndex = new Random().nextInt(wallpapers.length);
		} while (wallpaperIndex == currentWallpaperIndex);
		return wallpapers[wallpaperIndex];
	}

	private Background getBackground(GameScene gameScene) {
		return gameScene.is3D() ? Env.$drawMode3D.get() == DrawMode.LINE ? U.colorBackground(Color.BLACK) : nextWallpaper()
				: U.colorBackground(Color.CORNFLOWERBLUE);
	}

	@SuppressWarnings("incomplete-switch")
	private void handleKeyPressed(KeyEvent e) {
		boolean shift = e.isShiftDown();

		if (e.isAltDown()) {
			switch (e.getCode()) {
			case A -> toggleAutopilot();
			case E -> gc.cheatEatAllPellets();
			case I -> toggleImmunity();
			case L -> addLives(3);
			case N -> enterNextLevel();
			case Q -> quitCurrentGameScene();
			case S -> changeTargetFramerate(shift ? -10 : 10);
			case V -> toggleGameVariant();
			case X -> gc.cheatKillGhosts();
			case Z -> startIntermissionScenesTest();
			case LEFT -> changePerspective(-1);
			case RIGHT -> changePerspective(1);
			case DIGIT3 -> toggleUse3DScene();
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

	public void changePerspective(int delta) {
		if (currentGameScene.is3D()) {
			Env.$perspective.set(Env.perspectiveShifted(delta));
			String perspectiveName = Env.message(Env.$perspective.get().name());
			showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
		}
	}

	public void changeTargetFramerate(int delta) {
		int newFrameRate = U.clamp(GameLoop.get().getTargetFrameRate() + delta, MIN_FRAMERATE, MAX_FRAMERATE);
		GameLoop.get().setTargetFrameRate(newFrameRate);
		showFlashMessage(1, "Target FPS set to %d Hz", GameLoop.get().getTargetFrameRate());
	}

	public void quitCurrentGameScene() {
		SoundManager.get().stopAll();
		currentGameScene.end();
		gc.changeState(GameState.INTRO);
	}

	public void enterNextLevel() {
		if (gc.gameRunning) {
			gc.changeState(GameState.LEVEL_COMPLETE);
			showFlashMessage(1, Env.CHEAT_TALK.next());
		}
	}

	public void enterLevel(int levelNumber) {
		if (gc.game.levelNumber == levelNumber) {
			return;
		}
		SoundManager.get().stopAll();
		if (levelNumber == 1) {
			gc.game.reset();
			gc.changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > gc.game.levelNumber ? gc.game.levelNumber + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				gc.game.setLevel(n);
			}
			gc.changeState(GameState.LEVEL_STARTING);
		}
	}

	public void addLives(int lives) {
		if (gc.gameRunning) {
			gc.game.player.lives += lives;
			showFlashMessage(1, "You have %d lives", gc.game.player.lives);
		}
	}

	public void startIntermissionScenesTest() {
		if (gc.state == GameState.INTRO) {
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
		if (!gc.gameRunning) {
			gc.selectGameVariant(gc.gameVariant.succ());
		}
	}

	public void toggleAutopilot() {
		gc.autoControlled = !gc.autoControlled;
		String message = Env.message(gc.autoControlled ? "autopilot_on" : "autopilot_off");
		showFlashMessage(1, message);
	}

	public void toggleImmunity() {
		gc.playerImmune = !gc.playerImmune;
		String message = Env.message(gc.playerImmune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(1, message);
	}

	public void toggleUse3DScene() {
		Env.toggle(Env.$3D);
		if (gameScenes.getScene(GameScenes.SCENE_2D) != gameScenes.getScene(GameScenes.SCENE_3D)) {
			SoundManager.get().stopAll();
			selectGameScene();
			if (currentGameScene instanceof PlayScene2D) {
				((PlayScene2D) currentGameScene).onSwitchBetween2DAnd3D();
			}
		}
		showFlashMessage(1, Env.message(Env.$3D.get() ? "use_3D_scene" : "use_2D_scene"));
	}

	public void toggleDrawMode() {
		Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}
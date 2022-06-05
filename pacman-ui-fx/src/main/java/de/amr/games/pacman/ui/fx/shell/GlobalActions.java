/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class GlobalActions {

	static GameController gameController;
	static GameUI ui;

	public static void quitCurrentScene() {
		ui.getCurrentGameScene().end();
		SoundManager.get().stopAll();
		gameController.returnToIntro();
	}

	public static void changePerspective(int delta) {
		if (ui.getCurrentGameScene().is3D()) {
			Env.$perspective.set(Env.perspectiveShifted(delta));
			String perspectiveName = Env.message(Env.$perspective.get().name());
			showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
		}
	}

	public static void addLives(int lives) {
		if (gameController.isGameRunning()) {
			gameController.game().lives += lives;
			showFlashMessage(1, "You have %d lives", gameController.game().lives);
		}
	}

	public static void enterLevel(int levelNumber) {
		if (gameController.state() == GameState.LEVEL_STARTING) {
			return;
		}
		if (gameController.game().level.number == levelNumber) {
			return;
		}
		SoundManager.get().stopAll();
		if (levelNumber == 1) {
			gameController.game().reset();
			gameController.changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > gameController.game().level.number ? gameController.game().level.number + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				gameController.game().setLevel(n);
			}
			gameController.changeState(GameState.LEVEL_STARTING);
		}
	}

	public static void startIntermissionScenesTest() {
		if (gameController.state() == GameState.INTRO) {
			gameController.startIntermissionTest();
		}
	}

	public static void toggleInfoPanelsVisible() {
		Env.toggle(ui.getInfoLayer().visibleProperty());
	}

	public static void togglePaused() {
		Env.toggle(Env.$paused);
		showFlashMessage(1, Env.$paused.get() ? "Paused" : "Resumed");
		log(Env.$paused.get() ? "Simulation paused." : "Simulation resumed.");
	}

	public static void selectNextGameVariant() {
		gameController.selectGameVariant(gameController.game().variant.succ());
	}

	public static void toggleAutopilot() {
		gameController.toggleAutoMoving();
		String message = Env.message(gameController.isAutoMoving() ? "autopilot_on" : "autopilot_off");
		showFlashMessage(1, message);
	}

	public static void toggleImmunity() {
		gameController.togglePlayerImmune();
		String message = Env.message(gameController.game().playerImmune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(1, message);
	}

	public static void toggleUse3DScene() {
		Env.toggle(Env.$3D);
		var game = gameController.game();
		var state = gameController.state();
		if (ui.getFittingScene(game, state, GameUI.SCENE_2D) != ui.getFittingScene(game, state, GameUI.SCENE_3D)) {
			ui.updateGameScene(gameController.state(), true);
			if (ui.getCurrentGameScene() instanceof PlayScene2D) {
				((PlayScene2D) ui.getCurrentGameScene()).onSwitchFrom3DScene();
			} else if (ui.getCurrentGameScene() instanceof PlayScene3D) {
				((PlayScene3D) ui.getCurrentGameScene()).onSwitchFrom2DScene();
			}
		}
		showFlashMessage(1, Env.message(Env.$3D.get() ? "use_3D_scene" : "use_2D_scene"));
	}

	public static void toggleDrawMode() {
		Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void toggleSoundMuted() {
		if (SoundManager.get().isMuted()) {
			if (gameController.credit() > 0) {
				SoundManager.get().setMuted(false);
			}
		} else {
			SoundManager.get().setMuted(true);
		}
	}
}
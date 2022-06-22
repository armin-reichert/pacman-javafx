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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameSounds;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.Talk;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class Actions {

	private Actions() {
	}

	private static GameController theGameController;
	private static GameUI theUI;

	public static void init(GameController gameController, GameUI ui) {
		theGameController = gameController;
		theUI = ui;
	}

	public static void showFlashMessage(String message, Object... args) {
		showFlashMessage(1, message, args);
	}

	public static void showFlashMessage(double seconds, String message, Object... args) {
		theUI.getFlashMessageView().showMessage(String.format(message, args), seconds);
	}

	public static void restartIntro() {
		theUI.getCurrentGameScene().end();
		theGameController.game().sounds().ifPresent(GameSounds::stopAll);
		theGameController.restartIntro();
	}

	public static void addLives(int lives) {
		if (theGameController.game().playing) {
			theGameController.game().lives += lives;
			showFlashMessage("You have %d lives", theGameController.game().lives);
		}
	}

	public static void enterLevel(int levelNumber) {
		if (theGameController.state() == GameState.LEVEL_STARTING) {
			return;
		}
		if (theGameController.game().level.number == levelNumber) {
			return;
		}
		theGameController.game().sounds().ifPresent(GameSounds::stopAll);
		if (levelNumber == 1) {
			theGameController.game().reset();
			theGameController.changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > theGameController.game().level.number ? theGameController.game().level.number + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				theGameController.game().setLevel(n);
			}
			theGameController.changeState(GameState.LEVEL_STARTING);
		}
	}

	public static void toggleInfoPanelsVisible() {
		Env.toggle(theUI.getDashboard().visibleProperty());
	}

	public static void togglePaused() {
		Env.toggle(Env.paused);
	}

	public static void singleStep() {
		if (Env.paused.get()) {
			GameLoop.get().runSingleStep(true);
		}
	}

	public static void selectNextGameVariant() {
		var gameVariant = theGameController.game().variant.next();
		theGameController.state().selectGameVariant(gameVariant);
	}

	public static void selectNextPerspective() {
		if (theUI.getCurrentGameScene().is3D()) {
			Env.perspective.set(Env.perspective.get().next());
			String perspectiveName = Talk.message(Env.perspective.get().name());
			showFlashMessage(Talk.message("camera_perspective", perspectiveName));
		}
	}

	public static void selectPrevPerspective() {
		if (theUI.getCurrentGameScene().is3D()) {
			Env.perspective.set(Env.perspective.get().prev());
			String perspectiveName = Talk.message(Env.perspective.get().name());
			showFlashMessage(Talk.message("camera_perspective", perspectiveName));
		}

	}

	public static void toggleAutopilot() {
		theGameController.game().autoControlled = !theGameController.game().autoControlled;
		String message = Talk.message(theGameController.game().autoControlled ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
	}

	public static void toggleImmunity() {
		theGameController.games().forEach(game -> game.isPacImmune = !game.isPacImmune);
		String message = Talk.message(theGameController.game().isPacImmune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
	}

	public static void toggleUse3DScene() {
		theUI.toggle3D();
		showFlashMessage(Talk.message(Env.use3D.get() ? "use_3D_scene" : "use_2D_scene"));
	}

	public static void toggleDrawMode() {
		Env.drawMode3D.set(Env.drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void toggleSoundMuted() {
		theGameController.game().sounds().ifPresent(snd -> {
			boolean muted = snd.isMuted();
			snd.setMuted(!muted);
			var msg = Talk.message(snd.isMuted() ? "sound_off" : "sound_on");
			showFlashMessage(msg);
		});
	}

	public static void cheatEatAllPellets() {
		theGameController.state().cheatEatAllPellets(theGameController.game());
		var cheatMessage = Talk.CHEAT_TALK.next();
		showFlashMessage(cheatMessage);
	}

	public static void cheatEnterNextLevel() {
		theGameController.state().cheatEnterNextLevel(theGameController.game());
	}

	public static void cheatKillAllEatableGhosts() {
		theGameController.state().cheatKillAllEatableGhosts(theGameController.game());
		var cheatMessage = Talk.CHEAT_TALK.next();
		showFlashMessage(cheatMessage);
	}
}
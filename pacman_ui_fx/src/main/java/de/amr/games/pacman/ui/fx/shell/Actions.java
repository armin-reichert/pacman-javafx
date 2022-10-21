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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.Resources;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.SceneManager;
import de.amr.games.pacman.ui.fx.texts.TextManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class Actions {

	private Actions() {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final String VM_HELP = "sound/common/press-key.mp3";
	private static final String VM_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	private static final String VM_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	private static final String VM_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	private static final String VM_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	private static GameUI theUI;
	private static AudioClip currentVoiceMessage;

	private static GameController theGameController() {
		return theUI.getGameController();
	}

	private static GameModel theGame() {
		return theGameController().game();
	}

	public static void setUI(GameUI ui) {
		theUI = ui;
	}

	public static void playVoiceMessage(String messageFileRelPath) {
		if (Env.SOUND_DISABLED) {
			return;
		}
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return;
		}
		var url = Resources.urlFromRelPath(messageFileRelPath);
		if (url != null) {
			try {
				currentVoiceMessage = new AudioClip(url.toExternalForm());
				currentVoiceMessage.play();
			} catch (Exception e) {
				LOGGER.error("Could not play voice message '%s'", messageFileRelPath);
			}
		}
	}

	public static void playHelpMessageAfterSeconds(double seconds) {
		Ufx.pauseSec(seconds, Actions::playHelpVoiceMessage).play();
	}

	public static void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public static void playHelpVoiceMessage() {
		playVoiceMessage(VM_HELP);
	}

	public static void showFlashMessage(String message, Object... args) {
		showFlashMessage(1, message, args);
	}

	public static void showFlashMessage(double seconds, String message, Object... args) {
		theUI.getFlashMessageView().showMessage(String.format(message, args), seconds);
	}

	public static void startGame() {
		if (theGame().hasCredit()) {
			stopVoiceMessage();
			theGameController().state().requestGame(theGame());
		}
	}

	public static void startCutscenesTest() {
		theGameController().state().startCutscenesTest(theGame());
		showFlashMessage("Cut scenes");
	}

	public static void restartIntro() {
		theUI.getCurrentGameScene().end();
		theGameController().sounds().stopAll();
		theGameController().restartIntro();
	}

	public static void reboot() {
		theUI.getCurrentGameScene().end();
		theGameController().sounds().stopAll();
		theGameController().reboot();
	}

	public static void addCredit() {
		theGameController().state().addCredit(theGame());
	}

	public static void cheatAddLives(int lives) {
		if (theGame().playing) {
			theGame().lives += lives;
			showFlashMessage("You have %d lives", theGame().lives);
		}
	}

	public static void enterLevel(int levelNumber) {
		if (theGameController().state() == GameState.LEVEL_STARTING) {
			return;
		}
		if (theGame().level.number() == levelNumber) {
			return;
		}
		theGameController().sounds().stopAll();
		if (levelNumber == 1) {
			theGame().reset();
			theGame().setLevel(levelNumber);
			theGameController().changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > theGame().level.number() ? theGame().level.number() + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				theGame().setLevel(n);
			}
			theGameController().changeState(GameState.LEVEL_STARTING);
		}
	}

	public static void togglePipViewVisible() {
		Env.toggle(Env.pipEnabledPy);
		if (Env.pipEnabledPy.get()) {
			showFlashMessage("Picture in Picture ON");
		} else {
			showFlashMessage("Picture in Picture OFF");
		}
	}

	public static void toggleDashboardVisible() {
		Env.toggle(theUI.getDashboard().visibleProperty());
	}

	public static void togglePaused() {
		Env.toggle(Env.pausedPy);
		theGameController().sounds().setMuted(Env.pausedPy.get());
	}

	public static void oneSimulationStep() {
		if (Env.pausedPy.get()) {
			theUI.getGameLoop().step(true);
		}
	}

	public static void tenSimulationSteps() {
		if (Env.pausedPy.get()) {
			theUI.getGameLoop().nsteps(10, true);
		}
	}

	public static void selectNextGameVariant() {
		var gameVariant = theGame().variant.next();
		theGameController().state().selectGameVariant(gameVariant);
	}

	public static void selectNextPerspective() {
		if (theUI.getCurrentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().next());
			String perspectiveName = TextManager.message(Env.perspectivePy.get().name());
			showFlashMessage(TextManager.message("camera_perspective", perspectiveName));
		}
	}

	public static void selectPrevPerspective() {
		if (theUI.getCurrentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().prev());
			String perspectiveName = TextManager.message(Env.perspectivePy.get().name());
			showFlashMessage(TextManager.message("camera_perspective", perspectiveName));
		}

	}

	public static void toggleAutopilot() {
		theGame().isPacAutoControlled = !theGame().isPacAutoControlled;
		var on = theGame().isPacAutoControlled;
		String message = TextManager.message(on ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(on ? VM_AUTOPILOT_ON : VM_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		theGameController().games().forEach(game -> game.isPacImmune = !game.isPacImmune);
		var on = theGame().isPacImmune;
		String message = TextManager.message(on ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(on ? VM_IMMUNITY_ON : VM_IMMUNITY_OFF);
	}

	public static void toggleUse3DScene() {
		Env.toggle(Env.use3DScenePy);
		if (SceneManager.hasDifferentScenesFor2DAnd3D(theGameController())) {
			theUI.updateGameScene(true);
			if (theUI.getCurrentGameScene() instanceof PlayScene2D playScene2D) {
				playScene2D.onSwitchFrom3D();
			} else if (theUI.getCurrentGameScene() instanceof PlayScene3D playScene3D) {
				playScene3D.onSwitchFrom2D();
			}
		} else {
			showFlashMessage(TextManager.message(Env.use3DScenePy.get() ? "use_3D_scene" : "use_2D_scene"));
		}
	}

	public static void toggleDrawMode() {
		Env.drawModePy.set(Env.drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void toggleSoundMuted() {
		var snd = theGameController().sounds();
		boolean wasMuted = snd.isMuted();
		snd.setMuted(!wasMuted);
		var msg = TextManager.message(snd.isMuted() ? "sound_off" : "sound_on");
		showFlashMessage(msg);
	}

	public static void cheatEatAllPellets() {
		theGameController().state().cheatEatAllPellets(theGame());
		var cheatMessage = TextManager.TALK_CHEATING.next();
		showFlashMessage(cheatMessage);
	}

	public static void cheatEnterNextLevel() {
		theGameController().state().cheatEnterNextLevel(theGame());
	}

	public static void cheatKillAllEatableGhosts() {
		theGameController().state().cheatKillAllEatableGhosts(theGame());
		var cheatMessage = TextManager.TALK_CHEATING.next();
		showFlashMessage(cheatMessage);
	}
}
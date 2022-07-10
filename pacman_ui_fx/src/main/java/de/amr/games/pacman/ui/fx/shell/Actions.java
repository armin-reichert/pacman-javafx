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
import de.amr.games.pacman.controller.common.GameSoundController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.ui.fx.Resources;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.texts.Texts;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Actions {

	private Actions() {
	}

	private static final Logger logger = LogManager.getFormatterLogger();

	private static final String VM_HELP = "sound/common/press-key.mp3";
	private static final String VM_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	private static final String VM_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	private static final String VM_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	private static final String VM_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	private static GameController theGameController;
	private static GameUI theUI;
	private static AudioClip currentVoiceMessage;

	public static void init(GameController gameController, GameUI ui) {
		theGameController = gameController;
		theUI = ui;
	}

	public static void playVoiceMessage(String messageFileRelPath) {
		if (GameSounds.SOUND_DISABLED) {
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
				logger.error("Could not play voice message '%s'", messageFileRelPath);
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
		if (theGameController.game().hasCredit()) {
			stopVoiceMessage();
			theGameController.state().requestGame(theGameController.game());
		}
	}

	public static void restartIntro() {
		theUI.getSceneManager().getCurrentGameScene().end();
		theGameController.sounds().ifPresent(GameSoundController::stopAll);
		theGameController.restartIntro();
		var hint = new PauseTransition(Duration.seconds(3));
		hint.setOnFinished(e -> playVoiceMessage(VM_HELP));
		hint.play();
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
		theGameController.sounds().ifPresent(GameSoundController::stopAll);
		if (levelNumber == 1) {
			theGameController.game().reset();
			theGameController.game().setLevel(levelNumber);
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

	public static void togglePipViewVisible() {
		Env.toggle(Env.pipVisiblePy);
	}

	public static void toggleDashboardVisible() {
		Env.toggle(theUI.getDashboard().visibleProperty());
	}

	public static void togglePaused() {
		Env.toggle(Env.pausedPy);
		theGameController.sounds().ifPresent(snd -> snd.setSilent(Env.pausedPy.get()));
	}

	public static void singleStep() {
		if (Env.pausedPy.get()) {
			theUI.getGameLoop().makeOneStep(true);
		}
	}

	public static void selectNextGameVariant() {
		var gameVariant = theGameController.game().variant.next();
		theGameController.state().selectGameVariant(gameVariant);
	}

	public static void selectNextPerspective() {
		if (theUI.getSceneManager().getCurrentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().next());
			String perspectiveName = Texts.message(Env.perspectivePy.get().name());
			showFlashMessage(Texts.message("camera_perspective", perspectiveName));
		}
	}

	public static void selectPrevPerspective() {
		if (theUI.getSceneManager().getCurrentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().prev());
			String perspectiveName = Texts.message(Env.perspectivePy.get().name());
			showFlashMessage(Texts.message("camera_perspective", perspectiveName));
		}

	}

	public static void toggleAutopilot() {
		theGameController.game().autoControlled = !theGameController.game().autoControlled;
		var on = theGameController.game().autoControlled;
		String message = Texts.message(on ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(on ? VM_AUTOPILOT_ON : VM_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		theGameController.games().forEach(game -> game.isPacImmune = !game.isPacImmune);
		var on = theGameController.game().isPacImmune;
		String message = Texts.message(on ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(on ? VM_IMMUNITY_ON : VM_IMMUNITY_OFF);
	}

	public static void toggleUse3DScene() {
		Env.toggle(Env.use3DScenePy);
		if (theUI.getSceneManager().hasScenesForBothDimensions()) {
			theUI.on2D3DChange();
		} else {
			showFlashMessage(Texts.message(Env.use3DScenePy.get() ? "use_3D_scene" : "use_2D_scene"));
		}
	}

	public static void toggleDrawMode() {
		Env.drawMode3DPy.set(Env.drawMode3DPy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void toggleSoundMuted() {
		theGameController.sounds().ifPresent(snd -> {
			boolean muted = snd.isMuted();
			snd.setMuted(!muted);
			var msg = Texts.message(snd.isMuted() ? "sound_off" : "sound_on");
			showFlashMessage(msg);
		});
	}

	public static void cheatEatAllPellets() {
		theGameController.state().cheatEatAllPellets(theGameController.game());
		var cheatMessage = Texts.TALK_CHEATING.next();
		showFlashMessage(cheatMessage);
	}

	public static void cheatEnterNextLevel() {
		theGameController.state().cheatEnterNextLevel(theGameController.game());
	}

	public static void cheatKillAllEatableGhosts() {
		theGameController.state().cheatKillAllEatableGhosts(theGameController.game());
		var cheatMessage = Texts.TALK_CHEATING.next();
		showFlashMessage(cheatMessage);
	}
}
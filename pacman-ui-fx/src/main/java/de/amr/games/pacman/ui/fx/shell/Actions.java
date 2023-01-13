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

import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.util.TextManager;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class Actions {

	private Actions() {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final String VOICE_HELP = "sound/common/press-key.mp3";
	private static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	private static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	private static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	private static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	private static final Random RND = new Random();

	private static GameUI ui;
	private static AudioClip currentVoiceMessage;

	private static GameController gameController() {
		return ui.gameController();
	}

	private static GameModel game() {
		return gameController().game();
	}

	private static GameState currentGameState() {
		return gameController().state();
	}

	public static void init(GameUI userInterface) {
		ui = Objects.requireNonNull(userInterface, "User Interface for actions must not be null");
	}

	public static void playVoiceMessage(String messageFileRelPath) {
		if (Env.SOUND_DISABLED) {
			return;
		}
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return;
		}
		var url = Env.urlFromRelPath(messageFileRelPath);
		if (url != null) {
			try {
				currentVoiceMessage = new AudioClip(url.toExternalForm());
				currentVoiceMessage.play();
			} catch (Exception e) {
				LOGGER.error("Could not play voice message '%s'", messageFileRelPath);
			}
		}
	}

	public static void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public static void playHelpVoiceMessage() {
		playVoiceMessage(VOICE_HELP);
	}

	public static void showFlashMessage(String message, Object... args) {
		showFlashMessage(1, message, args);
	}

	public static void showFlashMessage(double seconds, String message, Object... args) {
		ui.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public static void startGame() {
		if (game().hasCredit()) {
			stopVoiceMessage();
			currentGameState().requestGame(game());
		}
	}

	public static void startCutscenesTest() {
		currentGameState().startCutscenesTest(game());
		showFlashMessage("Cut scenes");
	}

	public static void restartIntro() {
		ui.currentGameScene().end();
		gameController().startIntro();
	}

	public static void reboot() {
		ui.currentGameScene().end();
		gameController().boot();
	}

	public static void addCredit() {
		currentGameState().addCredit(game());
	}

	public static void enterLevel(int newLevelNumber) {
		if (currentGameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					game().nextLevel();
				}
				gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
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
		Env.toggle(ui.dashboard().visibleProperty());
	}

	public static void togglePaused() {
		Env.toggle(Env.pausedPy);
		gameController().sounds().setMuted(Env.pausedPy.get());
	}

	public static void oneSimulationStep() {
		if (Env.pausedPy.get()) {
			ui.gameLoop().step(true);
		}
	}

	public static void tenSimulationSteps() {
		if (Env.pausedPy.get()) {
			ui.gameLoop().nsteps(10, true);
		}
	}

	public static void selectNextGameVariant() {
		var gameVariant = game().variant().next();
		currentGameState().selectGameVariant(gameVariant);
	}

	public static void selectNextPerspective() {
		if (ui.currentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().next());
			String perspectiveName = TextManager.message(Env.perspectivePy.get().name());
			showFlashMessage(TextManager.message("camera_perspective", perspectiveName));
		}
	}

	public static void selectPrevPerspective() {
		if (ui.currentGameScene().is3D()) {
			Env.perspectivePy.set(Env.perspectivePy.get().prev());
			String perspectiveName = TextManager.message(Env.perspectivePy.get().name());
			showFlashMessage(TextManager.message("camera_perspective", perspectiveName));
		}

	}

	public static void toggleAutopilot() {
		gameController().toggleAutoControlled();
		var auto = gameController().isAutoControlled();
		String message = TextManager.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? VOICE_AUTOPILOT_ON : VOICE_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = TextManager.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? VOICE_IMMUNITY_ON : VOICE_IMMUNITY_OFF);
	}

	public static void toggleLevelTestModel() {
		gameController().levelTestMode = !gameController().levelTestMode;
		if (!gameController().levelTestMode) {
			gameController().boot();
		} else {
			showFlashMessage("Level TEST MODE");
		}
	}

	public static void toggleUse3DScene() {
		Env.toggle(Env.threeDScenesPy);
		if (ui.sceneManager().findGameScene(gameController(), 3).isPresent()) {
			ui.updateGameScene(true);
			if (ui.currentGameScene().is3D()) {
				ui.currentGameScene().onSwitchFrom2D();
			} else {
				ui.currentGameScene().onSwitchFrom3D();
			}
		} else {
			showFlashMessage(TextManager.message(Env.threeDScenesPy.get() ? "use_3D_scene" : "use_2D_scene"));
		}
	}

	public static void toggleDrawMode() {
		Env.drawModePy.set(Env.drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void toggleSoundMuted() {
		boolean muted = gameController().sounds().isMuted();
		gameController().sounds().setMuted(!muted);
		var msg = TextManager.message(gameController().sounds().isMuted() ? "sound_off" : "sound_on");
		showFlashMessage(msg);
	}

	public static void cheatAddLives(int numLives) {
		game().setLives(numLives + game().lives());
	}

	public static void cheatEatAllPellets() {
		currentGameState().cheatEatAllPellets(game());
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(TextManager.TALK_CHEATING.next());
		}
	}

	public static void cheatEnterNextLevel() {
		currentGameState().cheatEnterNextLevel(game());
	}

	public static void cheatKillAllEatableGhosts() {
		currentGameState().cheatKillAllEatableGhosts(game());
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(TextManager.TALK_CHEATING.next());
		}
	}
}
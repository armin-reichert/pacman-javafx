/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.controller.common.GameState.INTRO;

import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class Actions {

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static final Random RND = new Random();

	private static GameUI ui;
	private static AudioClip currentVoiceMessage;

	public static void setUI(GameUI theUI) {
		ui = Objects.requireNonNull(theUI, "User Interface for actions must not be null");
	}

	private static GameController gameController() {
		return ui.gameController();
	}

	private static GameModel game() {
		return gameController().game();
	}

	private static GameState gameState() {
		return gameController().state();
	}

	public static void playVoiceMessage(String messageFileRelPath) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return;
		}
		var url = ResourceMgr.urlFromRelPath(messageFileRelPath);
		if (url != null) {
			try {
				currentVoiceMessage = new AudioClip(url.toExternalForm());
				currentVoiceMessage.play();
			} catch (Exception e) {
				LOG.error("Could not play voice message '%s'", messageFileRelPath);
			}
		}
	}

	public static void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public static void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public static void showFlashMessageSeconds(double seconds, String message, Object... args) {
		ui.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public static void startGame() {
		if (game().hasCredit()) {
			stopVoiceMessage();
			gameController().startPlaying();
		}
	}

	public static void startCutscenesTest() {
		gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public static void restartIntro() {
		ui.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (game().isPlaying()) {
			game().changeCredit(-1);
		}
		gameController().restart(INTRO);
	}

	public static void reboot() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().end();
		}
		playHelpVoiceMessage(4);
		gameController().restart(GameState.BOOT);
	}

	public static void playHelpVoiceMessage(int delaySeconds) {
		Ufx.afterSeconds(delaySeconds, () -> playVoiceMessage(ResourceMgr.VOICE_HELP)).play();
	}

	public static void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		gameController().addCredit();
	}

	public static void enterLevel(int newLevelNumber) {
		if (gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
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
		Ufx.toggle(Env.pipVisiblePy);
		if (Env.pipVisiblePy.get()) {
			showFlashMessage("Picture in Picture ON");
		} else {
			showFlashMessage("Picture in Picture OFF");
		}
	}

	public static void toggleDashboardVisible() {
		Ufx.toggle(ui.dashboard().visibleProperty());
	}

	public static void togglePaused() {
		Ufx.toggle(Env.simulationPausedPy);
		// TODO mute and unmute?
		if (Env.simulationPausedPy.get()) {
			SoundHandler.sounds(game()).stopAll();
		}
	}

	public static void oneSimulationStep() {
		if (Env.simulationPausedPy.get()) {
			ui.simulation().executeSingleStep(true);
		}
	}

	public static void tenSimulationSteps() {
		if (Env.simulationPausedPy.get()) {
			ui.simulation().executeSteps(10, true);
		}
	}

	public static void selectNextGameVariant() {
		var gameVariant = game().variant().next();
		gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessage(4);
	}

	public static void selectNextPerspective() {
		if (ui.currentGameScene().is3D()) {
			var nextPerspective = Env.d3_perspectivePy.get().next();
			Env.d3_perspectivePy.set(nextPerspective);
			String perspectiveName = ResourceMgr.message(nextPerspective.name());
			showFlashMessage(ResourceMgr.message("camera_perspective", perspectiveName));
		}
	}

	public static void selectPrevPerspective() {
		if (ui.currentGameScene().is3D()) {
			var prevPerspective = Env.d3_perspectivePy.get().prev();
			Env.d3_perspectivePy.set(prevPerspective);
			String perspectiveName = ResourceMgr.message(prevPerspective.name());
			showFlashMessage(ResourceMgr.message("camera_perspective", perspectiveName));
		}
	}

	public static void toggleAutopilot() {
		gameController().toggleAutoControlled();
		var auto = gameController().isAutoControlled();
		String message = ResourceMgr.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? ResourceMgr.VOICE_AUTOPILOT_ON : ResourceMgr.VOICE_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = ResourceMgr.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? ResourceMgr.VOICE_IMMUNITY_ON : ResourceMgr.VOICE_IMMUNITY_OFF);
	}

	public static void startLevelTestMode() {
		if (gameState() == GameState.INTRO) {
			gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public static void toggleUse3DScene() {
		Ufx.toggle(Env.d3_enabledPy);
		if (ui.findGameScene(3).isPresent()) {
			ui.updateGameScene(true);
			ui.currentGameScene().onSceneVariantSwitch();
		} else {
			showFlashMessage(ResourceMgr.message(Env.d3_enabledPy.get() ? "use_3D_scene" : "use_2D_scene"));
		}
	}

	public static void toggleDrawMode() {
		Env.d3_drawModePy.set(Env.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public static void cheatAddLives(int numLives) {
		game().setLives(numLives + game().lives());
		showFlashMessage(ResourceMgr.message("cheat_add_lives", numLives));
	}

	public static void cheatEatAllPellets() {
		gameController().cheatEatAllPellets();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(ResourceMgr.getCheatingMessage());
		}
	}

	public static void cheatEnterNextLevel() {
		gameController().cheatEnterNextLevel();
	}

	public static void cheatKillAllEatableGhosts() {
		gameController().cheatKillAllEatableGhosts();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(ResourceMgr.getCheatingMessage());
		}
	}
}
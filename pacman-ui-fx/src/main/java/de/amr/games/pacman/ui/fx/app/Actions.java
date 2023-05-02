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

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.RND;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class Actions {

	private static ActionContext context;
	private static AudioClip currentVoiceMessage;

	public static void init(ActionContext context) {
		Actions.context = context;
	}

	public static void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> playVoiceMessage(AppRes.Sounds.VOICE_HELP)).play();
	}

	public static void playVoiceMessage(AudioClip voiceMessage) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return; // don't interrupt voice message still playing, maybe enqueue?
		}
		currentVoiceMessage = voiceMessage;
		currentVoiceMessage.play();
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
		context.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public static void startGame() {
		if (context.game().hasCredit()) {
			stopVoiceMessage();
			context.gameController().startPlaying();
		}
	}

	public static void startCutscenesTest() {
		context.gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public static void restartIntro() {
		context.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (context.game().isPlaying()) {
			context.game().changeCredit(-1);
		}
		context.gameController().restart(INTRO);
	}

	public static void reboot() {
		if (context.currentGameScene() != null) {
			context.currentGameScene().end();
		}
		playHelpVoiceMessageAfterSeconds(4);
		context.gameController().restart(GameState.BOOT);
	}

	public static void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		context.gameController().addCredit();
	}

	public static void enterLevel(int newLevelNumber) {
		if (context.gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		context.game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					context.game().nextLevel();
				}
				context.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	public static void togglePaused() {
		Ufx.toggle(Env.simulationPausedPy);
		// TODO mute and unmute?
		if (Env.simulationPausedPy.get()) {
			AppRes.Sounds.gameSounds(context.game().variant()).stopAll();
		}
	}

	public static void oneSimulationStep() {
		if (Env.simulationPausedPy.get()) {
			context.gameLoop().executeSingleStep(true);
		}
	}

	public static void tenSimulationSteps() {
		if (Env.simulationPausedPy.get()) {
			context.gameLoop().executeSteps(10, true);
		}
	}

	public static void changeSimulationSpeed(int delta) {
		int newFramerate = context.gameLoop().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			Env.simulationSpeedPy.set(newFramerate);
			showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public static void resetSimulationSpeed() {
		Env.simulationSpeedPy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, "%dHz".formatted(Env.simulationSpeedPy.get()));
	}

	public static void selectNextGameVariant() {
		var gameVariant = context.game().variant().next();
		context.gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessageAfterSeconds(4);
	}

	public static void toggleAutopilot() {
		context.gameController().toggleAutoControlled();
		var auto = context.gameController().isAutoControlled();
		String message = AppRes.Texts.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? AppRes.Sounds.VOICE_AUTOPILOT_ON : AppRes.Sounds.VOICE_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		context.game().setImmune(!context.game().isImmune());
		var immune = context.game().isImmune();
		String message = AppRes.Texts.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? AppRes.Sounds.VOICE_IMMUNITY_ON : AppRes.Sounds.VOICE_IMMUNITY_OFF);
	}

	public static void startLevelTestMode() {
		if (context.gameState() == GameState.INTRO) {
			context.gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public static void cheatAddLives(int numLives) {
		context.game().setLives(numLives + context.game().lives());
		showFlashMessage(AppRes.Texts.message("cheat_add_lives", context.game().lives()));
	}

	public static void cheatEatAllPellets() {
		context.gameController().cheatEatAllPellets();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(AppRes.Texts.pickCheatingMessage());
		}
	}

	public static void cheatEnterNextLevel() {
		context.gameController().cheatEnterNextLevel();
	}

	public static void cheatKillAllEatableGhosts() {
		context.gameController().cheatKillAllEatableGhosts();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(AppRes.Texts.pickCheatingMessage());
		}
	}
}
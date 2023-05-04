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

	private ActionContext context;
	private AudioClip currentVoiceMessage;

	public Actions() {
	}

	public void setContext(ActionContext context) {
		this.context = context;
	}

	public void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> playVoiceMessage(Game2d.Sounds.VOICE_HELP)).play();
	}

	public void playVoiceMessage(AudioClip voiceMessage) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return; // don't interrupt voice message still playing, maybe enqueue?
		}
		currentVoiceMessage = voiceMessage;
		currentVoiceMessage.play();
	}

	public void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		context.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public void startGame() {
		if (context.game().hasCredit()) {
			stopVoiceMessage();
			context.gameController().startPlaying();
		}
	}

	public void startCutscenesTest() {
		context.gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public void restartIntro() {
		context.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (context.game().isPlaying()) {
			context.game().changeCredit(-1);
		}
		context.gameController().restart(INTRO);
	}

	public void reboot() {
		if (context.currentGameScene() != null) {
			context.currentGameScene().end();
		}
		playHelpVoiceMessageAfterSeconds(4);
		context.gameController().restart(GameState.BOOT);
	}

	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		context.gameController().addCredit();
	}

	public void enterLevel(int newLevelNumber) {
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

	public void togglePaused() {
		Ufx.toggle(Game2d.simulationPausedPy);
		// TODO mute and unmute?
		if (Game2d.simulationPausedPy.get()) {
			Game2d.Sounds.gameSounds(context.game().variant()).stopAll();
		}
	}

	public void oneSimulationStep() {
		if (Game2d.simulationPausedPy.get()) {
			context.gameLoop().executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (Game2d.simulationPausedPy.get()) {
			context.gameLoop().executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = context.gameLoop().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			Game2d.simulationSpeedPy.set(newFramerate);
			showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		Game2d.simulationSpeedPy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, "%dHz".formatted(Game2d.simulationSpeedPy.get()));
	}

	public void selectNextGameVariant() {
		var gameVariant = context.game().variant().next();
		context.gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessageAfterSeconds(4);
	}

	public void toggleAutopilot() {
		context.gameController().toggleAutoControlled();
		var auto = context.gameController().isAutoControlled();
		String message = Game2d.Texts.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? Game2d.Sounds.VOICE_AUTOPILOT_ON : Game2d.Sounds.VOICE_AUTOPILOT_OFF);
	}

	public void toggleImmunity() {
		context.game().setImmune(!context.game().isImmune());
		var immune = context.game().isImmune();
		String message = Game2d.Texts.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? Game2d.Sounds.VOICE_IMMUNITY_ON : Game2d.Sounds.VOICE_IMMUNITY_OFF);
	}

	public void startLevelTestMode() {
		if (context.gameState() == GameState.INTRO) {
			context.gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives(int numLives) {
		context.game().setLives(numLives + context.game().lives());
		showFlashMessage(Game2d.Texts.message("cheat_add_lives", context.game().lives()));
	}

	public void cheatEatAllPellets() {
		context.gameController().cheatEatAllPellets();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(Game2d.Texts.pickCheatingMessage());
		}
	}

	public void cheatEnterNextLevel() {
		context.gameController().cheatEnterNextLevel();
	}

	public void cheatKillAllEatableGhosts() {
		context.gameController().cheatKillAllEatableGhosts();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(Game2d.Texts.pickCheatingMessage());
		}
	}
}
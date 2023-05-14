/*
MIT License

Copyright (c) 2023 Armin Reichert

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
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;
import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;
import static de.amr.games.pacman.ui.fx.util.Ufx.shift;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public class Game2dActions {

	public static final KeyCodeCombination CHEAT_EAT_ALL = alt(KeyCode.E);
	public static final KeyCodeCombination CHEAT_ADD_LIVES = alt(KeyCode.L);
	public static final KeyCodeCombination CHEAT_NEXT_LEVEL = alt(KeyCode.N);
	public static final KeyCodeCombination CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination AUTOPILOT = alt(KeyCode.A);
	public static final KeyCodeCombination DEBUG_INFO = alt(KeyCode.D);
	public static final KeyCodeCombination IMMUNITIY = alt(KeyCode.I);
	public static final KeyCodeCombination MUTE = alt(KeyCode.M);

	public static final KeyCodeCombination PAUSE = just(KeyCode.P);
	public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
	public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
	public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);
	public static final KeyCodeCombination SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);

	public static final KeyCodeCombination QUIT = just(KeyCode.Q);
	public static final KeyCodeCombination TEST_LEVELS = alt(KeyCode.T);
	public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);
	public static final KeyCodeCombination PLAY_CUTSCENES = alt(KeyCode.Z);

	public static final KeyCodeCombination SHOW_HELP = just(KeyCode.H);
	public static final KeyCodeCombination BOOT = just(KeyCode.F3);
	public static final KeyCodeCombination FULLSCREEN = just(KeyCode.F11);

	private Game2dUI ui;

	public void setUI(Game2dUI ui) {
		this.ui = ui;
	}

	public void stopVoiceMessage() {
		ui.stopVoice();
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		ui.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public void startGame() {
		if (ui.game().hasCredit()) {
			ui.stopVoice();
			ui.gameController().startPlaying();
		}
	}

	public void startCutscenesTest() {
		ui.gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public void restartIntro() {
		ui.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (ui.game().isPlaying()) {
			ui.game().changeCredit(-1);
		}
		ui.gameController().restart(INTRO);
	}

	public void reboot() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().end();
		}
		ui.playVoice(Game2d.assets.voiceExplainKeys, 4);
		ui.gameController().restart(GameState.BOOT);
	}

	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		ui.gameController().addCredit();
	}

	public void enterLevel(int newLevelNumber) {
		if (ui.gameController().state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		ui.game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					ui.game().nextLevel();
				}
				ui.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	public void togglePaused() {
		Ufx.toggle(ui.clock().pausedPy);
		// TODO mute and unmute?
		if (ui.clock().pausedPy.get()) {
			Game2d.assets.gameSounds(ui.game().variant()).stopAll();
		}
	}

	public void oneSimulationStep() {
		if (ui.clock().pausedPy.get()) {
			ui.clock().executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (ui.clock().pausedPy.get()) {
			ui.clock().executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = ui.clock().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			ui.clock().targetFrameratePy.set(newFramerate);
			showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		ui.clock().targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, "%dHz".formatted(ui.clock().targetFrameratePy.get()));
	}

	public void selectNextGameVariant() {
		var gameVariant = ui.game().variant().next();
		ui.gameController().selectGameVariant(gameVariant);
		ui.playVoice(Game2d.assets.voiceExplainKeys, 4);
	}

	public void toggleAutopilot() {
		ui.gameController().toggleAutoControlled();
		var auto = ui.gameController().isAutoControlled();
		String message = fmtMessage(Game2d.assets.messages, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		ui.updateHelpContent();
		ui.playVoice(auto ? Game2d.assets.voiceAutopilotOn : Game2d.assets.voiceAutopilotOff);
	}

	public void toggleImmunity() {
		ui.game().setImmune(!ui.game().isImmune());
		var immune = ui.game().isImmune();
		String message = fmtMessage(Game2d.assets.messages, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		ui.updateHelpContent();
		ui.playVoice(immune ? Game2d.assets.voiceImmunityOn : Game2d.assets.voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (ui.gameController().state() == GameState.INTRO) {
			ui.gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives(int n) {
		int newLivesCount = ui.game().lives() + n;
		ui.game().setLives(newLivesCount);
		showFlashMessage(fmtMessage(Game2d.assets.messages, "cheat_add_lives", newLivesCount));
	}

	public void cheatEatAllPellets() {
		ui.gameController().cheatEatAllPellets();
	}

	public void cheatEnterNextLevel() {
		ui.gameController().cheatEnterNextLevel();
	}

	public void cheatKillAllEatableGhosts() {
		ui.gameController().cheatKillAllEatableGhosts();
	}
}
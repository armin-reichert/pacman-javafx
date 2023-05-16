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
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;
import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;
import static de.amr.games.pacman.ui.fx.util.Ufx.shift;

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

/**
 * This is 2D-only version of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. The model layer consists
 * of the two game models <code> PacManGame</code> and <code> MsPacManGame</code>. The controller is a finite-state
 * machine which is triggered 60 times per second by the game loop. The user interface listens to game events sent from
 * the controller/model layer. The model and controller layers are decoupled from the user interface. This allow to
 * attach different user interfaces without having to change the controller or model.
 * 
 * <p>
 * As a proof of concept I implemented also a (simpler) Swing user interface, see repository
 * <a href="https://github.com/armin-reichert/pacman-ui-swing">Pac-Man Swing UI</a>.
 * 
 * @author Armin Reichert
 */
public class Game2d extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	//@formatter:off
	public static final BooleanProperty showDebugInfoPy   = new SimpleBooleanProperty(false);
	public static final IntegerProperty simulationStepsPy = new SimpleIntegerProperty(1);

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITIY         = alt(KeyCode.I);

	public static final KeyCodeCombination KEY_PAUSE             = just(KeyCode.P);
	public static final KeyCodeCombination KEY_PAUSE_STEP        = shift(KeyCode.P);
	public static final KeyCodeCombination KEY_SINGLE_STEP       = just(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_TEN_STEPS         = shift(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination KEY_START_GAME        = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination KEY_ADD_CREDIT        = just(KeyCode.DIGIT5);

	public static final KeyCodeCombination KEY_QUIT              = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_TEST_LEVELS       = alt(KeyCode.T);
	public static final KeyCodeCombination KEY_SELECT_VARIANT    = just(KeyCode.V);
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.Z);

	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);
	//@formatter:on

	public static Game2d app;;
	public static Game2dAssets assets;
	public static Game2dUI ui;

	private Settings cfg = new Settings();

	@Override
	public void init() {
		app = this;
		cfg.merge(getParameters().getNamed());
		Logger.info("Game configuration: {}", cfg);
		assets = new Game2dAssets();
	}

	@Override
	public void start(Stage stage) {
		stage.setFullScreen(cfg.fullScreen);
		ui = new Game2dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);

		ui.init(cfg);
		ui.startClockAndShowStage();
		reboot();

		Logger.info("Game started. {} Hz language={}", ui.clock().targetFrameratePy.get(), Locale.getDefault());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}

	// --- Actions

	public void startGame() {
		if (ui.game().hasCredit()) {
			ui.stopVoice();
			ui.gameController().startPlaying();
		}
	}

	public void startCutscenesTest() {
		ui.gameController().startCutscenesTest();
		ui.showFlashMessage("Cut scenes");
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
			ui.showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		ui.clock().targetFrameratePy.set(GameModel.FPS);
		ui.showFlashMessageSeconds(0.75, "%dHz".formatted(ui.clock().targetFrameratePy.get()));
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
		ui.showFlashMessage(message);
		ui.updateHelpContent();
		ui.playVoice(auto ? Game2d.assets.voiceAutopilotOn : Game2d.assets.voiceAutopilotOff);
	}

	public void toggleImmunity() {
		ui.game().setImmune(!ui.game().isImmune());
		var immune = ui.game().isImmune();
		String message = fmtMessage(Game2d.assets.messages, immune ? "player_immunity_on" : "player_immunity_off");
		ui.showFlashMessage(message);
		ui.updateHelpContent();
		ui.playVoice(immune ? Game2d.assets.voiceImmunityOn : Game2d.assets.voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (ui.gameController().state() == GameState.INTRO) {
			ui.gameController().restart(GameState.LEVEL_TEST);
			ui.showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives() {
		int newLivesCount = ui.game().lives() + 3;
		ui.game().setLives(newLivesCount);
		ui.showFlashMessage(fmtMessage(Game2d.assets.messages, "cheat_add_lives", newLivesCount));
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
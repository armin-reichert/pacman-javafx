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

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is 2D version of the Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private final Settings cfg = new Settings();

	@Override
	public void init() {
		cfg.merge(getParameters().getNamed());
		PacManGames2d.app = this;
		PacManGames2d.assets = new PacManGames2dAssets();
		Logger.info("Game initialized, configuration: {}", cfg);
	}

	@Override
	public void start(Stage stage) {
		var ui = new PacManGames2dUI(cfg.variant, stage, cfg.zoom * 28 * 8, cfg.zoom * 36 * 8);
		PacManGames2d.ui = ui;
		ui.init(cfg);
		stage.setFullScreen(cfg.fullScreen);
		ui.startClockAndShowStage();
		reboot();

		Logger.info("Game started. {} Hz language={}", PacManGames2d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames2d.ui.clock().stop();
		Logger.info("Game stopped.");
	}

	// --- Actions

	public void startGame() {
		if (PacManGames2d.ui.game().hasCredit()) {
			PacManGames2d.ui.stopVoice();
			PacManGames2d.ui.gameController().startPlaying();
		}
	}

	public void startCutscenesTest() {
		PacManGames2d.ui.gameController().startCutscenesTest();
		PacManGames2d.ui.showFlashMessage("Cut scenes");
	}

	public void restartIntro() {
		PacManGames2d.ui.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (PacManGames2d.ui.game().isPlaying()) {
			PacManGames2d.ui.game().changeCredit(-1);
		}
		PacManGames2d.ui.gameController().restart(INTRO);
	}

	public void reboot() {
		if (PacManGames2d.ui.currentGameScene() != null) {
			PacManGames2d.ui.currentGameScene().end();
		}
		PacManGames2d.ui.playVoice(PacManGames2d.assets.voiceExplainKeys, 4);
		PacManGames2d.ui.gameController().restart(GameState.BOOT);
	}

	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		PacManGames2d.ui.gameController().addCredit();
	}

	public void enterLevel(int newLevelNumber) {
		if (PacManGames2d.ui.gameController().state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		PacManGames2d.ui.game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					PacManGames2d.ui.game().nextLevel();
				}
				PacManGames2d.ui.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	public void togglePaused() {
		Ufx.toggle(PacManGames2d.ui.clock().pausedPy);
		// TODO mute and unmute?
		if (PacManGames2d.ui.clock().pausedPy.get()) {
			PacManGames2d.assets.gameSounds(PacManGames2d.ui.game().variant()).stopAll();
		}
	}

	public void oneSimulationStep() {
		if (PacManGames2d.ui.clock().pausedPy.get()) {
			PacManGames2d.ui.clock().executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (PacManGames2d.ui.clock().pausedPy.get()) {
			PacManGames2d.ui.clock().executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = PacManGames2d.ui.clock().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			PacManGames2d.ui.clock().targetFrameratePy.set(newFramerate);
			PacManGames2d.ui.showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		PacManGames2d.ui.clock().targetFrameratePy.set(GameModel.FPS);
		PacManGames2d.ui.showFlashMessageSeconds(0.75, "%dHz".formatted(PacManGames2d.ui.clock().targetFrameratePy.get()));
	}

	public void selectNextGameVariant() {
		var gameVariant = PacManGames2d.ui.game().variant().next();
		PacManGames2d.ui.gameController().selectGameVariant(gameVariant);
		PacManGames2d.ui.playVoice(PacManGames2d.assets.voiceExplainKeys, 4);
	}

	public void toggleAutopilot() {
		PacManGames2d.ui.gameController().toggleAutoControlled();
		var auto = PacManGames2d.ui.gameController().isAutoControlled();
		String message = fmtMessage(PacManGames2d.assets.messages, auto ? "autopilot_on" : "autopilot_off");
		PacManGames2d.ui.showFlashMessage(message);
		PacManGames2d.ui.playVoice(auto ? PacManGames2d.assets.voiceAutopilotOn : PacManGames2d.assets.voiceAutopilotOff);
	}

	public void toggleImmunity() {
		PacManGames2d.ui.game().setImmune(!PacManGames2d.ui.game().isImmune());
		var immune = PacManGames2d.ui.game().isImmune();
		String message = fmtMessage(PacManGames2d.assets.messages, immune ? "player_immunity_on" : "player_immunity_off");
		PacManGames2d.ui.showFlashMessage(message);
		PacManGames2d.ui.playVoice(immune ? PacManGames2d.assets.voiceImmunityOn : PacManGames2d.assets.voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (PacManGames2d.ui.gameController().state() == GameState.INTRO) {
			PacManGames2d.ui.gameController().restart(GameState.LEVEL_TEST);
			PacManGames2d.ui.showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives() {
		int newLivesCount = PacManGames2d.ui.game().lives() + 3;
		PacManGames2d.ui.game().setLives(newLivesCount);
		PacManGames2d.ui.showFlashMessage(fmtMessage(PacManGames2d.assets.messages, "cheat_add_lives", newLivesCount));
	}

	public void cheatEatAllPellets() {
		PacManGames2d.ui.gameController().cheatEatAllPellets();
	}

	public void cheatEnterNextLevel() {
		PacManGames2d.ui.gameController().cheatEnterNextLevel();
	}

	public void cheatKillAllEatableGhosts() {
		PacManGames2d.ui.gameController().cheatKillAllEatableGhosts();
	}
}
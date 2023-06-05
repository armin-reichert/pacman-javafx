/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public interface PacManGamesUserInterface {

	GameClock clock();

	GameController gameController();

	default GameModel game() {
		return gameController().game();
	}

	default GameVariant gameVariant() {
		return game().variant();
	}

	default GameState gameState() {
		return gameController().state();
	}

	GameScene currentGameScene();

	void init(Stage stage, Settings cfg, Theme theme);

	Theme theme();

	SpritesheetMsPacManGame spritesheetMsPacManGame();

	SpritesheetPacManGame spritesheetPacManGame();

	default Spritesheet spritesheet() {
		switch (gameVariant()) {
		case MS_PACMAN:
			return spritesheetMsPacManGame();
		case PACMAN:
			return spritesheetPacManGame();
		default:
			throw new IllegalGameVariantException(gameVariant());
		}
	}

	void show();

	void addCredit();

	void startGame();

	void restartIntro();

	void enterLevel(int intValue);

	void selectGameVariant(GameVariant variant);

	AudioClip audioClip(String clipName);

	void stopAllSounds();

	default void ensureLoop(AudioClip clip, int repetitions) {
		if (!clip.isPlaying()) {
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	default void ensureLoopEndless(AudioClip clip) {
		ensureLoop(clip, AudioClip.INDEFINITE);
	}

	void ensureSirenStarted(int sirenIndex);

	void playVoice(AudioClip clip, float delaySeconds);

	default void playVoice(AudioClip clip) {
		playVoice(clip, 0);
	}

	void stopVoice();

	void startCutscenesTest();

	void cheatAddLives();

	void cheatEatAllPellets();

	void cheatEnterNextLevel();

	void cheatKillAllEatableGhosts();

	void toggleAutopilot();

	void toggleImmunity();

	void toggleCanvasScaled();

	void togglePaused();
}
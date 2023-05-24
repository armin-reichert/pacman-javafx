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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.rendering2d.Theme;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.GameClock;
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

	void show();

	void addCredit();

	void startGame();

	void restartIntro();

	void enterLevel(int intValue);

	void selectNextGameVariant();

	void stopAllSounds();

	void ensureSirenStarted(int sirenIndex);

	void stopMunchingSound();

	void loopGhostReturningSound();

	void stopGhostReturningSound();

	void playLevelCompleteSound();

	void playGameOverSound();

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

	void togglePaused();
}
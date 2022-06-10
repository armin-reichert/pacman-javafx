/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.GhostState;
import javafx.animation.Animation;

/**
 * @author Armin Reichert
 */
public class PlaySceneSounds extends GameEventAdapter {

	private static final PlaySceneSounds theOne = new PlaySceneSounds();

	public static void setGameController(GameController gameController) {
		theOne.gameController = gameController;
	}

	public static void update(GameState state) {
		if (state == GameState.HUNTING) {
			if (theOne.game().pac.starvingTicks == 10) {
				SoundManager.get().stop(GameSound.PACMAN_MUNCH);
			}
			if (theOne.game().huntingTimer.tick() == 0) {
				SoundManager.get().ensureSirenStarted(theOne.game().huntingTimer.phase() / 2);
			}
		}
	}

	private GameController gameController;

	private PlaySceneSounds() {
		GameEventing.addEventListener(this);
	}

	private GameModel game() {
		return gameController.game();
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
		SoundManager.get().ensureSirenStarted(game().huntingTimer.phase() / 2);
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		SoundManager.get().stopSirens();
		SoundManager.get().ensureLoop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		SoundManager.get().ensureLoop(GameSound.PACMAN_MUNCH, Animation.INDEFINITE);
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		SoundManager.get().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostStartsReturningHome(GameEvent e) {
		SoundManager.get().ensurePlaying(GameSound.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game().ghosts(GhostState.DEAD).count() == 0) {
			SoundManager.get().stop(GameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		case READY -> {
			SoundManager.get().stopAll();
			if (!gameController.isGameRunning()) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}
		case PACMAN_DYING -> {
			SoundManager.get().stopAll();
		}
		case GHOST_DYING -> {
			SoundManager.get().play(GameSound.GHOST_EATEN);
		}
		case LEVEL_COMPLETE -> {
			SoundManager.get().stopAll();
		}
		case GAME_OVER -> {
			SoundManager.get().stopAll();
		}
		default -> {
			// nope
		}
		}

	}
}
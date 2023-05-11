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
package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.Game2d;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class SoundHandler {

	public void onSoundEvent(SoundEvent event) {
		var sounds = Game2d.assets.gameSounds(event.game.variant());
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN -> sounds.play(AudioClipID.BONUS_EATEN);
		case GameModel.SE_CREDIT_ADDED -> sounds.play(AudioClipID.CREDIT);
		case GameModel.SE_EXTRA_LIFE -> sounds.play(AudioClipID.EXTRA_LIFE);
		case GameModel.SE_GHOST_EATEN -> sounds.play(AudioClipID.GHOST_EATEN);
		case GameModel.SE_HUNTING_PHASE_STARTED_0 -> sounds.ensureSirenStarted(0);
		case GameModel.SE_HUNTING_PHASE_STARTED_2 -> sounds.ensureSirenStarted(1);
		case GameModel.SE_HUNTING_PHASE_STARTED_4 -> sounds.ensureSirenStarted(2);
		case GameModel.SE_HUNTING_PHASE_STARTED_6 -> sounds.ensureSirenStarted(3);
		case GameModel.SE_READY_TO_PLAY -> sounds.play(AudioClipID.GAME_READY);
		case GameModel.SE_PACMAN_DEATH -> sounds.play(AudioClipID.PACMAN_DEATH);
		// TODO this does not sound as in the original game
		case GameModel.SE_PACMAN_FOUND_FOOD -> sounds.ensureLoop(AudioClipID.PACMAN_MUNCH, AudioClip.INDEFINITE);
		case GameModel.SE_PACMAN_POWER_ENDS -> {
			sounds.stop(AudioClipID.PACMAN_POWER);
			event.game.level().ifPresent(level -> sounds.ensureSirenStarted(level.huntingPhase() / 2));
		}
		case GameModel.SE_PACMAN_POWER_STARTS -> {
			sounds.stopSirens();
			sounds.stop(AudioClipID.PACMAN_POWER);
			sounds.loop(AudioClipID.PACMAN_POWER, AudioClip.INDEFINITE);
		}
		case GameModel.SE_START_INTERMISSION_1 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_1);
			case PACMAN -> sounds.loop(AudioClipID.INTERMISSION_1, 2);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_2 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_2);
			case PACMAN -> sounds.play(AudioClipID.INTERMISSION_1);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_3 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_3);
			case PACMAN -> sounds.loop(AudioClipID.INTERMISSION_1, 2);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_STOP_ALL_SOUNDS -> sounds.stopAll();
		default -> {
			// ignore
		}
		}
	}
}
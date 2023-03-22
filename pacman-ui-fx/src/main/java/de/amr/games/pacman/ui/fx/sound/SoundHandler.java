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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class SoundHandler {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public static final Object[][] MS_PACMAN_SOUND_DATA = { //
			{ SoundClipID.BONUS_EATEN, "sound/mspacman/Fruit.mp3", 1.0 }, //
			{ SoundClipID.CREDIT, "sound/mspacman/Credit.mp3", 1.0 }, //
			{ SoundClipID.EXTRA_LIFE, "sound/mspacman/ExtraLife.mp3", 1.0 }, //
			{ SoundClipID.GAME_READY, "sound/mspacman/Start.mp3", 1.0 }, //
			{ SoundClipID.GHOST_EATEN, "sound/mspacman/Ghost.mp3", 1.0 }, //
			{ SoundClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_1, "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_2, "sound/mspacman/Act2TheChase.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_3, "sound/mspacman/Act3Junior.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_DEATH, "sound/mspacman/Died.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_MUNCH, "sound/mspacman/Pill.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_POWER, "sound/mspacman/ScaredGhost.mp3", 1.0 }, //
			{ SoundClipID.SIREN_1, "sound/mspacman/GhostNoise1.mp3", 1.0 }, //
			{ SoundClipID.SIREN_2, "sound/mspacman/GhostNoise2.mp3", 1.0 }, //
			{ SoundClipID.SIREN_3, "sound/mspacman/GhostNoise3.mp3", 1.0 }, //
			{ SoundClipID.SIREN_4, "sound/mspacman/GhostNoise4.mp3", 1.0 }, //
	};

	public static final Object[][] PACMAN_SOUND_DATA = { //
			{ SoundClipID.BONUS_EATEN, "sound/pacman/eat_fruit.mp3", 1.0 }, //
			{ SoundClipID.CREDIT, "sound/pacman/credit.wav", 1.0 }, //
			{ SoundClipID.EXTRA_LIFE, "sound/pacman/extend.mp3", 1.0 }, //
			{ SoundClipID.GAME_READY, "sound/pacman/game_start.mp3", 1.0 }, //
			{ SoundClipID.GHOST_EATEN, "sound/pacman/eat_ghost.mp3", 1.0 }, //
			{ SoundClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_1, "sound/pacman/intermission.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_DEATH, "sound/pacman/pacman_death.wav", 0.5 }, //
			{ SoundClipID.PACMAN_MUNCH, "sound/pacman/munch_2.wav", 1.0 }, //
			{ SoundClipID.PACMAN_POWER, "sound/pacman/power_pellet.mp3", 1.0 }, //
			{ SoundClipID.SIREN_1, "sound/pacman/siren_1.mp3", 0.5 }, //
			{ SoundClipID.SIREN_2, "sound/pacman/siren_2.mp3", 0.5 }, //
			{ SoundClipID.SIREN_3, "sound/pacman/siren_3.mp3", 0.5 }, //
			{ SoundClipID.SIREN_4, "sound/pacman/siren_4.mp3", 0.5 }, //
	};

	private static final SoundPlayer SOUNDS_MS_PACMAN = new SoundPlayer(MS_PACMAN_SOUND_DATA);
	private static final SoundPlayer SOUNDS_PACMAN = new SoundPlayer(PACMAN_SOUND_DATA);

	public static SoundPlayer sounds(GameModel game) {
		return switch (game.variant()) {
		case MS_PACMAN -> SOUNDS_MS_PACMAN;
		case PACMAN -> SOUNDS_PACMAN;
		default -> throw new IllegalStateException();
		};
	}

	public void onSoundEvent(SoundEvent event) {
		LOG.trace("Handle sound event: %s", event);
		var sounds = sounds(event.game);
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN -> sounds.play(SoundClipID.BONUS_EATEN);
		case GameModel.SE_CREDIT_ADDED -> sounds.play(SoundClipID.CREDIT);
		case GameModel.SE_EXTRA_LIFE -> sounds.play(SoundClipID.EXTRA_LIFE);
		case GameModel.SE_GHOST_EATEN -> sounds.play(SoundClipID.GHOST_EATEN);
		case GameModel.SE_HUNTING_PHASE_STARTED_0 -> sounds.ensureSirenStarted(0);
		case GameModel.SE_HUNTING_PHASE_STARTED_2 -> sounds.ensureSirenStarted(1);
		case GameModel.SE_HUNTING_PHASE_STARTED_4 -> sounds.ensureSirenStarted(2);
		case GameModel.SE_HUNTING_PHASE_STARTED_6 -> sounds.ensureSirenStarted(3);
		case GameModel.SE_READY_TO_PLAY -> sounds.play(SoundClipID.GAME_READY);
		case GameModel.SE_PACMAN_DEATH -> sounds.play(SoundClipID.PACMAN_DEATH);
		// TODO this does not sound as in the original game
		case GameModel.SE_PACMAN_FOUND_FOOD -> sounds.ensureLoop(SoundClipID.PACMAN_MUNCH, AudioClip.INDEFINITE);
		case GameModel.SE_PACMAN_POWER_ENDS -> {
			sounds.stop(SoundClipID.PACMAN_POWER);
			event.game.level().ifPresent(level -> sounds.ensureSirenStarted(level.huntingPhase() / 2));
		}
		case GameModel.SE_PACMAN_POWER_STARTS -> {
			sounds.stopSirens();
			sounds.stop(SoundClipID.PACMAN_POWER);
			sounds.loop(SoundClipID.PACMAN_POWER, AudioClip.INDEFINITE);
		}
		case GameModel.SE_START_INTERMISSION_1 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClipID.INTERMISSION_1);
			case PACMAN -> sounds.loop(SoundClipID.INTERMISSION_1, 2);
			default -> throw new IllegalArgumentException();
			}
		}
		case GameModel.SE_START_INTERMISSION_2 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClipID.INTERMISSION_2);
			case PACMAN -> sounds.play(SoundClipID.INTERMISSION_1);
			default -> throw new IllegalArgumentException();
			}
		}
		case GameModel.SE_START_INTERMISSION_3 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClipID.INTERMISSION_3);
			case PACMAN -> sounds.loop(SoundClipID.INTERMISSION_1, 2);
			default -> throw new IllegalArgumentException();
			}
		}
		case GameModel.SE_STOP_ALL_SOUNDS -> sounds.stopAll();
		default -> {
			// ignore
		}
		}
	}
}
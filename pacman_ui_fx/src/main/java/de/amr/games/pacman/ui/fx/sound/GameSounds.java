/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameSoundController;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.ui.fx.Resources;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class GameSounds implements GameSoundController {

	private static final Logger logger = LogManager.getFormatterLogger();

	public static final GameSounds MS_PACMAN_SOUNDS = new GameSounds();
	public static final GameSounds PACMAN_SOUNDS = new GameSounds();

	static {
		//@formatter:off
		PACMAN_SOUNDS.load(GameSound.BONUS_EATEN,        "sound/pacman/eat_fruit.mp3");
		PACMAN_SOUNDS.load(GameSound.CREDIT,             "sound/pacman/credit.mp3");
		PACMAN_SOUNDS.load(GameSound.EXTRA_LIFE,         "sound/pacman/extend.mp3");
		PACMAN_SOUNDS.load(GameSound.GAME_READY,         "sound/pacman/game_start.mp3");
		PACMAN_SOUNDS.load(GameSound.GHOST_EATEN,        "sound/pacman/eat_ghost.mp3");
		PACMAN_SOUNDS.load(GameSound.GHOST_RETURNING,    "sound/pacman/retreating.mp3");
		PACMAN_SOUNDS.load(GameSound.INTERMISSION_1,     "sound/pacman/intermission.mp3");
		PACMAN_SOUNDS.load(GameSound.PACMAN_DEATH,       "sound/pacman/pacman_death.wav");
		PACMAN_SOUNDS.load(GameSound.PACMAN_MUNCH,       "sound/pacman/munch_1.wav");
		PACMAN_SOUNDS.load(GameSound.PACMAN_POWER,       "sound/pacman/power_pellet.mp3");
		PACMAN_SOUNDS.load(GameSound.SIREN_1,            "sound/pacman/siren_1.mp3");
		PACMAN_SOUNDS.load(GameSound.SIREN_2,            "sound/pacman/siren_2.mp3");
		PACMAN_SOUNDS.load(GameSound.SIREN_3,            "sound/pacman/siren_3.mp3");
		PACMAN_SOUNDS.load(GameSound.SIREN_4,            "sound/pacman/siren_4.mp3");
		logger.info("Ms. Pac-Man game sounds loaded");
		
		MS_PACMAN_SOUNDS.load(GameSound.BONUS_EATEN,     "sound/mspacman/Fruit.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.CREDIT,          "sound/mspacman/Coin Credit.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.EXTRA_LIFE,      "sound/mspacman/Extra Life.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.GAME_READY,      "sound/mspacman/Start.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.GHOST_EATEN,     "sound/mspacman/Ghost.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.GHOST_RETURNING, "sound/mspacman/Ghost Eyes.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.INTERMISSION_1,  "sound/mspacman/They Meet Act 1.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.INTERMISSION_2,  "sound/mspacman/The Chase Act 2.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.INTERMISSION_3,  "sound/mspacman/Junior Act 3.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.PACMAN_DEATH,    "sound/mspacman/Died.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.PACMAN_MUNCH,    "sound/mspacman/Ms. Pac Man Pill.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.PACMAN_POWER,    "sound/mspacman/Scared Ghost.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.SIREN_1,         "sound/mspacman/Ghost Noise 1.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.SIREN_2,         "sound/mspacman/Ghost Noise 2.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.SIREN_3,         "sound/mspacman/Ghost Noise 3.mp3");
		MS_PACMAN_SOUNDS.load(GameSound.SIREN_4,         "sound/mspacman/Ghost Noise 4.mp3");
		logger.info("Ms. Pac-Man game sounds loaded");
		//@formatter:on
	}

	protected final Map<GameSound, AudioClip> clips = new EnumMap<>(GameSound.class);
	protected boolean silent;
	protected boolean muted;

	@Override
	public void setSilent(boolean silent) {
		this.silent = silent;
		if (silent) {
			stopAll();
		}
	}

	@Override
	public boolean isMuted() {
		return muted;
	}

	@Override
	public void setMuted(boolean muted) {
		this.muted = muted;
		if (muted) {
			stopAll();
		}
	}

	protected void load(GameSound sound, String relPath) {
		var url = Resources.urlFromRelPath(relPath);
		if (url == null) {
			var absPath = Resources.absPath(relPath);
			logger.error("Game sound %s not loaded: resource '%s' not found", sound, absPath);
			return;
		}
		var urlStr = url.toExternalForm();
		logger.trace("Try loading clip from '%s'", urlStr);
		try {
			clips.put(sound, new AudioClip(urlStr));
			logger.trace("ok");
		} catch (Exception e) {
			logger.error("Game sound %s not loaded: %s", sound, e.getMessage());
		}
	}

	protected void playClip(AudioClip clip) {
		if (!silent && !muted) {
			clip.play();
		}
	}

	protected AudioClip getClip(GameSound sound) {
		if (!clips.containsKey(sound)) {
			throw new SoundException("No clip found for sound %s", sound);
		}
		return clips.get(sound);
	}

	@Override
	public boolean isPlaying(GameSound sound) {
		return getClip(sound).isPlaying();
	}

	@Override
	public void ensurePlaying(GameSound sound) {
		if (!isPlaying(sound)) {
			play(sound);
		}
	}

	@Override
	public void play(GameSound sound) {
		loop(sound, 1);
	}

	@Override
	public void ensureLoop(GameSound sound, int repetitions) {
		if (!isPlaying(sound)) {
			loop(sound, repetitions);
		}
	}

	@Override
	public void loop(GameSound sound, int repetitions) {
		AudioClip clip = getClip(sound);
		clip.setCycleCount(repetitions);
		playClip(clip);
	}

	@Override
	public void stop(GameSound sound) {
		getClip(sound).stop();
	}

	@Override
	public void stopAll() {
		for (AudioClip clip : clips.values()) {
			clip.stop();
		}
	}

	// -----------------

	@Override
	public void startSiren(int sirenIndex) {
		stopSirens();
		var siren = switch (sirenIndex) {
		case 0 -> GameSound.SIREN_1;
		case 1 -> GameSound.SIREN_2;
		case 2 -> GameSound.SIREN_3;
		case 3 -> GameSound.SIREN_4;
		default -> throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
		};
		getClip(siren).setVolume(0.2);
		loop(siren, Animation.INDEFINITE);
		logger.trace("Siren %s started", siren);
	}

	@Override
	public Stream<GameSound> sirens() {
		return Stream.of(GameSound.SIREN_1, GameSound.SIREN_2, GameSound.SIREN_3, GameSound.SIREN_4);
	}

	@Override
	public void ensureSirenStarted(int sirenIndex) {
		if (sirens().noneMatch(this::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	@Override
	public void stopSirens() {
		sirens().forEach(siren -> {
			if (isPlaying(siren)) {
				getClip(siren).stop();
				logger.trace("Siren %s stopped", siren);
			}
		});
	}
}
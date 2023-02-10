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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class GameSounds {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Map<GameSound, String> PACMAN_MAP = new EnumMap<>(GameSound.class);
	static {
		//@formatter:off
		PACMAN_MAP.put(GameSound.BONUS_EATEN,        "sound/pacman/eat_fruit.mp3");
		PACMAN_MAP.put(GameSound.CREDIT,             "sound/pacman/credit.wav");
		PACMAN_MAP.put(GameSound.EXTRA_LIFE,         "sound/pacman/extend.mp3");
		PACMAN_MAP.put(GameSound.GAME_READY,         "sound/pacman/game_start.mp3");
		PACMAN_MAP.put(GameSound.GHOST_EATEN,        "sound/pacman/eat_ghost.mp3");
		PACMAN_MAP.put(GameSound.GHOST_RETURNING,    "sound/pacman/retreating.mp3");
		PACMAN_MAP.put(GameSound.INTERMISSION_1,     "sound/pacman/intermission.mp3");
		PACMAN_MAP.put(GameSound.PACMAN_DEATH,       "sound/pacman/pacman_death.wav");
		PACMAN_MAP.put(GameSound.PACMAN_MUNCH,       "sound/pacman/munch_1.wav");
		PACMAN_MAP.put(GameSound.PACMAN_POWER,       "sound/pacman/power_pellet.mp3");
		PACMAN_MAP.put(GameSound.SIREN_1,            "sound/pacman/siren_1.mp3");
		PACMAN_MAP.put(GameSound.SIREN_2,            "sound/pacman/siren_2.mp3");
		PACMAN_MAP.put(GameSound.SIREN_3,            "sound/pacman/siren_3.mp3");
		PACMAN_MAP.put(GameSound.SIREN_4,            "sound/pacman/siren_4.mp3");
		//@formatter:on
	}

	private static final Map<GameSound, String> MS_PACMAN_MAP = new EnumMap<>(GameSound.class);
	static {
		//@formatter:off
		MS_PACMAN_MAP.put(GameSound.BONUS_EATEN,     "sound/mspacman/Fruit.mp3");
		MS_PACMAN_MAP.put(GameSound.CREDIT,          "sound/mspacman/Coin Credit.mp3");
		MS_PACMAN_MAP.put(GameSound.EXTRA_LIFE,      "sound/mspacman/Extra Life.mp3");
		MS_PACMAN_MAP.put(GameSound.GAME_READY,      "sound/mspacman/Start.mp3");
		MS_PACMAN_MAP.put(GameSound.GHOST_EATEN,     "sound/mspacman/Ghost.mp3");
		MS_PACMAN_MAP.put(GameSound.GHOST_RETURNING, "sound/mspacman/Ghost Eyes.mp3");
		MS_PACMAN_MAP.put(GameSound.INTERMISSION_1,  "sound/mspacman/They Meet Act 1.mp3");
		MS_PACMAN_MAP.put(GameSound.INTERMISSION_2,  "sound/mspacman/The Chase Act 2.mp3");
		MS_PACMAN_MAP.put(GameSound.INTERMISSION_3,  "sound/mspacman/Junior Act 3.mp3");
		MS_PACMAN_MAP.put(GameSound.PACMAN_DEATH,    "sound/mspacman/Died.mp3");
		MS_PACMAN_MAP.put(GameSound.PACMAN_MUNCH,    "sound/mspacman/Ms. Pac Man Pill.mp3");
		MS_PACMAN_MAP.put(GameSound.PACMAN_POWER,    "sound/mspacman/Scared Ghost.mp3");
		MS_PACMAN_MAP.put(GameSound.SIREN_1,         "sound/mspacman/Ghost Noise 1.mp3");
		MS_PACMAN_MAP.put(GameSound.SIREN_2,         "sound/mspacman/Ghost Noise 2.mp3");
		MS_PACMAN_MAP.put(GameSound.SIREN_3,         "sound/mspacman/Ghost Noise 3.mp3");
		MS_PACMAN_MAP.put(GameSound.SIREN_4,         "sound/mspacman/Ghost Noise 4.mp3");
		//@formatter:on
	}

	public static final GameSounds NO_SOUNDS = new GameSounds("No Sounds", Map.of());
	public static final GameSounds MS_PACMAN_SOUNDS = new GameSounds("Ms. Pac-Man Sounds", MS_PACMAN_MAP);
	public static final GameSounds PACMAN_SOUNDS = new GameSounds("Pac-Man Sounds", PACMAN_MAP);

	private final Map<GameSound, AudioClip> clips = new EnumMap<>(GameSound.class);
	private boolean silent;
	private boolean muted;

	public GameSounds(String mapName, Map<GameSound, String> relPathMap) {
		relPathMap.forEach(this::loadClip);
		LOG.trace("Sounds '%s' loaded", mapName);
	}

	private void loadClip(GameSound sound, String relPath) {
		var url = ResourceMgr.urlFromRelPath(relPath);
		try {
			clips.put(sound, new AudioClip(url.toExternalForm()));
			LOG.trace("Audio clip created: key='%s', URL '%s'", sound, url);
		} catch (Exception e) {
			LOG.error("Audio clip creation failed: %s", e.getMessage());
		}
	}

	public Optional<AudioClip> getClip(GameSound sound) {
		return Optional.ofNullable(clips.get(sound));
	}

	private void playClip(AudioClip clip) {
		if (!silent && !muted) {
			clip.play();
		}
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
		if (muted) {
			stopAll();
		}
	}

	public boolean isPlaying(GameSound sound) {
		return getClip(sound).map(AudioClip::isPlaying).orElse(false);
	}

	public void ensurePlaying(GameSound sound) {
		if (!isPlaying(sound)) {
			play(sound);
		}
	}

	public void play(GameSound sound) {
		loop(sound, 1);
	}

	public void ensureLoop(GameSound sound, int repetitions) {
		if (!isPlaying(sound)) {
			loop(sound, repetitions);
		}
	}

	public void loop(GameSound sound, int repetitions) {
		getClip(sound).ifPresent(clip -> {
			clip.setCycleCount(repetitions);
			playClip(clip);
		});
	}

	public void stop(GameSound sound) {
		getClip(sound).ifPresent(AudioClip::stop);
	}

	public void stopAll() {
		for (AudioClip clip : clips.values()) {
			clip.stop();
		}
		stopSirens();
	}

	public void startSiren(int sirenIndex) {
		stopSirens();
		var siren = switch (sirenIndex) {
		case 0 -> GameSound.SIREN_1;
		case 1 -> GameSound.SIREN_2;
		case 2 -> GameSound.SIREN_3;
		case 3 -> GameSound.SIREN_4;
		default -> throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
		};
		getClip(siren).ifPresent(clip -> clip.setVolume(1.0));
		loop(siren, Animation.INDEFINITE);
		LOG.trace("Siren %s started", siren);
	}

	public Stream<GameSound> sirens() {
		return Stream.of(GameSound.SIREN_1, GameSound.SIREN_2, GameSound.SIREN_3, GameSound.SIREN_4);
	}

	public void ensureSirenStarted(int sirenIndex) {
		if (sirens().noneMatch(this::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens().forEach(siren -> {
			if (isPlaying(siren)) {
				getClip(siren).ifPresent(AudioClip::stop);
				LOG.trace("Siren %s stopped", siren);
			}
		});
	}
}
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

import static de.amr.games.pacman.lib.Logging.log;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * Sound manager.
 * 
 * TODO: I have no clue how to handle sound "professionally".
 * 
 * @author Armin Reichert
 */
public class SoundManager {

	private static SoundManager theOne = new SoundManager();

	public static SoundManager get() {
		return theOne;
	}

	private Map<GameSound, AudioClip> clips_PacMan;
	private Map<GameSound, AudioClip> clips_MsPacMan;
	private GameVariant variant;
	private boolean muted;
	private boolean stopped;

	private SoundManager() {
		selectGameVariant(GameVariant.PACMAN);
	}

	private void loadMsPacManClips() {
		clips_MsPacMan = new EnumMap<>(GameSound.class);
		//@formatter:off
		put(clips_MsPacMan, GameSound.BONUS_EATEN,     "/mspacman/sound/Fruit.mp3");
		put(clips_MsPacMan, GameSound.CREDIT,          "/mspacman/sound/Coin Credit.mp3");
		put(clips_MsPacMan, GameSound.EXTRA_LIFE,      "/mspacman/sound/Extra Life.mp3");
		put(clips_MsPacMan, GameSound.GAME_READY,      "/mspacman/sound/Start.mp3");
		put(clips_MsPacMan, GameSound.GHOST_EATEN,     "/mspacman/sound/Ghost.mp3");
		put(clips_MsPacMan, GameSound.GHOST_RETURNING, "/mspacman/sound/Ghost Eyes.mp3");
		put(clips_MsPacMan, GameSound.INTERMISSION_1,  "/mspacman/sound/They Meet Act 1.mp3");
		put(clips_MsPacMan, GameSound.INTERMISSION_2,  "/mspacman/sound/The Chase Act 2.mp3");
		put(clips_MsPacMan, GameSound.INTERMISSION_3,  "/mspacman/sound/Junior Act 3.mp3");
		put(clips_MsPacMan, GameSound.PACMAN_MUNCH,    "/mspacman/sound/Ms. Pac Man Pill.mp3");
		put(clips_MsPacMan, GameSound.PACMAN_DEATH,    "/mspacman/sound/Died.mp3");
		put(clips_MsPacMan, GameSound.PACMAN_POWER,    "/mspacman/sound/Scared Ghost.mp3");
		put(clips_MsPacMan, GameSound.SIREN_1,         "/mspacman/sound/Ghost Noise 1.mp3");
		put(clips_MsPacMan, GameSound.SIREN_2,         "/mspacman/sound/Ghost Noise 2.mp3");
		put(clips_MsPacMan, GameSound.SIREN_3,         "/mspacman/sound/Ghost Noise 3.mp3");
		put(clips_MsPacMan, GameSound.SIREN_4,         "/mspacman/sound/Ghost Noise 4.mp3");
		//@formatter:on
		log("Ms. Pac-Man audio clips loaded");
	}

	private void loadPacManClips() {
		clips_PacMan = new EnumMap<>(GameSound.class);
		//@formatter:off
		put(clips_PacMan, GameSound.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		put(clips_PacMan, GameSound.CREDIT,          "/pacman/sound/credit.mp3");
		put(clips_PacMan, GameSound.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		put(clips_PacMan, GameSound.GAME_READY,      "/pacman/sound/game_start.mp3");
		put(clips_PacMan, GameSound.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		put(clips_PacMan, GameSound.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		put(clips_PacMan, GameSound.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		put(clips_PacMan, GameSound.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		put(clips_PacMan, GameSound.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
		put(clips_PacMan, GameSound.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		put(clips_PacMan, GameSound.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		put(clips_PacMan, GameSound.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		put(clips_PacMan, GameSound.SIREN_1,         "/pacman/sound/siren_1.mp3");
		put(clips_PacMan, GameSound.SIREN_2,         "/pacman/sound/siren_2.mp3");
		put(clips_PacMan, GameSound.SIREN_3,         "/pacman/sound/siren_3.mp3");
		put(clips_PacMan, GameSound.SIREN_4,         "/pacman/sound/siren_4.mp3");
		//@formatter:on
		log("Pac-Man audio clips loaded");
	}

	private void put(Map<GameSound, AudioClip> map, GameSound sound, String path) {
		URL url = getClass().getResource(path);
		if (url == null) {
			throw new RuntimeException("Sound resource does not exist: " + path);
		}
		map.put(sound, new AudioClip(url.toString()));
	}

	public void selectGameVariant(GameVariant variant) {
		this.variant = variant;
	}

	private Map<GameSound, AudioClip> clips() {
		return switch (variant) {
		case MS_PACMAN -> {
			if (clips_MsPacMan == null) {
				loadMsPacManClips();
			}
			yield clips_MsPacMan;
		}
		case PACMAN -> {
			if (clips_PacMan == null) {
				loadPacManClips();
			}
			yield clips_PacMan;
		}
		};
	}

	public AudioClip getClip(GameSound sound) {
		return clips().get(sound);
	}

	public boolean isPlaying(GameSound sound) {
		return getClip(sound).isPlaying();
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
		if (!muted && !stopped) {
			AudioClip clip = getClip(sound);
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	public void stop(GameSound sound) {
		getClip(sound).stop();
	}

	public void stopAll() {
		for (AudioClip clip : clips().values()) {
			clip.stop();
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

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public Stream<GameSound> sirens() {
		return Stream.of(GameSound.SIREN_1, GameSound.SIREN_2, GameSound.SIREN_3, GameSound.SIREN_4);
	}

	public void startSiren(int sirenIndex) {
		var siren = switch (sirenIndex) {
		case 0 -> GameSound.SIREN_1;
		case 1 -> GameSound.SIREN_2;
		case 2 -> GameSound.SIREN_3;
		case 3 -> GameSound.SIREN_4;
		default -> throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
		};
		getClip(siren).setVolume(0.2);
		loop(siren, Animation.INDEFINITE);
	}

	public void ensureStartSiren(int sirenIndex) {
		if (!sirens().anyMatch(this::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens().forEach(this::stop);
		log("Siren(s) stopped");
	}
}
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

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameSoundController;
import de.amr.games.pacman.model.common.GameSound;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 *
 */
public class AbstractGameSounds implements GameSoundController {

	private static final Logger logger = LogManager.getFormatterLogger();

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

	protected void put(Map<GameSound, AudioClip> map, GameSound sound, URL url) {
		try {
			map.put(sound, new AudioClip(url.toString()));
		} catch (Exception e) {
			e.printStackTrace();
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
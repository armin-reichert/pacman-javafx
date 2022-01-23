/*
MIT License

Copyright (c) 2021 Armin Reichert

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
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.ui.GameSounds;
import javafx.scene.media.AudioClip;

/**
 * Sound manager.
 * <p>
 * TODO: I have no clue how to handle sound "professionally"
 * 
 * @author Armin Reichert
 */
public class SoundManager {

	public boolean loggingEnabled = false;

	private void log(String message, Object... args) {
		if (loggingEnabled) {
			Logging.log(message, args);
		}
	}

	private final Map<GameSounds, URL> url = new EnumMap<>(GameSounds.class);
	private final Map<GameSounds, AudioClip> clipCache = new HashMap<>();
	private boolean muted;

	public void put(GameSounds sound, String path) {
		url.put(sound, getClass().getResource(path));
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
		if (muted) {
			stopAll();
		}
	}

	public AudioClip getClip(GameSounds sound) {
		if (!clipCache.containsKey(sound)) {
			AudioClip clip = new AudioClip(url.get(sound).toExternalForm());
			clipCache.put(sound, clip);
		}
		return clipCache.get(sound);
	}

	public void play(GameSounds sound) {
		if (!muted) {
			log("Play sound %s", sound);
			getClip(sound).play();
		}
	}

	public void loop(GameSounds sound, int repetitions) {
		if (!muted) {
			log("Loop sound %s repetition=%d", sound, repetitions);
			AudioClip clip = getClip(sound);
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	public void stop(GameSounds sound) {
		log("Stop sound %s", sound);
		getClip(sound).stop();
	}

	public void stopAll() {
		for (AudioClip clip : clipCache.values()) {
			clip.stop();
		}
		clipCache.clear();
	}
}
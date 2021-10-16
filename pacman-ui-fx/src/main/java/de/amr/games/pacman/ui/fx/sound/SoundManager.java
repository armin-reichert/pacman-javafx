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

import static de.amr.games.pacman.lib.Logging.log;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.amr.games.pacman.ui.PacManGameSound;
import javafx.scene.media.AudioClip;

/**
 * Sound manager for Pac-Man game variants.
 * 
 * @author Armin Reichert
 */
public class SoundManager {

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final Map<PacManGameSound, AudioClip> clipCache = new HashMap<>();
	private boolean muted;

	public SoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public AudioClip getClip(PacManGameSound sound) {
		if (!clipCache.containsKey(sound)) {
			AudioClip clip = new AudioClip(fnSoundURL.apply(sound).toExternalForm());
			clipCache.put(sound, clip);
		}
		return clipCache.get(sound);
	}

	public void play(PacManGameSound sound) {
		if (!muted) {
			log("Play sound %s", sound);
			getClip(sound).play();
		}
	}

	public void loop(PacManGameSound sound, int repetitions) {
		if (!muted) {
			log("Loop sound %s repetition=%d", sound, repetitions);
			AudioClip clip = getClip(sound);
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	public void stop(PacManGameSound sound) {
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
package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.amr.games.pacman.ui.sound.PacManGameSound;
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
			getClip(sound).play();
		}
	}

	public void loop(PacManGameSound sound, int repetitions) {
		if (!muted) {
			AudioClip clip = getClip(sound);
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	public void stop(PacManGameSound sound) {
		getClip(sound).stop();
	}

	public void stopAll() {
		for (AudioClip clip : clipCache.values()) {
			clip.stop();
		}
		clipCache.clear();
	}
}
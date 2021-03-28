package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
	private final List<AudioClip> clipCache = new ArrayList<>();
	private final AudioClip munch0, munch1;
	private int munchIndex;
	private boolean muted;

	public SoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
		munchIndex = 0;
		munch0 = new AudioClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH).toExternalForm());
		munch1 = new AudioClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH).toExternalForm());
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	private AudioClip getClip(PacManGameSound sound) {
		AudioClip clip = null;
		if (sound == PacManGameSound.PACMAN_MUNCH) {
			clip = munchIndex == 0 ? munch0 : munch1;
			munchIndex = (munchIndex + 1) % 2;
		} else {
			clip = new AudioClip(fnSoundURL.apply(sound).toExternalForm());
			clipCache.add(clip);
		}
		return clip;
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
		for (AudioClip clip : clipCache) {
			clip.stop();
		}
		clipCache.clear();
		munch0.stop();
		munch1.stop();
	}
}
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
	private final List<AudioClip> clipsRequested = new ArrayList<>();

	public SoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
	}

	private AudioClip getClip(PacManGameSound sound) {
		AudioClip clip = new AudioClip(fnSoundURL.apply(sound).toExternalForm());
		clipsRequested.add(clip);
		return clip;
	}

	public void play(PacManGameSound sound) {
		getClip(sound).play();
	}

	public void loop(PacManGameSound sound, int repetitions) {
		AudioClip clip = getClip(sound);
		clip.setCycleCount(repetitions);
		clip.play();
	}

	public void stop(PacManGameSound sound) {
		AudioClip clip = getClip(sound);
		clip.stop();
	}

	public void stopAll() {
		for (AudioClip clip : clipsRequested) {
			clip.stop();
		}
		clipsRequested.clear();
	}
}
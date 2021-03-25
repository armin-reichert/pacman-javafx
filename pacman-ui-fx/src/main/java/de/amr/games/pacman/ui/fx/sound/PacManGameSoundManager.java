package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.media.AudioClip;

/**
 * Sound manager for Pac-Man game variants.
 * 
 * @author Armin Reichert
 */
public class PacManGameSoundManager implements SoundManager {

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final List<AudioClip> clipsRequested = new ArrayList<>();

	public PacManGameSoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
	}

	private AudioClip getClip(PacManGameSound sound) {
		AudioClip clip = new AudioClip(fnSoundURL.apply(sound).toExternalForm());
		clipsRequested.add(clip);
		return clip;
	}

	@Override
	public void play(PacManGameSound sound) {
		getClip(sound).play();
	}

	@Override
	public void loop(PacManGameSound sound, int repetitions) {
		AudioClip clip = getClip(sound);
		clip.setCycleCount(repetitions);
		clip.play();
	}

	@Override
	public void stop(PacManGameSound sound) {
		AudioClip clip = getClip(sound);
		clip.stop();
	}

	@Override
	public void stopAll() {
		for (AudioClip clip : clipsRequested) {
			clip.stop();
		}
		clipsRequested.clear();
	}
}
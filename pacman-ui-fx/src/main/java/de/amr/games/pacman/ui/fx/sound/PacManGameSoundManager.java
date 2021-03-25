package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Sound manager for Pac-Man game variants.
 * 
 * @author Armin Reichert
 */
public class PacManGameSoundManager implements SoundManager {

	private final Map<PacManGameSound, Double> playbackRate = Map.of(//
			PacManGameSound.GHOST_SIREN_1, 1.25//
	);

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final Map<PacManGameSound, MediaPlayer> clipCache = new EnumMap<>(PacManGameSound.class);

	public PacManGameSoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
	}

	private MediaPlayer getClip(PacManGameSound sound) {
		MediaPlayer clip = null;
		if (clipCache.containsKey(sound)) {
			clip = clipCache.get(sound);
		} else {
			clip = new MediaPlayer(new Media(fnSoundURL.apply(sound).toExternalForm()));
			clipCache.put(sound, clip);
		}
		clip.seek(Duration.ZERO);
		clip.setRate(playbackRate.getOrDefault(sound, 1.0));
		return clip;
	}

	@Override
	public void play(PacManGameSound sound) {
		getClip(sound).play();
	}

	@Override
	public void loop(PacManGameSound sound, int repetitions) {
		MediaPlayer player = getClip(sound);
		player.setCycleCount(repetitions);
		player.play();
	}

	@Override
	public void stop(PacManGameSound sound) {
		MediaPlayer player = getClip(sound);
		player.stop();
	}

	@Override
	public void stopAll() {
		for (MediaPlayer clip : clipCache.values()) {
			clip.stop();
		}
	}
}
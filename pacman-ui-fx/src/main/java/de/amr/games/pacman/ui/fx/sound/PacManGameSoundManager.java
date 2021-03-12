package de.amr.games.pacman.ui.fx.sound;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Sound manager for Pac-Man game variants.
 * 
 * TODO use FX media functionality
 * 
 * @author Armin Reichert
 */
public class PacManGameSoundManager implements SoundManager {

	private static final int MUNCHES = 2;

	private final Function<PacManGameSound, URL> fnSoundURL;
	private final Map<PacManGameSound, Clip> clipCache = new EnumMap<>(PacManGameSound.class);
	private final Clip[] munchClips = new Clip[MUNCHES];
	private int munchIndex;

	public PacManGameSoundManager(Function<PacManGameSound, URL> fnSoundURL) {
		this.fnSoundURL = fnSoundURL;
		for (int i = 0; i < MUNCHES; ++i) {
			munchClips[i] = createAndOpenClip(fnSoundURL.apply(PacManGameSound.PACMAN_MUNCH));
		}
		munchIndex = 0;
	}

	private Clip createAndOpenClip(URL url) {
		try (BufferedInputStream bs = new BufferedInputStream(url.openStream())) {
			try (AudioInputStream as = AudioSystem.getAudioInputStream(bs)) {
				Clip clip = AudioSystem.getClip();
				clip.open(as);
				return clip;
			}
		} catch (Exception x) {
			throw new RuntimeException("Error opening audio clip", x);
		}
	}

	// TODO how to avoid warning about potential resource leak?
	@SuppressWarnings("resource")
	private Clip getClip(PacManGameSound sound) {
		Clip clip = null;
		if (sound == PacManGameSound.PACMAN_MUNCH) {
			clip = munchClips[munchIndex];
			munchIndex = (munchIndex + 1) % MUNCHES;
		} else if (clipCache.containsKey(sound)) {
			clip = clipCache.get(sound);
		} else {
			clip = createAndOpenClip(fnSoundURL.apply(sound));
			clipCache.put(sound, clip);
		}
		clip.setFramePosition(0);
		return clip;
	}

	@SuppressWarnings("resource")
	@Override
	public void play(PacManGameSound sound) {
		getClip(sound).start();
	}

	@SuppressWarnings("resource")
	@Override
	public void loop(PacManGameSound sound, int repetitions) {
		Clip clip = getClip(sound);
		clip.setFramePosition(0);
		clip.loop(repetitions == Integer.MAX_VALUE ? Clip.LOOP_CONTINUOUSLY : repetitions - 1);
	}

	@SuppressWarnings("resource")
	@Override
	public void stop(PacManGameSound sound) {
		getClip(sound).stop();
	}

	@Override
	public void stopAll() {
		for (Clip clip : clipCache.values()) {
			clip.stop();
		}
		for (Clip clip : munchClips) {
			clip.stop();
		}
	}
}
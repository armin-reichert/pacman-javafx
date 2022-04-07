/**
 * 
 */
package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.ui.GameSound;
import javafx.scene.media.AudioClip;

/**
 * Maps sound symbols to audio clips.
 * 
 * @author Armin Reichert
 */
public class SoundMap {

	private final Map<GameSound, AudioClip> clips = new EnumMap<>(GameSound.class);

	public void put(GameSound sound, String path) {
		URL url = getClass().getResource(path);
		if (url != null) {
			clips.put(sound, new AudioClip(url.toString()));
		} else {
			throw new RuntimeException("Sound resource does not exist: " + path);
		}
	}

	public AudioClip getClip(GameSound sound) {
		return clips.get(sound);
	}

	public Collection<AudioClip> clips() {
		return clips.values();
	}
}
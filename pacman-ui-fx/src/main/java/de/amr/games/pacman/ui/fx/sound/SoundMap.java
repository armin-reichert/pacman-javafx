/**
 * 
 */
package de.amr.games.pacman.ui.fx.sound;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.ui.GameSounds;
import javafx.scene.media.AudioClip;

/**
 * Sound map.
 * 
 * @author Armin Reichert
 */
public class SoundMap {

	private final Map<GameSounds, AudioClip> clips = new HashMap<>();

	public void put(GameSounds sound, String path) {
		String url = getClass().getResource(path).toExternalForm();
		clips.put(sound, new AudioClip(url));
	}

	public AudioClip getClip(GameSounds sound) {
		return clips.get(sound);
	}

	public Collection<AudioClip> clips() {
		return clips.values();
	}
}
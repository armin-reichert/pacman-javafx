/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx.sound.common;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class Sounds {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final Map<SoundClipID, AudioClip> clips = new EnumMap<>(SoundClipID.class);

	private static AudioClip loadClip(String relPath, double volume) {
		var url = ResourceMgr.urlFromRelPath(relPath);
		var clip = new AudioClip(url.toExternalForm());
		clip.setVolume(volume);
		return clip;
	}

	public Sounds(Object[][] data) {
		for (var row : data) {
			SoundClipID id = (SoundClipID) row[0];
			String path = (String) row[1];
			double volume = (double) row[2];
			var clip = loadClip(path, volume);
			clips.put(id, clip);
		}
	}

	public Optional<AudioClip> getClip(SoundClipID sound) {
		return Optional.ofNullable(clips.get(sound));
	}

	private void playClip(AudioClip clip) {
		clip.play();
	}

	public boolean isPlaying(SoundClipID sound) {
		return getClip(sound).map(AudioClip::isPlaying).orElse(false);
	}

	public void ensurePlaying(SoundClipID sound) {
		if (!isPlaying(sound)) {
			play(sound);
		}
	}

	public void play(SoundClipID sound) {
		loop(sound, 1);
	}

	public void ensureLoop(SoundClipID sound, int repetitions) {
		if (!isPlaying(sound)) {
			loop(sound, repetitions);
		}
	}

	public void loop(SoundClipID sound, int repetitions) {
		getClip(sound).ifPresent(clip -> {
			clip.setCycleCount(repetitions);
			playClip(clip);
		});
	}

	public void stop(SoundClipID sound) {
		getClip(sound).ifPresent(AudioClip::stop);
	}

	public void stopAll() {
		for (AudioClip clip : clips.values()) {
			clip.stop();
		}
		stopSirens();
		stop(SoundClipID.PACMAN_MUNCH);
	}

	public void startSiren(int sirenIndex) {
		stopSirens();
		var siren = switch (sirenIndex) {
		case 0 -> SoundClipID.SIREN_1;
		case 1 -> SoundClipID.SIREN_2;
		case 2 -> SoundClipID.SIREN_3;
		case 3 -> SoundClipID.SIREN_4;
		default -> throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
		};
		loop(siren, Animation.INDEFINITE);
		LOG.trace("Siren %s started", siren);
	}

	public Stream<SoundClipID> sirens() {
		return Stream.of(SoundClipID.SIREN_1, SoundClipID.SIREN_2, SoundClipID.SIREN_3, SoundClipID.SIREN_4);
	}

	public void ensureSirenStarted(int sirenIndex) {
		if (sirens().noneMatch(this::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens().forEach(siren -> {
			if (isPlaying(siren)) {
				getClip(siren).ifPresent(AudioClip::stop);
				LOG.trace("Siren %s stopped", siren);
			}
		});
	}
}
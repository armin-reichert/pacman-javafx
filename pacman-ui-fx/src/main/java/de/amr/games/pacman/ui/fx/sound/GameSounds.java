/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.app.Game2d;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class GameSounds {

	record ClipInfo(String path, double volume, AudioClip clip) {
	}

	private final Map<AudioClipID, ClipInfo> clipInfoMap = new EnumMap<>(AudioClipID.class);

	public GameSounds(Object[][] data, boolean preload) {
		for (var row : data) {
			AudioClipID id = (AudioClipID) row[0];
			String path = (String) row[1];
			double volume = (double) row[2];
			if (preload) {
				clipInfoMap.put(id, new ClipInfo(path, volume, makeAudioClip(id, path, volume)));

			} else {
				clipInfoMap.put(id, new ClipInfo(path, volume, null));
			}
		}
	}

	private static AudioClip makeAudioClip(AudioClipID id, String path, double volume) {
		var clip = Game2d.RESOURCE_MANAGER.audioClip(path); // TODO
		clip.setVolume(volume);
		Logger.info("Audio clip created, id={} volume={}, source={}", id, clip.getVolume(), clip.getSource());
		return clip;
	}

	private AudioClip getOrCreateAudioClip(AudioClipID id) {
		var info = clipInfoMap.get(id);
		if (info.clip == null) {
			var clip = makeAudioClip(id, info.path, info.volume());
			clipInfoMap.put(id, new ClipInfo(info.path, info.volume, clip));
			return clip;
		}
		return info.clip();
	}

	public Optional<AudioClip> getClip(AudioClipID clipID) {
		var clip = getOrCreateAudioClip(clipID);
		return Optional.ofNullable(clip);
	}

	public boolean isPlaying(AudioClipID clipID) {
		return getClip(clipID).map(AudioClip::isPlaying).orElse(false);
	}

	public void play(AudioClipID clipID) {
		if (!isPlaying(clipID)) {
			var optionalClip = getClip(clipID);
			optionalClip.ifPresent(clip -> {
				clip.setCycleCount(1); // might have been looped at previous call
				clip.play();
			});
		} else {
			Logger.info("Sound clip {} already playing", clipID);
		}
	}

	public void ensureLoop(AudioClipID clipID, int repetitions) {
		if (!isPlaying(clipID)) {
			loop(clipID, repetitions);
		}
	}

	public void loop(AudioClipID clipID, int repetitions) {
		getClip(clipID).ifPresent(clip -> {
			clip.setCycleCount(repetitions);
			clip.play();
		});
	}

	public void stop(AudioClipID clipID) {
		getClip(clipID).ifPresent(AudioClip::stop);
	}

	public void stopAll() {
		clipInfoMap.values().stream().map(ClipInfo::clip).filter(Objects::nonNull).forEach(AudioClip::stop);
	}

	public void startSiren(int sirenIndex) {
		stopSirens();
		var siren = switch (sirenIndex) {
		case 0 -> AudioClipID.SIREN_1;
		case 1 -> AudioClipID.SIREN_2;
		case 2 -> AudioClipID.SIREN_3;
		case 3 -> AudioClipID.SIREN_4;
		default -> throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
		};
		loop(siren, Animation.INDEFINITE);
		Logger.trace("Siren {} started", siren);
	}

	public Stream<AudioClipID> sirens() {
		return Stream.of(AudioClipID.SIREN_1, AudioClipID.SIREN_2, AudioClipID.SIREN_3, AudioClipID.SIREN_4);
	}

	/**
	 * @param sirenIndex index of siren (0..3)
	 */
	public void ensureSirenStarted(int sirenIndex) {
		if (sirens().noneMatch(this::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens().forEach(siren -> {
			if (isPlaying(siren)) {
				getClip(siren).ifPresent(AudioClip::stop);
				Logger.trace("Siren {} stopped", siren);
			}
		});
	}
}
/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.Validations;
import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.Arrays;

public class SirenPlayer {

    private final URL[] urls;
    private final AudioClip[] clips;

    public SirenPlayer(URL... sirenURLArray) {
        urls = Arrays.copyOf(sirenURLArray, sirenURLArray.length);
        clips = new AudioClip[sirenURLArray.length];
    }

    public void playSiren(int number, double volume, double rate) {
        final int index = number - 1;
        if (Validations.inClosedRange(index, 0, urls.length - 1)) {
            if (clips[index] == null) {
                final var clip = new AudioClip(urls[index].toExternalForm());
                clip.setCycleCount(AudioClip.INDEFINITE);
                clip.setRate(rate);
                clips[index] = clip;
                clip.play(volume);
            }
        }
    }

    public void playSiren(int number, double volume) {
        playSiren(number, volume, 1.0);
    }

    public void playSiren(int number) {
        playSiren(number, 1.0);
    }

    public void stopSiren(int number) {
        final int index = number - 1;
        if (Validations.inClosedRange(index, 0, urls.length - 1)) {
            if (clips[index] != null) {
                clips[index].stop();
                clips[index] = null;
            }
        }
    }

    public void stopSirens() {
        for (AudioClip clip : clips) {
            if (clip != null) {
                clip.stop();
            }
        }
    }
}

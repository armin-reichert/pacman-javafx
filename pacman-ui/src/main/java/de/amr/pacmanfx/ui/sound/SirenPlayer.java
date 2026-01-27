/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.Validations;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.AudioClip;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Arrays;

public class SirenPlayer {

    private final BooleanProperty mute = new SimpleBooleanProperty(false);

    private final URL[] urls;
    private final AudioClip[] clips;

    private int currentSirenNumber;

    public SirenPlayer(URL... sirenURLArray) {
        urls = Arrays.copyOf(sirenURLArray, sirenURLArray.length);
        clips = new AudioClip[sirenURLArray.length];
        muteProperty().addListener((_, _, muted) -> {
            if (muted) {
                stopSirens();
            }
        });
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public void ensureSirenPlaying(int number) {
        ensureSirenPlaying(number, 1.0);
    }

    public void ensureSirenPlaying(int number, double volume) {
        ensureSirenPlaying(number, volume, 1.0);
    }

    public void ensureSirenPlaying(int number, double volume, double rate) {
        if (!Validations.inClosedRange(number, 1, urls.length)) {
            Logger.error("Invalid siren number: {}", number);
            return;
        }
        if (currentSirenNumber != number) {
            if (currentSirenNumber != 0) {
                stopCurrentSiren();
            }
            currentSirenNumber = number;
        }
        if (mute.get()) {
            return;
        }
        final int index = number - 1;
        if (clips[index] == null) {
            final var clip = new AudioClip(urls[index].toExternalForm());
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.setRate(rate);
            clip.play(volume);
            clips[index] = clip;
        }
    }

    public void stopCurrentSiren() {
        if (currentSirenNumber != 0) {
            final int index = currentSirenNumber - 1;
            if (clips[index] != null) {
                clips[index].stop();
                clips[index] = null;
            }
        }
    }

    public void stopSirens() {
        for (int i = 0; i< clips.length; i++) {
            if (clips[i] != null) {
                clips[i].stop();
                clips[i] = null;
            }
        }
    }
}

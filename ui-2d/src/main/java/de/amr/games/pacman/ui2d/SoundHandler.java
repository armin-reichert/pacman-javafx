/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

/**
 * @author Armin Reichert
 */
public interface SoundHandler {

    void playVoice(String clipID, double delaySeconds);

    void stopVoice();

    AudioClip audioClip(String key);

    void playAudioClip(String key);

    void stopAudioClip(String key);

    void ensureAudioClipRepeats(String key, int repetitions);

    void ensureAudioClipLoops(String key);

    default void stop(MediaPlayer player) {
        if (player != null) {
            player.stop();
        }
    }
    void stopAllSounds();

    void ensureSirenPlaying(int sirenIndex);

    void stopSirens();

    void playMunchingSound();

    void stopMunchingSound();

    void playPowerSound();

    void playIntermissionSound(int number);
}
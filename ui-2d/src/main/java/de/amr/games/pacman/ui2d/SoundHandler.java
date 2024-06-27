/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public interface SoundHandler {

    void stopVoice();

    void playVoice(String clipID, double delaySeconds);

    AudioClip audioClip(String key);

    default void playAudioClip(String key) {
        audioClip(key).play();
    }

    default void stopAudioClip(String key) {
        audioClip(key).stop();
    }

    void stopAllSounds();

    void ensureSirenPlaying(int sirenIndex);

    void stopSiren();

    void ensureAudioLoop(String key, int repetitions);

    void ensureAudioLoop(String key);
}
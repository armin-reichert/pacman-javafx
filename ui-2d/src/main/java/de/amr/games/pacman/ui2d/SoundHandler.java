package de.amr.games.pacman.ui2d;

import javafx.scene.media.AudioClip;

public interface SoundHandler {

    void stopVoice();

    AudioClip audioClip(String key);

    default void playAudioClip(String key) {
        audioClip(key).play();
    }

    default void stopAudioClip(String key) {
        audioClip(key).stop();
    }

    void stopAllSounds();

    void ensureSirenStarted(int sirenIndex);

    void stopSirens();

    void ensureAudioLoop(String key, int repetitions);

    void ensureAudioLoop(String key);

}

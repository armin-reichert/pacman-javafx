/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import javafx.scene.media.MediaPlayer;

/**
 * @author Armin Reichert
 */
public interface SoundHandler {

    boolean isMuted();

    void mute(boolean muted);

    void playVoice(String voiceClipID, double delaySeconds);

    void stopVoice();

    void playAudioClip(String key);

    default void stopSound(MediaPlayer player) {
        if (player != null) {
            player.stop();
        }
    }

    void stopAllSounds();

    void ensureSirenPlaying(int sirenIndex);

    void stopSiren();

    void playStartGameSound();

    void playIntermissionSound(int number);

    void playMunchingSound();

    void stopMunchingSound();

    void playPowerSound();

    void playGhostReturningHomeSound();

    void stopGhostReturningHomeSound();
}
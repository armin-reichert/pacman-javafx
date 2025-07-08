package de.amr.pacmanfx.ui.sound;

import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

public interface SoundManager {

    MediaPlayer createMediaPlayer(String key, int numRepetitions);

    void setEnabled(boolean enabled);

    BooleanProperty mutedProperty();
    void toggleMuted();

    void playAudioClip(String key, double volume);

    void play(SoundID id);
    void pause(SoundID id);
    void stop(SoundID id);
    void stopAll();
    void stopVoice();
    void playVoice(String key, double delaySeconds);

    void selectSiren(int number);
    void playSiren();
    void pauseSiren();
    void stopSiren();
}

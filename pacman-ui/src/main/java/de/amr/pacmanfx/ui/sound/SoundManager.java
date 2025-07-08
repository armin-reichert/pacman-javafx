package de.amr.pacmanfx.ui.sound;

import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

public interface SoundManager {

    MediaPlayer createMediaPlayer(String keySuffix, int numRepetitions);

    BooleanProperty enabledProperty();
    void setEnabled(boolean enabled);

    BooleanProperty mutedProperty();
    void toggleMuted();

    void playAudioClip(String keySuffix, double volume);

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

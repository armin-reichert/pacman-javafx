package de.amr.pacmanfx.ui.sound;

import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

public interface SoundManager {


    BooleanProperty enabledProperty();
    void setEnabled(boolean enabled);

    BooleanProperty mutedProperty();

    MediaPlayer createMediaPlayerFromMyNamespace(String keySuffix, int numRepetitions);
//    AudioClip createAudioClipFromMyANamespace(String keySuffix);
    void playAudioClipFromMyNamespace(String keySuffix);

    void play(SoundID id);
    void pause(SoundID id);
    void stop(SoundID id);
    void stopAll();
    void stopVoice();
    void playVoice(String key, double delaySeconds);

    void playSiren(SoundID sirenID, double volume);
    void pauseSiren();
    void stopSiren();
}

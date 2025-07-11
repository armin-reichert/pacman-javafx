package de.amr.pacmanfx.ui.sound;

import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

public interface SoundManager {
    BooleanProperty enabledProperty();
    void setEnabled(boolean enabled);
    BooleanProperty mutedProperty();
    default void play(Object id) { play(id, 1); }
    void play(Object id, int repetitions);
    default void loop(Object id) { play(id, MediaPlayer.INDEFINITE); }
    void pause(Object id);
    void stop(Object id);
    void stopAll();
    void stopVoice();
    void playVoice(SoundID voiceID, double delaySeconds);
    void playSiren(SoundID sirenID, double volume);
    void pauseSiren();
    void stopSiren();
}

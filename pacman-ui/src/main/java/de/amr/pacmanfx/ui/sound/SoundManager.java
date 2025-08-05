package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.lib.Disposable;
import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

public interface SoundManager extends Disposable {
    BooleanProperty enabledProperty();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    BooleanProperty mutedProperty();
    default void play(Object id) { play(id, 1); }
    void play(Object id, int repetitions);
    boolean isPlaying(Object id);
    default void loop(Object id) { play(id, MediaPlayer.INDEFINITE); }
    void pause(Object id);
    boolean isPaused(Object id);
    void stop(Object id);
    boolean isStopped(Object id);
    void stopAll();
    void stopVoice();
    void playVoice(SoundID voiceID, double delaySeconds);
    void playSiren(SoundID sirenID, double volume);
    void pauseSiren();
    void stopSiren();
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.sound;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.basics.ui.Voice;
import javafx.beans.property.BooleanProperty;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public interface SoundManager extends Disposable {

    BooleanProperty muteProperty();

    void stopAll();

    Voice voice();

    void setEnabled(boolean b);

    void add(SoundEntry entry);

    void remove(SoundEntry entry);

    void playLoop(Named soundID);

    void stop(Named soundID);

    void play(Named soundID);

    void play(Named soundID, int repetitions);

    boolean isPlaying(Named soundID);

    boolean isEnabled();

    void addMediaPlayer(Named soundID, URL url);

    void addAudioClip(Named soundID, URL url);

    MediaPlayer mediaPlayer(Named soundID);
}

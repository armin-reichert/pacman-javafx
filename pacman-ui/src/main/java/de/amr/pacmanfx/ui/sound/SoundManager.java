/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.lib.Disposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SoundManager implements Disposable {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, Object> soundMap = new HashMap<>();

    private SoundID currentSirenID;
    private MediaPlayer currentVoice;

    public SoundManager() {}

    @Override
    public void dispose() {
        stopAll();
        enabledProperty.unbind();
        mutedProperty.unbind();
        soundMap.clear();
        currentVoice = null;
        currentSirenID = null;
        Logger.info("Disposed default sound manager {}", this);
    }

    public MediaPlayer mediaPlayer(Object key) {
        requireNonNull(key);
        if (!soundMap.containsKey(key)) {
            throw new IllegalArgumentException("Unknown media player key '%s'".formatted(key));
        }
        if (soundMap.get(key) instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer;
        }
        throw new IllegalArgumentException("Sound entry with key '%s' is not a media player".formatted(key));
    }

    private void register(Object key, Object value) {
        Object prevValue = soundMap.put(key, value);
        if (prevValue != null) {
            Logger.warn("Replaced sound  with key '{}', old value was {}", key, value);
        }
    }

    public void registerAudioClip(Object key, URL url) {
        requireNonNull(key);
        requireNonNull(url);
        register(key, url);
    }

    public void registerMediaPlayer(Object key, URL url) {
        requireNonNull(key);
        requireNonNull(url);
        var mediaPlayer = new MediaPlayer(new Media(url.toExternalForm()));
        mediaPlayer.setVolume(1.0);
        mediaPlayer.muteProperty().bind(Bindings.createBooleanBinding(
            () -> mutedProperty().get() || !enabledProperty().get(),
            mutedProperty(), enabledProperty()
        ));
        Logger.info("Media player registered: key='{}', URL='{}'", key, url);
        register(key, mediaPlayer);
    }

    public void registerVoice(SoundID id, URL url) {
        registerMediaPlayer(id, url);
        // voice is also played when sound manager is disabled!
        mediaPlayer(id).muteProperty().bind(mutedProperty);
    }

    public BooleanProperty enabledProperty() {
        return enabledProperty;
    }

    public boolean isEnabled() {
        return enabledProperty.get();
    }

    public void setEnabled(boolean enabled) {
        enabledProperty.set(enabled);
    }

    public BooleanProperty mutedProperty() {
        return mutedProperty;
    }

    public void loop(Object id) {
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
        }
        play(id, MediaPlayer.INDEFINITE);
    }

    public void play(Object id) {
        play(id, 1);
    }

    public void play(Object id, int repetitions) {
        requireNonNull(id);
        if (mutedProperty.get()) {
            Logger.trace("Sound with ID '{}' not played, sound is muted", id);
            return;
        }
        if (!enabledProperty.get()) {
            Logger.trace("Sound with ID '{}' not played, sound is disabled", id);
            return;
        }
        if (!soundMap.containsKey(id)) {
            Logger.error("No media player and no clip URL registered with ID '{}'", id);
            return;
        }
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            Logger.trace("Play media player ({} times) with ID '{}'",
                repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
            mediaPlayer.setCycleCount(repetitions);
            mediaPlayer.play();
        }
        else if (value instanceof URL url) {
            Logger.trace("Create and play audio clip ({} times) with ID '{}'",
                repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
            AudioClip audioClip = new AudioClip(url.toExternalForm());
            audioClip.setCycleCount(repetitions);
            audioClip.play(1.0); //TODO add volume parameter?
        }
    }

    public boolean isPlaying(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    public void pause(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.pause();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be paused", id);
        }
    }

    public boolean isPaused(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED;
        }
        return false;
    }

    public void stop(Object id)  {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.stop();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be stopped", id);
        }
    }

    public boolean isStopped(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.STOPPED;
        }
        return false;
    }

    public void stopAll() {
        soundMap.values().stream().filter(MediaPlayer.class::isInstance).map(MediaPlayer.class::cast).forEach(MediaPlayer::stop);
        stopSiren();
        stopVoice();
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    public void playVoice(SoundID id) {
        requireNonNull(id);
        if (!id.isVoiceID()) {
            Logger.error("Sound ID '{}' is no voice ID", id);
        }
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            currentVoice = mediaPlayer;
            currentVoice.seek(Duration.ZERO);
            Logger.info("Play voice");
            currentVoice.play();
        }
    }

    public void stopVoice() {
        if (currentVoice != null) {
            Logger.trace("Stop voice");
            currentVoice.stop();
        }
    }

    public void playSiren(SoundID sirenID, double volume) {
        requireNonNull(sirenID);
        if (!sirenID.isSirenID()) {
            throw new IllegalArgumentException("Illegal siren ID '%s'".formatted(sirenID));
        }
        if (currentSirenID != sirenID) {
            stopSiren();
        }
        currentSirenID = sirenID;
        MediaPlayer sirenPlayer = (MediaPlayer) soundMap.get(sirenID);
        if (sirenPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            sirenPlayer.setVolume(volume);
            sirenPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            Logger.trace("Play siren with ID '{}' (indefinite times) at volume {}", sirenID, sirenPlayer.getVolume());
            sirenPlayer.play();
        }
    }

    public void pauseSiren() {
        if (currentSirenID != null) {
            MediaPlayer siren = (MediaPlayer) soundMap.get(currentSirenID);
            Logger.info("Paused siren with ID '{}'", currentSirenID);
            siren.pause();
        }
    }

    public void stopSiren() {
        if (currentSirenID != null) {
            MediaPlayer siren = (MediaPlayer) soundMap.get(currentSirenID);
            Logger.trace("Stop siren with ID '{}'", currentSirenID);
            siren.stop();
        }
    }
}
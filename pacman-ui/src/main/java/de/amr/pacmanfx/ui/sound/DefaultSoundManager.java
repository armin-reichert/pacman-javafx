/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

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

public class DefaultSoundManager implements SoundManager {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, Object> soundMap = new HashMap<>();

    private SoundID currentSirenID;
    private MediaPlayer currentVoice;

    public DefaultSoundManager() {}

    @Override
    public void dispose() {
        stopAll();
        enabledProperty.unbind();
        mutedProperty.unbind();
        soundMap.clear();
        currentVoice = null;
        currentSirenID = null;
        Logger.info("Destroyed sound manager {}", this);
    }

    public MediaPlayer mediaPlayer(Object key) {
        requireNonNull(key);
        if (!soundMap.containsKey(key)) {
            throw new IllegalArgumentException("Unknown media player key '%s'".formatted(key));
        }
        if (!(soundMap.get(key) instanceof MediaPlayer)) {
            throw new IllegalArgumentException("Sound entry with key '%s' is not a media player".formatted(key));
        }
        return (MediaPlayer) soundMap.get(key);
    }

    private void setSoundMapValue(Object key, Object value) {
        Object prevValue = soundMap.put(requireNonNull(key), requireNonNull(value));
        if (prevValue != null) {
            Logger.warn("Replaced sound map entry with key '{}', old value was {}", key, value);
        }
    }

    public void registerAudioClip(Object key, URL url) {
        setSoundMapValue(key, url);
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
        Logger.info("Media player created from URL '{}'", url);
        setSoundMapValue(key, mediaPlayer);
    }

    public void registerVoice(SoundID id, URL url) {
        registerMediaPlayer(id, url);
        // voice is also played when enabled (game-specific) is false!
        mediaPlayer(id).muteProperty().bind(mutedProperty);
    }

    @Override
    public BooleanProperty enabledProperty() {
        return enabledProperty;
    }

    @Override
    public void setEnabled(boolean enabled) {
        enabledProperty.set(enabled);
    }

    @Override
    public BooleanProperty mutedProperty() {
        return mutedProperty;
    }

    @Override
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

    @Override
    public boolean isPlaying(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    @Override
    public void pause(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            Logger.info("Pause media player '{}'", id);
            mediaPlayer.pause();
            if (mediaPlayer.getStatus() != MediaPlayer.Status.PAUSED) {
                Logger.error("WTF! Why is media player {} not paused but in state {}?", id, mediaPlayer.getStatus());
            }
        }
        else if (value instanceof URL) {
            Logger.warn("Pausing audio clip with ID '{}' is not supported", id);
        }
    }

    @Override
    public boolean isPaused(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED;
        }
        return false;
    }

    @Override
    public void stop(Object id)  {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            Logger.trace("Stop media player with ID '{}'", id);
            mediaPlayer.stop();
        }
        else if (value instanceof URL) {
            Logger.warn("Stopping audio clip with ID '{}' is not supported", id);
        }
    }

    @Override
    public boolean isStopped(Object id) {
        requireNonNull(id);
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.STOPPED;
        }
        return false;
    }

    @Override
    public void stopAll() {
        soundMap.values().stream().filter(MediaPlayer.class::isInstance).map(MediaPlayer.class::cast).forEach(MediaPlayer::stop);
        stopSiren();
        stopVoice();
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    @Override
    public void playVoice(SoundID id, double delaySeconds) {
        requireNonNull(id);
        if (!id.isVoiceID()) {
            Logger.error("Sound ID '{}' is no voice ID", id);
        }
        Object value = soundMap.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            currentVoice = mediaPlayer;
            currentVoice.setStartTime(Duration.seconds(delaySeconds));
            Logger.trace("Play voice");
            currentVoice.play();
        }
    }

    @Override
    public void stopVoice() {
        if (currentVoice != null) {
            Logger.trace("Stop voice");
            currentVoice.stop();
        }
    }

    @Override
    public void playSiren(SoundID sirenID, double volume) {
        requireNonNull(sirenID);
        if (!sirenID.isSirenID()) {
            throw new IllegalArgumentException("Illegal siren ID '%s'".formatted(sirenID));
        }
        if (currentSirenID != sirenID) {
            stopSiren();
        }
        currentSirenID = sirenID;
        MediaPlayer siren = (MediaPlayer) soundMap.get(sirenID);
        if (siren.getStatus() != MediaPlayer.Status.PLAYING) {
            siren.setVolume(volume);
            siren.setCycleCount(MediaPlayer.INDEFINITE);
            Logger.trace("Play siren with ID '{}' (indefinite times) at volume {}", sirenID, siren.getVolume());
            siren.play();
        }
    }

    @Override
    public void pauseSiren() {
        if (currentSirenID != null) {
            MediaPlayer siren = (MediaPlayer) soundMap.get(currentSirenID);
            Logger.info("Paused siren with ID '{}'", currentSirenID);
            siren.pause();
        }
    }

    @Override
    public void stopSiren() {
        if (currentSirenID != null) {
            MediaPlayer siren = (MediaPlayer) soundMap.get(currentSirenID);
            Logger.trace("Stop siren with ID '{}'", currentSirenID);
            siren.stop();
        }
    }
}
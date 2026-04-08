/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
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

    private final BooleanProperty muteProperty = new SimpleBooleanProperty(false);

    private final Map<Object, Object> map = new HashMap<>();

    public SoundManager() {}

    @Override
    public void dispose() {
        stopAll();
        enabledProperty.unbind();
        final int numEntries = map.size();
        map.clear();
        Logger.info("{} sound objects removed", numEntries);
    }

    public MediaPlayer mediaPlayer(Object key) {
        requireNonNull(key);
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("Unknown media player key '%s'".formatted(key));
        }
        if (map.get(key) instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer;
        }
        throw new IllegalArgumentException("Sound entry with key '%s' is not a media player".formatted(key));
    }

    public void register(Object key, Object value) {
        Object prevValue = map.put(key, value);
        if (prevValue != null) {
            Logger.warn("Replaced sound '{}', was {}", key, value);
        }
    }

    public void registerAudioClipURL(Object key, URL url) {
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
            () -> muteProperty().get() || !enabledProperty().get(),
            muteProperty(), enabledProperty()
        ));
        Logger.info("Media player: key='{}', URL='{}'", key, url);
        register(key, mediaPlayer);
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

    public BooleanProperty muteProperty() {
        return muteProperty;
    }

    public void loop(Object soundID) {
        Object value = map.get(soundID);
        if (value == null) {
            return; // ignore missing sound
        }
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
        }
        play(soundID, MediaPlayer.INDEFINITE);
    }

    public void play(Object soundID) {
        play(soundID, 1);
    }

    public void play(Object soundID, int repetitions) {
        requireNonNull(soundID);
        if (muteProperty.get()) {
            Logger.trace("Sound '{}' not played (reason: muted)", soundID);
            return;
        }
        if (!enabledProperty.get()) {
            Logger.trace("Sound '{}' not played (reason: disabled)", soundID);
            return;
        }
        if (!map.containsKey(soundID)) {
            Logger.error("Sound '{}' not played (reason: not registered)", soundID);
            return;
        }
        Object value = map.get(soundID);
        switch (value) {
            case MediaPlayer mediaPlayer -> {
                Logger.trace("Play media player ({} times) with ID '{}'",
                    repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, soundID);
                mediaPlayer.setCycleCount(repetitions);
                mediaPlayer.play();
            }
            case URL url -> {
                Logger.trace("Create and play audio clip ({} times) with ID '{}'",
                    repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, soundID);
                final var audioClip = new AudioClip(url.toExternalForm());
                audioClip.setCycleCount(repetitions);
                audioClip.play(1.0); //TODO add volume parameter?
            }
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public boolean isPlaying(Object soundID) {
        requireNonNull(soundID);
        Object value = map.get(soundID);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    public void pause(Object soundID) {
        requireNonNull(soundID);
        Object value = map.get(soundID);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.pause();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be paused", soundID);
        }
    }

    public void stop(Object soundID)  {
        requireNonNull(soundID);
        Object value = map.get(soundID);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.stop();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be stopped", soundID);
        }
    }

    public void stopAll() {
        map.values().stream().filter(MediaPlayer.class::isInstance).map(MediaPlayer.class::cast).forEach(MediaPlayer::stop);
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }
}
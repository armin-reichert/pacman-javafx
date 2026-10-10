/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.sound;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.pacmanfx.uilib.widgets.Voice;
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

    public sealed interface SoundResource permits AudioClipResource, MediaPlayerResource {}

    public record AudioClipResource(AudioClip clip) implements SoundResource {}

    public record MediaPlayerResource(MediaPlayer player) implements SoundResource {}

    public record SoundEntry(Class<? extends SoundResource> type, Named id, URL url) {

        public static SoundEntry audioClip(Named id, URL url) {
            return new SoundEntry(AudioClipResource.class, id, url);
        }

        public static SoundEntry mediaPlayer(Named id, URL url) {
            return new SoundEntry(MediaPlayerResource.class, id, url);
        }
    }

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private final BooleanProperty mute = new SimpleBooleanProperty(false);

    private final Map<Named, SoundResource> soundMap = new HashMap<>();

    private final Voice voice = new Voice();

    public SoundManager() {
        voice.muteProperty().bind(Bindings.createBooleanBinding(
            () -> mute.get() || !enabled.get(),
            mute, enabled
        ));
    }

    public Voice voice() {
        return voice;
    }

    @Override
    public void dispose() {
        stopAll();
        enabled.unbind();
        soundMap.clear();
    }

    public void add(SoundEntry entry) {
        requireNonNull(entry);
        if (entry.type() == AudioClipResource.class) {
            addAudioClip(entry.id(), entry.url());
        } else if (entry.type() == MediaPlayerResource.class) {
            addMediaPlayer(entry.id(), entry.url());
        }
    }

    public void remove(SoundEntry entry) {
        requireNonNull(entry);
        unregister(entry.id());
    }

    public void addAudioClip(Named soundID, URL url) {
        requireNonNull(soundID);
        requireNonNull(url);

        register(soundID, new AudioClipResource(new AudioClip(url.toExternalForm())));
    }

    public void addMediaPlayer(Named soundID, URL url) {
        requireNonNull(soundID);
        requireNonNull(url);

        final var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
            () -> muteProperty().get() || !enabledProperty().get(),
            muteProperty(), enabledProperty()
        ));
        register(soundID, new MediaPlayerResource(player));
    }

    public void unregister(Named soundID) {
        requireNonNull(soundID);
        soundMap.remove(soundID);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public boolean isMute() {
        return mute.get();
    }

    public void playLoop(Named soundID) {
        requireNonNull(soundID);

        final SoundResource value = soundMap.get(soundID);
        if (value == null) {
            return; // ignore missing sound
        }
        if (value instanceof MediaPlayerResource(MediaPlayer mediaPlayer)) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
        }
        play(soundID, MediaPlayer.INDEFINITE);
    }

    public void play(Named soundID) {
        play(soundID, 1);
    }

    public void play(Named soundID, int repetitions) {
        requireNonNull(soundID);
        if (isMute() || !isEnabled()) {
            return;
        }
        if (!soundMap.containsKey(soundID)) {
            Logger.error("Sound '{}' not played (reason: not registered)", soundID);
            return;
        }
        final SoundResource value = soundMap.get(soundID);
        switch (value) {
            case MediaPlayerResource(MediaPlayer player) -> {
                player.setCycleCount(repetitions);
                player.play();
            }
            case AudioClipResource(AudioClip clip) -> {
                clip.setCycleCount(repetitions);
                clip.play(1.0); //TODO add volume parameter?
            }
        }
    }

    public boolean isPlaying(Named soundID) {
        requireNonNull(soundID);
        return switch (soundMap.get(soundID)) {
            case null -> false;
            case MediaPlayerResource(MediaPlayer player) -> player.getStatus().equals(MediaPlayer.Status.PLAYING);
            case AudioClipResource(_) -> false;
        };
    }

    public void pause(Named soundID) {
        requireNonNull(soundID);
        switch (soundMap.get(soundID)) {
            case null -> {}
            case MediaPlayerResource(MediaPlayer player) -> player.pause();
            case AudioClipResource(AudioClip _) -> Logger.warn("Audio clip id='{}' cannot be paused", soundID);
        }
    }

    public void stop(Named soundID)  {
        requireNonNull(soundID);
        switch (soundMap.get(soundID)) {
            case null -> {}
            case MediaPlayerResource(MediaPlayer player) -> player.stop();
            case AudioClipResource(AudioClip _) -> Logger.warn("Audio clip id='{}' cannot be stopped", soundID);
        }
    }

    public void stopAll() {
        soundMap.values().forEach(sound -> {
            if (sound instanceof MediaPlayerResource(MediaPlayer mediaPlayer)) {
                mediaPlayer.stop();
            }
        });
        Logger.debug("All media players stopped");
    }

    public MediaPlayer mediaPlayer(Named soundID) {
        requireNonNull(soundID);
        return switch (soundMap.get(soundID)) {
            case MediaPlayerResource(MediaPlayer player) -> player;
            case AudioClipResource(AudioClip _) -> throw new IllegalArgumentException(
                "Sound entry with id='%s' is not a media player".formatted(soundID));
        };
    }

    // private

    private void register(Named soundID, SoundResource defaultValue) {
        final SoundResource prevValue = soundMap.put(soundID, defaultValue);
        if (prevValue != null) {
            Logger.warn("Replaced sound id='{}': {} (was: {})", soundID, defaultValue);
        }
        Logger.debug("Registered sound id='{}': {}", soundID, defaultValue);
    }
}
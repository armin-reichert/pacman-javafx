/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.sound;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.game.Game;
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

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty muteProperty = new SimpleBooleanProperty(false);

    private final Map<SoundID, SoundResource> soundMap = new HashMap<>();

    private final Game game;

    public SoundManager(Game game) {
        this.game = requireNonNull(game);
    }

    private MediaPlayer voicePlayer;

    public void playVoice(Media voiceMedia) {
        requireNonNull(voiceMedia);
        if (voicePlayer != null && voicePlayer.getMedia().equals(voiceMedia)) {
            Logger.warn("Voice {} already playing", voiceMedia);
            return;
        }
        stopAndDisposeVoice();
        voicePlayer = new MediaPlayer(voiceMedia);
        voicePlayer.muteProperty().bind(game.ui().settings().mutedProperty);
        voicePlayer.setOnError(() ->
            Logger.error("Voice playback error: {}", voicePlayer.getError())
        );
        voicePlayer.play();
    }

    public void stopAndDisposeVoice() {
        if (voicePlayer != null) {
            voicePlayer.stop();
            voicePlayer.muteProperty().unbind();
            voicePlayer.dispose();
            voicePlayer = null;
        }
    }

    @Override
    public void dispose() {
        final int numEntries = soundMap.size();
        stopAll();
        enabledProperty.unbind();
        soundMap.clear();
        if (voicePlayer != null) {
            stopAndDisposeVoice();
        }
        Logger.info("{} sound objects removed", numEntries);
    }

    public void setAudioClip(SoundID soundID, URL url) {
        requireNonNull(soundID);
        requireNonNull(url);

        registerSoundResource(soundID, new AudioClipResource(new AudioClip(url.toExternalForm())));
    }

    public void setMediaPlayer(SoundID soundID, URL url) {
        requireNonNull(soundID);
        requireNonNull(url);

        final var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
            () -> muteProperty().get() || !enabledProperty().get(),
            muteProperty(), enabledProperty()
        ));
        registerSoundResource(soundID, new MediaPlayerResource(player));
    }

    public void unregisterSound(SoundID soundID) {
        requireNonNull(soundID);
        soundMap.remove(soundID);
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

    public boolean isMute() {
        return muteProperty.get();
    }

    public void loop(SoundID soundID) {
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

    public void play(SoundID soundID) {
        play(soundID, 1);
    }

    public void play(SoundID soundID, int repetitions) {
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

    public boolean isPlaying(SoundID soundID) {
        requireNonNull(soundID);
        return switch (soundMap.get(soundID)) {
            case null -> false;
            case MediaPlayerResource(MediaPlayer player) -> player.getStatus().equals(MediaPlayer.Status.PLAYING);
            case AudioClipResource(_) -> false;
        };
    }

    public void pause(SoundID soundID) {
        requireNonNull(soundID);
        switch (soundMap.get(soundID)) {
            case null -> {}
            case MediaPlayerResource(MediaPlayer player) -> player.pause();
            case AudioClipResource(AudioClip _) -> Logger.warn("Audio clip id='{}' cannot be paused", soundID);
        }
    }

    public void stop(SoundID soundID)  {
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

    public MediaPlayer mediaPlayer(SoundID soundID) {
        requireNonNull(soundID);
        return switch (soundMap.get(soundID)) {
            case MediaPlayerResource(MediaPlayer player) -> player;
            case AudioClipResource(AudioClip _) -> throw new IllegalArgumentException(
                "Sound entry with id='%s' is not a media player".formatted(soundID));
        };
    }

    // private

    private void registerSoundResource(SoundID soundID, SoundResource defaultValue) {
        final SoundResource prevValue = soundMap.put(soundID, defaultValue);
        if (prevValue != null) {
            Logger.warn("Replaced sound id='{}': {} (was: {})", soundID, defaultValue);
        }
        Logger.debug("Registered sound id='{}': {}", soundID, defaultValue);
    }
}
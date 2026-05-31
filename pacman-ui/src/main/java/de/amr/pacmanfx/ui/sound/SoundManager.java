/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.sound;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.GameUI_Constants;
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

    private final Map<SoundID, Object> soundMap = new HashMap<>();

    public SoundManager() {}

    private MediaPlayer voicePlayer;

    public void playVoice(Media voiceMedia) {
        requireNonNull(voiceMedia);
        if (voicePlayer != null && voicePlayer.getMedia().equals(voiceMedia)) {
            Logger.warn("Voice {} already playing", voiceMedia);
            return;
        }
        stopAndDisposeVoice();
        voicePlayer = new MediaPlayer(voiceMedia);
        voicePlayer.muteProperty().bind(GameUI_Constants.PROPERTY_MUTED);
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
        registerSoundURL(soundID, url);
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

        registerSoundURL(soundID, player);
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

        final Object value = soundMap.get(soundID);
        if (value == null) {
            return; // ignore missing sound
        }
        if (value instanceof MediaPlayer mediaPlayer) {
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
        final Object value = soundMap.get(soundID);
        switch (value) {
            case MediaPlayer player -> {
                player.setCycleCount(repetitions);
                player.play();
            }
            case URL url -> {
                final var clip = new AudioClip(url.toExternalForm());
                clip.setCycleCount(repetitions);
                clip.play(1.0); //TODO add volume parameter?
            }
            default -> throw new IllegalStateException("Unexpected sound map value %s".formatted(value));
        }
    }

    public boolean isPlaying(SoundID soundID) {
        requireNonNull(soundID);
        final Object value = soundMap.get(soundID);
        return value instanceof MediaPlayer player
            && (player.getStatus() == MediaPlayer.Status.PLAYING);
    }

    public void pause(SoundID soundID) {
        requireNonNull(soundID);
        final Object value = soundMap.get(soundID);
        if (value instanceof MediaPlayer player) {
            player.pause();
        }
        else if (value instanceof URL url) {
            Logger.warn("Audio clip id='{}' url='{}' cannot be paused", soundID, url);
        }
    }

    public void stop(SoundID soundID)  {
        requireNonNull(soundID);
        final Object value = soundMap.get(soundID);
        if (value instanceof MediaPlayer player) {
            player.stop();
        }
        else if (value instanceof URL url) {
            Logger.warn("Audio clip id='{}' url='{}' cannot be stopped", soundID, url);
        }
    }

    public void stopAll() {
        soundMap.values().stream()
            .filter(MediaPlayer.class::isInstance)
            .map(MediaPlayer.class::cast)
            .forEach(MediaPlayer::stop);
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    public MediaPlayer mediaPlayer(SoundID soundID) {
        requireNonNull(soundID);
        if (!soundMap.containsKey(soundID)) {
            throw new IllegalArgumentException("Unknown sound ID '%s'".formatted(soundID));
        }
        if (!(soundMap.get(soundID) instanceof MediaPlayer player)) {
            throw new IllegalArgumentException("Sound entry with id='%s' is not a media player".formatted(soundID));
        }
        return player;
    }

    // private

    private void registerSoundURL(SoundID soundID, Object object) {
        final Object prevValue = soundMap.put(soundID, object);
        if (prevValue != null) {
            Logger.warn("Replaced sound id='{}': {} (was: {})", soundID, object);
        }
        Logger.info("Registered sound id='{}': {}", soundID, object);
    }
}
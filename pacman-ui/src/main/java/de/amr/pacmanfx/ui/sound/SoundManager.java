/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.lib.Disposable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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

    private static final float VOICE_FADE_OUT_SECONDS = 1.5f;

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, Object> map = new HashMap<>();

    private MediaPlayer voicePlayer;
    private final PauseTransition voiceDelay = new PauseTransition();

    private SirenPlayer sirenPlayer;

    public SoundManager() {
        mutedProperty().addListener((_, _, muted) -> {
            if (muted && sirenPlayer != null) {
                sirenPlayer.stopSirens();
            }
        });
    }

    @Override
    public void dispose() {
        Logger.info("Dispose sound manager: {} entries", map.size());
        stopAll();
        enabledProperty.unbind();
        mutedProperty.unbind();
        map.clear();
        voicePlayer = null;
        sirenPlayer = null;
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
            () -> mutedProperty().get() || !enabledProperty().get(),
            mutedProperty(), enabledProperty()
        ));
        Logger.info("Media player: key='{}', URL='{}'", key, url);
        register(key, mediaPlayer);
    }

    public void registerMedia(Object key, URL url) {
        requireNonNull(key);
        requireNonNull(url);
        var media = new Media(url.toExternalForm());
        Logger.info("Media: key='{}', URL='{}'", key, url);
        register(key, media);
    }

    public void registerSirens(URL... sirenURLs) {
        sirenPlayer = new SirenPlayer(sirenURLs);
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
        Object value = map.get(id);
        if (value == null) {
            return; // ignore missing sound
        }
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
            Logger.trace("Sound '{}' not played (reason: muted)", id);
            return;
        }
        if (!enabledProperty.get()) {
            Logger.trace("Sound '{}' not played (reason: disabled)", id);
            return;
        }
        if (!map.containsKey(id)) {
            Logger.error("Sound '{}' not played (reason: not registered)", id);
            return;
        }
        Object value = map.get(id);
        switch (value) {
            case MediaPlayer mediaPlayer -> {
                Logger.trace("Play media player ({} times) with ID '{}'",
                    repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
                mediaPlayer.setCycleCount(repetitions);
                mediaPlayer.play();
            }
            case URL url -> {
                Logger.trace("Create and play audio clip ({} times) with ID '{}'",
                    repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
                final var audioClip = new AudioClip(url.toExternalForm());
                audioClip.setCycleCount(repetitions);
                audioClip.play(1.0); //TODO add volume parameter?
            }
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public boolean isPlaying(Object id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    public void pause(Object id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.pause();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be paused", id);
        }
    }

    public void stop(Object id)  {
        requireNonNull(id);
        Object value = map.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            mediaPlayer.stop();
        }
        else if (value instanceof URL) {
            Logger.warn("Audio clip '{}' cannot be stopped", id);
        }
    }

    public void stopAll() {
        stopSiren();
        stopVoice();
        map.values().stream().filter(MediaPlayer.class::isInstance).map(MediaPlayer.class::cast).forEach(MediaPlayer::stop);
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    // Voices

    public void playVoiceAfterSec(float delaySeconds, SoundID soundID) {
        requireNonNull(soundID);
        if (!soundID.isVoiceID()) {
            Logger.error("Sound ID '{}' is no voice ID", soundID);
            return;
        }
        final Media voice = valueOfType(soundID, Media.class);
        voicePlayer = new MediaPlayer(voice);
        if (delaySeconds > 0) {
            voicePlayer.setOnReady(() -> {
                voiceDelay.stop();
                voiceDelay.setDuration(Duration.seconds(delaySeconds));
                voiceDelay.setOnFinished(_ -> {
                    if (voicePlayer != null) voicePlayer.play();
                });
                voiceDelay.play();
            });
        }
        else {
            voicePlayer.play();
        }
    }

    public void stopVoice() {
        voiceDelay.stop();
        if (voicePlayer == null) {
            return;
        }
        if (voicePlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            final var fade = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(
                    voicePlayer.volumeProperty(), voicePlayer.getVolume())),
                new KeyFrame(Duration.seconds(VOICE_FADE_OUT_SECONDS), new KeyValue(
                    voicePlayer.volumeProperty(), 0))
            );
            fade.setOnFinished(_ -> {
                if (voicePlayer != null) {
                    voicePlayer.stop();
                    voicePlayer.setVolume(1);
                }
            });
            fade.play();
        }
    }

    // Sirens

    public void playSiren(int number, double volume) {
        if (sirenPlayer == null) {
            Logger.error("No sirens registered");
            return;
        }
        sirenPlayer.ensureSirenPlaying(number, volume);
    }

    public void stopSiren() {
        if (sirenPlayer == null) {
            Logger.error("No sirens registered");
            return;
        }
        sirenPlayer.stopCurrentSiren();
    }

    @SuppressWarnings("unchecked")
    private <C> C valueOfType(Object key, Class<C> type) {
        requireNonNull(key);
        requireNonNull(type);
        final Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("No sound value with key '%s' exists".formatted(key));
        }
        if (type.isInstance(value)) {
            return (C) value;
        }
        throw new IllegalArgumentException("Sound '%s' is not a media object".formatted(key));
    }
}
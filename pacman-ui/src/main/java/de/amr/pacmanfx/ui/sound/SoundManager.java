/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.lib.Disposable;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
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
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class SoundManager implements Disposable {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, Object> map = new HashMap<>();

    private MediaPlayer voicePlayer;
    private final PauseTransition voiceDelay = new PauseTransition();

    private SoundID currentSirenID;
    private MediaPlayer sirenPlayer;

    public SoundManager() {}

    @Override
    public void dispose() {
        stopAll();
        enabledProperty.unbind();
        mutedProperty.unbind();
        map.clear();
        voicePlayer = null;
        currentSirenID = null;
        Logger.info("Disposed default sound manager {}", this);
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
            Logger.warn("Replaced sound  with key '{}', old value was {}", key, value);
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
        Logger.info("Media player registered: key='{}', URL='{}'", key, url);
        register(key, mediaPlayer);
    }

    public void registerMedia(Object key, URL url) {
        requireNonNull(key);
        requireNonNull(url);
        var media = new Media(url.toExternalForm());
        Logger.info("Media registered: key='{}', URL='{}'", key, url);
        register(key, media);
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
        Object value = map.get(id);
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
        if (!map.containsKey(id)) {
            Logger.error("No media player and no clip URL registered with ID '{}'", id);
            return;
        }
        Object value = map.get(id);
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

    public boolean isPaused(Object id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED;
        }
        return false;
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

    public boolean isStopped(Object id) {
        requireNonNull(id);
        Object value = map.get(id);
        if (value instanceof MediaPlayer mediaPlayer) {
            return mediaPlayer.getStatus() ==  MediaPlayer.Status.STOPPED;
        }
        return false;
    }

    public void stopAll() {
        allOfType(MediaPlayer.class).forEach(MediaPlayer::stop);
        if (sirenPlayer != null) {
            stopSiren();
        }
        if (voicePlayer != null) {
            stopVoice();
        }
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    public void playVoiceAfterSec(float delaySeconds, SoundID id) {
        requireNonNull(id);
        if (!id.isVoiceID()) {
            Logger.error("Sound ID '{}' is no voice ID", id);
            return;
        }
        Media voiceMedia = valueOfType(id, Media.class);
        voicePlayer = new MediaPlayer(voiceMedia);
        if (delaySeconds > 0) {
            voicePlayer.setOnReady(() -> {
                voiceDelay.stop();
                voiceDelay.setDuration(Duration.seconds(delaySeconds));
                voiceDelay.setOnFinished(e -> voicePlayer.play());
                voiceDelay.play();
            });
        }
        else {
            voicePlayer.play();
        }
    }

    public void stopVoice() {
        if (voicePlayer != null) {
            Logger.trace("Stop voice");
            if (voiceDelay.getStatus() == Animation.Status.RUNNING) {
                voiceDelay.stop();
            }
            voicePlayer.stop();
        }
    }

    public void playSiren(SoundID soundID, double volume) {
        requireNonNull(soundID);
        if (!soundID.isSirenID()) {
            throw new IllegalArgumentException("Illegal siren ID '%s'".formatted(soundID));
        }
        if (soundID != currentSirenID) {
            if (currentSirenID != null) {
                stopSiren();
            }
            createSirenPlayer(soundID);
            currentSirenID = soundID;
        }
        sirenPlayer.setVolume(volume);
        sirenPlayer.play();
        Logger.trace("Playing siren '{}' at volume {}", soundID, sirenPlayer.getVolume());
    }

    public void pauseSiren() {
        if (sirenPlayer != null) {
            Logger.info("Paused siren with ID '{}'", currentSirenID);
            sirenPlayer.pause();
        }
        else Logger.error("Siren player not yet created");
    }

    public void stopSiren() {
        if (sirenPlayer != null) {
            Logger.trace("Stop siren with ID '{}'", currentSirenID);
            sirenPlayer.stop();
        }
        else Logger.error("Siren player not yet created");
    }

    // private stuff

    private <C> Stream<C> allOfType(Class<C> cl_ass) {
        return map.values().stream().filter(cl_ass::isInstance).map(cl_ass::cast);
    }

    @SuppressWarnings("unchecked")
    private <C> C valueOfType(Object key, Class<C> cl_ass) {
        requireNonNull(key);
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown media player key '%s'".formatted(key));
        }
        if (cl_ass.isInstance(value)) {
            return (C) value;
        }
        throw new IllegalArgumentException("Sound entry with key '%s' is not a media object".formatted(key));
    }

    private void createSirenPlayer(SoundID sirenID) {
        sirenPlayer = new MediaPlayer(valueOfType(sirenID, Media.class));
        sirenPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        sirenPlayer.setVolume(1);
        sirenPlayer.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty().get() || !enabledProperty().get(),
                mutedProperty(), enabledProperty()
        ));
        Logger.info("Siren player created, siren ID='{}'", sirenID);
    }
}
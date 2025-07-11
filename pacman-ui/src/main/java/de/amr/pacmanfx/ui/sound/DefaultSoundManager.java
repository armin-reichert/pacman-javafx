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

import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;

//TODO when to destroy all these resources?
public class DefaultSoundManager implements SoundManager {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private final Map<Object, URL> clipURLMap = new HashMap<>();
    private SoundID currentSirenID;
    private MediaPlayer voice;

    public DefaultSoundManager() {}

    public MediaPlayer mediaPlayer(Object key) {
        return mediaPlayerMap.get(key);
    }

    public void registerMediaPlayer(Object id, URL url) {
        requireNonNull(id);
        requireNonNull(url);
        MediaPlayer mediaPlayer = createMediaPlayer(url);
        mediaPlayerMap.put(id, mediaPlayer);
    }

    private MediaPlayer createMediaPlayer(URL url) {
        var mediaPlayer = new MediaPlayer(new Media(url.toExternalForm()));
        mediaPlayer.setVolume(1.0);
        mediaPlayer.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty().get() || !enabledProperty().get(),
                mutedProperty(), enabledProperty()
        ));
        Logger.info("Media player created from URL '{}'", url);
        return mediaPlayer;
    }

    public void registerAudioClip(Object key, URL url) {
        clipURLMap.put(requireNonNull(key), requireNonNull(url));
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
        if (mediaPlayerMap.containsKey(id)) {
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            Logger.trace("Play media player ({} times) with ID '{}'",
                repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
            mediaPlayer.setCycleCount(repetitions);
            mediaPlayer.play();
        }
        else if (clipURLMap.containsKey(id)) {
            URL url = clipURLMap.get(id);
            if (url == null) {
                Logger.error("No audio clip URL found with ID '{}'", id);
            } else {
                Logger.info("Create and play audio clip ({} times) with ID '{}'",
                    repetitions == MediaPlayer.INDEFINITE ? "indefinite" : repetitions, id);
                AudioClip audioClip = new AudioClip(url.toExternalForm());
                audioClip.setCycleCount(repetitions);
                audioClip.play(1.0); //TODO add volume parameter?
            }
        }
        else {
            Logger.error("No media player and no clip URL registered with ID '{}'", id);
        }
    }

    @Override
    public void pause(Object id) {
        if (mediaPlayerMap.containsKey(id)) {
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            if (mediaPlayer != null) {
                Logger.trace("Pause media player with ID '{}'", id);
                mediaPlayer.pause();
            }
        }
        else if (clipURLMap.containsKey(id)) {
            Logger.warn("Pausing audio clip with ID '{}' is not supported", id);
        }
    }

    @Override
    public void stop(Object id)  {
        if (mediaPlayerMap.containsKey(id)) {
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            if (mediaPlayer != null) {
                Logger.trace("Stop media player with ID '{}'", id);
                mediaPlayer.stop();
            }
        }
        else if (clipURLMap.containsKey(id)) {
            Logger.warn("Stopping audio clip with ID '{}' is not supported", id);
        }
    }

    @Override
    public void stopAll() {
        mediaPlayerMap.values().forEach(MediaPlayer::stop);
        stopSiren();
        stopVoice();
        Logger.debug("All sounds (media players, siren, voice) stopped");
    }

    @Override
    public void stopVoice() {
        if (voice != null) {
            Logger.trace("Stop voice");
            voice.stop();
        }
    }

    @Override
    public void playVoice(String assetKey, double delaySeconds) {
        URL url = theAssets().get(assetKey);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        // media player stays in state PLAYING, so we remove the reference when it reaches the end
        voice.muteProperty().bind(mutedProperty);
        voice.setStartTime(Duration.seconds(delaySeconds));
        Logger.trace("Play voice");
        voice.play(); // play also if enabled is set to false!
    }

    @Override
    public void playSiren(SoundID sirenID, double volume) {
        requireNonNull(sirenID);
        if (sirenID != SoundID.SIREN_1 && sirenID != SoundID.SIREN_2 && sirenID != SoundID.SIREN_3 && sirenID != SoundID.SIREN_4) {
            throw new IllegalArgumentException("Illegal siren ID '%s'".formatted(sirenID));
        }
        if (currentSirenID != sirenID) {
            stopSiren();
        }
        currentSirenID = sirenID;
        MediaPlayer siren = mediaPlayerMap.get(sirenID);
        if (siren.getStatus() != MediaPlayer.Status.PLAYING) {
            siren.setVolume(volume);
            siren.setCycleCount(MediaPlayer.INDEFINITE);
            Logger.info("Play siren with ID '{}' (indefinite times) at volume {}", sirenID, siren.getVolume());
            siren.play();
        }
    }

    @Override
    public void pauseSiren() {
        if (currentSirenID != null) {
            mediaPlayerMap.get(currentSirenID).pause();
            Logger.info("Paused siren with ID '{}'", currentSirenID);
        }
    }

    @Override
    public void stopSiren() {
        if (currentSirenID != null) {
            Logger.info("Stop siren with ID '{}'", currentSirenID);
            mediaPlayerMap.get(currentSirenID).stop();
        }
    }
}
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

public class DefaultSoundManager implements SoundManager {

    private final String assetNamespace;
    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<Object, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private final Map<Object, URL> clipURLMap = new HashMap<>();
    private SoundID currentSirenID;

    private MediaPlayer voice;


    public DefaultSoundManager(String assetNamespace) {
        this.assetNamespace = requireNonNull(assetNamespace);
    }

    public MediaPlayer mediaPlayer(Object key) {
        return mediaPlayerMap.get(key);
    }

    public void registerMediaPlayer(Object id, URL url) {
        MediaPlayer mediaPlayer = createMediaPlayer(url);
        mediaPlayerMap.put(id, mediaPlayer);
    }

    private MediaPlayer createMediaPlayer(URL url) {
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty().get() || !enabledProperty().get(),
                mutedProperty(), enabledProperty()
        ));
        Logger.debug("Media player created from URL {}", url);
        return player;
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
        if (mutedProperty.get()) {
            Logger.trace("Sound with ID {} not played, sound is muted", id);
            return;
        }
        if (!enabledProperty.get()) {
            Logger.trace("Sound with ID {} not played, sound is disabled", id);
            return;
        }
        if (mediaPlayerMap.containsKey(id)) {
            Logger.info("Play media player with ID='{}'", id);
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            if (mediaPlayer != null) {
                mediaPlayer.setCycleCount(repetitions);
                mediaPlayer.play();
            } else {
                Logger.warn("Cannot play sound ID '{}': no media player found", id);
            }
        }
        else if (clipURLMap.containsKey(id)) {
            Logger.info("Create and play audio clip for ID '{}'", id);
            URL url = clipURLMap.get(id);
            if (url == null) {
                Logger.error("No audio clip URL found for ID '{}'", id);
            } else {
                AudioClip audioClip = new AudioClip(url.toExternalForm());
                audioClip.setCycleCount(repetitions);
                audioClip.play(1.0); //TODO volume
            }
        }
        else {
            Logger.error("No media player and no clip URL registered for ID='{}'", id);
        }
    }

    @Override
    public void pause(Object id) {
        if (mediaPlayerMap.containsKey(id)) {
            Logger.debug("Pause media player for ID '{}'", id);
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        }
        else if (clipURLMap.containsKey(id)) {
            Logger.debug("Pausing audio clip with ID='{}' is not supported", id);
        }
    }

    @Override
    public void stop(Object id)  {
        if (mediaPlayerMap.containsKey(id)) {
            Logger.debug("Stop media player for ID '{}'", id);
            MediaPlayer mediaPlayer = mediaPlayerMap.get(id);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        }
        else if (clipURLMap.containsKey(id)) {
            Logger.debug("Stopping audio clip with ID='{}' is not supported", id);
        }
    }

    @Override
    public void stopAll() {
        mediaPlayerMap.forEach((key, player) -> player.stop());
        stop(SoundID.PAC_MAN_MUNCHING); // TODO check
        stopSiren();
        stopVoice();
        Logger.debug("All sounds stopped");
    }

    @Override
    public void stopVoice() {
        if (voice != null) {
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
        voice.play(); // play also if enabledPy is set to false

    }

    @Override
    public void playSiren(SoundID sirenID, double volume) {
        requireNonNull(sirenID);
        if (sirenID != SoundID.SIREN_1
                && sirenID != SoundID.SIREN_2
                && sirenID != SoundID.SIREN_3
                && sirenID != SoundID.SIREN_4) {
            throw new IllegalArgumentException("Illegal sound ID for siren: " + sirenID);
        }
        if (currentSirenID != sirenID) {
            stopSiren();
            MediaPlayer sirenPlayer = mediaPlayerMap.get(sirenID);
            if (sirenPlayer == null) {
                Logger.error("Could not create media player for siren ID {}", sirenID);
                currentSirenID = null;
            } else {
                currentSirenID = sirenID;
                sirenPlayer.setVolume(volume);
                sirenPlayer.play();
                Logger.info("Created new siren player {}, playing at volume {} ", sirenID, sirenPlayer.getVolume());
            }
        } else {
            MediaPlayer sirenPlayer = mediaPlayerMap.get(sirenID);
            sirenPlayer.setVolume(volume);
            sirenPlayer.play();
            Logger.trace("Playing siren {} at volume {}", sirenID, sirenPlayer.getVolume());
        }
    }

    @Override
    public void pauseSiren() {
        if (currentSirenID != null) {
            mediaPlayerMap.get(currentSirenID).pause();
        }
    }

    @Override
    public void stopSiren() {
        if (currentSirenID != null) {
            mediaPlayerMap.get(currentSirenID).stop();
        }
    }
}

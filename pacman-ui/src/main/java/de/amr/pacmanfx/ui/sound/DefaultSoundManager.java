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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;

public class DefaultSoundManager implements SoundManager {

    private final String assetNamespace;
    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    //TODO store as assets?
    protected final Map<String, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private final EnumMap<SoundID, MediaPlayer> sirenPlayerMap = new EnumMap<>(SoundID.class);
    private SoundID currentSirenID;

    private MediaPlayer voice;

    public DefaultSoundManager(String assetNamespace) {
        this.assetNamespace = requireNonNull(assetNamespace);
    }

    public MediaPlayer addMediaPlayer(SoundID id, int numRepetitions) {
        MediaPlayer mediaPlayer = createMediaPlayer(id.keySuffix(), numRepetitions);
        if (mediaPlayer != null) {
            mediaPlayerMap.put(id.keySuffix(), mediaPlayer);
        } else {
            Logger.warn("Media player for ID '{}' could not be created", id);
        }
        return mediaPlayer;
    }

    @Override
    public MediaPlayer createMediaPlayer(String keySuffix, int numRepetitions) {
        String key = assetNamespace + keySuffix;
        URL url = theAssets().get(key);
        if (url == null) {
            Logger.warn("Missing audio resource '%s'".formatted(keySuffix));
            return null;
        }
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(numRepetitions);
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty().get() || !enabledProperty().get(),
                mutedProperty(), enabledProperty()
        ));
        player.statusProperty().addListener((py,ov,nv) -> logPlayerStatusChange(player, keySuffix, ov, nv));
        Logger.debug("Media player created from URL {}", url);
        return player;
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
    public void playAudioClip(String keySuffix, double volume) {
        requireNonNull(keySuffix);
        if (!mutedProperty.get() && enabledProperty.get()) {
            String key = assetNamespace + keySuffix;
            AudioClip clip = theAssets().get(key);
            if (clip == null) {
                Logger.error("No audio clip with key {}", key);
                return;
            }
            clip.setVolume(volume);
            clip.play();
        }
    }

    private Optional<MediaPlayer> mediaPlayer(String key) {
        return Optional.ofNullable(mediaPlayerMap.get(key));
    }

    public void play(SoundID id) {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Play media player '{}'", id.keySuffix());
                Optional<MediaPlayer> optPlayer = mediaPlayer(id.keySuffix());
                if (optPlayer.isPresent()) {
                    optPlayer.get().play();
                } else {
                    Logger.warn("Cannot play sound ID '{}': no media player found", id);
                }
            }
            case CLIP -> {
                Logger.debug("Play audio clip '{}'", id.keySuffix());
                playAudioClip(id.keySuffix(), 1);
            }
        }
    }

    public void pause(SoundID id) {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Pause media player '{}'", id.keySuffix());
                mediaPlayer(id.keySuffix()).ifPresent(MediaPlayer::pause);
            }
            case CLIP -> {
                Logger.debug("Pausing audio clip '{}' not supported", id.keySuffix());
            }
        }
    }

    public void stop(SoundID id)  {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Play media player '{}'", id.keySuffix());
                mediaPlayer(id.keySuffix()).ifPresent(MediaPlayer::stop);
            }
            case CLIP -> {
                Logger.debug("Stopping audio clip '{}' not supported", id.keySuffix());
            }
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
        if (!sirenPlayerMap.containsKey(sirenID) || currentSirenID != sirenID) {
            stopSiren();
            MediaPlayer sirenPlayer = createMediaPlayer(sirenID.keySuffix(), MediaPlayer.INDEFINITE);
            if (sirenPlayer == null) {
                Logger.error("Could not create media player for siren ID {}", sirenID);
                currentSirenID = null;
            } else {
                currentSirenID = sirenID;
                sirenPlayerMap.put(currentSirenID, sirenPlayer);
                sirenPlayer.setVolume(volume);
                sirenPlayer.play();
                Logger.info("Created new siren player, playing at volume " + sirenPlayer.getVolume());
            }
        }
    }

    @Override
    public void pauseSiren() {
        if (currentSirenID != null) {
            sirenPlayerMap.get(currentSirenID).stop();
        }
    }

    @Override
    public void stopSiren() {
        if (currentSirenID != null) {
            sirenPlayerMap.get(currentSirenID).stop();
        }
    }

    private void logMediaPlayerStatus() {
        for (String key : mediaPlayerMap.keySet()) {
            mediaPlayer(key).ifPresent(player -> logMediaPlayerStatus(player, key));
        }
        if (currentSirenID != null) {
            logMediaPlayerStatus(sirenPlayerMap.get(currentSirenID), currentSirenID.keySuffix());
        }
        logMediaPlayerStatus(voice, "Voice");
    }

    private void logMediaPlayerStatus(MediaPlayer player, String key) {
        if (player != null) {
            Logger.debug("[{}] state={} volume={}", key, player.getStatus() != null ? player.getStatus() : "UNDEFINED", player.getVolume());
        } else {
            Logger.debug("No player exists for key {}", key);
        }
    }

    private void logPlayerStatusChange(MediaPlayer player, String key, MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
        Logger.debug("[{}] {} -> {}, volume {}", key, (oldStatus != null ? oldStatus : "undefined"), newStatus, player.getVolume());
    }

}

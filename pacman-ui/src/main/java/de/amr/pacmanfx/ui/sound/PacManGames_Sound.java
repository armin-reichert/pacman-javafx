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
import java.util.Optional;

import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static java.util.Objects.requireNonNull;

public class PacManGames_Sound implements SoundManager {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<String, Map<String, MediaPlayer>> mediaPlayerMaps = new HashMap<>();

    private String gameVariant;
    private String assetNamespace;
    private Siren siren;
    private MediaPlayer voice;

    public void selectGameVariant(String gameVariant, String assetNamespace) {
        this.gameVariant = requireNonNull(gameVariant);
        this.assetNamespace = requireNonNull(assetNamespace);
        if (mediaPlayerMap(gameVariant).isEmpty()) {
            var map = new HashMap<String, MediaPlayer>();
            mediaPlayerMaps.put(gameVariant, map);
            Logger.debug("Created media player map for game variant {}", gameVariant);

            addMediaPlayerMapEntry(map, SoundID.BONUS_BOUNCING,   createMediaPlayerForID(SoundID.BONUS_BOUNCING, MediaPlayer.INDEFINITE));
            addMediaPlayerMapEntry(map, SoundID.GAME_OVER,        createMediaPlayerForID(SoundID.GAME_OVER, 1));
            addMediaPlayerMapEntry(map, SoundID.GAME_READY,       createMediaPlayerForID(SoundID.GAME_READY, 1));
            addMediaPlayerMapEntry(map, SoundID.GHOST_RETURNS,    createMediaPlayerForID(SoundID.GHOST_RETURNS, MediaPlayer.INDEFINITE));
            addMediaPlayerMapEntry(map, SoundID.LEVEL_COMPLETE,   createMediaPlayerForID(SoundID.LEVEL_COMPLETE, 1));
            addMediaPlayerMapEntry(map, SoundID.PAC_MAN_MUNCHING, createMediaPlayerForID(SoundID.PAC_MAN_MUNCHING, MediaPlayer.INDEFINITE));
            addMediaPlayerMapEntry(map, SoundID.PAC_MAN_DEATH,    createMediaPlayerForID(SoundID.PAC_MAN_DEATH, 1));
            addMediaPlayerMapEntry(map, SoundID.PAC_MAN_POWER,    createMediaPlayerForID(SoundID.PAC_MAN_POWER, MediaPlayer.INDEFINITE));

            //TODO this is total crap, clean it up
            MediaPlayer bounceSound = map.get("bonus_bouncing");
            if (bounceSound != null && gameVariant.equals("MS_PACMAN_TENGEN")) {
                bounceSound.setRate(0.25);
            }
        }
        siren = null;
        logMediaPlayerStatus();
    }

    private void addMediaPlayerMapEntry(HashMap<String, MediaPlayer> map, SoundID id, MediaPlayer sound) {
        if (sound != null) {
            map.put(id.key(), sound);
        }
    }

    public void setEnabled(boolean enabled) {
        enabledProperty.set(enabled);
    }

    public boolean isEnabled() {
        return enabledProperty.get();
    }

    public void playAudioClip(String keySuffix, double volume) {
        requireNonNull(keySuffix);
        if (isUnMuted() && isEnabled()) {
            String key = assetNamespace + ".audio." + keySuffix;
            AudioClip clip = theAssets().get(key);
            if (clip == null) {
                Logger.error("No audio clip with key {}", key);
                return;
            }
            clip.setVolume(volume);
            clip.play();
        }
    }

    public void playVoice(String voiceClipID, double delaySeconds) {
        URL url = theAssets().get(voiceClipID);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        // media player stays in state PLAYING, so we remove the reference when it reaches the end
        voice.muteProperty().bind(mutedProperty);
        voice.setStartTime(Duration.seconds(delaySeconds));
        voice.play(); // play also if enabledPy is set to false
    }

    public void stopVoice() {
        if (voice != null) {
            voice.stop();
        }
    }

    public BooleanProperty mutedProperty() {
        return mutedProperty;
    }

    public void setMuted(boolean muted) {
        mutedProperty.set(muted);
    }

    public void toggleMuted() {
        setMuted(isUnMuted());
    }

    public boolean isUnMuted() {
        return !mutedProperty.get();
    }

    public void selectSiren(int number) {
        if (number < 1 || number > 4) {
            Logger.error("Siren number must be in 1..4 but is " + number);
            return;
        }
        if (siren == null || siren.number() != number) {
            if (siren != null) {
                siren.player().stop();
            }
            MediaPlayer sirenPlayer = createMediaPlayer("siren." + number, MediaPlayer.INDEFINITE);
            if (sirenPlayer == null) {
                //Logger.error("Could not create media player for siren number {}", number);
                siren = null;
            } else {
                sirenPlayer.setVolume(0.5);
                siren = new Siren(number, sirenPlayer);
            }
        }
    }

    public void playSiren() {
        if (siren != null) {
            siren.player().play();
        }
    }

    public void pauseSiren() {
        if (siren != null) {
            siren.player().pause();
        }
    }

    public void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public void stopAll() {
        mediaPlayerMaps.get(gameVariant).forEach((key, player) -> player.stop());
        stop(SoundID.PAC_MAN_MUNCHING); // TODO check
        stopSiren();
        stopVoice();
        Logger.debug("All sounds stopped ({})", gameVariant);
    }

    public void play(SoundID id) {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Play media player '{}'", id.key());
                mediaPlayer(id.key()).ifPresent(MediaPlayer::play);
            }
            case CLIP -> {
                Logger.debug("Play audio clip '{}'", id.key());
                playAudioClip(id.key(), 1);
            }
        }
    }

    public void pause(SoundID id) {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Pause media player '{}'", id.key());
                mediaPlayer(id.key()).ifPresent(MediaPlayer::pause);
            }
            case CLIP -> {
                Logger.debug("Pausing audio clip '{}' not supported", id.key());
            }
        }
    }

    public void stop(SoundID id)  {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                Logger.debug("Play media player '{}'", id.key());
                mediaPlayer(id.key()).ifPresent(MediaPlayer::stop);
            }
            case CLIP -> {
                Logger.debug("Stopping audio clip '{}' not supported", id.key());
            }
        }
    }

    public void dispose(SoundID id) {
        switch (id.type()) {
            case MEDIA_PLAYER -> {
                mediaPlayer(id.key()).ifPresent(mediaPlayer -> {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayerMap(gameVariant).remove(id.key());
                    Logger.debug("Dispose media player '{}'", id.key());
                });
            }
            case CLIP -> {
                Logger.debug("Disposing audio clip '{}' is not supported", id.key());
            }
        }
    }

    private MediaPlayer createMediaPlayerForID(SoundID id, int repetitions) {
        return createMediaPlayer(id.key(), repetitions);
    }

    public MediaPlayer createMediaPlayer(String keySuffix, int repetitions) {
        String key = assetNamespace + ".audio." + keySuffix;
        URL url = theAssets().get(key);
        if (url == null) {
            Logger.warn("Missing audio resource '%s' (%s)".formatted(keySuffix, gameVariant));
            return null;
        }
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(repetitions);
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty.get() || !enabledProperty.get(),
                mutedProperty, enabledProperty
        ));
        player.statusProperty().addListener((py,ov,nv) -> logPlayerStatusChange(player, keySuffix, ov, nv));
        Logger.debug("Media player created from URL {}", url);
        return player;
    }

    private Map<String, MediaPlayer> mediaPlayerMap(String gameVariant) {
        if (!mediaPlayerMaps.containsKey(gameVariant)) {
            mediaPlayerMaps.put(gameVariant, new HashMap<>());
        }
        return mediaPlayerMaps.get(gameVariant);
    }

    private Optional<MediaPlayer> mediaPlayer(String key) {
        return Optional.ofNullable(mediaPlayerMap(gameVariant).get(key));
    }

    private void logMediaPlayerStatus() {
        for (String key : mediaPlayerMap(gameVariant).keySet()) {
            mediaPlayer(key).ifPresent(player -> logMediaPlayerStatus(player, key));
        }
        if (siren != null) {
            logMediaPlayerStatus(siren.player(), "Siren" + siren.number());
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
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

public class PacManGames_Sound {

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);

    private final Map<String, Map<String, MediaPlayer>> mediaPlayersByGameVariant = new HashMap<>();

    private String gameVariant;
    private String assetNamespace;
    private Siren siren;
    private MediaPlayer voice;

    public MediaPlayer createSound(String key, int repetitions) {
        String url = soundURL(key);
        if (url == null) {
            Logger.warn("Missing audio resource '%s' (%s)".formatted(key, gameVariant));
            return null;
        }
        var player = new MediaPlayer(new Media(url));
        player.setCycleCount(repetitions);
        player.setVolume(1.0);
        player.muteProperty().bind(Bindings.createBooleanBinding(
                () -> mutedProperty.get() || !enabledProperty.get(),
                mutedProperty, enabledProperty
        ));
        player.statusProperty().addListener((py,ov,nv) -> logPlayerStatusChange(player, key, ov, nv));
        Logger.debug("Media player created from URL {}", url);
        return player;
    }

    private String soundURL(String keySuffix) {
        String key = assetNamespace + ".audio." + keySuffix;
        URL url = theAssets().get(key);
        return url != null ? url.toExternalForm() : null;
    }

    private Map<String, MediaPlayer> soundsByGameVariant(String gameVariant) {
        if (!mediaPlayersByGameVariant.containsKey(gameVariant)) {
            mediaPlayersByGameVariant.put(gameVariant, new HashMap<>());
        }
        return mediaPlayersByGameVariant.get(gameVariant);
    }

    public void selectGameVariant(String gameVariant, String assetNamespace) {
        this.gameVariant = requireNonNull(gameVariant);
        this.assetNamespace = requireNonNull(assetNamespace);
        if (soundsByGameVariant(gameVariant).isEmpty()) {
            var soundMap = new HashMap<String, MediaPlayer>();
            addSoundMapEntry(soundMap, "game_over",      createSound("game_over"));
            addSoundMapEntry(soundMap, "game_ready",     createSound("game_ready"));
            addSoundMapEntry(soundMap, "ghost_returns",  createSound("ghost_returns", MediaPlayer.INDEFINITE));
            addSoundMapEntry(soundMap, "level_complete", createSound("level_complete"));
            addSoundMapEntry(soundMap, "pacman_munch",   createSound("pacman_munch", MediaPlayer.INDEFINITE));
            addSoundMapEntry(soundMap, "pacman_death",   createSound("pacman_death"));
            addSoundMapEntry(soundMap, "pacman_power",   createSound("pacman_power", MediaPlayer.INDEFINITE));
            addSoundMapEntry(soundMap, "bonus_bouncing", createSound("bonus_bouncing", MediaPlayer.INDEFINITE));

            //TODO this is crap
            MediaPlayer bounceSound = soundMap.get("bonus_bouncing");
            if (bounceSound != null && gameVariant.equals("MS_PACMAN_TENGEN")) {
                bounceSound.setRate(0.25);
            }

            mediaPlayersByGameVariant.put(gameVariant, soundMap);
            Logger.debug("Created sound map for game variant {}", gameVariant);
        }
        siren = null;
        logPlayerStatus();
    }

    private void addSoundMapEntry(HashMap<String, MediaPlayer> soundMap, String key, MediaPlayer sound) {
        if (sound != null) {
            soundMap.put(key, sound);
        }
    }

    private void logPlayerStatus() {
        for (String key : mediaPlayersByGameVariant.get(gameVariant).keySet()) {
            player(key).ifPresent(player -> logPlayerStatus(player, key));
        }
        if (siren != null) {
            logPlayerStatus(siren.player(), "Siren" + siren.number());
        }
        logPlayerStatus(voice, "Voice");
    }

    private void logPlayerStatus(MediaPlayer player, String key) {
        if (player != null) {
            Logger.debug("[{}] state={} volume={}", key, player.getStatus() != null ? player.getStatus() : "UNDEFINED", player.getVolume());
        } else {
            Logger.debug("No player exists for key {}", key);
        }
    }

    private void logPlayerStatusChange(MediaPlayer player, String key, MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
        Logger.debug("[{}] {} -> {}, volume {}", key, (oldStatus != null ? oldStatus : "undefined"), newStatus, player.getVolume());
    }

    public void setEnabled(boolean enabled) {
        enabledProperty.set(enabled);
    }

    public boolean isEnabled() {
        return enabledProperty.get();
    }

    public MediaPlayer createSound(String key) {
        return createSound(key, 1);
    }

    public void playClipIfEnabled(String keySuffix, double volume) {
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

    public Optional<MediaPlayer> player(String key) {
        return Optional.ofNullable(mediaPlayersByGameVariant.get(gameVariant).get(key));
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
            MediaPlayer sirenPlayer = createSound("siren." + number, MediaPlayer.INDEFINITE);
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
        mediaPlayersByGameVariant.get(gameVariant).forEach((key, player) -> player.stop());
        stopMunchingSound(); // TODO check
        stopSiren();
        stopVoice();
        Logger.debug("All sounds stopped ({})", gameVariant);
    }

    public void playBonusBouncingSound() { player("bonus_bouncing").ifPresent(MediaPlayer::play); }

    public void stopBonusBouncingSound() { player("bonus_bouncing").ifPresent(MediaPlayer::stop); }

    public void playBonusEatenSound() {
        playClipIfEnabled("bonus_eaten", 1);
    }

    public void playInsertCoinSound() {
        playClipIfEnabled("credit", 1);
    }

    public void playExtraLifeSound() {
        playClipIfEnabled("extra_life", 1);
    }

    public void playGameOverSound() { player("game_over").ifPresent(MediaPlayer::play); }

    public void playGameReadySound() {
        player("game_ready").ifPresent(MediaPlayer::play);
    }

    public void playGhostEatenSound() {
        playClipIfEnabled("ghost_eaten", 1);
    }

    public void playGhostReturningHomeSound() {
        player("ghost_returns").ifPresent(MediaPlayer::play);
    }

    public void stopGhostReturningHomeSound() { player("ghost_returns").ifPresent(MediaPlayer::stop); }

    public void playLevelChangedSound() {
        playClipIfEnabled("sweep", 1);
    }

    public void playLevelCompleteSound() {
        player("level_complete").ifPresent(MediaPlayer::play);
    }

    public void playMunchingSound() {
        player("pacman_munch").ifPresent(MediaPlayer::play);
    }

    public void stopMunchingSound() { player("pacman_munch").ifPresent(MediaPlayer::stop); }

    public void pauseMunchingSound() { player("pacman_munch").ifPresent(MediaPlayer::pause); }

    public void playPacDeathSound() { player("pacman_death").ifPresent(MediaPlayer::play); }

    public void playPacPowerSound() { player("pacman_power").ifPresent(MediaPlayer::play); }

    public void stopPacPowerSound() { player("pacman_power").ifPresent(MediaPlayer::stop); }
}
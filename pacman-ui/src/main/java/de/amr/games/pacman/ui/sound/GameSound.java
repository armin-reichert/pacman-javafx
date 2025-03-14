/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.sound;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.AssetStorage;
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

import static de.amr.games.pacman.lib.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public class GameSound {

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sound is {}", get() ? "enabled" : "disabled");
        }
    };

    private final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    private GameVariant gameVariant;
    private String assetNamespace;
    private AssetStorage assets;

    private final Map<GameVariant, Map<String, MediaPlayer>> soundsByGameVariant = new EnumMap<>(GameVariant.class);
    {
        for (GameVariant variant : GameVariant.values()) {
            soundsByGameVariant.put(variant, new HashMap<>());
        }
    }

    private Siren siren;
    private MediaPlayer voice;

    private String soundURL(String keySuffix) {
        String assetKey = assetNamespace + ".audio." + keySuffix;
        URL url = assets.get(assetKey);
        return url != null ? url.toExternalForm() : null;
    }

    public MediaPlayer makeSoundLoop(String keySuffix) {
        return makeSound(keySuffix, true);
    }

    public MediaPlayer makeSound(String keySuffix) {
        return makeSound(keySuffix, false);
    }

    private MediaPlayer makeSound(String keySuffix, boolean loop) {
        String url = soundURL(keySuffix);
        if (url == null) {
            Logger.warn("Missing audio resource '%s' (%s)".formatted(keySuffix, gameVariant));
            return null;
        }
        var player = new MediaPlayer(new Media(url));
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(1.0);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logPlayerStatusChange(player, keySuffix, ov, nv));
        Logger.info("Media player created from URL {}", url);
        return player;
    }

    public void clearSounds(GameVariant gameVariant) {
        soundsByGameVariant.get(gameVariant).clear();
    }

    public void selectGameVariant(GameVariant gameVariant, String assetNamespace) {
        this.gameVariant = assertNotNull(gameVariant);
        this.assetNamespace = assertNotNull(assetNamespace);
        if (soundsByGameVariant.get(gameVariant).isEmpty()) {
            var soundMap = new HashMap<String, MediaPlayer>();
            soundMap.put("game_over",      makeSound("game_over"));
            soundMap.put("game_ready",     makeSound("game_ready"));
            soundMap.put("ghost_returns",  makeSoundLoop("ghost_returns"));
            soundMap.put("level_complete", makeSound("level_complete"));
            soundMap.put("pacman_munch",   makeSoundLoop("pacman_munch"));
            soundMap.put("pacman_death",   makeSound("pacman_death"));
            soundMap.put("pacman_power",   makeSoundLoop("pacman_power"));
            soundMap.put("bonus_bouncing", makeSoundLoop("bonus_bouncing"));

            //TODO check this
            MediaPlayer bounceSound = soundMap.get("bonus_bouncing");
            if (bounceSound != null && gameVariant == GameVariant.MS_PACMAN_TENGEN) {
                bounceSound.setRate(0.25);
            }

            soundsByGameVariant.put(gameVariant, soundMap);
            Logger.info("Created sound map for game variant {}", gameVariant);
        }
        siren = null;
        logPlayerStatus();
    }

    public void playVoice(String voiceClipID, double delaySeconds) {
        if (voice != null) {
            Logger.info("Cannot play voice {}, another voice is already playing", voiceClipID);
            return;
        }
        URL url = assets.get(voiceClipID);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        // media player stays in state PLAYING, so we remove the reference when it reaches the end
        voice.setOnEndOfMedia(() -> voice = null);
        voice.muteProperty().bind(mutedPy);
        voice.setStartTime(Duration.seconds(delaySeconds));
        voice.play(); // play also if enabledPy is set to false
    }

    public void stopVoice() {
        if (voice != null) {
            voice.stop();
        }
    }

    private Map<String, MediaPlayer> players(GameVariant gameVariant) {
        return soundsByGameVariant.get(gameVariant);
    }

    private void logPlayerStatus() {
        for (String key : players(gameVariant).keySet()) {
            logPlayerStatus(players(gameVariant).get(key), key);
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

    private void playIfEnabled(MediaPlayer player) {
        if (player == null) {
            //Logger.error("Cannot play sound, player is NULL");
            return;
        }
        if (isEnabled()) {
            player.play();
        }
    }

    private void playIfEnabled(String key) {
        playIfEnabled(players(gameVariant).get(key));
    }

    private void stop(MediaPlayer player) {
        if (player == null) {
            Logger.debug("No media player to stop");
            return;
        }
        player.stop();
    }

    private void stop(String key) {
        stop(players(gameVariant).get(key));
    }

    public void playClipIfEnabled(String keySuffix, double volume) {
        assertNotNull(keySuffix);
        String key = assetNamespace + ".audio." + keySuffix;
        AudioClip clip = assets.get(key);
        if (clip == null) {
            Logger.error("No audio clip with key {}", key);
            return;
        }
        if (isUnMuted() && isEnabled()) {
            clip.setVolume(volume);
            clip.play();
        }
    }

    // Public API

    public void setAssets(AssetStorage assets) {
        this.assets = assertNotNull(assets);
    }

    public BooleanProperty enabledProperty() {
        return enabledPy;
    }

    public BooleanProperty mutedProperty() {
        return mutedPy;
    }

    public void stopAll() {
        for (MediaPlayer player : players(gameVariant).values()) {
            stop(player);
        }
        stopMunchingSound(); // TODO check
        stopSiren();
        stopVoice();
        Logger.info("All sounds stopped ({})", gameVariant);
    }

    public void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    public boolean isEnabled() {
        return enabledPy.get();
    }

    public void setMuted(boolean muted) {
        mutedPy.set(muted);
    }

    public void toggleMuted() {
        setMuted(isUnMuted());
    }

    public boolean isUnMuted() {
        return !mutedPy.get();
    }

    public void selectSiren(int number) {
        if (number < 1 || number > 4) {
            Logger.error("Siren number must be in 1..4 but is " + number);
            return;
        }
        if (siren == null || siren.number() != number) {
            if (siren != null) {
                stop(siren.player());
            }
            MediaPlayer sirenPlayer = makeSoundLoop("siren." + number);
            if (sirenPlayer == null) {
                //Logger.error("Could not create media player for siren number {}", number);
                siren = null;
            } else {
                siren = new Siren(number, sirenPlayer);
            }
        }
    }

    public void playSiren() {
        if (siren != null) {
            playIfEnabled(siren.player());
        }
    }

    public void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public void playBonusBouncingSound() { playIfEnabled("bonus_bouncing"); }

    public void stopBonusBouncingSound() { stop("bonus_bouncing"); }

    public void playBonusEatenSound() {
        playClipIfEnabled("bonus_eaten", 1);
    }

    public void playInsertCoinSound() {
        playClipIfEnabled("credit", 1);
    }

    public void playExtraLifeSound() {
        playClipIfEnabled("extra_life", 1);
    }

    public void playGameOverSound() {
        playIfEnabled("game_over");
    }

    public void playGameReadySound() {
        playIfEnabled("game_ready");
    }

    public void playGhostEatenSound() {
        playClipIfEnabled("ghost_eaten", 1);
    }

    public void playGhostReturningHomeSound() {
        playIfEnabled("ghost_returns");
    }

    public void stopGhostReturningHomeSound() {
        stop("ghost_returns");
    }

    public void playLevelChangedSound() {
        playClipIfEnabled("sweep", 1);
    }

    public void playLevelCompleteSound() {
        playIfEnabled("level_complete");
    }

    public void playMunchingSound() {
        playIfEnabled("pacman_munch");
    }

    public void stopMunchingSound() {
        stop("pacman_munch");
    }

    public void playPacDeathSound() {
        playIfEnabled("pacman_death");
    }

    public void playPacPowerSound() {
        playIfEnabled("pacman_power");
    }

    public void stopPacPowerSound() {
        stop("pacman_power");
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.sound;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.util.AssetStorage;
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

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;

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
    private AssetStorage assets;

    // These are created when game variant changes
    private final Map<GameVariant, Map<String, MediaPlayer>> playerMapByGameVariant = new HashMap<>();
    {
        for (GameVariant variant : GameVariant.values()) {
            playerMapByGameVariant.put(variant, Map.of());
        }
    }

    // These are created on demand
    private Siren siren;
    private MediaPlayer voice;

    //TODO check volume settings
    public void init(GameVariant gameVariant) {
        this.gameVariant = checkNotNull(gameVariant);
        if (playerMapByGameVariant.get(gameVariant).isEmpty()) {
            Map<String, MediaPlayer> sounds = new HashMap<>();
            sounds.put("game_over", makeSound("game_over", 1, false));
            sounds.put("game_ready", makeSound("game_ready", 1, false));
            sounds.put("ghost_returns", makeSound("ghost_returns", 1, true));
            sounds.put("level_complete", makeSound("level_complete", 1, false));
            sounds.put("pacman_munch", makeSound("pacman_munch", 1, true));
            sounds.put("pacman_death", makeSound("pacman_death", 1, false));
            sounds.put("pacman_power", makeSound("pacman_power", 1, true));
            MediaPlayer bouncePlayer = makeSound("bonus_bouncing", 1, true);
            if (bouncePlayer != null) {
                bouncePlayer.setRate(0.5);
            }
            sounds.put("bonus_bouncing", bouncePlayer);

            playerMapByGameVariant.put(gameVariant, sounds);
            Logger.info("Created media players for game variant {}", gameVariant);
        }
        siren = null;
        logPlayerStatus();
    }

    private Map<String, MediaPlayer> players(GameVariant gameVariant) {
        return playerMapByGameVariant.get(gameVariant);
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

    public MediaPlayer makeSound(String keySuffix, double volume, boolean loop) {
        String assetKey = assetPrefix(gameVariant) + ".audio." + keySuffix;
        URL url = assets.get(assetKey);
        if (url == null) {
            Logger.warn("Missing audio resource '%s' (%s)".formatted(assetKey, gameVariant));
            return null;
        }
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(volume);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logPlayerStatusChange(player, keySuffix, ov, nv));
        Logger.info("Media player created from URL {}", url);
        return player;
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
        checkNotNull(keySuffix);
        String assetKey = assetPrefix(gameVariant) + ".audio." + keySuffix;
        AudioClip clip = assets.get(assetKey);
        if (clip == null) {
            Logger.error("No audio clip with key {}", assetKey);
            return;
        }
        if (isUnMuted() && isEnabled()) {
            clip.setVolume(volume);
            clip.play();
        }
    }

    // Public API

    public void setAssets(AssetStorage assets) {
        this.assets = checkNotNull(assets);
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
            MediaPlayer sirenPlayer = makeSound("siren." + number, 1, true);
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

    public void playCreditSound() {
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
}
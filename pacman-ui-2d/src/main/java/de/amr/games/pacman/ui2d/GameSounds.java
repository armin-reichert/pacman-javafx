/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;

/**
 * @author Armin Reichert
 */
public class GameSounds {

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sound is {}abled", get() ? "en" : "dis");
        }
    };

    private final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(GameVariant.PACMAN) {
        @Override
        protected void invalidated() {
            initMediaPlayers(get());
        }
    };

    private final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    private AssetStorage assets;

    // These are created when game variant changes
    private final Map<String, MediaPlayer> players = new HashMap<>();

    // These are created on demand
    private MediaPlayer intermissionSound;
    private Siren siren;
    private MediaPlayer voice;

    //TODO check volume settings
    private void initMediaPlayers(GameVariant variant) {
        players.clear();
        players.put("game_over",      createPlayer(variant, assets, "game_over", 0.5, false));
        players.put("game_ready",     createPlayer(variant, assets, "game_ready", 0.5, false));
        players.put("ghost_returns",  createPlayer(variant, assets, "ghost_returns", 0.5, true));
        players.put("level_complete", createPlayer(variant, assets, "level_complete", 0.5, false));
        players.put("pacman_munch",   createPlayer(variant, assets, "pacman_munch", 0.5, true));
        players.put("pacman_death",   createPlayer(variant, assets, "pacman_death", 0.5, false));
        players.put("pacman_power",   createPlayer(variant, assets, "pacman_power", 0.5, true));
        intermissionSound = null;
        siren = null;
        logPlayerStatus();
    }

    private void logPlayerStatus() {
        for (String key : players.keySet()) {
            logPlayerStatus(players.get(key), key);
        }
        logPlayerStatus(intermissionSound, "Intermission");
        if (siren != null) {
            logPlayerStatus(siren.player(), "Siren" + siren.number());
        }
        logPlayerStatus(voice, "Voice");
    }

    private void logPlayerStatus(MediaPlayer player, String key) {
        if (player != null) {
            Logger.info("[{}] state={} volume={}", key, player.getStatus() != null ? player.getStatus() : "UNDEFINED", player.getVolume());
        } else {
            Logger.info("No player exists for key {}", key);
        }
    }

    private void logPlayerStatusChange(MediaPlayer player, String key, MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
        Logger.info("[{}] {} -> {}, volume {}", key, (oldStatus != null ? oldStatus : "undefined"), newStatus, player.getVolume());
    }

    private MediaPlayer createPlayer(GameVariant variant, AssetStorage assets, String keySuffix, double volume, boolean loop) {
        String assetKey = assetPrefix(variant) + ".audio." + keySuffix;
        URL url = assets.get(assetKey);
        if (url == null) {
            String msg = "Could not load audio resource using asset key: " + assetKey;
            Logger.error(msg);
            throw new MissingResourceException(msg, GameSounds.class.getName(), assetKey);
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
            Logger.error("Cannot play sound, player is NULL");
            return;
        }
        if (isEnabled()) {
            player.play();
        }
    }

    private void playIfEnabled(String key) {
        playIfEnabled(players.get(key));
    }

    private void stop(MediaPlayer player) {
        if (player == null) {
            Logger.error("Cannot stop sound, player is NULL");
            return;
        }
        player.stop();
    }

    private void stop(String key) {
        stop(players.get(key));
    }

    private void playClipIfEnabled(String keySuffix) {
        checkNotNull(keySuffix);
        String assetKey = assetPrefix(gameVariantPy.get()) + ".audio." + keySuffix;
        AudioClip clip = assets.get(assetKey);
        if (clip == null) {
            Logger.error("No audio clip with key {}", assetKey);
            return;
        }
        if (isUnMuted() && isEnabled()) {
            clip.setVolume(0.5);
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

    public ObjectProperty<GameVariant> gameVariantProperty() {
        return gameVariantPy;
    }

    public BooleanProperty mutedProperty() {
        return mutedPy;
    }

    public void stopAll() {
        for (MediaPlayer player : players.values()) {
            stop(player);
        }
        stop(intermissionSound);
        stopSiren();
        stopVoice();
        Logger.info("All sounds stopped ({})", gameVariantPy.get());
    }

    public boolean isEnabled() {
        return enabledPy.get();
    }

    public boolean isUnMuted() {
        return !mutedPy.get();
    }

    public void setMuted(boolean muted) {
        mutedPy.set(muted);
    }

    public void toggleMuted() {
        setMuted(isUnMuted());
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
            siren = new Siren(number, createPlayer(gameVariantPy.get(), assets, "siren." + number, 0.25, true));
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

    public void playBonusEatenSound() {
        playClipIfEnabled("bonus_eaten");
    }

    public void playCreditSound() {
        playClipIfEnabled("credit");
    }

    public void playExtraLifeSound() {
        playClipIfEnabled("extra_life");
    }

    public void playGameOverSound() {
        playIfEnabled("game_over");
    }

    public void playGameReadySound() {
        playIfEnabled("game_ready");
    }

    public void playGhostEatenSound() {
        playClipIfEnabled("ghost_eaten");
    }

    public void playGhostReturningHomeSound() {
        playIfEnabled("ghost_returns");
    }

    public void stopGhostReturningHomeSound() {
        stop("ghost_returns");
    }

    public void playLevelChangedSound() {
        playClipIfEnabled("sweep");
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
        playIfEnabled("pac_power");
    }

    public void stopPacPowerSound() {
        stop("pac_power");
    }

    public void playIntermissionSound(int number) {
        if (number < 1 || number > 3) {
            Logger.error("Intermission number must be from 1..3 but is " + number);
            return;
        }
        intermissionSound = switch (gameVariantPy.get()) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> createPlayer(gameVariantPy.get(), assets, "intermission." + number, 0.5, false);
            case PACMAN, PACMAN_XXL -> {
                var player = createPlayer(gameVariantPy.get(), assets, "intermission", 0.5, false);
                player.setCycleCount(number == 2 ? 1 : 2);
                yield player;
            }
        };
        intermissionSound.play();
    }

    public void playVoice(String voiceClipID, double delaySeconds) {
        if (voice != null) {
            Logger.info("Cannot play voice {}, another voice is already playing", voiceClipID);
            return;
        }
        URL url = assets.get(voiceClipID);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        // media player stays in state PLAYING so we remove the reference when it reaches the end
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
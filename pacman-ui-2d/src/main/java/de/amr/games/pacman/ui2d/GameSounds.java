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
import java.util.MissingResourceException;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;

/**
 * @author Armin Reichert
 */
public class GameSounds {

    private final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(GameVariant.PACMAN) {
        @Override
        protected void invalidated() {
            loadSoundsForGameVariant(get());
        }
    };

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sounds are " + (get() ? "enabled" : "disabled"));
        }
    };

    private final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    private AssetStorage assets;

    // These are created when game variant changes
    private MediaPlayer gameOverSound;
    private MediaPlayer gameReadySound;
    private MediaPlayer levelCompleteSound;
    private MediaPlayer munchingSound;
    private MediaPlayer pacDeathSound;
    private MediaPlayer pacPowerSound;
    private MediaPlayer ghostReturnsHomeSound;

    // These are created on demand
    private MediaPlayer intermissionSound;
    private Siren siren;
    private MediaPlayer voice;

    private void logSounds() {
        logSound(gameOverSound, "Game Over");
        logSound(gameReadySound, "Game Ready");
        logSound(ghostReturnsHomeSound, "Ghost Returning Home");
        logSound(intermissionSound, "Intermission");
        logSound(levelCompleteSound, "Level Complete");
        logSound(munchingSound, "Munching");
        logSound(pacDeathSound, "Death");
        logSound(pacPowerSound, "Power");
        if (siren != null) {
            logSound(siren.player(), "Siren" + siren.number());
        }
    }

    private void logSound(MediaPlayer sound, String description) {
        if (sound != null) {
            Logger.debug("[{}] state={} volume={}", description,
                sound.getStatus() != null ? sound.getStatus() : "UNDEFINED", sound.getVolume());
        }
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
        Logger.info("Media player created from URL {}", url);
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(volume);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logSounds());
        return player;
    }

    private void playSound(MediaPlayer player) {
        if (player != null && isEnabled()) {
            player.play();
        }
    }

    private void stopSound(MediaPlayer player) {
        if (player != null)
            player.stop();
    }

    private void playClip(String keySuffix) {
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

    private void loadSoundsForGameVariant(GameVariant variant) {
        gameOverSound = createPlayer(variant, assets, "game_over", 0.5, false);
        gameReadySound = createPlayer(variant, assets, "game_ready", 0.5, false);
        ghostReturnsHomeSound = createPlayer(variant, assets, "ghost_returns", 0.5, true);
        levelCompleteSound = createPlayer(variant, assets, "level_complete", 0.5, false);
        munchingSound = createPlayer(variant, assets, "pacman_munch", 0.5, true);
        pacDeathSound = createPlayer(variant, assets, "pacman_death", 0.5, false);
        pacPowerSound = createPlayer(variant, assets, "pacman_power", 0.5, true);
        // these are created on demand
        intermissionSound = null;
        siren = null;
    }

    // Public API

    public void setAssets(AssetStorage assets) {
        this.assets = checkNotNull(assets);
    }

    public ObjectProperty<GameVariant> gameVariantProperty() {
        return gameVariantPy;
    }

    public BooleanProperty enabledProperty() {
        return enabledPy;
    }

    public BooleanProperty mutedProperty() {
        return mutedPy;
    }

    public void stopAll() {
        stopSound(gameOverSound);
        stopSound(gameReadySound);
        stopSound(ghostReturnsHomeSound);
        stopSound(intermissionSound);
        stopSound(levelCompleteSound);
        stopSound(munchingSound);
        stopSound(pacDeathSound);
        stopSound(pacPowerSound);
        stopSiren();
        stopVoice();
        Logger.info("All sounds stopped ({})", gameVariantPy.get());
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

    public boolean isEnabled() {
        return enabledPy.get();
    }

    public void selectSiren(int number) {
        if (number < 1 || number > 4) {
            Logger.error("Siren number must be in 1..4 but is " + number);
            return;
        }
        if (siren == null || siren.number() != number) {
            if (siren != null) {
                stopSound(siren.player());
            }
            siren = new Siren(number, createPlayer(gameVariantPy.get(), assets, "siren." + number, 0.25, true));
        }
    }

    public void playSiren() {
        if (siren != null) {
            playSound(siren.player());
        }
    }

    public void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public void playBonusEatenSound() {
        playClip("bonus_eaten");
    }

    public void playCreditSound() {
        playClip("credit");
    }

    public void playExtraLifeSound() {
        playClip("extra_life");
    }

    public void playGameOverSound() {
        playSound(gameOverSound);
    }

    public void playGameReadySound() {
        playSound(gameReadySound);
    }

    public void playGhostEatenSound() {
        playClip("ghost_eaten");
    }

    public void playGhostReturningHomeSound() {
        playSound(ghostReturnsHomeSound);
    }

    public void stopGhostReturningHomeSound() {
        stopSound(ghostReturnsHomeSound);
    }

    public void playLevelChangedSound() {
        playClip("sweep");
    }

    public void playLevelCompleteSound() {
        playSound(levelCompleteSound);
    }

    public void playMunchingSound() {
        playSound(munchingSound);
    }

    public void stopMunchingSound() {
        stopSound(munchingSound);
    }

    public void playPacDeathSound() {
        playSound(pacDeathSound);
    }

    public void playPacPowerSound() {
        playSound(pacPowerSound);
    }

    public void stopPacPowerSound() {
        stopSound(pacPowerSound);
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
        // media player stays in state PLAYING so we reset the reference when it reaches the end
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
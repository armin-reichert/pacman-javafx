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

    private static final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(GameVariant.PACMAN) {
        @Override
        protected void invalidated() {
            loadSoundsForCurrentGameVariant();
        }
    };

    private static final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sounds are " + (get() ? "enabled" : "disbaled"));
        }
    };

    private static final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    private static AssetStorage assets;

    // These are created when game variant changes
    private static MediaPlayer gameOverSound;
    private static MediaPlayer gameReadySound;
    private static MediaPlayer levelCompleteSound;
    private static MediaPlayer munchingSound;
    private static MediaPlayer pacDeathSound;
    private static MediaPlayer pacPowerSound;
    private static MediaPlayer ghostReturningHomeSound;

    // These are created on demand
    private static MediaPlayer intermissionSound;
    private static Siren siren;
    private static MediaPlayer voice;

    public static void setAssets(AssetStorage assets) {
        GameSounds.assets = checkNotNull(assets);
    }

    public static ObjectProperty<GameVariant> gameVariantProperty() {
        return gameVariantPy;
    }

    public static BooleanProperty enabledProperty() {
        return enabledPy;
    }

    public static BooleanProperty mutedProperty() {
        return mutedPy;
    }

    private static void loadSoundsForCurrentGameVariant() {
        gameOverSound = createPlayer("game_over", 0.5, false);
        gameReadySound = createPlayer("game_ready", 0.5, false);
        ghostReturningHomeSound = createPlayer("ghost_returning", 0.5, true);
        levelCompleteSound = createPlayer("level_complete", 0.5, false);
        munchingSound = createPlayer("pacman_munch", 0.5, true);
        pacDeathSound = createPlayer("pacman_death", 0.5, false);
        pacPowerSound = createPlayer("pacman_power", 0.5, true);
        // these are created on demand
        intermissionSound = null;
        siren = null;
    }

    public static void stopAll() {
        stopSound(gameOverSound);
        stopSound(gameReadySound);
        stopSound(ghostReturningHomeSound);
        stopSound(intermissionSound);
        stopSound(levelCompleteSound);
        stopSound(munchingSound);
        stopSound(pacDeathSound);
        stopSound(pacPowerSound);
        stopSiren();
        stopVoice();
        Logger.info("All sounds stopped");
    }

    private static void logSounds() {
        logSound(gameOverSound, "Game Over");
        logSound(gameReadySound, "Game Ready");
        logSound(ghostReturningHomeSound, "Ghost Returning Home");
        logSound(intermissionSound, "Intermission");
        logSound(levelCompleteSound, "Level Complete");
        logSound(munchingSound, "Munching");
        logSound(pacDeathSound, "Death");
        logSound(pacPowerSound, "Power");
        if (siren != null) {
            logSound(siren.player(), "Siren" + siren.number());
        }
    }

    private static void logSound(MediaPlayer sound, String description) {
        if (sound != null) {
            Logger.debug("[{}] state={} volume={}", description,
                sound.getStatus() != null ? sound.getStatus() : "UNDEFINED", sound.getVolume());
        }
    }

    private static MediaPlayer createPlayer(String keySuffix, double volume, boolean loop) {
        GameVariant variant = gameVariantPy.get();
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

    private static void playSound(MediaPlayer player) {
        if (player != null && isEnabled()) {
            player.play();
        }
    }

    private static void stopSound(MediaPlayer player) {
        if (player != null)
            player.stop();
    }

    private static void playClip(String keySuffix) {
        checkNotNull(keySuffix);
        String assetKey = assetPrefix(gameVariantPy.get()) + ".audio." + keySuffix;
        AudioClip clip = assets.get(assetKey);
        if (clip == null) {
            Logger.error("No audio clip with key {}", assetKey);
            return;
        }
        if (!isMuted() && isEnabled()) {
            clip.setVolume(0.5);
            clip.play();
        }
    }

    public static boolean isMuted() {
        return mutedPy.get();
    }

    public static void mute(boolean muted) {
        mutedPy.set(muted);
    }

    public static void toggleMuted() {
        mute(!isMuted());
    }

    public static boolean isEnabled() {
        return enabledPy.get();
    }

    public static void selectSiren(int number) {
        if (number < 1 || number > 4) {
            Logger.error("Siren number must be in 1..4 but is " + number);
            return;
        }
        if (siren == null || siren.number() != number) {
            if (siren != null) {
                stopSound(siren.player());
            }
            siren = new Siren(number, createPlayer("siren." + number, 0.25, true));
        }
    }

    public static void playSiren() {
        if (siren != null) {
            playSound(siren.player());
        }
    }

    public static void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public static void playBonusEatenSound() {
        playClip("bonus_eaten");
    }

    public static void playCreditSound() {
        playClip("credit");
    }

    public static void playExtraLifeSound() {
        playClip("extra_life");
    }

    public static void playGameOverSound() {
        playSound(gameOverSound);
    }

    public static void playGameReadySound() {
        playSound(gameReadySound);
    }

    public static void playGhostEatenSound() {
        playClip("ghost_eaten");
    }

    public static void playGhostReturningHomeSound() {
        playSound(ghostReturningHomeSound);
    }

    public static void stopGhostReturningHomeSound() {
        stopSound(ghostReturningHomeSound);
    }

    public static void playLevelChangedSound() {
        playClip("sweep");
    }

    public static void playLevelCompleteSound() {
        playSound(levelCompleteSound);
    }

    public static void playMunchingSound() {
        playSound(munchingSound);
    }

    public static void stopMunchingSound() {
        stopSound(munchingSound);
    }

    public static void playPacDeathSound() {
        playSound(pacDeathSound);
    }

    public static void playPacPowerSound() {
        playSound(pacPowerSound);
    }

    public static void stopPacPowerSound() {
        stopSound(pacPowerSound);
    }

    public static void playIntermissionSound(int number) {
        if (number < 1 || number > 3) {
            Logger.error("Intermission number must be from 1..3 but is " + number);
            return;
        }
        switch (gameVariantPy.get()) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> intermissionSound = createPlayer("intermission." + number, 0.5, false);
            case PACMAN, PACMAN_XXL -> {
                intermissionSound = createPlayer("intermission", 0.5, false);
                intermissionSound.setCycleCount(number == 2 ? 1 : 2);
            }
        }
        intermissionSound.play();
    }

    public static void playVoice(String voiceClipID, double delaySeconds) {
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

    public static void stopVoice() {
        if (voice != null) {
            voice.stop();
        }
    }
}
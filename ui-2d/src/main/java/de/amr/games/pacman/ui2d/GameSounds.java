/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.util.AssetMap;
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

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GameSounds {

    public static final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            loadSoundsForCurrentGameVariant();
        }
    };

    public static final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sounds are " + (get() ? "enabled" : "disbaled"));
        }
    };

    public static final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    private static AssetMap assets;

    // These are created when game variant changes
    private static MediaPlayer gameOverSound;
    private static MediaPlayer gameReadySound;
    private static MediaPlayer munchingSound;
    private static MediaPlayer pacDeathSound;
    private static MediaPlayer pacPowerSound;
    private static MediaPlayer ghostReturningHomeSound;

    // These are created on demand
    private static MediaPlayer intermissionSound;
    private static Siren siren;
    private static MediaPlayer voice;

    public static void init(AssetMap assets) {
        GameSounds.assets = checkNotNull(assets);
    }

    private static void loadSoundsForCurrentGameVariant() {
        gameOverSound = createAudioPlayer("game_over", 0.5, false);
        gameReadySound = createAudioPlayer("game_ready", 0.5, false);
        ghostReturningHomeSound = createAudioPlayer("ghost_returning", 0.5, true);
        munchingSound = createAudioPlayer("pacman_munch", 0.5, true);
        pacDeathSound = createAudioPlayer("pacman_death", 0.5, false);
        pacPowerSound = createAudioPlayer("pacman_power", 0.5, true);
        // these are created on demand
        intermissionSound = null;
        siren = null;
    }

    public static void stopAll() {
        stopSound(gameOverSound);
        stopSound(gameReadySound);
        stopSound(ghostReturningHomeSound);
        stopSound(intermissionSound);
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
        logSound(munchingSound, "Munching");
        logSound(pacDeathSound, "Death");
        logSound(pacPowerSound, "Power");
        if (siren != null) {
            logSound(siren.player(), "Siren" + siren.number());
        }
    }

    private static void logSound(MediaPlayer sound, String description) {
        if (sound != null) {
            Logger.info("[{}] state={} volume={}", description,
                sound.getStatus() != null ? sound.getStatus() : "UNDEFINED", sound.getVolume());
        }
    }

    private static String audioPrefix(GameVariant variant) {
        String prefix = variant == GameVariant.PACMAN_XXL ? GameVariant.PACMAN.resourceKey() : variant.resourceKey();
        return prefix + ".audio.";
    }

    private static MediaPlayer createAudioPlayer(String keySuffix, double volume, boolean loop) {
        GameVariant variant = gameVariantPy.get();
        URL url = assets.get(audioPrefix(variant) + keySuffix);
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        Logger.info("Media player created from URL {}", url);
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(volume);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logSounds());
        return player;
    }

    private static void playSound(MediaPlayer player) {
        if (enabledPy.get()) {
            player.play();
        }
    }

    private static void stopSound(MediaPlayer player) {
        if (player != null)
            player.stop();
    }

    private static void playClip(String keySuffix) {
        if (!isEnabled()) {
            return;
        }
        checkNotNull(keySuffix);
        GameVariant variant = gameVariantPy.get();
        AudioClip clip = assets.get(audioPrefix(variant) + keySuffix);
        if (clip == null) {
            Logger.error("No sound exists for key {}", audioPrefix(variant) + keySuffix);
            return;
        }
        if (!isMuted()) {
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

    public static void updatePlaySceneSound(GameContext context) {
        if (context.game().isDemoLevel()) {
            return;
        }
        playHuntingSound(context);
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            playGhostReturningHomeSound();
        } else {
            stopGhostReturningHomeSound();
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
        enabledPy.set(true); // TODO check this
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

    public static void playHuntingSound(GameContext context) {
        //TODO check this
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenIndex = context.game().huntingPhaseIndex() / 2;
            int sirenNumber = sirenIndex + 1;
            if (siren != null && siren.number() != sirenNumber) {
                siren.player().stop();
            }
            if (siren == null || siren.number() != sirenNumber) {
                siren = new Siren(sirenNumber, createAudioPlayer("siren." + sirenNumber, 0.25, true));
            }
            playSound(siren.player());
        }
    }

    public static void playLevelChangedSound() {
        playClip("sweep");
    }

    public static void playLevelCompleteSound() {
        playClip("level_complete");
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
        GameVariant variant = gameVariantPy.get();
        switch (variant) {
            case MS_PACMAN -> {
                intermissionSound = createAudioPlayer("intermission." + number, 0.5, false);
                intermissionSound.play();
            }
            case PACMAN, PACMAN_XXL -> {
                intermissionSound = createAudioPlayer("intermission", 0.5, false);
                intermissionSound.setCycleCount(number == 1 || number == 3 ? 2 : 1);
                intermissionSound.play();
            }
        }
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
        voice.statusProperty().addListener((py,ov,nv) -> Logger.info("Voice status {} -> {}", ov, nv));
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
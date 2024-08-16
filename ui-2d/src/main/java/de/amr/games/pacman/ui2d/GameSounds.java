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

    public static final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    public static final BooleanProperty enabledPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            Logger.info("Game sounds are " + (get() ? "enabled" : "disbaled"));
        }
    };

    private static AssetMap assets;

    // These are created when game variant changes
    private static MediaPlayer startGameSound;
    private static MediaPlayer munchingSound;
    private static MediaPlayer powerSound;
    private static MediaPlayer ghostReturningHomeSound;

    // These are created on demand
    private static MediaPlayer intermissionSound;
    private static Siren siren;
    private static MediaPlayer voice;

    public static void init(AssetMap assets) {
        GameSounds.assets = checkNotNull(assets);
    }

    private static void loadSoundsForCurrentGameVariant() {
        ghostReturningHomeSound = createAudioPlayer("ghost_returning", 0.5, true);
        munchingSound = createAudioPlayer("pacman_munch", 0.5, true);
        powerSound = createAudioPlayer("pacman_power", 0.5, true);
        startGameSound = createAudioPlayer("game_ready", 0.5, false);
        // these are created on demand
        intermissionSound = null;
        siren = null;
    }

    private static void logSound(MediaPlayer sound, String description) {
        if (sound != null) {
            Logger.info(description + ": state={} volume={}", sound.getStatus() != null ? sound.getStatus() : "UNDEFINED", sound.getVolume());
        }
    }

    private static void logSounds() {
        logSound(ghostReturningHomeSound, "GhostReturningHome Sound");
        logSound(munchingSound, "Munching Sound");
        logSound(powerSound, "Power Sound");
        logSound(startGameSound, "StartingGame Sound");
        logSound(intermissionSound, "Intermission Sound");
        if (siren != null) {
            logSound(siren.player(), "Siren" + siren.number());
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

    public static void stop(MediaPlayer player) {
        if (player != null)
            player.stop();
    }

    public static void stopAll() {
        stop(ghostReturningHomeSound);
        stop(intermissionSound);
        stop(munchingSound);
        stop(powerSound);
        stop(startGameSound);
        stopSiren();
        stopVoice();
        Logger.info("All sounds stopped");
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

    public static void playGhostEatenSound() {
        playClip("ghost_eaten");
    }

    public static void playLevelChangedSound() {
        playClip("sweep");
    }

    public static void playLevelCompleteSound() {
        playClip("level_complete");
    }

    public static void playStartGameSound() {
        playSound(startGameSound);
    }

    public static void playGameOverSound() {
        playClip("game_over");
    }

    public static void playMunchingSound() {
        playSound(munchingSound);
    }

    public static void stopMunchingSound() {
        stop(munchingSound);
    }

    public static void playPowerSound() {
        playSound(powerSound);
    }

    public static void stopPowerSound() {
        stop(powerSound);
    }

    public static void playGhostReturningHomeSound() {
        playSound(ghostReturningHomeSound);
    }

    public static void stopGhostReturningHomeSound() {
        stop(ghostReturningHomeSound);
    }

    public static void playPacManDeathSound() {
        playClip("pacman_death");
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
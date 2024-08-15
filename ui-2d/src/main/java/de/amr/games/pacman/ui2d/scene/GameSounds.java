/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.Siren;
import de.amr.games.pacman.ui2d.util.AssetMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static AssetMap assets;
    private static GameContext context;

    public static BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    public static MediaPlayer voice;
    public static Siren siren;
    public static MediaPlayer startGameSound;
    public static MediaPlayer intermissionSound;
    public static MediaPlayer munchingSound;
    public static MediaPlayer powerSound;
    public static MediaPlayer ghostReturningHomeSound;

    public static void init(AssetMap assets, GameContext context) {
        GameSounds.assets = checkNotNull(assets);
        GameSounds.context = checkNotNull(context);
    }

    private static String audioPrefix() {
        String prefix = context.game().variant() == GameVariant.PACMAN_XXL
            ? GameVariant.PACMAN.resourceKey() : context.game().variant().resourceKey();
        return prefix + ".audio.";
    }

    private static MediaPlayer createSoundPlayer(String keySuffix, double volume, boolean loop) {
        URL url = assets.get(audioPrefix() + keySuffix);
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        Logger.info("Media player created from URL {}", url);
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(volume);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logSound());
        return player;
    }

    public static void updateSound() {
        if (context.game().isDemoLevel()) {
            return;
        }
        ensureSirenPlaying();
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

    public static void ensureSirenPlaying() {
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            ensureSirenPlaying(context.game().huntingPhaseIndex() / 2);
        }
    }

    public static void playBonusEatenSound() {
        if (!context.game().isDemoLevel()) {
            playAudioClip("bonus_eaten");
        }
    }

    public static void playCreditSound() {
        playAudioClip("credit");
    }

    public static void playExtraLifeSound() {
        if (!context.game().isDemoLevel()) {
            playAudioClip("extra_life");
        }
    }

    public static void playGhostEatenSound() {
        if (!context.game().isDemoLevel()) {
            playAudioClip("ghost_eaten");
        }
    }

    public static void playLevelCompleteSound() {
        if (!context.game().isDemoLevel()) {
            GameSounds.playAudioClip("level_complete");
        }
    }

    public static void playLevelChangedSound() {
        if (!context.game().isDemoLevel()) {
            GameSounds.playAudioClip("sweep");
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

    private static void playAudioClip(String keySuffix) {
        checkNotNull(keySuffix);
        AudioClip clip = assets.get(audioPrefix() + keySuffix);
        if (clip == null) {
            Logger.error("No sound exists for key {}", audioPrefix() + keySuffix);
            return;
        }
        if (!isMuted()) {
            clip.setVolume(0.5);
            clip.play();
        }
    }

    public static void stopSound(MediaPlayer sound) {
        if (sound != null)
            sound.stop();
    }

    public static void stopAllSounds() {
        stopSound(ghostReturningHomeSound);
        stopSound(intermissionSound);
        stopSound(munchingSound);
        stopSound(powerSound);
        stopSound(startGameSound);
        stopSiren();
        stopVoice();
        //assets.audioClips().forEach(AudioClip::stop); // TODO needed anymore?
        Logger.info("All sounds stopped");
    }

    /**
     * Deletes media players, they get recreated for the current game variant on demand.
     */
    public static void deleteSounds() {
        ghostReturningHomeSound = null;
        intermissionSound = null;
        munchingSound = null;
        powerSound = null;
        startGameSound = null;
        siren = null;
        Logger.info("Sounds deleted. Will be recreated on demand.");
    }

    /**
     * @param sirenIndex index of siren (0..3)
     */
    public static void ensureSirenPlaying(int sirenIndex) {
        if (sirenIndex < 0 || sirenIndex > 3) {
            throw new IllegalArgumentException("Illegal siren index: " + sirenIndex);
        }
        int sirenNumber = sirenIndex + 1;
        if (siren != null && siren.number() != sirenNumber) {
            siren.player().stop();
        }
        if (siren == null || siren.number() != sirenNumber) {
            siren = new Siren(sirenNumber, createSoundPlayer("siren." + sirenNumber, 0.25, true));
        }
        siren.player().play();
    }

    public static void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public static void playStartGameSound() {
        if (startGameSound == null) {
            startGameSound = createSoundPlayer("game_ready", 0.5, false);
        }
        startGameSound.play();
    }

    public static void playGameOverSound() {
        playAudioClip("game_over");
    }

    public static void playMunchingSound() {
        if (munchingSound == null) {
            munchingSound = createSoundPlayer("pacman_munch", 0.5, true);
        }
        munchingSound.play();
    }

    public static void stopMunchingSound() {
        stopSound(munchingSound);
    }

    public static void playPowerSound() {
        if (powerSound == null) {
            powerSound = createSoundPlayer("pacman_power", 0.5, true);
        }
        powerSound.play();
    }

    public static void stopPowerSound() {
        stopSound(powerSound);
    }

    public static void playGhostReturningHomeSound() {
        if (ghostReturningHomeSound == null) {
            ghostReturningHomeSound = createSoundPlayer("ghost_returning", 0.5, true);
        }
        ghostReturningHomeSound.play();
    }

    public static void stopGhostReturningHomeSound() {
        stopSound(ghostReturningHomeSound);
    }

    public static void playPacManDeathSound() {
        playAudioClip("pacman_death");
    }

    public static void playIntermissionSound(int number) {
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                intermissionSound = createSoundPlayer("intermission." + number, 0.5, false);
                intermissionSound.play();
            }
            case PACMAN, PACMAN_XXL -> {
                intermissionSound = createSoundPlayer("intermission", 0.5, false);
                intermissionSound.setCycleCount(number == 1 || number == 3 ? 2 : 1);
                intermissionSound.play();
            }
        }
    }

    public static void playVoice(String voiceClipID, double delaySeconds) {
        if (voice != null && voice.getStatus() == MediaPlayer.Status.PLAYING) {
            Logger.info("Cannot play voice {}, another voice is already playing", voiceClipID);
            return;
        }
        URL url = assets.get(voiceClipID);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        voice.muteProperty().bind(mutedPy);
        voice.setStartTime(Duration.seconds(delaySeconds));
        voice.play();
    }

    public static void stopVoice() {
        if (voice != null) {
            voice.stop();
        }
    }

    private static void logSound() {
        if (startGameSound != null) {
            Logger.debug("Start Game Sound: {} volume {}", startGameSound.getStatus(), startGameSound.getVolume());
        }
        if (siren != null) {
            Logger.debug("Siren {}: {} volume {}", siren.number(), siren.player().getStatus(), siren.player().getVolume());
        }
        if (munchingSound != null) {
            Logger.debug("Munching Sound: {} volume {}", munchingSound.getStatus(), munchingSound.getVolume());
        }
        if (powerSound != null) {
            Logger.debug("Power Sound: {} volume {}", powerSound.getStatus(), powerSound.getVolume());
        }
        if (ghostReturningHomeSound != null) {
            Logger.debug("Ghost Returning Home Sound: {} volume {}", ghostReturningHomeSound.getStatus(), ghostReturningHomeSound.getVolume());
        }
    }
}
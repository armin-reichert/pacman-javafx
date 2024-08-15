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

    private static AssetMap assets;
    private static GameContext context;

    public static final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            loadAllSoundsForGameVariant(get());
        }
    };

    public static final BooleanProperty mutedPy = new SimpleBooleanProperty(false);

    // These are created when game variant changes
    private static MediaPlayer startGameSound;
    private static MediaPlayer munchingSound;
    private static MediaPlayer powerSound;
    private static MediaPlayer ghostReturningHomeSound;

    // These are created on demand
    private static MediaPlayer intermissionSound;
    private static Siren siren;
    private static MediaPlayer voice;

    public static void init(PacManGames2dUI ui) {
        GameSounds.assets = checkNotNull(ui.assets());
        GameSounds.context = checkNotNull(ui);
    }

    private static String audioPrefix(GameVariant variant) {
        String prefix = variant == GameVariant.PACMAN_XXL ? GameVariant.PACMAN.resourceKey() : variant.resourceKey();
        return prefix + ".audio.";
    }

    private static MediaPlayer createAudioPlayer(GameVariant variant, String keySuffix, double volume, boolean loop) {
        URL url = assets.get(audioPrefix(variant) + keySuffix);
        var player = new MediaPlayer(new Media(url.toExternalForm()));
        Logger.info("Media player created from URL {}", url);
        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(volume);
        player.muteProperty().bind(mutedPy);
        player.statusProperty().addListener((py,ov,nv) -> logSound());
        return player;
    }

    private static void loadAllSoundsForGameVariant(GameVariant variant) {
        ghostReturningHomeSound = createAudioPlayer(variant, "ghost_returning", 0.5, true);
        munchingSound = createAudioPlayer(variant, "pacman_munch", 0.5, true);
        powerSound = createAudioPlayer(variant, "pacman_power", 0.5, true);
        startGameSound = createAudioPlayer(variant, "game_ready", 0.5, false);

        // these are created on demand
        intermissionSound = null;
        siren = null;
    }

    private static void playClip(GameVariant variant, String keySuffix) {
        checkNotNull(keySuffix);
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

    public static void updatePlaySceneSound() {
        if (context.game().isDemoLevel()) {
            return;
        }
        playHuntingSound();
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

    public static void playHuntingSound() {
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenIndex = context.game().huntingPhaseIndex() / 2;
            int sirenNumber = sirenIndex + 1;
            if (siren != null && siren.number() != sirenNumber) {
                siren.player().stop();
            }
            if (siren == null || siren.number() != sirenNumber) {
                siren = new Siren(sirenNumber, createAudioPlayer(gameVariantPy.get(), "siren." + sirenNumber, 0.25, true));
            }
            siren.player().play();
        }
    }

    public static void stopSiren() {
        if (siren != null) {
            siren.player().stop();
        }
    }

    public static void playBonusEatenSound() {
        if (!context.game().isDemoLevel()) {
            playClip(gameVariantPy.get(), "bonus_eaten");
        }
    }

    public static void playCreditSound() {
        playClip(gameVariantPy.get(), "credit");
    }

    public static void playExtraLifeSound() {
        if (!context.game().isDemoLevel()) {
            playClip(gameVariantPy.get(), "extra_life");
        }
    }

    public static void playGhostEatenSound() {
        if (!context.game().isDemoLevel()) {
            playClip(gameVariantPy.get(), "ghost_eaten");
        }
    }

    public static void playLevelChangedSound() {
        if (!context.game().isDemoLevel()) {
            GameSounds.playClip(gameVariantPy.get(), "sweep");
        }
    }

    public static void playLevelCompleteSound() {
        if (!context.game().isDemoLevel()) {
            GameSounds.playClip(gameVariantPy.get(), "level_complete");
        }
    }

    public static void playStartGameSound() {
        startGameSound.play();
    }

    public static void playGameOverSound() {
        playClip(gameVariantPy.get(), "game_over");
    }

    public static void playMunchingSound() {
        munchingSound.play();
    }

    public static void stopMunchingSound() {
        stop(munchingSound);
    }

    public static void playPowerSound() {
        powerSound.play();
    }

    public static void stopPowerSound() {
        stop(powerSound);
    }

    public static void playGhostReturningHomeSound() {
        ghostReturningHomeSound.play();
    }

    public static void stopGhostReturningHomeSound() {
        stop(ghostReturningHomeSound);
    }

    public static void playPacManDeathSound() {
        playClip(gameVariantPy.get(), "pacman_death");
    }

    public static void playIntermissionSound(int number) {
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                intermissionSound = createAudioPlayer(gameVariantPy.get(), "intermission." + number, 0.5, false);
                intermissionSound.play();
            }
            case PACMAN, PACMAN_XXL -> {
                intermissionSound = createAudioPlayer(gameVariantPy.get(), "intermission", 0.5, false);
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
        voice.statusProperty().addListener((py,ov,nv) -> {
            Logger.info("Voice status {} -> {}", ov, nv);
        });
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
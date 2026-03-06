/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.pacmanfx.model.GameControl.CommonGameState.HUNTING;
import static java.util.Objects.requireNonNull;

/**
 * Central manager responsible for playing and stopping contextual sound effects
 * during gameplay. It decides which sounds to trigger or terminate based on
 * the current game state, actor conditions, timers, and level properties.
 * <p>
 * This class prevents overlapping or lingering sounds and ensures proper cleanup.
 * All sound decisions are centralized here for easier maintenance and debugging.
 * </p>
 */
public class PlayingSoundEffects {

    /**
     * Volume level for siren sounds (adjusted low to prevent clipping/distortion).
     */
    public static final float SIREN_VOLUME = 0.33f;

    private final SoundManager soundManager;

    private long lastMunchingSoundPlayedTick;
    private byte munchingSoundDelay;

    /**
     * Creates a new sound effects manager using the given sound manager.
     *
     * @param soundManager the underlying sound playback service (must not be null)
     */
    public PlayingSoundEffects(SoundManager soundManager) {
        this.soundManager = requireNonNull(soundManager);
    }

    /**
     * Sets the minimum number of simulation ticks that must pass between
     * consecutive plays of the Pac-Man munching sound.
     *
     * @param ticks delay in ticks (0 = play on every pellet eaten)
     */
    public void setMunchingSoundDelay(byte ticks) {
        this.munchingSoundDelay = ticks;
    }

    /**
     * Enables or disables all sound playback.
     *
     * @param enabled {@code true} to allow sounds, {@code false} to mute everything
     */
    public void setEnabled(boolean enabled) {
        soundManager.setEnabled(enabled);
    }

    /**
     * Starts looping the bonus active sound.
     */
    public void playBonusActiveSound() {
        soundManager.loop(SoundID.BONUS_ACTIVE);
    }

    /**
     * Stops the bonus active sound.
     */
    public void playBonusExpiredSound() {
        soundManager.stop(SoundID.BONUS_ACTIVE);
    }

    /**
     * Stops the bonus active loop and plays the bonus eaten sound once.
     */
    public void playBonusEatenSound() {
        soundManager.stop(SoundID.BONUS_ACTIVE);
        soundManager.play(SoundID.BONUS_EATEN);
    }

    /**
     * Plays the coin insertion sound effect.
     */
    public void playCoinInsertedSound() {
        soundManager.play(SoundID.COIN_INSERTED);
    }

    /**
     * Plays the extra life awarded sound.
     */
    public void playExtraLifeSound() {
        soundManager.play(SoundID.EXTRA_LIFE);
    }

    /**
     * Plays the game ready / start sound (usually at level beginning).
     */
    public void playGameReadySound() {
        soundManager.play(SoundID.GAME_READY);
    }

    /**
     * Stops all sounds and plays the game over jingle.
     */
    public void playGameOverSound() {
        stopAll();
        soundManager.play(SoundID.GAME_OVER);
    }

    /**
     * Plays the sound when a ghost is eaten.
     */
    public void playGhostEatenSound() {
        soundManager.play(SoundID.GHOST_EATEN);
    }

    /**
     * Starts looping the ghost returning/ghost in house sound if not already playing.
     */
    public void playGhostReturningToHouseSound() {
        if (!soundManager.isPlaying(SoundID.GHOST_RETURNS)) {
            soundManager.loop(SoundID.GHOST_RETURNS);
        }
    }

    /**
     * Stops the ghost returning sound if currently playing.
     */
    public void stopGhostReturningToHouseSound() {
        soundManager.stop(SoundID.GHOST_RETURNS);
    }

    /**
     * Manages level-wide playing sounds (siren + ghost returning) when in HUNTING state.
     * <p>
     * Does nothing if sound is globally disabled.
     * </p>
     *
     * @param level the current game level
     */
    public void playLevelPlayingSound(GameLevel level) {
        if (!soundManager.isEnabled()) {
            return;
        }
        if (level.game().control().state().nameMatches(HUNTING.name())) {
            playSiren(level);
            playGhostSounds(level.pac(), level.ghosts());
        }
    }

    /**
     * Stops all sounds and plays the Pac-Man death animation sound.
     */
    public void playPacDeadSound() {
        stopAll();
        soundManager.play(SoundID.PAC_MAN_DEATH);
    }

    /**
     * Plays the Pac-Man munching sound if enough simulation ticks have passed
     * since the last playback (to avoid too frequent/repetitive playback).
     *
     * @param now current simulation tick count
     */
    public void playPacMunchingSound(long now) {
        long passed = now - lastMunchingSoundPlayedTick;
        Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
        if (passed > munchingSoundDelay || munchingSoundDelay == 0) {
            soundManager.play(SoundID.PAC_MAN_MUNCHING);
            lastMunchingSoundPlayedTick = now;
        }
    }

    /**
     * Stops any siren and starts looping the Pac-Man power (energized) sound.
     */
    public void playPacPowerSound() {
        stopSiren();
        soundManager.loop(SoundID.PAC_MAN_POWER);
    }

    /**
     * Stops the Pac-Man power sound.
     */
    public void stopPacPowerSound() {
        soundManager.stop(SoundID.PAC_MAN_POWER);
    }

    /**
     * Plays the appropriate siren sound (1–4) based on the current hunting phase.
     * <p>
     * Siren is only played when Pac-Man is being chased (not powered).
     * </p>
     *
     * @param level current game level
     */
    public void playSiren(GameLevel level) {
        final boolean pacChased = !level.pac().powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            final int huntingPhase = level.huntingTimer().phaseIndex();
            final int sirenNumber = 1 + huntingPhase / 2;
            soundManager.playSiren(sirenNumber, SIREN_VOLUME);
        }
    }

    /**
     * Stops any currently playing siren sound.
     */
    public void stopSiren() {
        soundManager.stopSiren();
    }

    /**
     * Immediately stops all currently playing sounds.
     */
    public void stopAll() {
        soundManager.stopAll();
    }

    /**
     * Starts or stops the ghost returning/ghost in house sound depending on
     * whether any ghost is currently in {@link GhostState#RETURNING_HOME}
     * or {@link GhostState#ENTERING_HOUSE} and Pac-Man is alive.
     *
     * @param pac    Pac-Man actor
     * @param ghosts stream of all ghosts in the level
     */
    public void playGhostSounds(Pac pac, Stream<Ghost> ghosts) {
        boolean ghostReturning = pac.isAlive() && ghosts.anyMatch(g ->
                g.state() == GhostState.RETURNING_HOME || g.state() == GhostState.ENTERING_HOUSE);
        if (ghostReturning) {
            playGhostReturningToHouseSound();
        } else {
            stopGhostReturningToHouseSound();
        }
    }
}
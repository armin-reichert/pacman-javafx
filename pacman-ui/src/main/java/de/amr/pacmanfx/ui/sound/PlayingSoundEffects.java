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

public class PlayingSoundEffects {

    //TODO fix sound files
    public static final float SIREN_VOLUME = 0.33f;

    private final SoundManager soundManager;

    private long lastMunchingSoundPlayedTick;
    private byte munchingSoundDelay;

    public PlayingSoundEffects(SoundManager soundManager) {
        this.soundManager = requireNonNull(soundManager);
    }

    public void setMunchingSoundDelay(byte ticks) {
        this.munchingSoundDelay = ticks;
    }

    public void setEnabled(boolean enabled) {
        soundManager.setEnabled(enabled);
    }

    public void playBonusActiveSound() {
        soundManager.loop(SoundID.BONUS_ACTIVE);
    }

    public void playBonusExpiredSound() {
        soundManager.stop(SoundID.BONUS_ACTIVE);
    }

    public void playBonusEatenSound() {
        soundManager.stop(SoundID.BONUS_ACTIVE);
        soundManager.play(SoundID.BONUS_EATEN);
    }

    public void playCoinInsertedSound() {
        soundManager.play(SoundID.COIN_INSERTED);
    }

    public void playExtraLifeSound() {
        soundManager.play(SoundID.EXTRA_LIFE);
    }

    public void playGameReadySound() {
        soundManager.play(SoundID.GAME_READY);
    }

    public void playGameOverSound() {
        stopAll();
        soundManager.play(SoundID.GAME_OVER);
    }
    public void playGhostEatenSound() {
        soundManager.play(SoundID.GHOST_EATEN);
    }

    public void playGhostReturningToHouseSound() {
        if (!soundManager.isPlaying(SoundID.GHOST_RETURNS)) {
            soundManager.loop(SoundID.GHOST_RETURNS);
        }
    }

    public void stopGhostReturningToHouseSound() {
        soundManager.stop(SoundID.GHOST_RETURNS);
    }

    public void playLevelPlayingSound(GameLevel level) {
        if (!soundManager.isEnabled()) {
            return;
        }
        if (level.game().control().state().nameMatches(HUNTING.name())) {
            playSiren(level);
            playGhostSounds(level.pac(), level.ghosts());
        }
    }
    public void playPacDeadSound() {
        stopAll();
        soundManager.play(SoundID.PAC_MAN_DEATH);
    }

    public void playPacMunchingSound(long now) {
        final long passed = now - lastMunchingSoundPlayedTick;
        Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
        if (passed > munchingSoundDelay || munchingSoundDelay == 0) {
            soundManager.play(SoundID.PAC_MAN_MUNCHING);
            lastMunchingSoundPlayedTick = now;
        }
    }

    public void playPacPowerSound() {
        stopSiren();
        soundManager.loop(SoundID.PAC_MAN_POWER);
    }

    public void stopPacPowerSound() {
        soundManager.stop(SoundID.PAC_MAN_POWER);
    }

    public void playSiren(GameLevel level) {
        final boolean pacChased = !level.pac().powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            final int huntingPhase = level.huntingTimer().phaseIndex();
            final int sirenNumber = 1 + huntingPhase / 2;
            soundManager.playSiren(sirenNumber, SIREN_VOLUME);
        }
    }

    public void stopSiren() {
        soundManager.stopSiren();
    }

    public void stopAll() {
        soundManager.stopAll();
    }

    public void playGhostSounds(Pac pac, Stream<Ghost> ghosts) {
        boolean ghostReturningToHouseExists = pac.isAlive() && ghosts.map(Ghost::state)
            .anyMatch(state -> state == GhostState.RETURNING_HOME || state == GhostState.ENTERING_HOUSE);
        if (ghostReturningToHouseExists) {
            playGhostReturningToHouseSound();
        } else {
            stopGhostReturningToHouseSound();
        }
    }
}

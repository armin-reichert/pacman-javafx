/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.TickTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.UsefulFunctions.isOdd;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public abstract class AbstractHuntingTimer {

    protected final TickTimer tickTimer;
    protected final int numPhases;
    private final IntegerProperty phaseIndex = new SimpleIntegerProperty();

    /**
     * @param name a readable name for this timer
     * @param numPhases the total number of scatter and chasing phases (4+4 in Arcade Pac-Man games)
     */
    protected AbstractHuntingTimer(String name, int numPhases) {
        this.tickTimer = new TickTimer(name);
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    /**
     * @param levelNumber game level number
     * @param phaseIndex index of hunting phase ({@code 0..numPhases - 1})
     * @return Duration (number of ticks) of phase.
     */
    public abstract long phaseDuration(int levelNumber, int phaseIndex);

    /**
     * Advances the hunting timer and starts the timer for the next phase if the current phase is complete.
     *
     * @param levelNumber the game level number (starts with 1)
     */
    public void update(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        if (tickTimer.hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex(), phase(), tickTimer.tickCount());
            int nextPhaseIndex = requireValidPhaseIndex(phaseIndex() + 1);
            startPhase(levelNumber, nextPhaseIndex);
        } else {
            tickTimer.doTick();
        }
    }

    /**
     * @return The current timer tick count
     */
    public long tickCount() {
        return tickTimer.tickCount();
    }

    public long durationTicks() {
        return tickTimer.durationTicks();
    }
    /**
     * Stops and resets the hunting timer and the phase index to zero.
     */
    public void reset() {
        tickTimer.stop();
        tickTimer.reset(TickTimer.INDEFINITE);
        phaseIndex.set(0);
    }

    /**
     * Starts the hunting timer.
     */
    public void start() {
        tickTimer.start();
    }

    /**
     * Stops the hunting timer.
     */
    public void stop() {
        tickTimer.stop();
    }

    /**
     * @return {@code true} if the hunting timer is stopped
     */
    public boolean isStopped() {
        return tickTimer.isStopped();
    }

    /**
     * @return the number of ticks remaining for the current hunting phase
     */
    public long remainingTicksOfCurrentPhase() {
        return tickTimer.remainingTicks();
    }

    public IntegerProperty phaseIndexProperty() { return phaseIndex; }

    public int phaseIndex() { return phaseIndex.get(); }

    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public HuntingPhase phase() { return
        isEven(phaseIndex()) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING;
    }

    public void startFirstPhase(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        startPhase(levelNumber, 0);
        logPhase();
    }

    public void logPhase() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), phase().name(),
            tickTimer.durationTicks(), (float) tickTimer.durationTicks() / Globals.NUM_TICKS_PER_SEC,
            this);
    }

    protected void startPhase(int levelNumber, int phaseIndex) {
        final long duration = phaseDuration(levelNumber, phaseIndex);
        tickTimer.restartTicks(duration);
        phaseIndexProperty().set(phaseIndex);
    }

    protected int requireValidPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return phaseIndex;
    }
}
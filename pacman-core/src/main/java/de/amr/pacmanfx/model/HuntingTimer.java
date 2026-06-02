/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.Globals;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.core.Globals.isEven;
import static de.amr.pacmanfx.core.Globals.isOdd;
import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public class HuntingTimer {

    protected final TickTimer tickTimer;

    protected final int numPhases;

    private final IntegerProperty phaseIndex = new SimpleIntegerProperty();

    /**
     * @param name a readable name for this timer
     * @param numPhases the total number of scatter and chasing phases (4+4 in Arcade Pac-Man games)
     */
    public HuntingTimer(String name, int numPhases) {
        this.tickTimer = new TickTimer(name);
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    /**
     * Advances the hunting timer and starts the timer for the next phase if the current phase is complete.
     *
     * @param rules the game rules
     * @param levelNumber the game level number (starts with 1)
     */
    public void update(GameRules rules, int levelNumber) {
        requireValidLevelNumber(levelNumber);
        if (tickTimer.hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex(), currentHuntingPhase(), tickTimer.tickCount());
            int nextPhaseIndex = requireValidPhaseIndex(phaseIndex() + 1);
            startPhase(rules, levelNumber, nextPhaseIndex);
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

    public HuntingPhase currentHuntingPhase() {
        return isEven(phaseIndex()) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING;
    }

    public boolean isChasing() {
        return currentHuntingPhase() == HuntingPhase.CHASING;
    }

    public boolean isScattering() {
        return currentHuntingPhase() == HuntingPhase.SCATTERING;
    }

    public void startFirstPhase(GameRules rules, int levelNumber) {
        requireValidLevelNumber(levelNumber);
        startPhase(rules, levelNumber, 0);
        logPhase();
    }

    public void logPhase() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), currentHuntingPhase().name(),
            tickTimer.durationTicks(), (float) tickTimer.durationTicks() / Globals.NUM_TICKS_PER_SEC,
            this);
    }

    protected void startPhase(GameRules rules, int levelNumber, int phaseIndex) {
        final long duration = rules.huntingPhaseDuration(levelNumber, phaseIndex);
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
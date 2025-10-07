/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.timer.TickTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.UsefulFunctions.isOdd;
import static java.util.Objects.requireNonNull;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public abstract class HuntingTimer extends TickTimer {

    protected final int numPhases;
    protected final IntegerProperty phaseIndex = new SimpleIntegerProperty();
    protected GameLevel gameLevel;

    protected HuntingTimer(String name, int numPhases) {
        super(requireNonNull(name));
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel = requireNonNull(gameLevel);
        stop();
        reset(TickTimer.INDEFINITE);
        phaseIndex.set(0);
    }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public IntegerProperty phaseIndexProperty() { return phaseIndex; }

    public int phaseIndex() { return phaseIndex.get(); }

    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public HuntingPhase phase() { return
        isEven(phaseIndex()) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING; }

    public void update() {
        if (gameLevel == null) {
            Logger.error("Cannot update hunting timer, no game level assigned");
            return;
        }
        if (hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex(), phase(), tickCount());
            int nextPhaseIndex = requireValidPhaseIndex(phaseIndex() + 1);
            startPhase(nextPhaseIndex);
        } else {
            doTick();
        }
    }

    public void startFirstPhase() {
        if (gameLevel == null) {
            Logger.error("Cannot start hunting timer, no game level assigned");
            return;
        }
        startPhase(0);
        logPhaseChange(); // no change event!
    }

    protected void startPhase(int phaseIndex) {
        long duration = huntingTicks(gameLevel.number(), phaseIndex);
        reset(duration);
        start();
        this.phaseIndex.set(phaseIndex);
    }

    protected int requireValidPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return phaseIndex;
    }

    protected void logPhaseChange() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), phase(), durationTicks(), (float) durationTicks() / Globals.NUM_TICKS_PER_SEC, this);
    }
}
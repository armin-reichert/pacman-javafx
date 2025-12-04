/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
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
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.UsefulFunctions.isOdd;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public abstract class HuntingTimer {

    protected final TickTimer tickTimer;
    protected final int numPhases;
    private final IntegerProperty phaseIndex = new SimpleIntegerProperty();

    protected HuntingTimer(String name, int numPhases) {
        this.tickTimer = new TickTimer(name);
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public long tickCount() {
        return tickTimer.tickCount();
    }

    public void reset() {
        tickTimer.stop();
        tickTimer.reset(TickTimer.INDEFINITE);
        phaseIndex.set(0);
    }

    public void start() {
        tickTimer.start();
    }

    public void stop() {
        tickTimer.stop();
    }

    public boolean isStopped() {
        return tickTimer.isStopped();
    }

    public long remainingTicks() {
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

    public void startFirstPhase(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        startPhase(levelNumber, 0);
        logPhaseChange();
    }

    public void logPhaseChange() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), phase(), tickTimer.durationTicks(),
            (float) tickTimer.durationTicks() / Globals.NUM_TICKS_PER_SEC, this);
    }

    protected void startPhase(int levelNumber, int phaseIndex) {
        final long duration = huntingTicks(levelNumber, phaseIndex);
        tickTimer.reset(duration);
        tickTimer.start();
        phaseIndexProperty().set(phaseIndex);
    }

    protected int requireValidPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return phaseIndex;
    }
}
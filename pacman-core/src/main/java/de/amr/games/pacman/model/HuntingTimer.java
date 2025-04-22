/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.timer.TickTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.*;

/**
 * Controls timing of hunting phases (alternating scatter vs. chasing phases).
 */
public abstract class HuntingTimer {

    public enum HuntingPhase {SCATTERING, CHASING}

    private final TickTimer timer = new TickTimer("Timer-" + getClass().getSimpleName());
    private final int numPhases;
    private final IntegerProperty phaseIndexPy = new SimpleIntegerProperty();

    protected HuntingTimer(int numPhases) {
        this.numPhases = requireNonNegativeInt(numPhases);
        phaseIndexPy.addListener((py, ov, nv) -> logPhase());
    }

    protected void logPhase() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), phase(), timer.durationTicks(), (float) timer.durationTicks() / Globals.TICKS_PER_SECOND, this);
    }

    public void reset() {
        timer.stop();
        timer.reset(TickTimer.INDEFINITE);
        phaseIndexPy.set(0);
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }
    public boolean isStopped() { return timer.isStopped(); }
    public long tickCount() { return timer.tickCount(); }
    public long remainingTicks() { return timer.remainingTicks(); }

    public IntegerProperty phaseIndexProperty() { return phaseIndexPy; }

    public HuntingPhase phase() { return isEven(phaseIndex()) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING; }

    public int phaseIndex() { return phaseIndexPy.get(); }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public void update(int levelNumber) {
        if (timer.hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex(), phase(), timer.tickCount());
            startNextPhase(levelNumber);
        } else {
            timer.doTick();
        }
    }

    private int requireValidPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return phaseIndex;
    }

    public void startFirstHuntingPhase(int levelNumber) {
        startPhase(0, requireValidLevelNumber(levelNumber));
        logPhase(); // no change event!
    }

    public void startNextPhase(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        int nextPhaseIndex = requireValidPhaseIndex(phaseIndex() + 1);
        startPhase(nextPhaseIndex, levelNumber);
    }

    private void startPhase(int phaseIndex, int levelNumber) {
        long duration = huntingTicks(levelNumber, phaseIndex);
        timer.reset(duration);
        timer.start();
        phaseIndexPy.set(phaseIndex);
    }

    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }
}
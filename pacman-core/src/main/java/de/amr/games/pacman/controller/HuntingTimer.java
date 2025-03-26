/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.timer.TickTimer;
import org.tinylog.Logger;

import java.util.Optional;


/**
 * Hunting happens in a sequence of retreat (scatter) and attack (chasing) phases.
 */
public abstract class HuntingTimer extends TickTimer {

    public enum HuntingPhase {SCATTERING, CHASING}

    private final int numPhases;
    private int phaseIndex;
    private HuntingPhase huntingPhase;
    private Runnable phaseChangeAction;

    protected HuntingTimer(int numPhases) {
        super("HuntingTimer");
        this.numPhases = numPhases;
        phaseIndex = 0;
        huntingPhase = HuntingPhase.SCATTERING;
    }

    public void reset() {
        stop();
        reset(TickTimer.INDEFINITE);
        phaseIndex = 0;
        huntingPhase = HuntingPhase.SCATTERING;
    }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public void setOnPhaseChange(Runnable phaseChangeAction) {
        this.phaseChangeAction = phaseChangeAction;
    }

    public void startFirstHuntingPhase(int levelNumber) {
        startHuntingPhase(0, HuntingPhase.SCATTERING, huntingTicks(levelNumber, 0));
    }

    public void update(int levelNumber) {
        if (hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex, huntingPhase, tickCount());
            startNextPhase(levelNumber);
        } else {
            doTick();
        }
    }

    public void startNextPhase(int levelNumber) {
        int nextPhaseIndex = phaseIndex + 1;
        // alternate between CHASING and SCATTERING
        HuntingPhase nextHuntingPhase = huntingPhase == HuntingPhase.SCATTERING
            ? HuntingPhase.CHASING : HuntingPhase.SCATTERING;
        startHuntingPhase(nextPhaseIndex, nextHuntingPhase, huntingTicks(levelNumber, nextPhaseIndex));
        if (phaseChangeAction != null) {
            phaseChangeAction.run();
        }
    }

    private void startHuntingPhase(int phaseIndex, HuntingPhase type, long duration) {
        this.phaseIndex = checkHuntingPhaseIndex(phaseIndex);
        huntingPhase = type;
        reset(duration);
        start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            this.phaseIndex, huntingPhase,
            durationTicks(), (float) durationTicks() / Globals.TICKS_PER_SECOND, this);
    }

    private byte checkHuntingPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return (byte) phaseIndex;
    }

    public int phaseIndex() {
        return phaseIndex;
    }

    public HuntingPhase huntingPhase() {
        return huntingPhase;
    }

    public Optional<Integer> currentScatterPhaseIndex() {
        return huntingPhase == HuntingPhase.SCATTERING
            ? Optional.of(phaseIndex / 2)
            : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return huntingPhase == HuntingPhase.CHASING
            ? Optional.of(phaseIndex / 2)
            : Optional.empty();
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.Optional;

/**
 * Hunting happens in a sequence of scatter and chasing phases.
 */
public abstract class HuntingTimer extends TickTimer {

    public enum PhaseType { SCATTERING, CHASING }

    private static byte checkHuntingPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + phaseIndex);
        }
        return (byte) phaseIndex;
    }

    private int phaseIndex;
    private PhaseType phaseType;
    private Runnable phaseChangeAction;

    protected HuntingTimer() {
        super("HuntingTimer");
        phaseIndex = 0;
        phaseType = PhaseType.SCATTERING;
    }

    public void reset() {
        stop();
        reset(TickTimer.INDEFINITE);
        phaseIndex = 0;
        phaseType = PhaseType.SCATTERING;
    }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public void setOnPhaseChange(Runnable phaseChangeAction) {
        this.phaseChangeAction = phaseChangeAction;
    }

    public void startHunting(int levelNumber) {
        startHuntingPhase(0, PhaseType.SCATTERING, huntingTicks(levelNumber, 0));
    }

    public void startNextPhase(int levelNumber) {
        int nextPhaseIndex = phaseIndex + 1;
        // alternate between CHASING and SCATTERING
        PhaseType nextPhaseType = phaseType == PhaseType.SCATTERING
            ? PhaseType.CHASING : PhaseType.SCATTERING;
        startHuntingPhase(nextPhaseIndex, nextPhaseType, huntingTicks(levelNumber, nextPhaseIndex));
        if (phaseChangeAction != null) {
            phaseChangeAction.run();
        }
    }

    private void startHuntingPhase(int phaseIndex, PhaseType type, long duration) {
        this.phaseIndex = checkHuntingPhaseIndex(phaseIndex);
        phaseType = type;
        reset(duration);
        start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            this.phaseIndex, phaseType,
            durationTicks(), (float) durationTicks() / GameModel.TICKS_PER_SECOND, this);
    }

    public int phaseIndex() {
        return phaseIndex;
    }

    public PhaseType phaseType() {
        return phaseType;
    }

    public Optional<Integer> currentScatterPhaseIndex() {
        return phaseType == HuntingTimer.PhaseType.SCATTERING
            ? Optional.of(phaseIndex / 2)
            : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return phaseType == HuntingTimer.PhaseType.CHASING
            ? Optional.of(phaseIndex / 2)
            : Optional.empty();
    }
}
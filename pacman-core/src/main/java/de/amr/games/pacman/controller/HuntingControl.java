/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

public class HuntingControl {

    public static byte checkHuntingPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + phaseIndex);
        }
        return (byte) phaseIndex;
    }

    public enum PhaseType { SCATTERING, CHASING }

    private final TickTimer timer;
    private int phaseIndex;
    private PhaseType phaseType;

    public HuntingControl(String name) {
        timer = new TickTimer(name);
        phaseIndex = 0;
        phaseType = PhaseType.SCATTERING;
    }

    public void update() {
        timer.tick();
    }

    public long currentTick() {
        return timer.currentTick();
    }

    public long remaining() {
        return timer.remaining();
    }

    public boolean hasExpired() {
        return timer.hasExpired();
    }

    public void reset(long duration) {
        phaseIndex = 0;
        timer.reset(duration);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public boolean isStopped() {
        return timer.isStopped();
    }

    public void startHuntingPhase(int phaseIndex, PhaseType type, long duration) {
        this.phaseIndex = checkHuntingPhaseIndex(phaseIndex);
        phaseType = type;
        timer.reset(duration);
        timer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
                this.phaseIndex, phaseType,
                timer.duration(), (float) timer.duration() / GameModel.TICKS_PER_SECOND, timer);
    }

    public int phaseIndex() {
        return phaseIndex;
    }

    public PhaseType phaseType() {
        return phaseType;
    }

}

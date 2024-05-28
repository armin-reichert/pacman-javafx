/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
public class Pulse {

    public static final boolean ON = true;
    public static final boolean OFF = false;

    private final int ticksPerPhase;
    private boolean startPhase;
    private int numPhases;
    private boolean phase;
    private int tick;
    private int numPhasesCompleted;
    private boolean running;

    public Pulse(int numPhases, int ticksPerPhase, boolean startPhase) {
        this.numPhases = numPhases;
        this.ticksPerPhase = ticksPerPhase;
        this.startPhase = startPhase;
        reset();
    }

    public Pulse(int ticksPerPhase, boolean startPhase) {
        this(Integer.MAX_VALUE, ticksPerPhase, startPhase);
    }

    public void reset() {
        tick = 0;
        numPhasesCompleted = 0;
        phase = startPhase;
        running = false;
    }

    public void restart() {
        reset();
        start();
    }

    public void restart(int numPhases) {
        this.numPhases = numPhases;
        reset();
        start();
    }

    public void tick() {
        if (!running || numPhasesCompleted == numPhases) {
            return;
        }
        ++tick;
        if (tick == ticksPerPhase) {
            numPhasesCompleted++;
            phase = !phase;
            tick = 0;
        }
    }

    public void setStartPhase(boolean startPhase) {
        this.startPhase = startPhase;
    }

    public boolean isOn() {
        return phase;
    }

    public boolean isOff() {
        return !phase;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.timer;

/**
 * A pulse switches between states ON and OFF and keeps each state for <code>halfPeriod</code> ticks.
 */
public class Pulse {

    public enum State {
        ON, OFF;

        public State inverse() {
            return this == ON ? OFF : ON;
        }
    }

    private final int halfPeriod;
    private State startState;
    private State state;
    private int tick;
    private boolean running;

    public Pulse(int halfPeriod, State startState) {
        this.halfPeriod = halfPeriod;
        this.startState = startState;
        reset();
    }

    public void reset() {
        stop();
        tick = 0;
        state = startState;
    }

    public void restart() {
        reset();
        start();
    }

    public void tick() {
        if (!running) return;
        ++tick;
        if (tick == halfPeriod) {
            state = state.inverse();
            tick = 0;
        }
    }

    public State state() {
        return state;
    }

    public void setStartState(State startState) {
        this.startState = startState;
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
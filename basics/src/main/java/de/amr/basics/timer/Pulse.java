/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.timer;

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
    private long pulseCount;
    private boolean running;
    private boolean pulseTriggered;

    public Pulse(int halfPeriod, State startState) {
        this.halfPeriod = halfPeriod;
        this.startState = startState;
        reset();
    }

    public void reset() {
        stop();
        pulseCount = 0;
        state = startState;
    }

    public void restart() {
        reset();
        start();
    }

    public void triggerPulse() {
        pulseTriggered = false;
        if (running) {
            ++pulseCount;
            if (pulseCount % halfPeriod == 0) {
                state = state.inverse();
                pulseTriggered = true;
            }
        }
    }

    public State state() {
        return state;
    }

    public boolean pulseTriggered() {
        return pulseTriggered;
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
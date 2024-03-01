/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.lib.TickTimerEvent.Type;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.TickTimer.State.*;

/**
 * A simple, but useful, passive timer counting ticks.
 *
 * @author Armin Reichert
 */
public class TickTimer {

    public enum State {
        READY, RUNNING, STOPPED, EXPIRED;
    }

    public static final long INDEFINITE = Long.MAX_VALUE;

    /**
     * @param sec seconds
     */
    public long secToTicks(double sec) {
        return Math.round(sec * 60);
    }

    public static String ticksToString(long ticks) {
        return ticks == INDEFINITE ? "indefinite" : "" + ticks;
    }

    private final String name;
    private State state;
    private long duration;
    private long tick;
    private List<Consumer<TickTimerEvent>> subscribers;

    public TickTimer(String name) {
        this.name = name;
        resetIndefinitely();
    }

    public void addEventListener(Consumer<TickTimerEvent> subscriber) {
        if (subscribers == null) {
            subscribers = new ArrayList<>(3);
        }
        subscribers.add(subscriber);
    }

    public void removeEventListener(Consumer<TickTimerEvent> subscriber) {
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    private void fireEvent(TickTimerEvent e) {
        if (subscribers != null) {
            subscribers.forEach(subscriber -> subscriber.accept(e));
        }
    }

    @Override
    public String toString() {
        return String.format("[%s %s tick: %s remaining: %s total: %s]", name, state, ticksToString(tick),
            ticksToString(remaining()), ticksToString(duration));
    }

    public State state() {
        return state;
    }

    public String name() {
        return name;
    }

    /**
     * Sets the timer to given duration and its state to {@link State#READY}. The timer is not running after this call!
     *
     * @param ticks timer duration in ticks
     */
    public void reset(long ticks) {
        duration = ticks;
        tick = 1;
        state = READY;
        Logger.trace("{} reset", this);
        fireEvent(new TickTimerEvent(Type.RESET, ticks));
    }

    /**
     * Sets the timer duration in seconds.
     *
     * @param seconds number of seconds
     */
    public void resetSeconds(double seconds) {
        reset(secToTicks(seconds));
    }

    /**
     * Sets the timer to run for an indefinite amount of time. The timer can be forced to expire by calling
     * {@link #expire()}.
     */
    public void resetIndefinitely() {
        reset(INDEFINITE);
    }

    /**
     * Starts the timer. If the timer is already running, does nothing.
     */
    public void start() {
        switch (state) {
            case RUNNING: {
                Logger.trace("Timer {} not started, already running", this);
                break;
            }
            case EXPIRED: {
                Logger.trace("Timer {} not started, has expired", this);
                break;
            }
            default: {
                state = RUNNING;
                tick = 1;
                Logger.trace("{} started", this);
                fireEvent(new TickTimerEvent(Type.STARTED));
                break;
            }
        }
    }

    /**
     * Convenience method to reset the timer to {@link TickTimer#INDEFINITE} and start it.
     */
    public void restartIndefinitely() {
        resetIndefinitely();
        start();
    }

    /**
     * Convenience method to reset the timer to given seconds and start it.
     *
     * @param seconds number of seconds
     */
    public void restartSeconds(double seconds) {
        resetSeconds(seconds);
        start();
    }

    /**
     * Stops the timer. If the timer is not running, does nothing.
     */
    public void stop() {
        switch (state) {
            case RUNNING: {
                state = STOPPED;
                Logger.trace("{} stopped", this);
                fireEvent(new TickTimerEvent(Type.STOPPED));
                break;
            }
            case STOPPED: {
                Logger.trace("{} already stopped", this);
                break;
            }
            case READY: {
                Logger.trace("{} not stopped, was not running", this);
                break;
            }
            case EXPIRED: {
                Logger.trace("{} not stopped, has expired", this);
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected value: " + state);
        }
    }

    /**
     * Advances the timer by one step, if it is running. Does nothing, else.
     */
    public void advance() {
        if (state == RUNNING) {
            if (tick == duration) {
                expire();
            } else {
                ++tick;
            }
        }
    }

    /**
     * Forces the timer to expire.
     */
    public void expire() {
        if (state != EXPIRED) {
            state = EXPIRED;
            Logger.trace("{} expired", this);
            fireEvent(new TickTimerEvent(Type.EXPIRED, tick));
        }
    }

    public boolean hasExpired() {
        return state == EXPIRED;
    }

    public boolean isReady() {
        return state == READY;
    }

    public boolean isRunning() {
        return state == RUNNING;
    }

    public boolean isStopped() {
        return state == STOPPED;
    }

    public long duration() {
        return duration;
    }

    public long tick() {
        return tick;
    }

    public boolean atSecond(double seconds) {
        return tick == secToTicks(seconds);
    }

    public boolean betweenSeconds(double begin, double end) {
        return secToTicks(begin) <= tick && tick < secToTicks(end);
    }

    public long remaining() {
        return duration == INDEFINITE ? INDEFINITE : duration - tick;
    }
}
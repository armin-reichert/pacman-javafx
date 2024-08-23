/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.timer;

import de.amr.games.pacman.lib.timer.TickTimerEvent.Type;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.timer.TickTimer.State.*;

/**
 * A simple but useful passive timer counting ticks.
 *
 * @author Armin Reichert
 */
public class TickTimer {

    public enum State {
        READY, RUNNING, STOPPED, EXPIRED
    }

    public static final long INDEFINITE = Long.MAX_VALUE; // TODO use -1?

    /**
     * @param sec seconds
     */
    public static long secToTicks(double sec) {
        return Math.round(sec * 60);
    }

    public static String ticksToString(long ticks) {
        return ticks == INDEFINITE ? "indefinite" : "" + ticks;
    }

    private final String name;
    private State state;
    private long duration;
    private long tickCount;
    private List<Consumer<TickTimerEvent>> listeners;

    public TickTimer(String name) {
        this.name = name != null ? name : "unnamed";
        resetIndefinitely();
    }

    @Override
    public String toString() {
        return String.format("[%s %s tick: %s remaining: %s total: %s]", name, state, ticksToString(tickCount),
            ticksToString(remaining()), ticksToString(duration));
    }

    /**
     * Sets the timer to given duration and its state to {@link State#READY}. The timer is not running after this call!
     *
     * @param duration timer duration in ticks
     */
    public void reset(long duration) {
        this.duration = duration;
        tickCount = 0;
        state = READY;
        Logger.trace("{} reset", this);
        publishEvent(new TickTimerEvent(Type.RESET, duration));
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
        if (state == STOPPED || state == READY) {
            state = RUNNING;
            Logger.trace("{} started", this);
            publishEvent(new TickTimerEvent(Type.STARTED));
        } else {
            Logger.warn("Timer {} not started, state is {}", this, state);
        }
    }

    /**
     * Stops the timer. If the timer is not running, does nothing.
     */
    public void stop() {
        if (state == RUNNING) {
            state = STOPPED;
            Logger.trace("{} stopped", this);
            publishEvent(new TickTimerEvent(Type.STOPPED));
        } else {
            Logger.warn("Timer {} not stopped, state is {}", this, state);
        }
    }

    /**
     * Advances the timer by one step, if it is running. Does nothing, else.
     */
    public void tick() {
        if (state == RUNNING) {
            if (tickCount == duration) {
                expire();
            } else {
                ++tickCount;
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
            publishEvent(new TickTimerEvent(Type.EXPIRED, tickCount));
        } else {
            Logger.warn("Timer {} not expired, state is {}", this, state);
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

    public State state() {
        return state;
    }

    public String name() {
        return name;
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

    public long currentTick() {
        return tickCount;
    }

    public boolean atSecond(double seconds) {
        return tickCount == secToTicks(seconds);
    }

    public boolean betweenSeconds(double begin, double end) {
        return secToTicks(begin) <= tickCount && tickCount < secToTicks(end);
    }

    public long remaining() {
        return duration == INDEFINITE ? INDEFINITE : duration - tickCount;
    }

    public void addListener(Consumer<TickTimerEvent> subscriber) {
        if (listeners == null) {
            listeners = new ArrayList<>(3);
        }
        listeners.add(subscriber);
    }

    public void removeListener(Consumer<TickTimerEvent> subscriber) {
        if (listeners != null) {
            listeners.remove(subscriber);
        }
    }

    private void publishEvent(TickTimerEvent e) {
        if (listeners != null) {
            listeners.forEach(subscriber -> subscriber.accept(e));
        }
    }
}
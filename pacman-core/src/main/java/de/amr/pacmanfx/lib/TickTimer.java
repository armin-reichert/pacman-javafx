/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.TickTimerEvent.Type;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.amr.pacmanfx.lib.TickTimer.State.*;
import static java.util.Objects.requireNonNull;

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
     * @param seconds seconds
     * @return number of ticks corresponding to given seconds at the normal frame rate (60Hz)
     */
    public static long secToTicks(double seconds) {
        return Math.round(seconds * Globals.NUM_TICKS_PER_SEC);
    }

    /**
     * @param ticks number of ticks
     * @return string representation of ticks
     */
    public static String ticksToString(long ticks) {
        return ticks == INDEFINITE ? "indefinite" : "" + ticks;
    }

    private static void assertValidTickNumber(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Invalid tick number: " + ticks);
        }
    }

    private final String name;
    private State state;
    private long duration;
    private long tickCount;
    private List<Consumer<TickTimerEvent>> listeners;

    public TickTimer(String name) {
        this.name = name != null ? name : "unnamed";
        resetIndefiniteTime();
    }

    @Override
    public String toString() {
        return "[%s %s tick: %s remaining: %s total: %s]".formatted(name, state,
            ticksToString(tickCount),
            ticksToString(remainingTicks()),
            ticksToString(duration)
        );
    }

    /**
     * Sets the timer to the given duration and sets the state to {@link State#READY}.
     * The timer is not yet running after this call.
     *
     * @param ticks timer duration in ticks
     */
    public void reset(long ticks) {
        assertValidTickNumber(ticks);
        duration = ticks;
        tickCount = 0;
        state = READY;
        Logger.trace("{} reset", this);
        publishEvent(new TickTimerEvent(Type.RESET, ticks));
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
    public void resetIndefiniteTime() {
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
        }
    }

    /**
     * Advances the timer by one step, if it is running. Does nothing, else.
     */
    public void doTick() {
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
        restartTicks(INDEFINITE);
    }

    /**
     * Convenience method to reset the timer to given number of seconds and start it.
     *
     * @param seconds number of seconds
     */
    public void restartSeconds(double seconds) {
        restartTicks(secToTicks(seconds));
    }

    /**
     * Convenience method to reset the timer to given number of ticks and start it.
     *
     * @param ticks number of ticks
     */
    public void restartTicks(long ticks) {
        assertValidTickNumber(ticks);
        reset(ticks);
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

    /**
     * @return timer duration in number of ticks at 60Hz
     */
    public long durationTicks() {
        return duration;
    }

    /**
     * @return the current tick (starting at 0)
     */
    public long tickCount() {
        return tickCount;
    }

    /**
     * @param second time instant
     * @return if timer is at instant
     */
    public boolean atSecond(double second) {
        return tickCount == secToTicks(second);
    }

    /**
     * @param begin start second (inclusive)
     * @param end end second (exclusive)
     * @return if timer is in current range
     */
    public boolean betweenSeconds(double begin, double end) {
        return secToTicks(begin) <= tickCount && tickCount < secToTicks(end);
    }

    /**
     * @return number of ticks remaining until timer duration, {@code INDEFINITE} if duration is indefinite
     */
    public long remainingTicks() {
        return duration == INDEFINITE ? INDEFINITE : duration - tickCount;
    }

    public void addListener(Consumer<TickTimerEvent> subscriber) {
        requireNonNull(subscriber);
        if (listeners == null) {
            listeners = new ArrayList<>(3);
        }
        listeners.add(subscriber);
    }

    public void removeListener(Consumer<TickTimerEvent> subscriber) {
        requireNonNull(subscriber);
        if (listeners != null) {
            listeners.remove(subscriber);
        }
    }

    private void publishEvent(TickTimerEvent event) {
        requireNonNull(event);
        if (listeners != null) {
            listeners.forEach(subscriber -> subscriber.accept(event));
        }
    }
}
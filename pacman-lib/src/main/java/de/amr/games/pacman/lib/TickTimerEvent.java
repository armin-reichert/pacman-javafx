/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
public class TickTimerEvent {

    public enum Type {
        RESET, STARTED, STOPPED, EXPIRED;
    }

    public TickTimerEvent(Type type, long ticks) {
        this.type = type;
        this.ticks = ticks;
    }

    public TickTimerEvent(Type type) {
        this.type = type;
        ticks = 0;
    }

    public final Type type;
    public final long ticks;
}
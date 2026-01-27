/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib;

import java.time.Duration;
import java.time.Instant;

public class StopWatch {

    private Instant startTime;

    public StopWatch() {
        reset();
    }

    public void reset() {
        startTime = Instant.now();
    }

    public Duration passedTime() {
        return Duration.between(startTime, Instant.now());
    }

    public double passedMillis() { return passedTime().toNanos() / 1e6; }

    public long passedNanos() { return passedTime().toNanos(); }
}

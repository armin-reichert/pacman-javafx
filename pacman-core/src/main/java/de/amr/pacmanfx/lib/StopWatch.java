/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import java.time.Duration;
import java.time.Instant;

public class StopWatch {

    private Instant startTime;

    public static StopWatch create() {
        StopWatch watch = new StopWatch();
        watch.reset();
        return watch;
    }

    public void reset() {
        startTime = Instant.now();
    }

    public Duration passedTime() {
        return Duration.between(startTime, Instant.now());
    }
}

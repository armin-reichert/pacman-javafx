/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.fsm;

@FunctionalInterface
public interface StateChangeListener<S> {

    void onStateChange(S oldState, S newState);
}

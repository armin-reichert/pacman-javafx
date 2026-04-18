/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.fsm;

@FunctionalInterface
public interface StateChangeListener<S> {

    void onStateChange(S oldState, S newState);
}

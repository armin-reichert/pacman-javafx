/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.fsm;

/**
 * @author Armin Reichert
 */
@FunctionalInterface
public interface FsmStateChangeListener<S> {

    void onStateChange(S oldState, S neeState);
}

/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
@FunctionalInterface
public interface FsmStateChangeListener<S> {

	void onStateChange(S oldState, S neeState);
}

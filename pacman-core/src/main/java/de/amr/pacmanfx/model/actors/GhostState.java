/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

/**
 * A ghost is exactly in one of these states at any point in time.
 *
 * @author Armin Reichert
 */
public enum GhostState {
    LOCKED,
    LEAVING_HOUSE,
    HUNTING_PAC,
    FRIGHTENED,
    EATEN,
    RETURNING_HOME,
    ENTERING_HOUSE
}
/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * A ghost is exactly in one of these states at any point in time.
 * 
 * @author Armin Reichert
 */
public enum GhostState {
	LOCKED, ENTERING_HOUSE, LEAVING_HOUSE, FRIGHTENED, HUNTING_PAC, EATEN, RETURNING_TO_HOUSE;
}
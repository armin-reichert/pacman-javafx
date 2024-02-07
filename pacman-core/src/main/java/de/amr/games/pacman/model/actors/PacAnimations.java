/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface PacAnimations {
	public static final String MUNCHING = "munching";
	public static final String DYING    = "dying";
	// In Pac-Man cutscene, big Pac-Man appears
	public static final String BIG_PACMAN = "big_pacman";
	// In Ms. Pac-Man cutscenes, also Ms. PacMan's husband appears
	public static final String HUSBAND_MUNCHING = "husband_munching";
}
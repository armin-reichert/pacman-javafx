/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

/**
 * @author Armin Reichert
 */
public class IllegalGhostIDException extends IllegalArgumentException {

	public IllegalGhostIDException(int id) {
		super(String.format("Illegal ghost ID value '%d' (Allowed values: 0-3)", id));
	}
}
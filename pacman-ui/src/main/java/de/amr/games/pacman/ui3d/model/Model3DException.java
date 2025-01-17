/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.model;

/**
 * @author Armin Reichert
 */
public class Model3DException extends RuntimeException {

    public Model3DException(String message, Object... args) {
        super(message.formatted(args));
    }
}
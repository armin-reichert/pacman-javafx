/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

/**
 * @author Armin Reichert
 */
public class Model3DException extends RuntimeException {

    public Model3DException(String message, Object... args) {
        super(message.formatted(args));
    }
}
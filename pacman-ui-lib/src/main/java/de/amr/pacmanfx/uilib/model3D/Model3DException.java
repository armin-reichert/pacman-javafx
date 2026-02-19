/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

/**
 * @author Armin Reichert
 */
public class Model3DException extends Exception {

    public Model3DException(String message, Throwable cause) {
        super(message, cause);
    }

    public Model3DException(String message, Object... args) {
        super(message.formatted(args));
    }
}
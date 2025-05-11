/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

/**
 * @author Armin Reichert
 */
public class GameException extends RuntimeException {

    public static GameException invalidGhostID(byte id) {
        return new GameException(String.format("Illegal ghost ID value '%d' (Allowed values: 0-3)", id));
    }

    public static GameException invalidLevelNumber(int number) {
        return new GameException(String.format("Illegal level number '%d' (Allowed values: 1-)", number));
    }

    public GameException(String message) {
        super(message, null);
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

public class GameException extends RuntimeException {

    public static GameException invalidGhostPersonality(byte id) {
        return new GameException(String.format("Illegal ghost ID value '%d' (Allowed values: 0-3)", id));
    }

    public static GameException invalidLevelNumber(int number) {
        return new GameException(String.format("Illegal level number '%d' (Allowed values: 1-)", number));
    }

    public GameException(String message) {
        super(message, null);
    }
}

/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

/**
 * @author Armin Reichert
 */
public class IllegalLevelNumberException extends IllegalArgumentException {

    public IllegalLevelNumberException(int number) {
        super(String.format("Illegal level number '%d' (Allowed values: 1-)", number));
    }
}
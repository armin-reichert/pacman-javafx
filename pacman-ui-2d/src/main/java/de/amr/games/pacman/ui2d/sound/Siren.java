/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.sound;

import javafx.scene.media.MediaPlayer;

/**
 * @author Armin Reichert
 */
public record Siren(int number, MediaPlayer player) {
    public Siren {
        if (number < 1 || number > 4) {
            throw new IllegalArgumentException("Illegal siren number: " + number);
        }
        if (player == null) {
            throw new IllegalArgumentException("No siren player specified");
        }
    }
}

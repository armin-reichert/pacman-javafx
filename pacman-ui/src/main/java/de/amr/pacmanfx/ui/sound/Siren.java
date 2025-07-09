/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import javafx.scene.media.MediaPlayer;

/**
 * @author Armin Reichert
 */
public record Siren(SoundID id, MediaPlayer player) {
    public Siren {
        if (player == null) {
            throw new IllegalArgumentException("No siren player specified");
        }
    }
}

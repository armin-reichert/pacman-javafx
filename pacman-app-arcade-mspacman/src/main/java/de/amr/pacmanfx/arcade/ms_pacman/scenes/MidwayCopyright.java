/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.image.Image;

import static java.util.Objects.requireNonNull;

public class MidwayCopyright extends Actor {
    private final Image logo;

    public MidwayCopyright(Image logo) {
        this.logo = requireNonNull(logo);
    }

    public Image logo() {
        return logo;
    }
}

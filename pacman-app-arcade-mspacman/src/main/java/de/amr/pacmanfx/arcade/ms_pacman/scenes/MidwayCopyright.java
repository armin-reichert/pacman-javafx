/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.paint.Color;

public class MidwayCopyright extends Actor {

    private Color color;

    public MidwayCopyright() { super(null); }

    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

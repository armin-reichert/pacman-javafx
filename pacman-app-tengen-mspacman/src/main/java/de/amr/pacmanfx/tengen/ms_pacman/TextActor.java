/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.actors.Actor;

public class TextActor extends Actor {

    private final String text;

    public TextActor(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.clapperboard;


import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class ClapperboardInscriptionComp implements GameEntityComponent {
    private int number;
    private String text;

    public int number() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void reset() {
    }
}

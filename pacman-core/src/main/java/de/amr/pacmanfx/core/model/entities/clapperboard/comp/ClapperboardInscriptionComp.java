/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.clapperboard.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import static java.util.Objects.requireNonNull;

public class ClapperboardInscriptionComp implements GameEntityComponent {
    private String number;
    private String text;

    public String number() {
        return number;
    }

    public void setNumber(String number) {
        this.number = requireNonNull(number);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = requireNonNull(text);
    }

    @Override
    public void reset() {}
}

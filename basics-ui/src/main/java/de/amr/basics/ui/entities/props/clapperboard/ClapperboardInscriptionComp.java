/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.clapperboard;

import de.amr.basics.ecs.GameEntityComp;

import static java.util.Objects.requireNonNull;

public class ClapperboardInscriptionComp implements GameEntityComp {
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
}

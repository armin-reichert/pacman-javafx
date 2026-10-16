/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.textview;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.Movement;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.basics.TileDimension.tilesPx;

public class TextView extends GameEntity {

    public TextView() {
        setComponent(TextViewDataComp.class, new TextViewDataComp());
        setComponent(Movement.class, new Movement());
    }

    public static TextView create(String text, Color color, Font font, float tileX, float tileY) {
        final var textDisplay = new TextView();
        textDisplay.data().setFillColor(color);
        textDisplay.data().setFont(font);
        textDisplay.data().setText(text);
        textDisplay.position().set(tilesPx(tileX), tilesPx(tileY));
        textDisplay.show();
        return textDisplay;
    }

    public TextViewDataComp data() {
        return assertComponent(TextViewDataComp.class);
    }

    public Movement movement() {
        return assertComponent(Movement.class);
    }
}

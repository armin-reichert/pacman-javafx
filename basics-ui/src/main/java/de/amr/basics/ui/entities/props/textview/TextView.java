/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.textview;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.basics.TileDimension.tilesPx;

public class TextView extends GameEntity {

    public TextView() {
        setComponent(TextViewDataComp.class, new TextViewDataComp());
        setComponent(MovementComp.class, new MovementComp());
    }

    public static TextView createText(String text, Color color, Font font, float tileX, float tileY) {
        final var textDisplay = new TextView();
        textDisplay.data().setFillColor(color);
        textDisplay.data().setFont(font);
        textDisplay.data().setText(text);
        textDisplay.pos().set(tilesPx(tileX), tilesPx(tileY));
        textDisplay.show();
        return textDisplay;
    }

    public TextViewDataComp data() {
        return assertComponent(TextViewDataComp.class);
    }

    public MovementComp movement() {
        return assertComponent(MovementComp.class);
    }
}

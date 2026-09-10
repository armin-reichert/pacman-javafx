/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.textdisplay.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextDisplayDataComp implements GameEntityComp {

    private String text;

    private Font font;

    private Color fillColor;

    private Color strokeColor;

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font font() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color fillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color strokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }
}

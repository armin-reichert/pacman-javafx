/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.props.textdisplay;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextDisplayDataComp implements GameEntityComp {

    private String text = "";

    private Font font = Font.font(8);

    private Color fillColor = Color.WHITE;

    private Color strokeColor = Color.WHITE;

    private boolean center = false;

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

    public boolean center() {
        return center;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    @Override
    public String toString() {
        return "TextDisplayDataComp{" +
            "text='" + text + '\'' +
            ", font=" + font +
            ", fillColor=" + fillColor +
            ", strokeColor=" + strokeColor +
            ", center=" + center +
            '}';
    }
}

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import javafx.scene.paint.Color;

public class ColoredBackground implements Renderable {

    private final RectShort rect;
    private final Color color;

    public ColoredBackground(int xMin, int yMin, int width, int height, Color color) {
        rect = new RectShort((short) xMin, (short) yMin, (short) width, (short) height);
        this.color = color;
    }

    public RectShort rect() {
        return rect;
    }

    public Color color() {
        return color;
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.BACKGROUND;
    }
}

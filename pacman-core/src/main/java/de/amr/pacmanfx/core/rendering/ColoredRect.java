/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import javafx.scene.paint.Color;

public class ColoredRect implements Renderable {

    private final RectShort rect;
    private final Color color;

    public ColoredRect(int xMin, int yMin, int width, int height, Color color) {
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

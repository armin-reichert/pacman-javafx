/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public class MenuOptionView implements Renderable {
    private final String label;
    private final int separatorTileX;
    private final Vector2f offset;

    private boolean selected;
    private String value;

    public MenuOptionView(String label, int separatorTileX, Vector2f offset) {
        this.label = label;
        this.separatorTileX = separatorTileX;
        this.offset = offset;
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }

    public String label() {
        return label;
    }

    public int separatorTileX() {
        return separatorTileX;
    }

    @Override
    public Vector2f offset() {
        return offset;
    }

    public boolean selected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

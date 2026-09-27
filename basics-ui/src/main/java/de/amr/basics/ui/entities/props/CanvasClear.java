/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props;

import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public class CanvasClear implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.BACKGROUND;
    }
}

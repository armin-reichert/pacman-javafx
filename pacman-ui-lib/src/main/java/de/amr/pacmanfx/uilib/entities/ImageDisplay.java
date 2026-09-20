/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.uilib.entities.imagedisplay.comp.ImageDisplayComp;

public class ImageDisplay extends GameEntity implements Renderable {

    public ImageDisplay() {
        setComp(ImageDisplayComp.class, new ImageDisplayComp());
    }

    public ImageDisplayComp image() {
        return reqComp(ImageDisplayComp.class);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}

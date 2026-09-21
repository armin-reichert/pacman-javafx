/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.imagedisplay;

import de.amr.basics.rendering.Renderable;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.ui.ecs.GameEntity;

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

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.imagedisplay;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public class ImageView extends GameEntity implements Renderable {

    public ImageView() {
        setComp(ImageViewComp.class, new ImageViewComp());
    }

    public ImageViewComp image() {
        return reqComp(ImageViewComp.class);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}

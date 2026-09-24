/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.imagedisplay;

import de.amr.basics.ecs.GameEntity;

public class ImageView extends GameEntity {

    public ImageView() {
        setComp(ImageViewComp.class, new ImageViewComp());
    }

    public ImageViewComp image() {
        return reqComp(ImageViewComp.class);
    }
}

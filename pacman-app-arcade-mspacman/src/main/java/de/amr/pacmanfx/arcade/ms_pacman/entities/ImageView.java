/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.pacmanfx.arcade.ms_pacman.entities.imageview.comp.ImageViewComp;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

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

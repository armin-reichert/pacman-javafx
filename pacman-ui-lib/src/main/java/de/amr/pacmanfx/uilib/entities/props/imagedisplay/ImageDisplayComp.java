/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities.props.imagedisplay;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import javafx.scene.image.Image;

public class ImageDisplayComp implements GameEntityComp {

    private Image image;

    public Image image() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}

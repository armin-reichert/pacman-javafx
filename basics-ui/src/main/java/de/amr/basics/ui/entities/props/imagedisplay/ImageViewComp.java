/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.imagedisplay;

import de.amr.basics.ecs.GameEntityComp;
import javafx.scene.image.Image;

public class ImageViewComp implements GameEntityComp {

    private Image image;

    private boolean smoothing;

    public Image image() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean smoothing() {
        return smoothing;
    }

    public void setSmoothing(boolean smoothing) {
        this.smoothing = smoothing;
    }
}

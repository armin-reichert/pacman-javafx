/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.messageview;

import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class MessageView extends Group implements DisposableGraphicsObject {

    private ImageView imageView;
    private float displaySeconds;

    public MessageView() {
        setRotationAxis(Rotate.X_AXIS);
        setRotate(90);
    }

    public ImageView imageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = requireNonNull(imageView);
        getChildren().setAll(imageView);
    }

    public void setDisplaySeconds(float displaySeconds) {
        this.displaySeconds = displaySeconds;
    }

    public float displaySeconds() {
        return displaySeconds;
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
        imageView = null;
    }
}
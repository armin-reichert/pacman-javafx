/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class MessageView3DComp implements GameEntityComp, DisposableGraphicsObject {

    private final Group root = new Group();
    private ImageView imageView;
    private float displaySeconds;

    public MessageView3DComp() {
        root.setRotationAxis(Rotate.X_AXIS);
        root.setRotate(90);
    }

    public Group root() {
        return root;
    }

    public ImageView imageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = requireNonNull(imageView);
        root.getChildren().setAll(imageView);
    }

    public void setDisplaySeconds(float displaySeconds) {
        this.displaySeconds = displaySeconds;
    }

    public float displaySeconds() {
        return displaySeconds;
    }

    @Override
    public void dispose() {
        cleanupGroup(root, true);
        imageView = null;
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.pacmanfx.model.GameLevelEntity;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class NumberBox3D extends Group implements GameLevelEntity {

    public static final int DEFAULT_SIZE_X = 14;
    public static final int DEFAULT_SIZE_Y = 8;
    public static final int DEFAULT_SIZE_Z = 8;

    private final Group riseGroup = new Group();
    private final Group rotateGroup = new Group();
    private final Box box;

    public NumberBox3D(Image numberImage) {
        box = new Box(DEFAULT_SIZE_X, DEFAULT_SIZE_Y, DEFAULT_SIZE_Z);

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(numberImage);
        box.setMaterial(material);

        // Build transform hierarchy
        rotateGroup.getChildren().add(box);
        riseGroup.getChildren().add(rotateGroup);
        getChildren().add(riseGroup);
    }

    public Box box() {
        return box;
    }

    public Group rotateGroup() {
        return rotateGroup;
    }

    public Group riseGroup() {
        return riseGroup;
    }
}

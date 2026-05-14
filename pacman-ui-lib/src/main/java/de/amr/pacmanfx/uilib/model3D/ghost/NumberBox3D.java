/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class NumberBox3D extends Box {

    public static final int DEFAULT_SIZE_X = 14;
    public static final int DEFAULT_SIZE_Y = 8;
    public static final int DEFAULT_SIZE_Z = 8;

    public NumberBox3D(Image numberImage) {
        super(DEFAULT_SIZE_X, DEFAULT_SIZE_Y, DEFAULT_SIZE_Z);

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(numberImage);
        setMaterial(material);
    }
}

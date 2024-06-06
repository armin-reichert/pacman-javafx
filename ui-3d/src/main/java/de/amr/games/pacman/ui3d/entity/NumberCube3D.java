/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class NumberCube3D extends Box {

    private final RotateTransition numberRotation;

    public NumberCube3D(double sizeX,double sizeY, double sizeZ) {
        super(sizeX, sizeY, sizeZ);

        numberRotation = new RotateTransition(Duration.seconds(1), this);
        numberRotation.setAxis(Rotate.X_AXIS);
        numberRotation.setFromAngle(0);
        numberRotation.setToAngle(360);
        numberRotation.setInterpolator(Interpolator.LINEAR);
        numberRotation.setRate(0.75);
    }

    public void stopRotation() {
        numberRotation.stop();
    }

    public void startRotation() {
        numberRotation.playFromStart();
    }

    public void setImage(Image image) {
        var material = new PhongMaterial();
        material.setDiffuseMap(image);
        setMaterial(material);
    }
}

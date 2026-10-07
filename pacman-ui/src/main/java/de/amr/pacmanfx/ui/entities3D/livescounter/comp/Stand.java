/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter.comp;


import de.amr.basics.ui.assets.DisposableGraphicsObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class Stand extends Group implements DisposableGraphicsObject {

    private final Cylinder pillar;
    private final Cylinder podium;

    public Stand(
        ObjectProperty<PhongMaterial> pillarMaterial,
        ObjectProperty<PhongMaterial> plateMaterial,
        DoubleProperty plateRadius,
        DoubleProperty plateThickness
    ) {
        requireNonNull(pillarMaterial);
        requireNonNull(plateMaterial);
        requireNonNull(plateRadius);
        requireNonNull(plateThickness);

        pillar = new Cylinder(1, 0.1);
        pillar.materialProperty().bind(pillarMaterial);
        pillar.translateZProperty().bind(pillar.heightProperty().multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);

        podium = new Cylinder();
        podium.radiusProperty().bind(plateRadius);
        podium.heightProperty().bind(plateThickness);
        podium.materialProperty().bind(plateMaterial);
        podium.translateZProperty().bind(pillar.heightProperty().add(plateThickness).negate());
        podium.setRotationAxis(Rotate.X_AXIS);
        podium.setRotate(90);

        getChildren().setAll(pillar, podium);
    }

    public Cylinder pillar() {
        return pillar;
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
    }
}

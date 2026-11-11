/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter;

import de.amr.basics.ui.assets.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class LivesCounterStand extends Group implements DisposableGraphicsObject {

    private final Cylinder pillar;

    public LivesCounterStand(
        PhongMaterial pillarMaterial,
        PhongMaterial plateMaterial,
        double plateRadius,
        double plateThickness
    ) {
        requireNonNull(pillarMaterial);
        requireNonNull(plateMaterial);

        pillar = new Cylinder(1, 0.1);
        pillar.setMaterial(pillarMaterial);
        pillar.translateZProperty().bind(pillar.heightProperty().multiply(-0.5));
        pillar.setRotationAxis(Rotate.X_AXIS);
        pillar.setRotate(90);

        final Cylinder podium = new Cylinder();
        podium.setRadius(plateRadius);
        podium.setHeight(plateThickness);
        podium.setMaterial(plateMaterial);
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

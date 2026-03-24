/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class MsPacManFemaleParts extends Group implements Disposable {

    private final Sphere bowLeft;
    private final Sphere bowRight;
    private final Sphere pearlLeft;
    private final Sphere pearlRight;
    private final Sphere boobLeft;
    private final Sphere boobRight;
    private final Sphere beautySpot;
    private PhongMaterial bowMaterial;
    private PhongMaterial pearlMaterial;
    private PhongMaterial beautySpotMaterial;
    private PhongMaterial silicone;

    public MsPacManFemaleParts(double pacSize, MsPacManComponentColors colors) {
        int divisions = 16; // 64 is default

        bowMaterial = coloredPhongMaterial(colors.hairBow());

        bowLeft = new Sphere(1.2, divisions);
        bowLeft.setMaterial(bowMaterial);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));

        bowRight = new Sphere(1.2, divisions);
        bowRight.setMaterial(bowMaterial);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));

        pearlMaterial = coloredPhongMaterial(colors.hairBowPearls());

        pearlLeft = new Sphere(0.4, divisions);
        pearlLeft.setMaterial(pearlMaterial);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));

        pearlRight = new Sphere(0.4, divisions);
        pearlRight.setMaterial(pearlMaterial);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));

        beautySpotMaterial = coloredPhongMaterial(Color.rgb(120, 120, 120));
        beautySpot = new Sphere(0.5, divisions);
        beautySpot.getTransforms().addAll(new Translate(-0.33 * pacSize, -0.4 * pacSize, -0.14 * pacSize));
        beautySpot.setMaterial(beautySpotMaterial);

        silicone = coloredPhongMaterial(colors.boobs());

        double bx = -0.2 * pacSize; // forward
        double by = 1.6; // or - 1.6 // sidewards
        double bz = 0.4 * pacSize; // up/down

        boobLeft = new Sphere(1.8, divisions);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        boobRight = new Sphere(1.8, divisions);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        getChildren().addAll(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

    @Override
    public void dispose() {
        getChildren().clear();
        bowLeft.setMaterial(null);
        bowRight.setMaterial(null);
        pearlLeft.setMaterial(null);
        pearlRight.setMaterial(null);
        boobLeft.setMaterial(null);
        boobRight.setMaterial(null);
        beautySpot.setMaterial(null);
        bowMaterial = null;
        pearlMaterial = null;
        beautySpotMaterial = null;
        silicone = null;
    }
}

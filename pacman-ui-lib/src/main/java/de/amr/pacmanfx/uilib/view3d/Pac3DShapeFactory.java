/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.view3d;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class Pac3DShapeFactory {

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @param withEyes if Pac has eyes
     * @return a new Pac body group
     */
    public Group createPacBody(PacSettings config, boolean withEyes) {
        requireNonNull(config);

        final MeshView head = new MeshView(PacManMeshes3D.instance().pacHeadMesh());
        head.setMaterial(coloredPhongMaterial(config.colors().headColor()));

        final MeshView eyes = new MeshView(PacManMeshes3D.instance().pacEyesMesh());
        eyes.setMaterial(coloredPhongMaterial(config.colors().eyesColor()));

        final MeshView palate = new MeshView(PacManMeshes3D.instance().pacPalateMesh());
        palate.setMaterial(coloredPhongMaterial(config.colors().palateColor()));

        final List<Node> parts = withEyes ? List.of(head, eyes, palate) : List.of(head, palate);
        final Group body = new Group(parts);

        final Translate toOrigin = moveToOrigin(head);
        parts.forEach(node -> node.getTransforms().add(toOrigin));

        body.getTransforms().addAll(
            scaleTo(body, config.size3D()),
            PacManMeshes3D.ORIENTATION_ADJUSTMENT);

        return body;
    }

    public Group createFemalePacBodyParts(PacSettings config) {
        requireNonNull(config);

        final int sphereDivisions = 16; // 64 is default

        final PhongMaterial bowMaterial = coloredPhongMaterial(config.msColors().hairBow());

        final Sphere bowLeft = new Sphere(1.2, sphereDivisions);
        bowLeft.setMaterial(bowMaterial);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -config.size3D() * 0.55));

        final Sphere bowRight = new Sphere(1.2, sphereDivisions);
        bowRight.setMaterial(bowMaterial);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -config.size3D() * 0.55));

        final PhongMaterial pearlMaterial = coloredPhongMaterial(config.msColors().hairBowPearls());

        final Sphere pearlLeft = new Sphere(0.4, sphereDivisions);
        pearlLeft.setMaterial(pearlMaterial);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -config.size3D() * 0.58));

        final Sphere pearlRight = new Sphere(0.4, sphereDivisions);
        pearlRight.setMaterial(pearlMaterial);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -config.size3D() * 0.58));

        final PhongMaterial beautySpotMaterial = coloredPhongMaterial(Color.rgb(120, 120, 120));
        final Sphere beautySpot = new Sphere(0.5, sphereDivisions);
        beautySpot.getTransforms().addAll(new Translate(-0.33 * config.size3D(), -0.4 * config.size3D(), -0.14 * config.size3D()));
        beautySpot.setMaterial(beautySpotMaterial);

        final PhongMaterial silicone = coloredPhongMaterial(config.msColors().boobs());

        final double bx = -0.2 * config.size3D(); // forward
        final double by = 1.6; // or - 1.6 // sidewards
        final double bz = 0.4 * config.size3D(); // up/down

        final Sphere boobLeft = new Sphere(1.8, sphereDivisions);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        final Sphere boobRight = new Sphere(1.8, sphereDivisions);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

    private Translate moveToOrigin(Node node) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
    }

    private Scale scaleTo(Node node, float size) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth());
    }
}

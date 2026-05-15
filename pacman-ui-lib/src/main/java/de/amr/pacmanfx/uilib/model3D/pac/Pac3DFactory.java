/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.HeadBangingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.HipSwayingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacChewingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacManDyingAnimation3D;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class Pac3DFactory {

    public static Pac3D createPacMan3D(AnimationRegistry animations, Pac pac, PacConfig config) {
        requireNonNull(animations);
        requireNonNull(pac);
        requireNonNull(config);

        final Pac3D pac3D = new Pac3D(animations, pac, createPacBody(config), createBlindPacBody(config));
        animations.register(Pac3D.AnimationID.CHEWING, new PacChewingAnimation3D(pac3D));
        animations.register(Pac3D.AnimationID.DYING,   new PacManDyingAnimation3D(pac3D));
        animations.register(Pac3D.AnimationID.MOVING,  new HeadBangingAnimation3D(pac3D));

        addPowerLight(pac3D, config.colors().headColor().desaturate());
        pac3D.setPowerMode(false);

        return pac3D;
    }

    public static Pac3D createMsPacMan3D(AnimationRegistry animations, Pac msPacMan, PacConfig config) {
        final Pac3D msPacMan3D = new Pac3D(animations, msPacMan, createPacBody(config), createBlindPacBody(config));
        msPacMan3D.bodyGroup().getChildren().add(createFemalePacBodyParts(config));

        animations.register(Pac3D.AnimationID.CHEWING, new PacChewingAnimation3D(msPacMan3D));
        animations.register(Pac3D.AnimationID.DYING, new MsPacMan3DDyingAnimation(msPacMan3D));
        animations.register(Pac3D.AnimationID.MOVING, new HipSwayingAnimation3D(msPacMan3D));

        addPowerLight(msPacMan3D, config.colors().headColor().desaturate());
        msPacMan3D.setPowerMode(false);

        return msPacMan3D;
    }

    private static void addPowerLight(Pac3D pac3D, Color color) {
        final var powerLight = new PointLight();
        powerLight.setColor(color);
        powerLight.translateXProperty().bind(pac3D.translateXProperty());
        powerLight.translateYProperty().bind(pac3D.translateYProperty());
        powerLight.setTranslateZ(-30);
        pac3D.setPowerLight(powerLight);
    }

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @return a new Pac body group
     */
    public static Group createPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config);
        final MeshView eyes = createPacEyes(config);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, eyes, palate);
        final Translate toOrigin = moveToOrigin(head);
        List.of(head, eyes, palate).forEach(node -> node.getTransforms().add(toOrigin));
        body.getTransforms().addAll(
            scaleTo(body, config.size3D()),
            PacManWorld3D.ORIENTATION_ADJUSTMENT);
        return body;
    }

    /**
     * Creates a Pac-Man body without eyes (used for jaw open/close animation).
     *
     * @param config the Pac configuration
     * @return a Pac body without eyes
     */
    public static Group createBlindPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, palate);
        final Translate toOrigin = moveToOrigin(head);
        List.of(head, palate).forEach(node -> node.getTransforms().add(toOrigin));
        body.getTransforms().addAll(
            scaleTo(body, config.size3D()),
            PacManWorld3D.ORIENTATION_ADJUSTMENT);
        return body;
    }

    public static MeshView createPacHead(PacConfig config) {
        final PhongMaterial boringMaterial = coloredPhongMaterial(config.colors().headColor());
        final MeshView head = new MeshView(PacManWorld3D.instance().pacHeadMesh());
        head.setMaterial(boringMaterial);
        return head;
    }

    public static MeshView createPacPalate(PacConfig config) {
        final MeshView palate = new MeshView(PacManWorld3D.instance().pacPalateMesh());
        palate.setMaterial(coloredPhongMaterial(config.colors().palateColor()));
        return palate;
    }

    public static MeshView createPacEyes(PacConfig config) {
        final MeshView eyes = new MeshView(PacManWorld3D.instance().pacEyesMesh());
        eyes.setMaterial(coloredPhongMaterial(config.colors().eyesColor()));
        return eyes;
    }

    /**
     * Creates the additional female parts used for Ms. Pac-Man (hair bow, pearls, etc.).
     *
     * @param config Pac configuration
     * @return a new female parts group
     */
    public static Group createFemalePacBodyParts(PacConfig config) {
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

    /**
     * Creates a complete Ms. Pac-Man body consisting of a Pac-Man base body
     * plus the additional female parts.
     *
     * @param config Pac configuration
     * @return a new Ms Pac-Man body instance
     */
    public static Group createMsPacManBody(PacConfig config) {
        return new Group(createPacBody(config), createFemalePacBodyParts(config));
    }

    private static Translate moveToOrigin(Node node) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
    }

    private static Scale scaleTo(Node node, float size) {
        requireNonNull(node);
        final Bounds b = node.getBoundsInLocal();
        return new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth());
    }
}

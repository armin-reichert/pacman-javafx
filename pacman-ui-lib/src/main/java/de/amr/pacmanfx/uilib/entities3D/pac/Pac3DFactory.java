/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.PacMan3DModel;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.*;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DTransformComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
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

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class Pac3DFactory {

    public static void createPacManView3D(AnimationRegistry animationRegistry, Pac pac, PacSettings config) {
        ensurePacHas3DView(pac, animationRegistry);

        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        view3D.setBodyAndJaw(createPacBody(config, true), createPacBody(config, false));

        configurePowerLight(view3D, config.colors().headColor().desaturate());

        final Pac3DAnimationComp animation = pac.requireComponent(Pac3DAnimationComp.class);
        final var chewing = new PacChewingAnimation3D(view3D);
        final var hipSwaying = new HeadBangingAnimation3D(view3D);
        final var dying = new PacManDyingAnimation3D(view3D);

        animation.setChewing(chewing);
        animation.setMovement(hipSwaying);
        animation.setDying(dying);

        animationRegistry.register(Pac3DAnimationID.CHEWING, chewing);
        animationRegistry.register(Pac3DAnimationID.MOVING,  hipSwaying);
        animationRegistry.register(Pac3DAnimationID.DYING,   dying);
    }

    public static void createMsPacManView3D(AnimationRegistry animationRegistry, Pac msPacMan, PacSettings config) {
        ensurePacHas3DView(msPacMan, animationRegistry);
        final Pac3DViewComp view3D = msPacMan.requireComponent(Pac3DViewComp.class);

        view3D.setBodyAndJaw(createPacBody(config, true), createPacBody(config, false));
        view3D.bodyGroup().getChildren().add(createFemalePacBodyParts(config));

        configurePowerLight(view3D, config.colors().headColor().desaturate());

        final Pac3DAnimationComp animation = msPacMan.requireComponent(Pac3DAnimationComp.class);
        final var chewing = new PacChewingAnimation3D(view3D);
        final var hipSwaying = new HipSwayingAnimation3D(view3D);
        final var dying = new MsPacManDyingAnimation3D(view3D);
        animation.setChewing(chewing);
        animation.setMovement(hipSwaying);
        animation.setDying(dying);

        animationRegistry.register(Pac3DAnimationID.CHEWING, chewing);
        animationRegistry.register(Pac3DAnimationID.MOVING,  hipSwaying);
        animationRegistry.register(Pac3DAnimationID.DYING,   dying);
    }

    private static void ensurePacHas3DView(Pac pac, AnimationRegistry animationRegistry) {
        if (!pac.hasComponent(Pac3DViewComp.class)) {
            pac.setComponent(Pac3DViewComp.class, new Pac3DViewComp());
            pac.setComponent(Pac3DTransformComp.class, new Pac3DTransformComp());
            pac.setComponent(Pac3DAnimationComp.class, new Pac3DAnimationComp(animationRegistry));
        }
    }

    private static void configurePowerLight(Pac3DViewComp view3D, Color color) {
        final PointLight powerLight = view3D.powerLight();
        powerLight.setColor(color);
        powerLight.translateXProperty().bind(view3D.root().translateXProperty());
        powerLight.translateYProperty().bind(view3D.root().translateYProperty());
        powerLight.setTranslateZ(-30);
    }

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @param withEyes if Pac has eyes
     * @return a new Pac body group
     */
    public static Group createPacBody(PacSettings config, boolean withEyes) {
        requireNonNull(config);

        final MeshView head = new MeshView(PacMan3DModel.instance().pacHeadMesh());
        head.setMaterial(coloredPhongMaterial(config.colors().headColor()));

        final MeshView eyes = new MeshView(PacMan3DModel.instance().pacEyesMesh());
        eyes.setMaterial(coloredPhongMaterial(config.colors().eyesColor()));

        final MeshView palate = new MeshView(PacMan3DModel.instance().pacPalateMesh());
        palate.setMaterial(coloredPhongMaterial(config.colors().palateColor()));

        final List<Node> parts = withEyes ? List.of(head, eyes, palate) : List.of(head, palate);
        final Group body = new Group(parts);

        final Translate toOrigin = moveToOrigin(head);
        parts.forEach(node -> node.getTransforms().add(toOrigin));

        body.getTransforms().addAll(
            scaleTo(body, config.size3D()),
            PacMan3DModel.ORIENTATION_ADJUSTMENT);

        return body;
    }

    public static Group createFemalePacBodyParts(PacSettings config) {
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

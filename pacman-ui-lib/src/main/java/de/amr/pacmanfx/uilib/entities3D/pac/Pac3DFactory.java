/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.PacMan3DModel;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.HeadBangingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.HipSwayingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.PacChewingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.PacManDyingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.MsPacManDyingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.pac.anim.Pac3DAnimationID;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.pac.system.Pac3DSupportSystem;
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

    public static void createPacManView3D(AnimationRegistry animationRegistry, GameEntity pac, PacSettings config) {
        Pac3DSupportSystem.makePac3D(pac, animationRegistry, createPacBody(config, true), createPacBody(config, false));

        final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        animationRegistry.register(Pac3DAnimationID.CHEWING, new PacChewingAnimation3D(view3D));
        animationRegistry.register(Pac3DAnimationID.DYING,   new PacManDyingAnimation3D(view3D));
        animationRegistry.register(Pac3DAnimationID.MOVING,  new HeadBangingAnimation3D(view3D.root()));
    }

    public static void createMsPacManView3D(AnimationRegistry animationRegistry, GameEntity msPacMan, PacSettings config) {
        Pac3DSupportSystem.makePac3D(msPacMan, animationRegistry, createPacBody(config, true), createPacBody(config, false));

        final Pac3DViewComp view3D = msPacMan.requireComponent(Pac3DViewComp.class);
        view3D.bodyGroup().getChildren().add(createFemalePacBodyParts(config));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        animationRegistry.register(Pac3DAnimationID.CHEWING, new PacChewingAnimation3D(view3D));
        animationRegistry.register(Pac3DAnimationID.DYING,   new MsPacManDyingAnimation3D(view3D));
        animationRegistry.register(Pac3DAnimationID.MOVING,  new HipSwayingAnimation3D(view3D.root()));
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

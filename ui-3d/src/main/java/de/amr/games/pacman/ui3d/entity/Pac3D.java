/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.scene.GameContext;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;
import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 *
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 *
 * @author Armin Reichert
 */
public class Pac3D extends Group {

    private static final String MESH_ID_EYES   = "PacMan.Eyes";
    private static final String MESH_ID_HEAD   = "PacMan.Head";
    private static final String MESH_ID_PALATE = "PacMan.Palate";

    public static Group createPacShape(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
        var head = new MeshView(model3D.mesh(MESH_ID_HEAD));
        head.setId(Model3D.toCSS_ID(MESH_ID_HEAD));
        head.setMaterial(coloredMaterial(headColor));

        var eyes = new MeshView(model3D.mesh(MESH_ID_EYES));
        eyes.setId(Model3D.toCSS_ID(MESH_ID_EYES));
        eyes.setMaterial(coloredMaterial(eyesColor));

        var palate = new MeshView(model3D.mesh(MESH_ID_PALATE));
        palate.setId(Model3D.toCSS_ID(MESH_ID_PALATE));
        palate.setMaterial(coloredMaterial(palateColor));

        var centeredOverOrigin = Model3D.centeredOverOrigin(head);
        Stream.of(head, eyes, palate).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        var root = new Group(head, eyes, palate);
        root.getTransforms().add(Model3D.scaled(root, size));

        // TODO check/fix Pac-Man mesh position and rotation in .obj file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        return root;
    }

    public static Group createFemaleParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        var bowMaterial = coloredMaterial(hairBowColor);

        var bowLeft = new Sphere(1.2);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
        bowLeft.setMaterial(bowMaterial);

        var bowRight = new Sphere(1.2);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
        bowRight.setMaterial(bowMaterial);

        var pearlMaterial = coloredMaterial(hairBowPearlsColor);

        var pearlLeft = new Sphere(0.4);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
        pearlLeft.setMaterial(pearlMaterial);

        var pearlRight = new Sphere(0.4);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
        pearlRight.setMaterial(pearlMaterial);

        var beautySpot = new Sphere(0.5);
        beautySpot.setMaterial(coloredMaterial(Color.rgb(100, 100, 100)));
        beautySpot.getTransforms().addAll(new Translate(-2.5, -4.5, 4.5));

        var silicone = coloredMaterial(boobsColor);

        double bx = -0.2 * pacSize; // forward
        double by = 1.6; // or - 1.6 // sidewards
        double bz = 0.4 * pacSize; // up/down
        var boobLeft = new Sphere(1.8);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        var boobRight = new Sphere(1.8);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

    private final Translate position = new Translate();
    private final Rotate orientation = new Rotate();
    private final Pac pac;
    private final Group shapeGroup;
    private Walking walking;
    private PointLight light;
    private final double size;

    public Pac3D(double size, Pac pac, Group shapeGroup) {
        this.size = size;
        this.pac = pac;
        this.shapeGroup = shapeGroup;
        shapeGroup.getTransforms().setAll(position, orientation);
        getChildren().add(shapeGroup);
        meshViews().forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));
    }
    public Stream<MeshView> meshViews() {
        return Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE).map(id -> meshView(shapeGroup, id));
    }

    public Translate position() {
        return position;
    }

    public void init(GameContext context) {
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        update(context);
    }

    public void update(GameContext context) {
        Vector2f center = pac.center();
        position.setX(center.x());
        position.setY(center.y());
        position.setZ(-0.5 * size);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pac.moveDir()));
        setVisible(pac.isVisible() && !outsideWorld(pac.world()));
        if (pac.isStandingStill()) {
            walking.stop();
        } else {
            walking.walk();
        }
        updateLight(context);
    }

    public void setLight(PointLight light) {
        checkNotNull(light);
        this.light = light;
        light.setMaxRange(2 * TS);
        light.translateXProperty().bind(position.xProperty());
        light.translateYProperty().bind(position.yProperty());
        light.setTranslateZ(-10);
    }

    public PointLight light() {
        return light;
    }

    private void updateLight(GameContext context) {
        if (light == null) {
            return;
        }
        var game = context.game();
        double radius = 0;
        if (game.powerTimer().duration() > 0) {
            double t = (double) game.powerTimer().remaining() / game.powerTimer().duration();
            radius = t * 6 * TS;
        }
        boolean hasPower = game.powerTimer().isRunning();
        light.setMaxRange(hasPower ? 2 * TS + radius : 0);
        light.setLightOn(lightedPy.get() && pac.isVisible() && hasPower);
    }

    private boolean outsideWorld(World world) {
        return position.getX() < HTS || position.getX() > TS * world.numCols() - HTS;
    }

    public Animation createMsPacManDyingAnimation() {
        var spin = new RotateTransition(Duration.seconds(0.5), this);
        spin.setAxis(Rotate.X_AXIS); //TODO check this
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setRate(2);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(
            spin,
            pauseSec(2)
        );
    }

    public Animation createPacManDyingAnimation(GameContext context) {
        Duration duration = Duration.seconds(1.0);
        short numSpins = 6;

        var spinning = new RotateTransition(duration.divide(numSpins), this);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        var shrinking = new ScaleTransition(duration, this);
        shrinking.setToX(0.5);
        shrinking.setToY(0.5);
        shrinking.setToZ(0.0);
        shrinking.setInterpolator(Interpolator.LINEAR);

        var falling = new TranslateTransition(duration, this);
        falling.setToZ(4);
        falling.setInterpolator(Interpolator.EASE_IN);

        //TODO does not yet work as I want to
        return new SequentialTransition(
            now(() -> init(context)),
            pauseSec(0.5),
            new ParallelTransition(spinning, shrinking, falling),
            doAfterSec(1.0, () -> {
                setVisible(false);
                setTranslateZ(0);
            })
        );
    }


    // Walking animations

    public void setFemaleWalking(boolean female) {
        if (female) {
            walking = new HipSwaying();
            walking.setPower(false);
        } else {
            walking = new HeadBanging();
            walking.setPower(false);
        }
    }

    public void setPowerWalking(boolean power) {
        walking.setPower(power);
    }

    private interface Walking {

        void walk();

        void stop();

        void setPower(boolean power);
    }

    private class HeadBanging implements Walking {

        private static final short DEFAULT_ANGLE_FROM = -25;
        private static final short DEFAULT_ANGLE_TO = 15;
        private static final Duration DEFAULT_DURATION = Duration.seconds(0.25);

        private final RotateTransition animation;

        public HeadBanging() {
            animation = new RotateTransition(DEFAULT_DURATION, Pac3D.this);
            animation.setAxis(Rotate.X_AXIS);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setAutoReverse(true);
            animation.setInterpolator(Interpolator.EASE_BOTH);
        }

        @Override
        public void setPower(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            animation.stop();
            animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
            animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
            animation.setRate(rate);
        }

        @Override
        public void walk() {
            if (pac.isStandingStill()) {
                stop();
                animation.getNode().setRotate(0);
                return;
            }
            var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(animation.getAxis())) {
                animation.stop();
                animation.setAxis(axis);
            }
            animation.play();
        }

        @Override
        public void stop() {
            animation.stop();
            animation.getNode().setRotationAxis(animation.getAxis());
            animation.getNode().setRotate(0);
        }
    }

    private class HipSwaying implements Walking {

        private static final short DEFAULT_ANGLE_FROM = -20;
        private static final short DEFAULT_ANGLE_TO = 20;
        private static final Duration DEFAULT_DURATION = Duration.seconds(0.4);

        private final RotateTransition animation;

        public HipSwaying() {
            animation = new RotateTransition(DEFAULT_DURATION, Pac3D.this);
            animation.setAxis(Rotate.Z_AXIS);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setAutoReverse(true);
            animation.setInterpolator(Interpolator.EASE_BOTH);
        }

        @Override
        public void setPower(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            animation.stop();
            animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
            animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
            animation.setRate(rate);
        }

        @Override
        public void walk() {
            if (pac.isStandingStill()) {
                stop();
                animation.getNode().setRotate(0);
                return;
            }
            animation.play();
        }

        @Override
        public void stop() {
            animation.stop();
            animation.getNode().setRotationAxis(animation.getAxis());
            animation.getNode().setRotate(0);
        }
    }
}
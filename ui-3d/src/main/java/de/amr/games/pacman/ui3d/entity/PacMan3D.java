/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

/**
 * @author Armin Reichert
 */
public class PacMan3D extends Pac3D {

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance, may be NULL
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        super(pacMan);
        zPosGround = -0.5 * size;

        var body = Pac3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );

        var shapeGroup = new Group(body);
        shapeGroup.getTransforms().setAll(position, orientation);
        getChildren().add(shapeGroup);

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
                .map(id -> meshView(shapeGroup, id))
                .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));

        if (pacMan != null) {
            walking = new HeadBanging();
            walking.setPower(false);
            light.setColor(theme.color("pacman.color.head").desaturate());
        }
    }

    private class HeadBanging implements Walking {

        private static final short DEFAULT_ANGLE_FROM = -25;
        private static final short DEFAULT_ANGLE_TO = 15;
        private static final Duration DEFAULT_DURATION = Duration.seconds(0.25);

        private final RotateTransition animation;

        public HeadBanging() {
            animation = new RotateTransition(DEFAULT_DURATION, PacMan3D.this);
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
}

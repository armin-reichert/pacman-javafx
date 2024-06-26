/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

/**
 * @author Armin Reichert
 */
public class PacMan3D extends Pac3D {

    private HeadBanging walkingAnimation;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance, may be NULL if used for lives counter
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        if (pacMan != null) {
            pac = pacMan;
            zStandingOnGround = -0.5 * size;
            walkingAnimation = new HeadBanging();
            walkingAnimation.setPower(false);
            light.setColor(theme.color("pacman.color.head").desaturate());
        }

        var body = createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
            .map(id -> meshView(body, id))
            .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));

        var shapeGroup = new Group(body);
        shapeGroup.getTransforms().setAll(position, orientation);
        getChildren().add(shapeGroup);
    }

    @Override
    public void startWalkingAnimation() {
        walkingAnimation.play();
    }

    @Override
    public void stopWalkingAnimation() {
        walkingAnimation.stop();
    }

    @Override
    public void setPower(boolean power) {
        walkingAnimation.setPower(power);
    }

    @Override
    public Animation createDyingAnimation(GameContext context) {
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

    private class HeadBanging {

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

        public void setPower(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            animation.stop();
            animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
            animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
            animation.setRate(rate);
        }

        public void play() {
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

        public void stop() {
            animation.stop();
            animation.getNode().setRotationAxis(animation.getAxis());
            animation.getNode().setRotate(0);
        }
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class PacMan3D extends AbstractPac3D {

    private static class HeadBanging {

        private static final float POWER_AMPLIFICATION = 2;
        private static final short ANGLE_FROM = -15;
        private static final short ANGLE_TO = 20;
        private static final Duration DURATION = Duration.seconds(0.3);

        private final RotateTransition banging;

        public HeadBanging(Node target) {
            banging = new RotateTransition(DURATION, target);
            banging.setAxis(Rotate.X_AXIS);
            banging.setCycleCount(Animation.INDEFINITE);
            banging.setAutoReverse(true);
            banging.setInterpolator(Interpolator.EASE_BOTH);
        }

        // Note: Massive headbanging can lead to a stroke!
        public void setStrokeMode(boolean power) {
            banging.stop();
            float rate = power ? POWER_AMPLIFICATION : 1;
            banging.setFromAngle(ANGLE_FROM * rate);
            banging.setToAngle(ANGLE_TO * rate);
            banging.setRate(rate);
        }

        public void update(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
            } else {
                Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
                if (!axis.equals(banging.getAxis())) {
                    banging.stop();
                    banging.setAxis(axis);
                }
                banging.play();
            }
        }

        public void stop() {
            banging.stop();
            banging.getNode().setRotationAxis(banging.getAxis());
            banging.getNode().setRotate(0);
        }
    }

    private final Node jaw;
    private final Group bodyGroup = new Group();
    private final HeadBanging headBanging;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param context game context
     * @param pacMan Pac-Man instance
     * @param size diameter of Pac-Man
     * @param model3D 3D model for Pac-Man
     */
    public PacMan3D(GameContext context, Pac pacMan, double size, Model3D model3D) {
        super(context, pacMan, size, model3D);

        Group body = PacModel3D.createPacShape(
            model3D, size,
            context.assets().color("pacman.color.head"),
            context.assets().color("pacman.color.eyes"),
            context.assets().color("pacman.color.palate")
        );

        jaw = PacModel3D.createPacSkull(
            model3D, size,
            context.assets().color("pacman.color.head"),
            context.assets().color("pacman.color.palate"));

        bodyGroup.getChildren().addAll(body, jaw);
        bodyGroup.getTransforms().add(rotation);

        createChewingAnimation(jaw);

        headBanging = new HeadBanging(bodyGroup);
        headBanging.setStrokeMode(false);

        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
        meshViewById(jaw,  PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw,  PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
    }

    @Override
    public Group root() {
        return bodyGroup;
    }

    @Override
    public void init() {
        super.init();
        headBanging.stop();
        headBanging.setStrokeMode(false);
        stopChewingAnimation();
    }

    @Override
    public void stopChewingAnimation() {
        chewingAnimation.stop();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }

    @Override
    public void stopWalkingAnimation() {
       headBanging.stop();
    }

    @Override
    public void updateAliveAnimation() {
        if (pac.isStandingStill()) {
            headBanging.stop();
            stopChewingAnimation();
        } else {
            headBanging.update(pac);
            chewingAnimation.play();
        }
    }

    @Override
    public void setPower(boolean power) {
        headBanging.setStrokeMode(power);
    }

    @Override
    public Animation createDyingAnimation() {
        Duration duration = Duration.seconds(1.0);
        byte numSpins = 6;

        var spins = new RotateTransition(duration.divide(numSpins), bodyGroup);
        spins.setAxis(Rotate.Z_AXIS);
        spins.setByAngle(360);
        spins.setCycleCount(numSpins);
        spins.setInterpolator(Interpolator.LINEAR);

        var shrinks = new ScaleTransition(duration.multiply(0.66), bodyGroup);
        shrinks.setToX(0.25);
        shrinks.setToY(0.25);
        shrinks.setToZ(0.02);

        var expands = new ScaleTransition(duration.multiply(0.34), bodyGroup);
        expands.setToX(0.75);
        expands.setToY(0.75);

        var sinks = new TranslateTransition(duration, bodyGroup);
        sinks.setToZ(0);

        return new SequentialTransition(
            now(this::init), // TODO check this
            pauseSec(0.5),
            new ParallelTransition(spins, new SequentialTransition(shrinks, expands), sinks),
            doAfterSec(1.0, () -> bodyGroup.setVisible(false))
        );
    }
}
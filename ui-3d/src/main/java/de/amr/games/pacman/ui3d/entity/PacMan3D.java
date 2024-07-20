/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

/**
 * @author Armin Reichert
 */
public class PacMan3D extends AbstractPac3D {

    private static class HeadBanging {

        private static final short ANGLE_FROM = -25;
        private static final short ANGLE_TO = 15;
        private static final Duration DURATION = Duration.seconds(0.25);

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
            double amplification = power ? 1.5 : 1;
            banging.stop();
            banging.setFromAngle(ANGLE_FROM * amplification);
            banging.setToAngle(ANGLE_TO * amplification);
            banging.setRate(amplification);
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

    private final Group bodyGroup;
    private final HeadBanging headBanging;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param pacMan Pac-Man instance
     * @param size diameter of Pac-Man
     * @param theme the theme
     */
    public PacMan3D(Pac pacMan,double size, Theme theme) {
        super(pacMan, size, theme);

        Group body = PacModel3D.createPacShape(
            model3D, size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );

        bodyGroup = new Group(body);
        bodyGroup.getTransforms().add(rotation);

        headBanging = new HeadBanging(bodyGroup);
        headBanging.setStrokeMode(false);

        jawRotation.setNode(jaw);
        bodyGroup.getChildren().add(jaw);

        Stream.of(PacModel3D.MESH_ID_EYES, PacModel3D.MESH_ID_HEAD, PacModel3D.MESH_ID_PALATE)
            .map(id -> meshView(bodyGroup, id))
            .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));
    }

    @Override
    public Node node() {
        return bodyGroup;
    }

    @Override
    public void init(GameContext context) {
        super.init(context);
        headBanging.stop();
        stopChewing();
    }

    @Override
    protected void updateAliveAnimation() {
        if (pac.isStandingStill()) {
            headBanging.stop();
            stopChewing();
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
    public Animation createDyingAnimation(GameContext context) {
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
            now(() -> init(context)), // TODO check this
            pauseSec(0.5),
            new ParallelTransition(spins, new SequentialTransition(shrinks, expands), sinks),
            doAfterSec(1.0, () -> bodyGroup.setVisible(false))
        );
    }
}
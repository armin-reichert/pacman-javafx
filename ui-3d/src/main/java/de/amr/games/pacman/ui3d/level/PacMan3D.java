/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetMap;
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

    private static class HeadBangingAnimation {

        private static final float POWER_AMPLIFICATION = 2;
        private static final short ANGLE_FROM = -15;
        private static final short ANGLE_TO = 20;
        private static final Duration DURATION = Duration.seconds(0.3);

        private final RotateTransition banging;

        public HeadBangingAnimation(Node target) {
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
    private final HeadBangingAnimation headBangingAnimation;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param pacMan Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset map
     */
    public PacMan3D(Pac pacMan, double size, AssetMap assets) {
        super(pacMan);

        Model3D model3D = assets.get("model3D.pacman");

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.eyes"),
            assets.color("pacman.color.palate")
        );

        jaw = PacModel3D.createPacSkull(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.palate"));

        bodyGroup.getChildren().addAll(body, jaw);
        bodyGroup.getTransforms().add(moveRotation);

        createChewingAnimation(jaw);

        headBangingAnimation = new HeadBangingAnimation(bodyGroup);
        headBangingAnimation.setStrokeMode(false);

        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
        meshViewById(jaw,  PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw,  PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        bodyGroup.setTranslateZ(-0.5 * size);
        light.setTranslateZ(-1.5 * size);
    }

    @Override
    public Group root() {
        return bodyGroup;
    }

    @Override
    public void init() {
        bodyGroup.setScaleX(1.0);
        bodyGroup.setScaleY(1.0);
        bodyGroup.setScaleZ(1.0);
        updatePosition(bodyGroup);
        updateMoveRotation();
        headBangingAnimation.stop();
        headBangingAnimation.setStrokeMode(false);
        stopChewingAnimation(jaw);
    }

    @Override
    public void update(GameContext context) {
        if (pac.isAlive()) {
            updatePosition(root());
            updateMoveRotation();
            updateVisibility(context.game());
            updateLight(context.game());
        }
        if (pac.isAlive() && !pac.isStandingStill()) {
            headBangingAnimation.update(pac);
            playChewingAnimation();
        } else {
            headBangingAnimation.stop();
            stopChewingAnimation(jaw);
        }
    }


    @Override
    public void setPower(boolean power) {
        headBangingAnimation.setStrokeMode(power);
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
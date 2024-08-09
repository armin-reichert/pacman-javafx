/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.ui2d.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D extends AbstractPac3D {

    private static class HipSwayingAnimation {

        private static final short ANGLE_FROM = -20;
        private static final short ANGLE_TO = 20;
        private static final Duration DURATION = Duration.seconds(0.4);

        private final RotateTransition swaying;

        public HipSwayingAnimation(Node target) {
            swaying = new RotateTransition(DURATION, target);
            swaying.setAxis(Rotate.Z_AXIS);
            swaying.setCycleCount(Animation.INDEFINITE);
            swaying.setAutoReverse(true);
            swaying.setInterpolator(Interpolator.EASE_BOTH);
        }

        // Winnetouch is the gay twin-brother of Abahachi
        public void setWinnetouchMode(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            swaying.stop();
            swaying.setFromAngle(ANGLE_FROM * amplification);
            swaying.setToAngle(ANGLE_TO * amplification);
            swaying.setRate(rate);
        }

        public void update(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
            } else {
                swaying.play();
            }
        }

        public void stop() {
            swaying.stop();
            swaying.getNode().setRotationAxis(swaying.getAxis());
            swaying.getNode().setRotate(0);
        }
    }

    private final Group bodyGroup = new Group();
    private final Node jaw;
    private final HipSwayingAnimation hipSwayingAnimation;

    /**
     * Creates a 3D Ms. Pac-Man.
     * @param msPacMan Ms. Pac-Man instance
     * @param size diameter of Pac-Man
     * @param model3D 3D model
     * @param assets asset map
     */
    public MsPacMan3D(Pac msPacMan, double size, Model3D model3D, AssetMap assets) {
        super(msPacMan, size, model3D);

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color("ms_pacman.color.head"),
            assets.color("ms_pacman.color.eyes"),
            assets.color("ms_pacman.color.palate"));

        Group femaleParts = PacModel3D.createFemaleParts(size,
            assets.color("ms_pacman.color.hairbow"),
            assets.color("ms_pacman.color.hairbow.pearls"),
            assets.color("ms_pacman.color.boobs"));

        jaw = PacModel3D.createPacSkull(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.palate"));

        bodyGroup.getChildren().addAll(body, femaleParts, jaw);
        bodyGroup.getTransforms().add(moveRotation);

        createChewingAnimation(jaw);

        hipSwayingAnimation = new HipSwayingAnimation(bodyGroup);
        hipSwayingAnimation.setWinnetouchMode(false);

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
        updatePosition(bodyGroup);
        updateMoveRotation();
        hipSwayingAnimation.stop();
        hipSwayingAnimation.setWinnetouchMode(false);
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
            hipSwayingAnimation.update(pac);
            playChewingAnimation();
        } else {
            hipSwayingAnimation.stop();
            stopChewingAnimation(jaw);
        }
    }

    @Override
    public void setPower(boolean power) {
        hipSwayingAnimation.setWinnetouchMode(power);
    }

    @Override
    public Animation createDyingAnimation() {
        var spin = new RotateTransition(Duration.seconds(0.25), bodyGroup);
        spin.setAxis(Rotate.Z_AXIS);
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(spin, pauseSec(2));
    }
}
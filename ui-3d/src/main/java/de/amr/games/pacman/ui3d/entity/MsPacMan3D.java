/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui3d.entity.PacModel3D.*;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D extends Pac3D {

    private HipSwaying walkingAnimation;

    /**
     * Creates a 3D Ms. Pac-Man.
     * @param size diameter of Pac-Man
     * @param msPacMan Ms. Pac-Man instance, may be NULL if used for lives counter
     * @param theme the theme
     */
    public MsPacMan3D(double size, Pac msPacMan, Theme theme) {
        if (msPacMan != null) {
            pac = msPacMan;
            zStandingOnGround = -0.5 * size;
            walkingAnimation = new HipSwaying();
            walkingAnimation.setPower(false);
            light.setColor(theme.color("ms_pacman.color.head").desaturate());
        }

        Group body = PacModel3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("ms_pacman.color.head"),
            theme.color("ms_pacman.color.eyes"),
            theme.color("ms_pacman.color.palate"));

        Group femaleParts = PacModel3D.createFemaleParts(size,
            theme.color("ms_pacman.color.hairbow"),
            theme.color("ms_pacman.color.hairbow.pearls"),
            theme.color("ms_pacman.color.boobs"));

        var shapeGroup = new Group(body, femaleParts);
        shapeGroup.getTransforms().setAll(orientation);
        getChildren().add(shapeGroup);

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
            .map(id -> Model3D.meshView(body, id))
            .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));
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
        var spin = new RotateTransition(Duration.seconds(0.5), this);
        spin.setAxis(Rotate.X_AXIS); //TODO check this
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setRate(2);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(spin, pauseSec(2));
    }

    private class HipSwaying {

        private static final short DEFAULT_ANGLE_FROM = -20;
        private static final short DEFAULT_ANGLE_TO = 20;
        private static final Duration DEFAULT_DURATION = Duration.seconds(0.4);

        private final RotateTransition animation;

        public HipSwaying() {
            animation = new RotateTransition(DEFAULT_DURATION, MsPacMan3D.this);
            animation.setAxis(Rotate.Z_AXIS);
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
            animation.play();
        }

        public void stop() {
            animation.stop();
            animation.getNode().setRotationAxis(animation.getAxis());
            animation.getNode().setRotate(0);
        }
    }
}
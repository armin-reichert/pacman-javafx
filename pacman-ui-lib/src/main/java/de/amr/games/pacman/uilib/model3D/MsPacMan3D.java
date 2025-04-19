/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.model3D;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.assets.AssetStorage;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MsPacMan3D extends Pac3DBase {

    public MsPacMan3D(Pac pac, double size, Model3D model3D, AssetStorage assets, String ans) {
        super(pac, size, model3D, assets, ans);

        Group femaleBodyParts = PacModel3D.createFemaleBodyParts(size,
            assets.color(ans + ".pac.color.hairbow"),
            assets.color(ans + ".pac.color.hairbow.pearls"),
            assets.color(ans + ".pac.color.boobs"));

        root.getChildren().add(femaleBodyParts);
    }

    @Override
    public Animation createDyingAnimation() {
        var spinning = new RotateTransition(Duration.seconds(0.25), root);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setFromAngle(0);
        spinning.setToAngle(360);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.setCycleCount(4);
        return spinning;
    }

    // Movement animation: Hip swaying

    private static final short HIP_ANGLE_FROM = -20;
    private static final short HIP_ANGLE_TO = 20;
    private static final Duration SWING_TIME = Duration.seconds(0.4);

    @Override
    protected void createMovementAnimation() {
        movementAnimation = new RotateTransition(SWING_TIME, root);
        movementAnimation.setAxis(Rotate.Z_AXIS);
        movementAnimation.setCycleCount(Animation.INDEFINITE);
        movementAnimation.setAutoReverse(true);
        movementAnimation.setInterpolator(Interpolator.EASE_BOTH);
        setMovementPowerMode(false);
    }

    @Override
    protected void startMovementAnimation() {
        movementAnimation.play();
    }

    @Override
    protected void stopMovementAnimation() {
        movementAnimation.stop();
        movementAnimation.getNode().setRotationAxis(movementAnimation.getAxis());
        movementAnimation.getNode().setRotate(0);
    }

    @Override
    protected void updateMovementAnimation() {}

    @Override
    public void setMovementPowerMode(boolean on) {
        double amplification = on ? 1.5 : 1;
        double rate = on ? 2 : 1;
        movementAnimation.stop();
        movementAnimation.setFromAngle(HIP_ANGLE_FROM * amplification);
        movementAnimation.setToAngle(HIP_ANGLE_TO * amplification);
        movementAnimation.setRate(rate);
    }
}
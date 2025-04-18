/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.assets.AssetStorage;
import de.amr.games.pacman.uilib.model3D.Model3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Ufx.doAfterSec;
import static de.amr.games.pacman.uilib.Ufx.now;

public class PacMan3D extends Pac3D {

    private RotateTransition movementAnimation;

    public PacMan3D(Pac pac, double size, Model3D model3D, AssetStorage assets, String ans) {
        super(pac, size, model3D, assets, ans);
    }

    @Override
    public void init() {
        root.setVisible(pac.isVisible());
        root.setScaleX(1.0);
        root.setScaleY(1.0);
        root.setScaleZ(1.0);

        updatePosition();
        stopChewingAndOpenMouth();
        stopMoveAnimation();
        setPowerMode(false);
    }


    @Override
    public void setPowerMode(boolean on) {
        float rate = on ? POWER_AMPLIFICATION : 1;
        movementAnimation.stop();
        movementAnimation.setFromAngle(BANG_ANGLE_FROM * rate);
        movementAnimation.setToAngle(BANG_ANGLE_TO * rate);
        movementAnimation.setRate(rate);
    }

    @Override
    public Animation createDyingAnimation() {
        Duration duration = Duration.seconds(1.5);
        byte numSpins = 6;

        var spinning = new RotateTransition(duration.divide(numSpins), root);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        var shrinking = new ScaleTransition(duration.multiply(0.5), root);
        shrinking.setToX(0.25);
        shrinking.setToY(0.25);
        shrinking.setToZ(0.02);

        var expanding = new ScaleTransition(duration.multiply(0.5), root);
        expanding.setToX(0.75);
        expanding.setToY(0.75);

        var sinking = new TranslateTransition(duration, root);
        sinking.setToZ(0);

        //TODO convert to Timeline?
        return new SequentialTransition(
            now(this::init), // TODO check this
            doAfterSec(0.5, THE_SOUND::playPacDeathSound),
            new ParallelTransition(spinning, new SequentialTransition(shrinking, expanding), sinking),
            doAfterSec(1.0, () -> root.setVisible(false))
        );
    }

    // Movement animation: Head banging

    static final float POWER_AMPLIFICATION = 2;
    static final short BANG_ANGLE_FROM = -10;
    static final short BANG_ANGLE_TO = 15;
    static final Duration BANG_TIME = Duration.seconds(0.3);

    protected void createMoveAnimation() {
        movementAnimation = new RotateTransition(BANG_TIME, root);
        movementAnimation.setAxis(Rotate.X_AXIS);
        movementAnimation.setCycleCount(Animation.INDEFINITE);
        movementAnimation.setAutoReverse(true);
        movementAnimation.setInterpolator(Interpolator.EASE_BOTH);
        setPowerMode(false);
    }

    protected void updateMoveAnimation() {
        if (pac.isStandingStill()) {
            stopMoveAnimation();
        } else {
            Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(movementAnimation.getAxis())) {
                movementAnimation.stop();
                movementAnimation.setAxis(axis);
            }
            movementAnimation.play();
        }
    }

    @Override
    protected void startMoveAnimation() {
        movementAnimation.play();
    }

    @Override
    protected void stopMoveAnimation() {
        movementAnimation.stop();
        movementAnimation.getNode().setRotationAxis(movementAnimation.getAxis());
        movementAnimation.getNode().setRotate(0);
    }
}
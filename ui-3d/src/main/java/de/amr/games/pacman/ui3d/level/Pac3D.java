/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    Node root();

    void init();

    void update(GameContext context);

    Animation createDyingAnimation();

    void setPower(boolean power);

    Property<DrawMode> drawModeProperty();

    PointLight light();

    static Animation createChewingAnimation(Node jaw) {
        final int openAngle = 0, closedAngle = -54;
        var closeMouth = new RotateTransition(Duration.millis(30), jaw);
        closeMouth.setAxis(Rotate.Y_AXIS);
        closeMouth.setFromAngle(openAngle);
        closeMouth.setToAngle(closedAngle);
        closeMouth.setInterpolator(Interpolator.LINEAR);
        var openMouth = new RotateTransition(Duration.millis(90), jaw);
        openMouth.setAxis(Rotate.Y_AXIS);
        openMouth.setFromAngle(closedAngle);
        openMouth.setToAngle(openAngle);
        openMouth.setInterpolator(Interpolator.LINEAR);
        var chewingAnimation = new SequentialTransition(openMouth, Ufx.pauseSec(0.1), closeMouth);
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
        chewingAnimation.statusProperty().addListener((py, ov, nv) -> {
            if (nv == Animation.Status.STOPPED) {
                jaw.setRotationAxis(Rotate.Y_AXIS);
                jaw.setRotate(0);
                Logger.info("Chewing stopped");
            }
        });
        return chewingAnimation;
    }
}
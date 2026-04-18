/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.Bonus3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class Bonus3DEdibleAnimation extends ManagedAnimation {

    private final Bonus bonus;
    private final Bonus3D shape;

    public Bonus3DEdibleAnimation(Bonus bonus, Bonus3D bonus3D) {
        super("Bonus (Edible, Symbol)");
        this.bonus = bonus;
        this.shape = bonus3D;
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var animation = new RotateTransition(Duration.seconds(1), shape);
        animation.setAxis(Rotate.X_AXIS);
        animation.setFromAngle(0);
        animation.setToAngle(360);
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setCycleCount(Animation.INDEFINITE);
        return animation;
    }

    public void update(GameLevel level) {
        if (animationFX != null && animationFX.getStatus() == Animation.Status.RUNNING) {
            RotateTransition rotateTransition = (RotateTransition) animationFX;
            Vector2f center = bonus.center();
            boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
            shape.setVisible(bonus.state() == BonusState.EDIBLE && !outsideWorld);
            Direction moveDir = bonus.moveDir();
            Point3D axis = moveDir.isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            rotateTransition.setRate(moveDir == Direction.DOWN || moveDir == Direction.LEFT ? 1 : -1);
            if (!rotateTransition.getAxis().equals(axis)) {
                rotateTransition.stop();
                rotateTransition.setAxis(axis);
                rotateTransition.play(); // continue from stopped rotation value
            }
        }
    }
}

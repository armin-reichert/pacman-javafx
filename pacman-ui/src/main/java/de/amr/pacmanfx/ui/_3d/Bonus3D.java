/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.MovingBonus;
import de.amr.pacmanfx.model.actors.StaticBonus;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui._3d.Settings3D.BONUS_3D_POINTS_WIDTH;
import static de.amr.pacmanfx.ui._3d.Settings3D.BONUS_3D_SYMBOL_WIDTH;
import static java.util.Objects.requireNonNull;

/**
 * 3D bonus symbol.
 */
public class Bonus3D extends Box {

    private final AnimationManager animationMgr;
    private final Bonus bonus;
    private final ImageView symbolImageView;
    private final ImageView pointsImageView;
    private final RotateTransition eatenAnimation;
    private final RotateTransition edibleAnimation;

    public Bonus3D(AnimationManager animationMgr, Bonus bonus, Image symbolImage, Image pointsImage) {
        super(BONUS_3D_SYMBOL_WIDTH, TS, TS);

        this.animationMgr = requireNonNull(animationMgr);
        this.bonus = requireNonNull(bonus);

        symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(BONUS_3D_SYMBOL_WIDTH);

        pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(BONUS_3D_POINTS_WIDTH);

        edibleAnimation = new RotateTransition(Duration.seconds(1), this);
        edibleAnimation.setAxis(Rotate.Z_AXIS); // to trigger initial change
        edibleAnimation.setFromAngle(0);
        edibleAnimation.setToAngle(360);
        edibleAnimation.setInterpolator(Interpolator.LINEAR);
        edibleAnimation.setCycleCount(Animation.INDEFINITE);

        eatenAnimation = new RotateTransition(Duration.seconds(1), this);
        eatenAnimation.setAxis(Rotate.X_AXIS);
        eatenAnimation.setByAngle(360);
        eatenAnimation.setInterpolator(Interpolator.LINEAR);
        eatenAnimation.setRate(2);
        eatenAnimation.setCycleCount(2);
    }

    public void update() {
        Vector2f center = bonus.actor().center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-HTS);
        optGameLevel().ifPresent(level -> {
            boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
            boolean visible = !(bonus.state() == Bonus.STATE_INACTIVE || outsideWorld);
            setVisible(visible);
            if (edibleAnimation.getStatus() == Animation.Status.RUNNING && bonus instanceof MovingBonus movingBonus) {
                updateMovingBonusEdibleAnimation(movingBonus.actor().moveDir());
            }
        });
    }

    private void updateMovingBonusEdibleAnimation(Direction moveDir) {
        Point3D rotationAxis = moveDir.isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
        edibleAnimation.setRate(moveDir == Direction.DOWN || moveDir == Direction.LEFT ? 1 : -1);
        if (!edibleAnimation.getAxis().equals(rotationAxis)) {
            edibleAnimation.stop();
            edibleAnimation.setAxis(rotationAxis);
            animationMgr.registerAndPlayFromStart("Bonus_Edible_Animation", edibleAnimation);
        }
    }

    public void showEdible() {
        setVisible(true);
        setWidth(BONUS_3D_SYMBOL_WIDTH);
        setTexture(symbolImageView.getImage());
        if (bonus instanceof StaticBonus) {
            edibleAnimation.setAxis(Rotate.X_AXIS);
        }
        animationMgr.registerAndPlayFromStart("Bonus_Edible_Animation", edibleAnimation);
    }

    public void expire() {
        edibleAnimation.stop();
        setVisible(false);
    }

    public void showEaten() {
        setVisible(true);
        setWidth(BONUS_3D_POINTS_WIDTH);
        setTexture(pointsImageView.getImage());
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        edibleAnimation.stop();
        animationMgr.registerAndPlayFromStart("Bonus_Eaten_Animation", eatenAnimation);
    }

    private void setTexture(Image texture) {
        //TODO when using Color.TRANSPARENT, nothing is shown, why?
        setMaterial(new PhongMaterial(Color.GHOSTWHITE, texture, null, null, null));
    }
}
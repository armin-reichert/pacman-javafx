/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
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
 * 3D representation of a bonus symbol.
 *
 * <p>A static bonus is represented by a rotating cube located at the worlds' bonus position displaying the bonus symbol
 * on each of its faces. When eaten, the bonus symbol is replaced by the points earned for eating the bonus.
 * For a moving bonus, the rotating cube moves through the world and rotates towards its current move direction.</p>
 */
public class Bonus3D extends Box {

    private final Bonus bonus;
    private final PhongMaterial symbolImageTexture;
    private final PhongMaterial pointsImageTexture;

    private final AnimationManager animationMgr;
    private RotateTransition eatenAnimation;
    private RotateTransition edibleAnimation;

    public Bonus3D(AnimationManager animationMgr, Bonus bonus, Image symbolImage, Image pointsImage) {
        super(BONUS_3D_SYMBOL_WIDTH, TS, TS);

        this.animationMgr = requireNonNull(animationMgr);
        this.bonus = requireNonNull(bonus);

        var symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(BONUS_3D_SYMBOL_WIDTH);
        symbolImageTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        var pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(BONUS_3D_POINTS_WIDTH);
        pointsImageTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);
    }

    public void update() {
        Vector2f center = bonus.actor().center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-HTS);
        optGameLevel().ifPresent(this::updateEdibleAnimation);
    }

    public void showEdible() {
        setVisible(true);
        setWidth(BONUS_3D_SYMBOL_WIDTH);
        setMaterial(symbolImageTexture);
        playEdibleAnimation();
    }

    public void expire() {
        stopEdibleAnimation();
        stopEatenAnimation();
        setVisible(false);
    }

    public void showEaten() {
        stopEdibleAnimation();
        setVisible(true);
        setWidth(BONUS_3D_POINTS_WIDTH);
        setMaterial(pointsImageTexture);
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        playEatenAnimation();
    }

    private RotateTransition createEdibleAnimation() {
        var animation = new RotateTransition(Duration.seconds(1), this);
        animation.setAxis(Rotate.X_AXIS);
        animation.setFromAngle(0);
        animation.setToAngle(360);
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setCycleCount(Animation.INDEFINITE);
        return animation;
    }

    private void playEdibleAnimation() {
        if (edibleAnimation == null) {
            edibleAnimation = createEdibleAnimation();
        }
        animationMgr.registerAndPlayFromStart(this, "Bonus_Edible", edibleAnimation);
    }

    private void stopEdibleAnimation() {
        if (edibleAnimation != null) {
            edibleAnimation.stop();
        }
    }

    private void updateEdibleAnimation(GameLevel level) {
        if (edibleAnimation != null
            && edibleAnimation.getStatus() == Animation.Status.RUNNING
            && bonus instanceof MovingBonus movingBonus)
        {
            Vector2f center = bonus.actor().center();
            boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
            setVisible(bonus.state() == Bonus.STATE_EDIBLE && !outsideWorld);
            Direction moveDir = movingBonus.actor().moveDir();
            Point3D axis = moveDir.isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            edibleAnimation.setRate(moveDir == Direction.DOWN || moveDir == Direction.LEFT ? 1 : -1);
            if (!edibleAnimation.getAxis().equals(axis)) {
                edibleAnimation.stop();
                edibleAnimation.setAxis(axis);
                edibleAnimation.play(); // play from stopped position, not from start
            }
        }
    }

    private RotateTransition createEatenAnimation() {
        var animation = new RotateTransition(Duration.seconds(1), this);
        animation.setAxis(Rotate.X_AXIS);
        animation.setByAngle(360);
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setRate(2);
        animation.setCycleCount(2);
        return animation;
    }

    private void playEatenAnimation() {
        if (eatenAnimation == null) {
            eatenAnimation = createEatenAnimation();
        }
        animationMgr.registerAndPlayFromStart(this, "Bonus_Eaten", eatenAnimation);
    }

    private void stopEatenAnimation() {
        if (eatenAnimation != null) {
            eatenAnimation.stop();
        }
    }
}
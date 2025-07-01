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
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
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
    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

    private EdibleAnimation edibleAnimation;
    private ManagedAnimation eatenAnimation;

    private class EdibleAnimation extends ManagedAnimation {

        public EdibleAnimation(AnimationManager animationManager) {
            super(animationManager, "Bonus_Edible");
        }

        @Override
        protected Animation createAnimation() {
            var animation = new RotateTransition(Duration.seconds(1), Bonus3D.this);
            animation.setAxis(Rotate.X_AXIS);
            animation.setFromAngle(0);
            animation.setToAngle(360);
            animation.setInterpolator(Interpolator.LINEAR);
            animation.setCycleCount(Animation.INDEFINITE);
            return animation;
        }

        public void update(GameLevel level) {
            if (animation != null && animation.getStatus() == Animation.Status.RUNNING
                && bonus instanceof MovingBonus movingBonus)
            {
                RotateTransition rotateTransition = (RotateTransition) animation;
                Vector2f center = bonus.actor().center();
                boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
                setVisible(bonus.state() == Bonus.STATE_EDIBLE && !outsideWorld);
                Direction moveDir = movingBonus.actor().moveDir();
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

    public Bonus3D(AnimationManager animationManager, Bonus bonus, Image symbolImage, Image pointsImage) {
        super(BONUS_3D_SYMBOL_WIDTH, TS, TS);

        requireNonNull(animationManager);
        this.bonus = requireNonNull(bonus);

        var symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(BONUS_3D_SYMBOL_WIDTH);
        symbolTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        var pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(BONUS_3D_POINTS_WIDTH);
        pointsTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);

        edibleAnimation = new EdibleAnimation(animationManager);

        eatenAnimation = new ManagedAnimation(animationManager, "Bonus_Eaten") {
            @Override
            protected Animation createAnimation() {
                var animation = new RotateTransition(Duration.seconds(1), Bonus3D.this);
                animation.setAxis(Rotate.X_AXIS);
                animation.setByAngle(360);
                animation.setInterpolator(Interpolator.LINEAR);
                animation.setRate(2);
                animation.setCycleCount(2);
                return animation;
            }
        };
    }

    public void destroy() {
        symbolTexture = null;
        pointsTexture = null;
        edibleAnimation.stop();
        edibleAnimation = null;
        eatenAnimation.stop();
        eatenAnimation = null;
    }

    public void update() {
        Vector2f center = bonus.actor().center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-HTS);
        optGameLevel().ifPresent(edibleAnimation::update);
    }

    public void showEdible() {
        setVisible(true);
        setWidth(BONUS_3D_SYMBOL_WIDTH);
        setMaterial(symbolTexture);
        edibleAnimation.play(ManagedAnimation.FROM_START);
    }

    public void showEaten() {
        edibleAnimation.stop();
        setVisible(true);
        setWidth(BONUS_3D_POINTS_WIDTH);
        setMaterial(pointsTexture);
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        eatenAnimation.play(ManagedAnimation.FROM_START);
    }

    public void expire() {
        edibleAnimation.stop();
        eatenAnimation.stop();
        setVisible(false);
    }
}
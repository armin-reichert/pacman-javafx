/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a bonus symbol.
 *
 * <p>A static bonus is represented by a rotating cube located at the worlds' bonus position displaying the bonus symbol
 * on each of its faces. When eaten, the bonus symbol is replaced by the points earned for eating the bonus.
 * For a moving bonus, the rotating cube moves through the world and rotates towards its current move direction.</p>
 */
public class Bonus3D extends Box implements Disposable {

    private final Bonus bonus;

    private final double symbolWidth;
    private final double pointsWidth;
    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

    private EdibleAnimation edibleAnimation;
    private ManagedAnimation eatenAnimation;

    private class EdibleAnimation extends ManagedAnimation {

        public EdibleAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Bonus_Edible");
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
            if (animation != null && animation.getStatus() == Animation.Status.RUNNING) {
                RotateTransition rotateTransition = (RotateTransition) animation;
                Vector2f center = Bonus3D.this.bonus.center();
                boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
                setVisible(Bonus3D.this.bonus.state() == BonusState.EDIBLE && !outsideWorld);
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

    public Bonus3D(AnimationRegistry animationRegistry, Bonus bonus, Image symbolImage, double symbolWidth, Image pointsImage, double pointsWidth) {
        requireNonNull(animationRegistry);
        this.bonus = requireNonNull(bonus);
        this.symbolWidth = requireNonNegative(symbolWidth);
        this.pointsWidth = requireNonNegative(pointsWidth);

        setWidth(symbolWidth);
        setHeight(TS);
        setDepth(TS);

        var symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(symbolWidth);
        symbolTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        var pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(pointsWidth);
        pointsTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);

        edibleAnimation = new EdibleAnimation(animationRegistry);

        eatenAnimation = new ManagedAnimation(animationRegistry, "Bonus_Eaten") {
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

    @Override
    public void dispose() {
        symbolTexture = null;
        pointsTexture = null;
        edibleAnimation.stop();
        edibleAnimation = null;
        eatenAnimation.stop();
        eatenAnimation = null;
    }

    public void update(GameContext gameContext) {
        Vector2f center = bonus.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-HTS);
        gameContext.optGameLevel().ifPresent(edibleAnimation::update);
    }

    public void showEdible() {
        setVisible(true);
        setWidth(symbolWidth);
        setMaterial(symbolTexture);
        edibleAnimation.playFromStart();
    }

    public void showEaten() {
        edibleAnimation.stop();
        setVisible(true);
        setWidth(pointsWidth);
        setMaterial(pointsTexture);
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        eatenAnimation.playFromStart();
    }

    public void expire() {
        edibleAnimation.stop();
        eatenAnimation.stop();
        setVisible(false);
    }
}
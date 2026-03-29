/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
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
public class Bonus3D extends Box implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {BONUS_EDIBLE, BONUS_EATEN}

    private final Bonus bonus;
    private final AnimationRegistry animations;

    private final double symbolWidth;
    private final double pointsWidth;
    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

    private class EdibleAnimation extends ManagedAnimation {

        public EdibleAnimation() {
            super("Bonus (Edible, Symbol)");
            setFactory(this::createAnimationFX);
        }

        private Animation createAnimationFX() {
            var animation = new RotateTransition(Duration.seconds(1), Bonus3D.this);
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

    public Bonus3D(AnimationRegistry animations, Bonus bonus, Image symbolImage, double symbolWidth, Image pointsImage, double pointsWidth) {
        this.animations = requireNonNull(animations);
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

        animations.register(AnimationID.BONUS_EDIBLE, new EdibleAnimation());

        final var eatenAnimation = new ManagedAnimation("Bonus (Eaten, Points)");
        eatenAnimation.setFactory(() -> {
            final var animation = new RotateTransition(Duration.seconds(1), Bonus3D.this);
            animation.setAxis(Rotate.X_AXIS);
            animation.setByAngle(360);
            animation.setInterpolator(Interpolator.LINEAR);
            animation.setRate(2);
            animation.setCycleCount(2);
            return animation;
        });
        animations.register(AnimationID.BONUS_EATEN, eatenAnimation);
    }

    @Override
    public void dispose() {
        symbolTexture = null;
        pointsTexture = null;
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::dispose);
        cleanupShape3D(this);
    }

    @Override
    public void update(GameLevel gameLevel) {
        switch (bonus.state()) {
            case INACTIVE, EATEN -> {}
            case EDIBLE -> {
                final Vector2f center = bonus.center();
                setTranslateX(center.x());
                setTranslateY(center.y());
                setTranslateZ(-HTS);
                animations.animation(AnimationID.BONUS_EDIBLE, EdibleAnimation.class).update(gameLevel);
            }
        }
    }

    public void showEdible() {
        setVisible(true);
        setWidth(symbolWidth);
        setMaterial(symbolTexture);
        animations.animation(AnimationID.BONUS_EDIBLE).playFromStart();
    }

    public void showEaten() {
        animations.animation(AnimationID.BONUS_EDIBLE).stop();
        setVisible(true);
        setWidth(pointsWidth);
        setMaterial(pointsTexture);
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        animations.animation(AnimationID.BONUS_EATEN).playFromStart();
    }

    public void expire() {
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
        setVisible(false);
    }
}
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.BonusEatenAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.BonusEdibleAnimation3D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

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
public class Bonus3D implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {BONUS_EDIBLE, BONUS_EATEN}

    private final Bonus bonus;
    private final AnimationRegistry animations;

    private final double symbolWidth;
    private final double pointsWidth;
    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

    private final Shape3D shape3D;

    public Bonus3D(AnimationRegistry animations, Bonus bonus, Image symbolImage, double symbolWidth, Image pointsImage, double pointsWidth) {
        this.animations = requireNonNull(animations);
        this.bonus = requireNonNull(bonus);
        this.symbolWidth = requireNonNegative(symbolWidth);
        this.pointsWidth = requireNonNegative(pointsWidth);

        shape3D = new Box(symbolWidth, TS, TS);

        var symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(symbolWidth);
        symbolTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        var pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(pointsWidth);
        pointsTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);

        animations.register(AnimationID.BONUS_EDIBLE, new BonusEdibleAnimation3D(bonus, this));
        animations.register(AnimationID.BONUS_EATEN, new BonusEatenAnimation3D(this));
    }

    @Override
    public void dispose() {
        symbolTexture = null;
        pointsTexture = null;
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::dispose);
        cleanupShape3D(shape3D);
    }

    @Override
    public void update(GameLevel gameLevel) {
        switch (bonus.state()) {
            case INACTIVE, EATEN -> {}
            case EDIBLE -> {
                final Vector2f center = bonus.center();
                shape3D.setTranslateX(center.x());
                shape3D.setTranslateY(center.y());
                shape3D.setTranslateZ(-HTS);
                animations.animation(AnimationID.BONUS_EDIBLE, BonusEdibleAnimation3D.class).update(gameLevel);
            }
        }
    }

    public Shape3D shape3D() {
        return shape3D;
    }

    public void showEdible() {
        shape3D.setVisible(true);
        if (shape3D instanceof Box box) {
            box.setWidth(symbolWidth);
        }
        shape3D.setMaterial(symbolTexture);
        animations.animation(AnimationID.BONUS_EDIBLE).playFromStart();
    }

    public void showEaten() {
        animations.animation(AnimationID.BONUS_EDIBLE).stop();
        shape3D.setVisible(true);
        if (shape3D instanceof Box box) {
            box.setWidth(pointsWidth);
        }
        shape3D.setMaterial(pointsTexture);
        shape3D.setRotationAxis(Rotate.X_AXIS);
        shape3D.setRotate(0);
        animations.animation(AnimationID.BONUS_EATEN).playFromStart();
    }

    public void expire() {
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
        shape3D.setVisible(false);
    }
}
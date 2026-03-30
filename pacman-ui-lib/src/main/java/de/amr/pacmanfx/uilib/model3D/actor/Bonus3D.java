/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.Bonus3DEatenAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.Bonus3DEdibleAnimation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
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
public class Bonus3D extends Box implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {BONUS_EDIBLE, BONUS_EATEN}

    private final Bonus bonus;
    private final AnimationRegistry animations;

    private final double symbolWidth;
    private final double pointsWidth;
    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

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

        animations.register(AnimationID.BONUS_EDIBLE, new Bonus3DEdibleAnimation(bonus, this));
        animations.register(AnimationID.BONUS_EATEN, new Bonus3DEatenAnimation(this));
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
                animations.animation(AnimationID.BONUS_EDIBLE, Bonus3DEdibleAnimation.class).update(gameLevel);
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
/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.BonusEatenAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.BonusRollingThroughWorldAnimation3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

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

    private final Translate translate = new Translate();
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private final Group top;
    private final Group rollingGroup;
    private final Shape3D shape3D;

    private final BonusRollingThroughWorldAnimation3D rollingAnimation;

    public Bonus3D(AnimationRegistry animations, Bonus bonus, Image symbolImage, double symbolWidth, Image pointsImage, double pointsWidth) {
        this.animations = requireNonNull(animations);
        this.bonus = requireNonNull(bonus);
        this.symbolWidth = requireNonNegative(symbolWidth);
        this.pointsWidth = requireNonNegative(pointsWidth);

        shape3D = new Box(symbolWidth, 8, 8);

        rollingGroup = new Group(shape3D);
        rollingGroup.getTransforms().addAll(rotateX, rotateY);

        top = new Group();
        top.getChildren().add(rollingGroup);

        top.getTransforms().add(translate);

        var symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(symbolWidth);
        symbolTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        var pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(pointsWidth);
        pointsTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);

        rollingAnimation = new BonusRollingThroughWorldAnimation3D(this);
        animations.register(AnimationID.BONUS_EATEN, new BonusEatenAnimation3D(this));
    }

    @Override
    public void update(GameLevel level) {
        switch (bonus.state()) {
            case INACTIVE, EATEN -> {}
            case EDIBLE -> {
                updatePosition(level);
                rollingAnimation.update();
            }
        }
    }

    @Override
    public void dispose() {
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::dispose);
        cleanupShape3D(shape3D);
        symbolTexture = null;
        pointsTexture = null;
    }

    public Bonus bonus() {
        return bonus;
    }

    public Node node() {
        return top;
    }

    public Shape3D shape3D() {
        return shape3D;
    }

    public Rotate rotateX() {
        return rotateX;
    }

    public Rotate rotateY() {
        return rotateY;
    }

    public void showEdible() {
        shape3D.setVisible(true);
        if (shape3D instanceof Box box) {
            box.setWidth(symbolWidth);
        }
        shape3D.setMaterial(symbolTexture);
    }

    public void showEaten() {
        shape3D.setVisible(true);
        if (shape3D instanceof Box box) {
            box.setWidth(pointsWidth);
        }
        shape3D.setMaterial(pointsTexture);

        // restore neutral orientation
        rotateX.setAngle(0);
        rotateY.setAngle(0);

        // Rotate around x-axis
        animations.animation(AnimationID.BONUS_EATEN).playFromStart();
    }

    public void expire() {
        animations.optAnimation(AnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(AnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
        shape3D.setVisible(false);
    }

    private void updatePosition(GameLevel level) {
        final Vector2f center = bonus.center();
        translate.setX(center.x());
        translate.setY(center.y());
        translate.setZ(-HTS);

        boolean outsideWorld = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
        top.setVisible(bonus.state() == BonusState.EDIBLE && !outsideWorld);
    }
}
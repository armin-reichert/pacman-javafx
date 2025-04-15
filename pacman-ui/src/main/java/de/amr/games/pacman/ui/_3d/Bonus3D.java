/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.StaticBonus;
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

import static de.amr.games.pacman.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D bonus symbol.
 *
 * @author Armin Reichert
 */
public class Bonus3D extends Box {

    private static final double SYMBOL_WIDTH = TS;
    private static final double POINTS_WIDTH = 1.8 * TS;

    private final Bonus bonus;
    private final ImageView symbolImageView;
    private final ImageView pointsImageView;
    private final RotateTransition eatenAnimation;
    private final RotateTransition edibleAnimation;

    public Bonus3D(Bonus bonus, Image symbolImage, Image pointsImage) {
        super(SYMBOL_WIDTH, TS, TS);

        requireNonNull(bonus);
        requireNonNull(symbolImage);
        requireNonNull(pointsImage);

        this.bonus = bonus;
        symbolImageView = new ImageView(symbolImage);
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(SYMBOL_WIDTH);

        pointsImageView = new ImageView(pointsImage);
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(POINTS_WIDTH);

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
        Vector2f center = bonus.actor().position().plus(HTS, HTS);
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-HTS);
        THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
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
            edibleAnimation.play();
        }
    }

    public void showEdible() {
        setVisible(true);
        setWidth(SYMBOL_WIDTH);
        setTexture(symbolImageView.getImage());
        if (bonus instanceof StaticBonus) {
            edibleAnimation.setAxis(Rotate.X_AXIS);
        }
        edibleAnimation.playFromStart();
    }

    public void onBonusExpired() {
        edibleAnimation.stop();
        setVisible(false);
    }

    public void showEaten() {
        edibleAnimation.stop();
        setVisible(true);
        setWidth(POINTS_WIDTH);
        setTexture(pointsImageView.getImage());
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        edibleAnimation.stop();
        eatenAnimation.playFromStart();
    }

    private void setTexture(Image texture) {
        //TODO when using Color.TRANSPARENT, nothing is shown, why?
        setMaterial(new PhongMaterial(Color.GHOSTWHITE, texture, null, null, null));
    }
}
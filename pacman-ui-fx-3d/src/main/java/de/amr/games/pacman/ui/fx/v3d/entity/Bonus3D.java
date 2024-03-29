/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.world.World;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * 3D bonus symbol.
 *
 * @author Armin Reichert
 */
public class Bonus3D extends Box {

    private final Bonus bonus;
    private final Image symbolImage;
    private final Image pointsImage;
    private final RotateTransition eatenAnimation;
    private final RotateTransition edibleAnimation;

    public Bonus3D(Bonus bonus, Image symbolImage, Image pointsImage) {
        super(TS, TS, TS);

        checkNotNull(bonus);
        checkNotNull(symbolImage);
        checkNotNull(pointsImage);

        this.bonus = bonus;
        this.symbolImage = symbolImage;
        this.pointsImage = pointsImage;

        edibleAnimation = new RotateTransition(Duration.seconds(1), this);
        edibleAnimation.setAxis(Rotate.Z_AXIS); // to trigger initial change
        edibleAnimation.setFromAngle(0);
        edibleAnimation.setToAngle(360);
        edibleAnimation.setInterpolator(Interpolator.LINEAR);
        edibleAnimation.setCycleCount(Animation.INDEFINITE);

        eatenAnimation = new RotateTransition(Duration.seconds(1), this);
        eatenAnimation.setAxis(Rotate.X_AXIS);
        eatenAnimation.setFromAngle(0);
        eatenAnimation.setToAngle(360);
        eatenAnimation.setInterpolator(Interpolator.LINEAR);
        eatenAnimation.setRate(2);
    }

    public void update(World world) {
        Vector2f position = bonus.entity().center();
        setTranslateX(position.x());
        setTranslateY(position.y());
        setTranslateZ(-HTS);
        boolean outside = position.x() < HTS || position.x() > world.numCols() * TS - HTS;
        boolean visible = bonus.state() != Bonus.STATE_INACTIVE && !outside;
        setVisible(visible);
        updateEdibleAnimation();
    }

    private void updateEdibleAnimation() {
        var rotationAxis = Rotate.X_AXIS; // default for static bonus
        if (bonus instanceof MovingBonus movingBonus) {
            rotationAxis = movingBonus.entity().moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (movingBonus.entity().moveDir() == Direction.UP || movingBonus.entity().moveDir() == Direction.RIGHT) {
                edibleAnimation.setRate(-1);
            } else {
                edibleAnimation.setRate(1);
            }
        }
        if (!edibleAnimation.getAxis().equals(rotationAxis)) {
            edibleAnimation.stop();
            edibleAnimation.setAxis(rotationAxis);
            edibleAnimation.play();
        }
    }

    public void showEdible() {
        setWidth(TS);
        setVisible(true);
        var imageView = new ImageView(symbolImage);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(getWidth());
        showImage(imageView.getImage());
        updateEdibleAnimation();
        edibleAnimation.playFromStart();
    }

    public void showEaten() {
        setRotationAxis(Rotate.X_AXIS);
        setRotate(0);
        setWidth(1.8 * TS);
        var imageView = new ImageView(pointsImage);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(1.8 * TS);
        showImage(imageView.getImage());
        eatenAnimation.playFromStart();
    }

    private void showImage(Image texture) {
        var material = new PhongMaterial(Color.GHOSTWHITE);
        material.setDiffuseMap(texture);
        setMaterial(material);
    }
}
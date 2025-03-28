/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.lib.Vector2i;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.Optional;

import static de.amr.games.pacman.uilib.Ufx.doAfterSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D energizer pellet.
 *
 * @author Armin Reichert
 */
public class Energizer3D extends Sphere implements Eatable3D {

    private static final double MIN_SCALE = 0.25;

    private final ScaleTransition pumping;
    private Animation eatenAnimation;

    public Energizer3D(double radius) {
        Globals.assertNonNegative(radius, "Energizer radius must be positive but is %f");
        setRadius(radius);
        setUserData(this);

        pumping = new ScaleTransition(Duration.seconds(1.0 / 4), this);
        pumping.setAutoReverse(true);
        pumping.setCycleCount(Animation.INDEFINITE);
        pumping.setInterpolator(Interpolator.EASE_BOTH);
        pumping.setFromX(1.0);
        pumping.setFromY(1.0);
        pumping.setFromZ(1.0);
        pumping.setToX(MIN_SCALE);
        pumping.setToY(MIN_SCALE);
        pumping.setToZ(MIN_SCALE);
    }

    @Override
    public String toString() {
        var pumpingText = pumping.getStatus() == Status.RUNNING ? ", pumping" : "";
        return String.format("[Energizer%s, tile: %s]", pumpingText, tile());
    }

    @Override
    public void setPosition(Point3D position) {
        requireNonNull(position);
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }

    @Override
    public Point3D position() {
        return new Point3D(getTranslateX(), getTranslateY(), getTranslateZ());
    }

    @Override
    public void setTile(Vector2i tile) {
        setUserData(tile);
    }

    @Override
    public Vector2i tile() {
        return (Vector2i) getUserData();
    }

    @Override
    public Shape3D shape3D() {
        return this;
    }

    @Override
    public Optional<Animation> getEatenAnimation() {
        return Optional.ofNullable(eatenAnimation);
    }

    public void setEatenAnimation(Animation animation) {
        this.eatenAnimation = animation;
    }

    @Override
    public void onEaten() {
        pumping.stop();
        var hideAfterDelay = doAfterSec(0.05, () -> setVisible(false));
        if (eatenAnimation != null) {
            new SequentialTransition(hideAfterDelay, eatenAnimation).play();
        } else {
            hideAfterDelay.play();
        }
    }

    public void startPumping() {
        pumping.playFromStart();
    }

    public void stopPumping() {
        pumping.stop();
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.Ufx.doAfterSec;
import static java.util.Objects.requireNonNull;

/**
 * 3D pellet.
 *
 * @author Armin Reichert
 */
public class Pellet3D implements Eatable3D {

    public static final String MESH_ID_PELLET = "Fruit";

    private final Shape3D shape;

    public Pellet3D(Model3D model3D, double radius) {
        requireNonNull(model3D);
        requirePositive(radius, "Pellet3D radius must be positive but is %f");

        shape = model3D.meshView(MESH_ID_PELLET);
        shape.setRotationAxis(Rotate.Z_AXIS);
        shape.setRotate(90);
        shape.setUserData(this);
        var bounds = shape.getBoundsInLocal();
        var max = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        var scaling = new Scale(2 * radius / max, 2 * radius / max, 2 * radius / max);
        shape.getTransforms().add(scaling);
    }

    @Override
    public void placeAtTile(Vector2i tile) {
        requireNonNull(tile);

        shape.setTranslateX(tile.x() * TS + HTS);
        shape.setTranslateY(tile.y() * TS + HTS);
        shape.setTranslateZ(-HTS);
    }

    @Override
    public Point3D position() {
        return new Point3D(shape.getTranslateX(), shape.getTranslateY(), shape.getTranslateZ());
    }

    @Override
    public Vector2i tile() {
        return tileAt((float) shape.getTranslateX(), (float) shape.getTranslateY());
    }

    @Override
    public Shape3D root() {
        return shape;
    }

    @Override
    public void onEaten() {
        doAfterSec(0.05, () -> shape.setVisible(false)).play();
    }

    @Override
    public Optional<Animation> getEatenAnimation() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("[Pellet, tile: %s, %s]", tile(), shape);
    }
}
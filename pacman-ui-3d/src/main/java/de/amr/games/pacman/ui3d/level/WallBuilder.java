/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public interface WallBuilder {

    static Node createWallBetweenTiles(
        Vector2i beginTile, Vector2i endTile,
        double thickness, DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return wallCenteredAt(center, length + thickness, thickness, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return wallCenteredAt(center, thickness, length, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    static Node wallCenteredAt(
        Vector2f center, double sizeX, double sizeY,
        DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        var base = new Box(sizeX, sizeY, wallHeightPy.get());
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.depthProperty().bind(wallHeightPy);
        base.materialProperty().bind(fillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, coatHeight);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(strokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    static Group cornerWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        Cylinder base = new Cylinder();
        base.setMouseTransparent(true);
        base.setRadius(radius);
        base.heightProperty().bind(wallHeightPy);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.materialProperty().bind(fillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder();
        top.setMouseTransparent(true);
        top.setRadius(radius);
        top.setHeight(coatHeight);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(strokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }


    static Node hWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), q.minus(p).length() + thickness, thickness,
            wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
    }

    static Node vWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), thickness, q.minus(p).length(),
            wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
    }

    static Node createCircularWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy) {

        Cylinder base = new Cylinder(radius, wallHeightPy.get());
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.heightProperty().bind(wallHeightPy);
        base.materialProperty().bind(fillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, coatHeight);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(strokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
public class WallBuilder {

    private PhongMaterial baseMaterial = new PhongMaterial();
    private PhongMaterial topMaterial = new PhongMaterial();

    public void setBaseMaterial(PhongMaterial material) {
        baseMaterial = material;
    }

    public void setTopMaterial(PhongMaterial material) {
        topMaterial = material;
    }

    public Node createWallBetweenTiles(
        Vector2i beginTile, Vector2i endTile,
        double thickness, DoubleProperty wallHeightPy, double coatHeight)
    {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return wallCenteredAt(center, length + thickness, thickness, wallHeightPy, coatHeight);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return wallCenteredAt(center, thickness, length, wallHeightPy, coatHeight);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Node wallCenteredAt(
        Vector2f center, double sizeX, double sizeY,
        DoubleProperty wallHeightPy, double coatHeight)
    {
        var base = new Box(sizeX, sizeY, wallHeightPy.get());
        base.setMaterial(baseMaterial);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.depthProperty().bind(wallHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, coatHeight);
        top.setMaterial(topMaterial);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Group cornerWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double coatHeight)
    {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, wallHeightPy.get(), divisions);
        base.setMaterial(baseMaterial);
        base.setMouseTransparent(true);
        base.heightProperty().bind(wallHeightPy);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, coatHeight, divisions);
        top.setMaterial(topMaterial);
        top.setMouseTransparent(true);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Node hWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double coatHeight)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), q.minus(p).length() + thickness, thickness,
            wallHeightPy, coatHeight);
    }

    public Node vWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double coatHeight)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), thickness, q.minus(p).length(), wallHeightPy, coatHeight);
    }

    public Node createCircularWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double coatHeight)
    {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, wallHeightPy.get(), divisions);
        base.setMaterial(baseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.heightProperty().bind(wallHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        base.setMouseTransparent(true);

        Cylinder top = new Cylinder(radius, coatHeight, divisions);
        top.setMaterial(topMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        top.setMouseTransparent(true);

        return new Group(base, top);
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
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
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_DRAW_MODE;

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
            return createWallWithCenter(center, length + thickness, thickness, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return createWallWithCenter(center, thickness, length, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    static Node createWallWithCenter(
            Vector2f center, double sizeX, double sizeY, DoubleProperty wallHeightPy, double coatHeight,
            Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy) {

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

    static void createCircularWall(
        Group parent, Vector2f center,
        DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy) {

        Cylinder base = new Cylinder(HTS, wallHeightPy.get());
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.heightProperty().bind(wallHeightPy);
        base.materialProperty().bind(wallFillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(HTS, coatHeight);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(wallStrokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        parent.getChildren().add(new Group(base, top));
    }

    static void buildObstacleWalls(
        Group parent,
        Obstacle obstacle,
        DoubleProperty wallHeightPy, double thickness, double coatHeight,
        Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy)
    {
        if (obstacle.isClosed() && obstacle.numSegments() == 4) {
            Vector2f center = obstacle.startPoint().minus(HTS, 0);
            createCircularWall(parent, center, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
            return;
        }
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            Obstacle.Segment seg = obstacle.segment(i);
            Vector2f q = p.plus(seg.vector());
            if (seg.isVerticalLine()) {
                Vector2f center = p.plus(seg.vector().scaled(0.5f));
                double length = seg.vector().length();
                Node wall = createWallWithCenter(center, thickness, length, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                parent.getChildren().add(wall);
            }
            else if (seg.isHorizontalLine()) {
                Vector2f center = p.plus(seg.vector().scaled(0.5f));
                double length = seg.vector().length();
                Node wall = createWallWithCenter(center, length + thickness, thickness, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                parent.getChildren().add(wall);
            }
            else if (seg.isNWCorner()) {
                if (seg.ccw()) {
                    Vector2f corner = p.plus(-r, 0);
                    Node hWall = hWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                } else {
                    Vector2f corner = p.plus(0, -r);
                    Node hWall = hWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                }
            }
            else if (seg.isSWCorner()) {
                if (seg.ccw()) {
                    Vector2f corner = p.plus(0, r);
                    Node hWall = hWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                } else {
                    Vector2f corner = p.plus(-r, 0);
                    Node hWall = hWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                }
            }
            else if (seg.isSECorner()) {
                if (seg.ccw()) {
                    Vector2f corner = p.plus(r, 0);
                    Node hWall = hWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                } else {
                    Vector2f corner = p.plus(0, r);
                    Node hWall = hWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                }
            }
            else if (seg.isNECorner()) {
                if (seg.ccw()) {
                    Vector2f corner = p.plus(0, -r);
                    Node hWall = hWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                } else {
                    Vector2f corner = p.plus(r, 0);
                    Node hWall = hWall(corner, p, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    Node vWall = vWall(corner, q, wallHeightPy, thickness, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                    parent.getChildren().addAll(hWall, vWall);
                }
            }
            p = q;
        }

    }

    static Node hWall(Vector2f p, Vector2f q,
            DoubleProperty wallHeightPy, double thickness, double coatHeight,
            Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy) {
        Vector2f center = p.plus(q).scaled(0.5f);
        double length = q.minus(p).length();
        return createWallWithCenter(center, length + thickness, thickness, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
    }

    static Node vWall(Vector2f p, Vector2f q,
          DoubleProperty wallHeightPy, double thickness, double coatHeight,
          Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy) {
        Vector2f center = p.plus(q).scaled(0.5f);
        double length = q.minus(p).length();
        return createWallWithCenter(center, thickness, length, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
    }
}
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.Tiles;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.List;

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
        DoubleProperty wallHeightPy, double topHeight)
    {
        var base = new Box(sizeX, sizeY, wallHeightPy.get());
        base.setMaterial(baseMaterial);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*topHeight));
        base.depthProperty().bind(wallHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, topHeight);
        top.setMaterial(topMaterial);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Group cornerWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double topHeight)
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
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*topHeight));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, topHeight, divisions);
        top.setMaterial(topMaterial);
        top.setMouseTransparent(true);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Node hWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double topHeight)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), q.minus(p).length() + thickness, thickness,
            wallHeightPy, topHeight);
    }

    public Node vWall(
        Vector2f p, Vector2f q,
        DoubleProperty wallHeightPy, double thickness, double topHeight)
    {
        return wallCenteredAt(p.plus(q).scaled(0.5f), thickness, q.minus(p).length(), wallHeightPy, topHeight);
    }

    public Node createCircularWall(
        Vector2f center, double radius,
        DoubleProperty wallHeightPy, double topHeight)
    {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, wallHeightPy.get(), divisions);
        base.setMaterial(baseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*topHeight));
        base.heightProperty().bind(wallHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        base.setMouseTransparent(true);

        Cylinder top = new Cylinder(radius, topHeight, divisions);
        top.setMaterial(topMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        top.setMouseTransparent(true);

        return new Group(base, top);
    }

    public void addOShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy, double topHeight) {
        Vector2f[] points = obstacle.points();
        if (obstacle.numSegments() == 4) {
            addTower(parent, new Vector2f(points[0].x(), points[1].y()), baseHeightPy, topHeight);
            Logger.info("Added one-tile circle, dead ends={}", obstacle.numDeadEnds());
        }
        else if (obstacle.numSegments() == 6) {
            Vector2f center0 = new Vector2f(points[0].x(), points[1].y());
            Vector2f center1 = new Vector2f(points[3].x(), points[4].y());
            Vector2f centerWall = center0.midpoint(center1);
            addTower(parent, center0, baseHeightPy, topHeight);
            addTower(parent, center1, baseHeightPy, topHeight);
            if (center0.x() < center1.x()) {
                addCastleWall(parent, centerWall, center0.manhattanDistance(center1), TS, baseHeightPy, topHeight);
            } else {
                addCastleWall(parent, centerWall, TS, center0.manhattanDistance(center1), baseHeightPy, topHeight);
            }
            Logger.info("Added {}-segment oval, dead ends={}", obstacle.numSegments(), obstacle.numDeadEnds());
        }
        else {
            // wider than one tile wide O-shape
            Vector2f centerTowerNW = new Vector2f(points[0].x(), points[1].y());
            Vector2f centerTowerSW = new Vector2f(points[3].x(), points[2].y());
            Vector2f centerTowerSE = new Vector2f(points[4].x(), points[5].y());
            Vector2f centerTowerNE = new Vector2f(points[7].x(), points[6].y());

            addTower(parent, centerTowerNW, baseHeightPy, topHeight);
            addTower(parent, centerTowerSW, baseHeightPy, topHeight);
            addTower(parent, centerTowerSE, baseHeightPy, topHeight);
            addTower(parent, centerTowerNE, baseHeightPy, topHeight);

            Vector2f centerWallN = centerTowerNW.midpoint(centerTowerNE);
            Vector2f centerWallS = centerTowerSW.midpoint(centerTowerSE);
            Vector2f centerWallW = centerTowerNW.midpoint(centerTowerSW);
            Vector2f centerWallE = centerTowerNE.midpoint(centerTowerSE);
            Vector2f center = centerWallW.midpoint(centerWallE);

            addCastleWall(parent, centerWallN, centerTowerNE.manhattanDistance(centerTowerNW), TS, baseHeightPy, topHeight);
            addCastleWall(parent, centerWallS, centerTowerSE.manhattanDistance(centerTowerSW), TS, baseHeightPy, topHeight);
            addCastleWall(parent, centerWallW, TS, centerTowerNW.manhattanDistance(centerTowerSW), baseHeightPy, topHeight);
            addCastleWall(parent, centerWallE, TS, centerTowerNE.manhattanDistance(centerTowerSE), baseHeightPy, topHeight);
            addCastleWall(parent, center,
                    centerWallW.manhattanDistance(centerWallE) - TS,
                    centerWallN.manhattanDistance(centerWallS) - TS,
                    baseHeightPy, topHeight);

            Logger.info("Added {}-segment oval, dead ends={}", obstacle.numSegments(), obstacle.numDeadEnds());
        }
    }

    public void addLShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy, double topHeight) {
        List<Integer> deadEndPositions = obstacle.deadEndSegmentPositions();
        Vector2f[] points = obstacle.points();
        int deadEnd0 = deadEndPositions.getFirst(), deadEnd1 = deadEndPositions.getLast();
        Vector2f center0 = deadEndCenter(obstacle, points, deadEnd0);
        Vector2f center1 = deadEndCenter(obstacle, points, deadEnd1);
        ObstacleSegment deadEnd0Segment = obstacle.segment(deadEnd0);
        Vector2f knee = null;
        if (deadEnd0Segment.isRoundedSECorner() || deadEnd0Segment.isRoundedNWCorner()) {
            knee = new Vector2f(center1.x(), center0.y());
        } else if (deadEnd0Segment.isRoundedSWCorner()) {
            knee = new Vector2f(center0.x(), center1.y());
        }
        addTower(parent, center0, baseHeightPy, topHeight);
        addTower(parent, center1, baseHeightPy, topHeight);
        addTower(parent, knee, baseHeightPy, topHeight);

        if (center0.x() == knee.x()) {
            addCastleWall(parent, center0.midpoint(knee), TS, center0.manhattanDistance(knee), baseHeightPy, topHeight);
        } else if (center0.y() == knee.y()) {
            addCastleWall(parent, center0.midpoint(knee), center0.manhattanDistance(knee), TS, baseHeightPy, topHeight);
        }
        if (center1.x() == knee.x()) {
            addCastleWall(parent, center1.midpoint(knee), TS, center1.manhattanDistance(knee), baseHeightPy, topHeight);
        } else if (center1.y() == knee.y()) {
            addCastleWall(parent, center1.midpoint(knee), center1.manhattanDistance(knee), TS, baseHeightPy, topHeight);
        }
    }

    public Vector2f deadEndCenter(Obstacle obstacle, Vector2f[] points, int deadEndIndex) {
        ObstacleSegment segment = obstacle.segment(deadEndIndex);
        return switch (segment.mapContent()) {
            case Tiles.CORNER_NW -> points[deadEndIndex].plus(0, HTS);
            case Tiles.CORNER_SW -> points[deadEndIndex].plus(HTS, 0);
            case Tiles.CORNER_SE -> points[deadEndIndex].plus(0, -HTS);
            case Tiles.CORNER_NE -> points[deadEndIndex].plus(-HTS, 0);
            default -> throw new IllegalStateException();
        };
    }

    public void addTower(Group parent, Vector2f center, DoubleProperty baseHeightPy, double topHeight) {
        Node tower = createCircularWall(center, HTS, baseHeightPy, topHeight);
        parent.getChildren().add(tower);
    }

    public void addCastleWall(Group parent, Vector2f center, double sizeX, double sizeY, DoubleProperty baseHeightPy, double topHeight) {
        Node wall = wallCenteredAt(center, sizeX, sizeY, baseHeightPy, topHeight);
        parent.getChildren().add(wall);
    }

    public void addGeneralShapeObstacle(Group parent, Obstacle obstacle, double thickness, DoubleProperty baseHeightPy, double topHeight) {
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = wallCenteredAt(p.midpoint(q), thickness, length, baseHeightPy, topHeight);
                parent.getChildren().add(wall);
            }
            else if (segment.isHorizontalLine()) {
                Node wall = wallCenteredAt(p.midpoint(q), length + thickness, thickness, baseHeightPy, topHeight);
                parent.getChildren().add(wall);
            }
            else if (segment.isNWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness, baseHeightPy, topHeight);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness, baseHeightPy, topHeight);
                }
            }
            else if (segment.isSWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness, baseHeightPy, topHeight);
                } else {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness, baseHeightPy, topHeight);
                }
            }
            else if (segment.isSECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness, baseHeightPy, topHeight);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness, baseHeightPy, topHeight);
                }
            }
            else if (segment.isNECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness, baseHeightPy, topHeight);
                } else {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness, baseHeightPy, topHeight);
                }
            }
            p = q;
        }
    }

    public void addGeneralShapeCorner(Group parent, Vector2f cornerPoint, Vector2f horEndPoint, Vector2f vertEndPoint, double thickness, DoubleProperty baseHeightPy, double topHeight) {
        Node hWall = wallCenteredAt(
                cornerPoint.midpoint(horEndPoint),
                Math.abs(cornerPoint.x() - horEndPoint.x()),
                thickness,
                baseHeightPy, topHeight);

        Node vWall = wallCenteredAt(
                cornerPoint.midpoint(vertEndPoint),
                thickness,
                Math.abs(cornerPoint.y() - vertEndPoint.y()),
                baseHeightPy, topHeight);

        Node cornerWall = cornerWall(cornerPoint,
                0.5 * thickness,
                baseHeightPy, topHeight);

        parent.getChildren().addAll(hWall, vWall, cornerWall);
    }
}
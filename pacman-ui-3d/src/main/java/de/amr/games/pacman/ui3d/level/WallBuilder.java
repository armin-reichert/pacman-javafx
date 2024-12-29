/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public class WallBuilder {

    private PhongMaterial baseMaterial = new PhongMaterial();
    private PhongMaterial topMaterial = new PhongMaterial();
    private double topHeight;

    public void setBaseMaterial(PhongMaterial material) {
        baseMaterial = material;
    }

    public void setTopMaterial(PhongMaterial material) {
        topMaterial = material;
    }

    public void setTopHeight(double topHeight) {
        this.topHeight = topHeight;
    }

    public Node createWallBetweenTiles(Vector2i beginTile, Vector2i endTile, double thickness, DoubleProperty wallHeightPy) {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return compositeWallCenteredAt(center, length + thickness, thickness, wallHeightPy);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return compositeWallCenteredAt(center, thickness, length, wallHeightPy);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Node compositeWallCenteredAt(Vector2f center, double sizeX, double sizeY, DoubleProperty wallHeightPy)
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

    public Group compositeCornerWall(Vector2f center, double radius, DoubleProperty wallHeightPy) {
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

    public Node compositeHWallBetween(Vector2f p, Vector2f q, DoubleProperty wallHeightPy, double thickness) {
        return compositeWallCenteredAt(p.plus(q).scaled(0.5f), p.manhattanDist(q) + thickness, thickness, wallHeightPy);
    }

    public Node compositeVWallBetween(Vector2f p, Vector2f q, DoubleProperty wallHeightPy, double thickness) {
        return compositeWallCenteredAt(p.plus(q).scaled(0.5f), thickness, p.manhattanDist(q), wallHeightPy);
    }

    public Node compositeCircularWall(Vector2f center, double radius, DoubleProperty wallHeightPy) {
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

    public void addOShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        if (obstacle.numSegments() == 4) {
            addTower(parent, new Vector2f(obstacle.point(0).x(), obstacle.point(1).y()), baseHeightPy);
            Logger.info("Added one-tile O-shape, dead ends={}", obstacle.numDeadEnds());
        }
        else if (obstacle.numSegments() == 6) {
            Vector2f center0 = new Vector2f(obstacle.point(0).x(), obstacle.point(1).y());
            Vector2f center1 = new Vector2f(obstacle.point(3).x(), obstacle.point(4).y());
            Vector2f centerWall = center0.midpoint(center1);
            addTower(parent, center0, baseHeightPy);
            addTower(parent, center1, baseHeightPy);
            if (center0.x() < center1.x()) {
                addCastleWall(parent, centerWall, center0.manhattanDist(center1), TS, baseHeightPy);
            } else {
                addCastleWall(parent, centerWall, TS, center0.manhattanDist(center1), baseHeightPy);
            }
            Logger.info("Added {}-segment O-shape, dead ends={}", obstacle.numSegments(), obstacle.numDeadEnds());
        }
        else {
            // wider than one tile wide O-shape
            Vector2f centerTowerNW = new Vector2f(obstacle.point(0).x(), obstacle.point(1).y());
            Vector2f centerTowerSW = new Vector2f(obstacle.point(3).x(), obstacle.point(2).y());
            Vector2f centerTowerSE = new Vector2f(obstacle.point(4).x(), obstacle.point(5).y());
            Vector2f centerTowerNE = new Vector2f(obstacle.point(7).x(), obstacle.point(6).y());

            addTower(parent, centerTowerNW, baseHeightPy);
            addTower(parent, centerTowerSW, baseHeightPy);
            addTower(parent, centerTowerSE, baseHeightPy);
            addTower(parent, centerTowerNE, baseHeightPy);

            Vector2f centerWallN = centerTowerNW.midpoint(centerTowerNE);
            Vector2f centerWallS = centerTowerSW.midpoint(centerTowerSE);
            Vector2f centerWallW = centerTowerNW.midpoint(centerTowerSW);
            Vector2f centerWallE = centerTowerNE.midpoint(centerTowerSE);
            Vector2f center = centerWallW.midpoint(centerWallE);

            addCastleWall(parent, centerWallN, centerTowerNE.manhattanDist(centerTowerNW), TS, baseHeightPy);
            addCastleWall(parent, centerWallS, centerTowerSE.manhattanDist(centerTowerSW), TS, baseHeightPy);
            addCastleWall(parent, centerWallW, TS, centerTowerNW.manhattanDist(centerTowerSW), baseHeightPy);
            addCastleWall(parent, centerWallE, TS, centerTowerNE.manhattanDist(centerTowerSE), baseHeightPy);
            addCastleWall(parent, center,
                    centerWallW.manhattanDist(centerWallE) - TS,
                    centerWallN.manhattanDist(centerWallS) - TS,
                    baseHeightPy);

            Logger.info("Added {}-segment oval, dead ends={}", obstacle.numSegments(), obstacle.numDeadEnds());
        }
    }

    public void addLShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        int[] d = obstacle.deadEndSegmentIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(d[0]);
        Vector2f c1 = obstacle.cornerCenter(d[1]);
        ObstacleSegment d0Segment = obstacle.segment(d[0]);
        Vector2f knee;
        if (d0Segment.isRoundedSECorner() || d0Segment.isRoundedNWCorner()) {
            knee = new Vector2f(c1.x(), c0.y());
        } else if (d0Segment.isRoundedSWCorner()) {
            knee = new Vector2f(c0.x(), c1.y());
        } else {
            Logger.error("Invalid L-shape detected: {}", obstacle);
            return;
        }
        addTower(parent, c0, baseHeightPy);
        addTower(parent, c1, baseHeightPy);
        addTower(parent, knee, baseHeightPy);

        if (c0.x() == knee.x()) {
            addCastleWall(parent, c0.midpoint(knee), TS, c0.manhattanDist(knee), baseHeightPy);
        } else if (c0.y() == knee.y()) {
            addCastleWall(parent, c0.midpoint(knee), c0.manhattanDist(knee), TS, baseHeightPy);
        }
        if (c1.x() == knee.x()) {
            addCastleWall(parent, c1.midpoint(knee), TS, c1.manhattanDist(knee), baseHeightPy);
        } else if (c1.y() == knee.y()) {
            addCastleWall(parent, c1.midpoint(knee), c1.manhattanDist(knee), TS, baseHeightPy);
        }
    }

    public void addCrossShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        int[] d = obstacle.deadEndSegmentIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(d[0]);
        Vector2f c1 = obstacle.cornerCenter(d[1]);
        Vector2f c2 = obstacle.cornerCenter(d[2]);
        Vector2f c3 = obstacle.cornerCenter(d[3]);
        Vector2f center = new Vector2f(c3.x(), c0.y());
        addTower(parent, c0, baseHeightPy);
        addTower(parent, c1, baseHeightPy);
        addTower(parent, c2, baseHeightPy);
        addTower(parent, c3, baseHeightPy);
        addCastleWall(parent, c0.midpoint(center), c0.manhattanDist(center), TS, baseHeightPy);
        addCastleWall(parent, c2.midpoint(center), c2.manhattanDist(center), TS, baseHeightPy);
        addCastleWall(parent, c1.midpoint(center), TS, c1.manhattanDist(center), baseHeightPy);
        addCastleWall(parent, c3.midpoint(center), TS, c3.manhattanDist(center), baseHeightPy);
    }

    public void addUShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        int[] d = obstacle.deadEndSegmentIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(d[0]);
        Vector2f c1 = obstacle.cornerCenter(d[1]);
        // find centers on opposite side of dead ends
        Vector2f oc0, oc1;
        if (d[0] == 6 && d[1] == 13) {
            // U in normal orientation, open on top
            oc0 = obstacle.cornerCenter(4); // right leg
            oc1 = obstacle.cornerCenter(2); // left leg
            addCastleWall(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0), baseHeightPy);
            addCastleWall(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1), baseHeightPy);
            addCastleWall(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS, baseHeightPy);
        }
        else if (d[0] == 2 && d[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            addCastleWall(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0), baseHeightPy);
            addCastleWall(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1), baseHeightPy);
            addCastleWall(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS, baseHeightPy);
        }
        else if (d[0] == 4 && d[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            addCastleWall(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS, baseHeightPy);
            addCastleWall(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS, baseHeightPy);
            addCastleWall(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1), baseHeightPy);
        }
        else if (d[0] == 0 && d[1] == 7) {
            // U open at left side, d[0]=top, d[1]=bottom
            oc0 = obstacle.cornerCenter(12); // right top
            oc1 = obstacle.cornerCenter(10); // right bottom
            addCastleWall(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS, baseHeightPy);
            addCastleWall(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS, baseHeightPy);
            addCastleWall(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1), baseHeightPy);
        }
        else {
            Logger.info("Invalid U-shape detected: {}", obstacle);
            return;
        }
        addTower(parent, c0,  baseHeightPy);
        addTower(parent, c1,  baseHeightPy);
        addTower(parent, oc0, baseHeightPy);
        addTower(parent, oc1, baseHeightPy);
    }

    public void addSShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        int[] d = obstacle.deadEndSegmentIndices().toArray();
        Vector2f[] c = obstacle.deadEndCenters(); // count=2
        addTower(parent, c[0], baseHeightPy);
        addTower(parent, c[1], baseHeightPy);
        Vector2f tc0, tc1;
        if (d[0] == 0 && d[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            addTower(parent, tc0, baseHeightPy);
            addTower(parent, tc1, baseHeightPy);
            addCastleWall(parent, c[0].midpoint(tc0), c[0].manhattanDist(tc0), TS, baseHeightPy);
            addCastleWall(parent, c[1].midpoint(tc1), c[1].manhattanDist(tc1), TS, baseHeightPy);
            // vertical wall
            addCastleWall(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1), baseHeightPy);
        }
        else if (d[0] == 4 && d[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            addTower(parent, tc0, baseHeightPy);
            addTower(parent, tc1, baseHeightPy);
            addCastleWall(parent, tc0.midpoint(c[1]), tc0.manhattanDist(c[1]), TS, baseHeightPy);
            addCastleWall(parent, c[0].midpoint(tc1), c[0].manhattanDist(tc1), TS, baseHeightPy);
            // vertical wall
            addCastleWall(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1), baseHeightPy);
        }
        else if (d[0] == 6 && d[1] == 13) {
            if (c[1].x() < c[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                addTower(parent, tc0, baseHeightPy);
                addTower(parent, tc1, baseHeightPy);
                // horizontal tc1 - tc0
                addCastleWall(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS, baseHeightPy);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWall(parent, c[1].midpoint(tc1), TS, c[1].manhattanDist(tc1), baseHeightPy);
                addCastleWall(parent, tc0.midpoint(c[0]), TS, tc0.manhattanDist(c[0]), baseHeightPy);
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                addTower(parent, tc0, baseHeightPy);
                addTower(parent, tc1, baseHeightPy);
                // horizontal tc1 - tc0
                addCastleWall(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS, baseHeightPy);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWall(parent, c[1].midpoint(tc1), TS, c[1].manhattanDist(tc1), baseHeightPy);
                addCastleWall(parent, tc0.midpoint(c[0]), TS, tc0.manhattanDist(c[0]), baseHeightPy);
            }
        }
        else {
            Logger.error("Invalid S-shape detected");
        }
    }

    public void addTShapeObstacle(Group parent, Obstacle obstacle, DoubleProperty baseHeightPy) {
        int[] d = obstacle.deadEndSegmentIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(d[0]);
        Vector2f c1 = obstacle.cornerCenter(d[1]);
        Vector2f c2 = obstacle.cornerCenter(d[2]);
        Vector2f join;
        if (c2.x() == c0.x() && c1.x() > c2.x()) {
            join = new Vector2f(c0.x(), c1.y());
        }
        else if (c0.y() == c2.y() && c1.y() > c0.y()) {
            join = new Vector2f(c1.x(), c0.y());
        }
        else if (c0.y() == c1.y() && c2.y() < c0.y()) {
            join = new Vector2f(c2.x(), c0.y());
        }
        else if (c2.x() == c1.x() && c0.x() < c1.x()) {
            join = new Vector2f(c1.x(), c0.y());
        }
        else {
            Logger.error("Invalid T-shape obstacle: {}", obstacle);
            return;
        }
        addTower(parent, c0, baseHeightPy);
        addTower(parent, c1, baseHeightPy);
        addTower(parent, c2, baseHeightPy);

        if (c0.x() == join.x()) {
            addCastleWall(parent, c0.midpoint(join), TS, c0.manhattanDist(join), baseHeightPy);
        } else if (c0.y() == join.y()) {
            addCastleWall(parent, c0.midpoint(join), c0.manhattanDist(join), TS, baseHeightPy);
        }
        if (c1.x() == join.x()) {
            addCastleWall(parent, c1.midpoint(join), TS, c1.manhattanDist(join), baseHeightPy);
        } else if (c1.y() == join.y()) {
            addCastleWall(parent, c1.midpoint(join), c1.manhattanDist(join), TS, baseHeightPy);
        }
        if (c2.x() == join.x()) {
            addCastleWall(parent, c2.midpoint(join), TS, c2.manhattanDist(join), baseHeightPy);
        } else if (c2.y() == join.y()) {
            addCastleWall(parent, c2.midpoint(join), c2.manhattanDist(join), TS, baseHeightPy);
        }
    }

    private void addTower(Group parent, Vector2f center, DoubleProperty baseHeightPy) {
        parent.getChildren().add(compositeCircularWall(center, HTS, baseHeightPy));
    }

    private void addCastleWall(Group parent, Vector2f center, double sizeX, double sizeY, DoubleProperty baseHeightPy) {
        parent.getChildren().add(compositeWallCenteredAt(center, sizeX, sizeY, baseHeightPy));
    }

    public void addGeneralShapeObstacle(Group parent, Obstacle obstacle, double thickness, DoubleProperty baseHeightPy) {
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), thickness, length, baseHeightPy);
                parent.getChildren().add(wall);
            }
            else if (segment.isHorizontalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), length + thickness, thickness, baseHeightPy);
                parent.getChildren().add(wall);
            }
            else if (segment.isNWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness, baseHeightPy);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness, baseHeightPy);
                }
            }
            else if (segment.isSWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness, baseHeightPy);
                } else {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness, baseHeightPy);
                }
            }
            else if (segment.isSECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness, baseHeightPy);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness, baseHeightPy);
                }
            }
            else if (segment.isNECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness, baseHeightPy);
                } else {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness, baseHeightPy);
                }
            }
            p = q;
        }
    }

    public void addGeneralShapeCorner(Group parent, Vector2f cornerPoint, Vector2f horEndPoint, Vector2f vertEndPoint, double thickness, DoubleProperty baseHeightPy) {
        Node hWall = compositeWallCenteredAt(
                cornerPoint.midpoint(horEndPoint),
                Math.abs(cornerPoint.x() - horEndPoint.x()),
                thickness,
                baseHeightPy);

        Node vWall = compositeWallCenteredAt(
                cornerPoint.midpoint(vertEndPoint),
                thickness,
                Math.abs(cornerPoint.y() - vertEndPoint.y()),
                baseHeightPy);

        Node cornerWall = compositeCornerWall(cornerPoint, 0.5 * thickness, baseHeightPy);

        parent.getChildren().addAll(hWall, vWall, cornerWall);
    }
}
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
import javafx.beans.property.SimpleDoubleProperty;
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
    private DoubleProperty baseHeightPy = new SimpleDoubleProperty(1.0);
    private double topHeight;

    public void setBaseMaterial(PhongMaterial material) {
        baseMaterial = material;
    }

    public void setTopMaterial(PhongMaterial material) {
        topMaterial = material;
    }

    public void setBaseHeightProperty(DoubleProperty py) {
        this.baseHeightPy = py;
    }

    public void setTopHeight(double topHeight) {
        this.topHeight = topHeight;
    }

    public Node createWallBetweenTiles(Vector2i beginTile, Vector2i endTile, double thickness) {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return compositeWallCenteredAt(center, length + thickness, thickness);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return compositeWallCenteredAt(center, thickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Node compositeWallCenteredAt(Vector2f center, double sizeX, double sizeY) {
        var base = new Box(sizeX, sizeY, baseHeightPy.get());
        base.setMaterial(baseMaterial);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(baseHeightPy.multiply(-0.5).add(2*topHeight));
        base.depthProperty().bind(baseHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, topHeight);
        top.setMaterial(topMaterial);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(baseHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Group compositeCornerWall(Vector2f center, double radius) {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, baseHeightPy.get(), divisions);
        base.setMaterial(baseMaterial);
        base.setMouseTransparent(true);
        base.heightProperty().bind(baseHeightPy);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(baseHeightPy.multiply(-0.5).add(2*topHeight));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, topHeight, divisions);
        top.setMaterial(topMaterial);
        top.setMouseTransparent(true);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(baseHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Node compositeHWallBetween(Vector2f p, Vector2f q, double thickness) {
        return compositeWallCenteredAt(p.midpoint(q), p.manhattanDist(q) + thickness, thickness);
    }

    public Node compositeVWallBetween(Vector2f p, Vector2f q, double thickness) {
        return compositeWallCenteredAt(p.midpoint(q), thickness, p.manhattanDist(q));
    }

    public Node compositeCircularWall(Vector2f center, double radius) {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, baseHeightPy.get(), divisions);
        base.setMaterial(baseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(baseHeightPy.multiply(-0.5).add(2*topHeight));
        base.heightProperty().bind(baseHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        base.setMouseTransparent(true);

        Cylinder top = new Cylinder(radius, topHeight, divisions);
        top.setMaterial(topMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(baseHeightPy.add(0.5*topHeight).multiply(-1).add(2*topHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        top.setMouseTransparent(true);

        return new Group(base, top);
    }

    public void addOShape3D(Group parent, Obstacle obstacle, boolean fillCenter) {
        switch (obstacle.encoding()) {
            case "dgfe" -> { // 1-tile circle
                addTower(parent, obstacle.cornerCenter(0));
                Logger.info("- Added O-shape 3D, segments={} U-turns={}", obstacle.numSegments(), obstacle.numUTurns());
            }
            case "dcgfce", "dgbfeb" -> { // oval with one small side (2 U-turns)
                Vector2f[] c = obstacle.uTurnCenters();
                addTower(parent, c[0]);
                addTower(parent, c[1]);
                addCastleWallBetween(parent, c[0], c[1]);
                Logger.info("- Added O-shape 3D, segments={} U-turns={}", obstacle.numSegments(), obstacle.numUTurns());
            }
            case "dcgbfceb" -> { // oval without U-turns
                var towers = new Vector2f[] {
                    obstacle.cornerCenter(0), obstacle.cornerCenter(2), obstacle.cornerCenter(4), obstacle.cornerCenter(6)
                };
                for (int i = 0; i < towers.length; ++i) {
                    addTower(parent, towers[i]);
                    int next = i < towers.length - 1 ? i + 1 : 0;
                    addCastleWallBetween(parent, towers[i], towers[next]);
                }
                if (fillCenter) {
                    Vector2f center = towers[0].midpoint(towers[2]);
                    double height = towers[0].manhattanDist(towers[1]) - TS, width = towers[0].manhattanDist(towers[3]) - TS;
                    addCastleWallWithCenter(parent, center, width, height);
                }
                Logger.info("- Added O-shape 3D, segments={} U-turns={}", obstacle.numSegments(), obstacle.numUTurns());
            }
            default -> Logger.error("Invalid O-shape detected {}", obstacle);
        }
    }

    public void addLShape3D(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        int[] d = obstacle.uTurnSegmentIndices().toArray();
        ObstacleSegment firstUTurn = obstacle.segment(d[0]);
        Vector2f corner = firstUTurn.isRoundedSECorner() || firstUTurn.isRoundedNWCorner()
            ? new Vector2f(c[1].x(), c[0].y())
            : new Vector2f(c[0].x(), c[1].y());
        addTower(parent, c[0]);
        addTower(parent, c[1]);
        addTower(parent, corner);
        addCastleWallBetween(parent, c[0], corner);
        addCastleWallBetween(parent, c[1], corner);
    }

    public void addHShape3D(Group parent, Obstacle obstacle) {
        String encoding = obstacle.encoding();
        switch (encoding) {
            case "dgefdgbfegdfeb" -> {
                // little H rotated 90 degrees
                Logger.error("Little-H obstacle creation still missing!");
            }
            case "dcgfdegfcedfge" -> {
                // little H in normal orientation
                Logger.error("Little-H obstacle creation still missing!");
            }
            case "dgbecfbdgbfebgcdbfeb" -> {
                // H rotated by 90 degrees
                Vector2f towerNW = obstacle.cornerCenter(0);
                Vector2f towerSW = obstacle.cornerCenter(8);
                Vector2f towerSE = obstacle.cornerCenter(10);
                Vector2f towerNE = obstacle.cornerCenter(17);
                addTower(parent, towerNW);
                addTower(parent, towerSW);
                addTower(parent, towerSE);
                addTower(parent, towerNE);
                addCastleWallBetween(parent, towerNW, towerNE);
                addCastleWallBetween(parent, towerSW, towerSE);
                Vector2f topJoin = towerNW.midpoint(towerNE);
                Vector2f bottomJoin = towerSW.midpoint(towerSE);
                addCastleWallWithCenter(parent, topJoin.midpoint(bottomJoin), TS, topJoin.manhattanDist(bottomJoin));
            }
            case "dcgfcdbecgfcedcfbgce" -> {
                // H in normal orientation
                Vector2f towerNW = obstacle.cornerCenter(0);
                Vector2f towerSW = obstacle.cornerCenter(2);
                Vector2f towerSE = obstacle.cornerCenter(9);
                Vector2f towerNE = obstacle.cornerCenter(12);
                addTower(parent, towerNW);
                addTower(parent, towerSW);
                addTower(parent, towerSE);
                addTower(parent, towerNE);
                Vector2f leftJoin = towerNW.midpoint(towerSW);
                Vector2f rightJoin = towerNE.midpoint(towerSE);
                Vector2f center = leftJoin.midpoint(rightJoin);
                addCastleWallWithCenter(parent, leftJoin, TS, towerNW.manhattanDist(towerSW));
                addCastleWallWithCenter(parent, rightJoin, TS, towerNE.manhattanDist(towerSE));
                addCastleWallWithCenter(parent, center, leftJoin.manhattanDist(rightJoin), TS);
            }
        }
    }

    public void addCross3D(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        for (Vector2f tower : c) {
            addTower(parent, tower);
        }
        addCastleWallBetween(parent, c[0], c[2]);
        addCastleWallBetween(parent, c[1], c[3]);
    }

    public void addUShape3D(Group parent, Obstacle obstacle) {
        int[] d = obstacle.uTurnSegmentIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(d[0]);
        Vector2f c1 = obstacle.cornerCenter(d[1]);
        // find centers on opposite side of U-turns
        Vector2f oc0, oc1;
        if (d[0] == 6 && d[1] == 13) {
            // U in normal orientation, open on top
            oc0 = obstacle.cornerCenter(4); // right leg
            oc1 = obstacle.cornerCenter(2); // left leg
            addCastleWallWithCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addCastleWallWithCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (d[0] == 2 && d[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            addCastleWallWithCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addCastleWallWithCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (d[0] == 4 && d[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            addCastleWallWithCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addCastleWallWithCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else if (d[0] == 0 && d[1] == 7) {
            // U open at left side, d[0]=top, d[1]=bottom
            oc0 = obstacle.cornerCenter(12); // right top
            oc1 = obstacle.cornerCenter(10); // right bottom
            addCastleWallWithCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addCastleWallWithCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else {
            Logger.info("Invalid U-shape detected: {}", obstacle);
            return;
        }
        addTower(parent, c0);
        addTower(parent, c1);
        addTower(parent, oc0);
        addTower(parent, oc1);
    }

    public void addSShape3D(Group parent, Obstacle obstacle) {
        int[] d = obstacle.uTurnSegmentIndices().toArray();
        Vector2f[] c = obstacle.uTurnCenters(); // count=2
        addTower(parent, c[0]);
        addTower(parent, c[1]);
        Vector2f tc0, tc1;
        if (d[0] == 0 && d[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            addTower(parent, tc0);
            addTower(parent, tc1);
            addCastleWallWithCenter(parent, c[0].midpoint(tc0), c[0].manhattanDist(tc0), TS);
            addCastleWallWithCenter(parent, c[1].midpoint(tc1), c[1].manhattanDist(tc1), TS);
            // vertical wall
            addCastleWallWithCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (d[0] == 4 && d[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            addTower(parent, tc0);
            addTower(parent, tc1);
            addCastleWallWithCenter(parent, tc0.midpoint(c[1]), tc0.manhattanDist(c[1]), TS);
            addCastleWallWithCenter(parent, c[0].midpoint(tc1), c[0].manhattanDist(tc1), TS);
            // vertical wall
            addCastleWallWithCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (d[0] == 6 && d[1] == 13) {
            if (c[1].x() < c[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                addTower(parent, tc0);
                addTower(parent, tc1);
                // horizontal tc1 - tc0
                addCastleWallWithCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWallWithCenter(parent, c[1].midpoint(tc1), TS, c[1].manhattanDist(tc1));
                addCastleWallWithCenter(parent, tc0.midpoint(c[0]), TS, tc0.manhattanDist(c[0]));
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                addTower(parent, tc0);
                addTower(parent, tc1);
                // horizontal tc1 - tc0
                addCastleWallWithCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWallWithCenter(parent, c[1].midpoint(tc1), TS, c[1].manhattanDist(tc1));
                addCastleWallWithCenter(parent, tc0.midpoint(c[0]), TS, tc0.manhattanDist(c[0]));
            }
        }
        else {
            Logger.error("Invalid S-shape detected");
        }
    }

    public void addTShape3D(Group parent, Obstacle obstacle) {
        int[] d = obstacle.uTurnSegmentIndices().toArray();
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
        addTower(parent, c0);
        addTower(parent, c1);
        addTower(parent, c2);

        if (c0.x() == join.x()) {
            addCastleWallWithCenter(parent, c0.midpoint(join), TS, c0.manhattanDist(join));
        } else if (c0.y() == join.y()) {
            addCastleWallWithCenter(parent, c0.midpoint(join), c0.manhattanDist(join), TS);
        }
        if (c1.x() == join.x()) {
            addCastleWallWithCenter(parent, c1.midpoint(join), TS, c1.manhattanDist(join));
        } else if (c1.y() == join.y()) {
            addCastleWallWithCenter(parent, c1.midpoint(join), c1.manhattanDist(join), TS);
        }
        if (c2.x() == join.x()) {
            addCastleWallWithCenter(parent, c2.midpoint(join), TS, c2.manhattanDist(join));
        } else if (c2.y() == join.y()) {
            addCastleWallWithCenter(parent, c2.midpoint(join), c2.manhattanDist(join), TS);
        }
    }

    private void addTower(Group parent, Vector2f center) {
        parent.getChildren().add(compositeCircularWall(center, HTS));
    }

    private void addCastleWallBetween(Group parent, Vector2f p, Vector2f q) {
        if (p.x() == q.x()) { // vertical wall
            addCastleWallWithCenter(parent, p.midpoint(q), TS, p.manhattanDist(q));
        } else if (p.y() == q.y()) { // horizontal wall
            addCastleWallWithCenter(parent, p.midpoint(q), p.manhattanDist(q), TS);
        } else {
            Logger.error("Can only add horizontal or vertical castle walls, p={}, q={}", p, q);
        }
    }

    private void addCastleWallWithCenter(Group parent, Vector2f center, double sizeX, double sizeY) {
        parent.getChildren().add(compositeWallCenteredAt(center, sizeX, sizeY));
    }

    public void addObstacle3D(Group parent, Obstacle obstacle, double thickness) {
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), thickness, length);
                parent.getChildren().add(wall);
            }
            else if (segment.isHorizontalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), length + thickness, thickness);
                parent.getChildren().add(wall);
            }
            else if (segment.isNWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness);
                }
            }
            else if (segment.isSWCorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness);
                } else {
                    addGeneralShapeCorner(parent, p.plus(-r, 0), p, q, thickness);
                }
            }
            else if (segment.isSECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness);
                } else {
                    addGeneralShapeCorner(parent, p.plus(0, r), q, p, thickness);
                }
            }
            else if (segment.isNECorner()) {
                if (segment.ccw()) {
                    addGeneralShapeCorner(parent, p.plus(0, -r), q, p, thickness);
                } else {
                    addGeneralShapeCorner(parent, p.plus(r, 0), p, q, thickness);
                }
            }
            p = q;
        }
    }

    private void addGeneralShapeCorner(Group parent, Vector2f corner, Vector2f horEndPoint, Vector2f vertEndPoint, double thickness) {
        Node hWall = compositeWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), thickness);
        Node vWall = compositeWallCenteredAt(corner.midpoint(vertEndPoint), thickness, corner.manhattanDist(vertEndPoint));
        Node cWall = compositeCornerWall(corner, 0.5 * thickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }
}
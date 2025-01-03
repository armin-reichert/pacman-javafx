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

import java.util.Arrays;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public class WorldRenderer3D {

    protected static final int CYLINDER_DIVISIONS = 24;

    protected PhongMaterial wallBaseMaterial = new PhongMaterial();
    protected PhongMaterial wallTopMaterial = new PhongMaterial();
    protected DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(1.0);
    protected double wallTopHeight;

    public void setWallBaseMaterial(PhongMaterial material) {
        wallBaseMaterial = material;
    }

    public void setWallTopMaterial(PhongMaterial material) {
        wallTopMaterial = material;
    }

    public void setWallBaseHeightProperty(DoubleProperty py) {
        wallBaseHeightPy = py;
    }

    public void setWallTopHeight(double height) {
        wallTopHeight = height;
    }

    public Node createCompositeWallCenteredAt(Vector2f center, double sizeX, double sizeY) {
        var base = new Box(sizeX, sizeY, wallBaseHeightPy.get());
        base.depthProperty().bind(wallBaseHeightPy);
        base.setMaterial(wallBaseMaterial);
        base.setMouseTransparent(true);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, wallTopHeight);
        top.setMaterial(wallTopMaterial);
        top.setMouseTransparent(true);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Node createCompositeWallBetweenTiles(Vector2i beginTile, Vector2i endTile, double thickness) {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = TS * (right.x() - left.x());
            return createCompositeWallCenteredAt(center, length + thickness, thickness);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = TS * (bottom.y() - top.y());
            return createCompositeWallCenteredAt(center, thickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Group createCompositeCornerWall(Vector2f center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(wallBaseMaterial);
        base.setMouseTransparent(true);
        base.heightProperty().bind(wallBaseHeightPy);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setMouseTransparent(true);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5* wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Node createCompositeHorizontalWallBetween(Vector2f p, Vector2f q, double thickness) {
        return createCompositeWallCenteredAt(p.midpoint(q), p.manhattanDist(q) + thickness, thickness);
    }

    public Node createCompositeVerticalWallBetween(Vector2f p, Vector2f q, double thickness) {
        return createCompositeWallCenteredAt(p.midpoint(q), thickness, p.manhattanDist(q));
    }

    public Node createCompositeCircularWall(Vector2f center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(wallBaseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.heightProperty().bind(wallBaseHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        base.setMouseTransparent(true);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        top.setMouseTransparent(true);

        return new Group(base, top);
    }

    protected void addTowers(Group parent, Vector2f... centers) {
        for (Vector2f center : centers) {
            parent.getChildren().add(createCompositeCircularWall(center, HTS));
        }
    }

    protected void addWall(Group parent, Vector2f p, Vector2f q) {
        if (p.x() == q.x()) { // vertical wall
            addWallAtCenter(parent, p.midpoint(q), TS, p.manhattanDist(q));
        } else if (p.y() == q.y()) { // horizontal wall
            addWallAtCenter(parent, p.midpoint(q), p.manhattanDist(q), TS);
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p, q);
        }
    }

    protected void addWallAtCenter(Group parent, Vector2f center, double sizeX, double sizeY) {
        parent.getChildren().add(createCompositeWallCenteredAt(center, sizeX, sizeY));
    }

    // default implementation for non-standard obstacles
    public void addObstacle3D(Group parent, Obstacle obstacle, double thickness) {
        addGenericObstacle3D(parent, obstacle, thickness);
    }

    // Standard 3D obstacles

    public void addOShape3D(Group parent, Obstacle obstacle, boolean fillCenter) {
        switch (obstacle.encoding()) {
            // 1-tile circle
            case "dgfe" -> addTowers(parent, obstacle.cornerCenter(0));

            // oval with one small side and 2 towers
            case "dcgfce", "dgbfeb" -> {
                Vector2f[] c = obstacle.uTurnCenters();
                addTowers(parent, c);
                addWall(parent, c[0], c[1]);
            }

            // larger oval with 4 "towers"
            case "dcgbfceb" -> {
                Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6);
                addTowers(parent, t);
                for (int i = 0; i < t.length; ++i) {
                    int next = i < t.length - 1 ? i + 1 : 0;
                    addWall(parent, t[i], t[next]);
                }
                if (fillCenter) {
                    double height = t[0].manhattanDist(t[1]) - TS, width = t[0].manhattanDist(t[3]) - TS;
                    addWallAtCenter(parent, t[0].midpoint(t[2]), width, height);
                }
            }

            default -> Logger.error("Invalid O-shape detected: {}", obstacle);
        }
    }

    public void addLShape3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        int[] d = obstacle.uTurnIndices().toArray();
        ObstacleSegment firstUTurn = obstacle.segment(d[0]);
        Vector2f corner = firstUTurn.isRoundedSECorner() || firstUTurn.isRoundedNWCorner()
            ? vec_2f(utc[1].x(), utc[0].y())
            : vec_2f(utc[0].x(), utc[1].y());
        addTowers(parent, utc);
        addTowers(parent, corner);
        addWall(parent, utc[0], corner);
        addWall(parent, utc[1], corner);
    }

    public void addFShape3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        switch (obstacle.encoding()) {
            case "dcgfcdbfebgdbfeb",
                 "dgbefbdgbecgfceb",
                 "dcgfcdbfebgcdbfeb",
                 "dgbecfbdgbecgfceb"-> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.y(), q.y()));
                float spineX = utc[2].x();
                Vector2f spineTop = vec_2f(spineX, utc[0].y());
                Vector2f spineMiddle = vec_2f(spineX, utc[1].y());
                addTowers(parent, utc);
                addTowers(parent, spineTop);
                addWall(parent, spineTop, utc[0]);
                addWall(parent, spineMiddle, utc[1]);
                addWall(parent, spineTop, utc[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[0].y();
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                Vector2f spineRight = vec_2f(utc[2].x(), spineY);
                addTowers(parent, utc);
                addTowers(parent, spineRight);
                addWall(parent, utc[0], spineRight);
                addWall(parent, spineMiddle, utc[1]);
                addWall(parent, spineRight, utc[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[2].y();
                Vector2f spineLeft = vec_2f(utc[0].x(), spineY);
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                addTowers(parent, utc);
                addTowers(parent, spineLeft);
                addWall(parent, spineLeft, utc[2]);
                addWall(parent, spineLeft, utc[0]);
                addWall(parent, spineMiddle, utc[1]);
            }
        }
    }

    public void addHShape3D(Group parent, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            // little H rotated 90 degrees
            case "dgefdgbfegdfeb" -> Logger.error("Little-H obstacle creation still missing!");
            // little H in normal orientation
            case "dcgfdegfcedfge" -> Logger.error("Little-H obstacle creation still missing!");
            case "dgbecfbdgbfebgcdbfeb" -> {
                // H rotated by 90 degrees
                Vector2f towerNW = obstacle.cornerCenter(0);
                Vector2f towerSW = obstacle.cornerCenter(8);
                Vector2f towerSE = obstacle.cornerCenter(10);
                Vector2f towerNE = obstacle.cornerCenter(17);
                Vector2f topJoin = towerNW.midpoint(towerNE);
                Vector2f bottomJoin = towerSW.midpoint(towerSE);
                addTowers(parent, towerNW, towerSW, towerSE, towerNE);
                addWall(parent, towerNW, towerNE);
                addWall(parent, towerSW, towerSE);
                addWallAtCenter(parent, topJoin.midpoint(bottomJoin), TS, topJoin.manhattanDist(bottomJoin));
            }
            case "dcgfcdbecgfcedcfbgce" -> {
                // H in normal orientation
                Vector2f towerNW = obstacle.cornerCenter(0);
                Vector2f towerSW = obstacle.cornerCenter(2);
                Vector2f towerSE = obstacle.cornerCenter(9);
                Vector2f towerNE = obstacle.cornerCenter(12);
                Vector2f leftJoin = towerNW.midpoint(towerSW);
                Vector2f rightJoin = towerNE.midpoint(towerSE);
                Vector2f center = leftJoin.midpoint(rightJoin);
                addTowers(parent, towerNW, towerSW, towerSE, towerNE);
                addWallAtCenter(parent, leftJoin, TS, towerNW.manhattanDist(towerSW));
                addWallAtCenter(parent, rightJoin, TS, towerNE.manhattanDist(towerSE));
                addWallAtCenter(parent, center, leftJoin.manhattanDist(rightJoin), TS);
            }
        }
    }

    public void addCross3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        addTowers(parent, utc);
        addWall(parent, utc[0], utc[2]);
        addWall(parent, utc[1], utc[3]);
    }

    //TODO rework and simplify
    public void addUShape3D(Group parent, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(uti[0]);
        Vector2f c1 = obstacle.cornerCenter(uti[1]);
        // find centers on opposite side of U-turns
        Vector2f oc0, oc1;
        if (uti[0] == 6 && uti[1] == 13) {
            // U in normal orientation, open on top
            oc0 = obstacle.cornerCenter(4); // right leg
            oc1 = obstacle.cornerCenter(2); // left leg
            addTowers(parent, c0, c1, oc0, oc1);
            addWallAtCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addWallAtCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addWallAtCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 2 && uti[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            addTowers(parent, c0, c1, oc0, oc1);
            addWallAtCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addWallAtCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addWallAtCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            addTowers(parent, c0, c1, oc0, oc1);
            addWallAtCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addWallAtCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addWallAtCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else if (uti[0] == 0 && uti[1] == 7) {
            // U open at left side, d[0]=top, d[1]=bottom
            oc0 = obstacle.cornerCenter(12); // right top
            oc1 = obstacle.cornerCenter(10); // right bottom
            addTowers(parent, c0, c1, oc0, oc1);
            addWallAtCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addWallAtCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addWallAtCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else {
            Logger.info("Invalid U-shape detected: {}", obstacle);
        }
    }

    //TODO rework and simplify
    public void addSShape3D(Group parent, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f[] utc = obstacle.uTurnCenters(); // count=2
        addTowers(parent, utc);
        Vector2f tc0, tc1;
        if (uti[0] == 0 && uti[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            addTowers(parent, tc0, tc1);
            addWallAtCenter(parent, utc[0].midpoint(tc0), utc[0].manhattanDist(tc0), TS);
            addWallAtCenter(parent, utc[1].midpoint(tc1), utc[1].manhattanDist(tc1), TS);
            // vertical wall
            addWallAtCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            addTowers(parent, tc0, tc1);
            addWallAtCenter(parent, tc0.midpoint(utc[1]), tc0.manhattanDist(utc[1]), TS);
            addWallAtCenter(parent, utc[0].midpoint(tc1), utc[0].manhattanDist(tc1), TS);
            // vertical wall
            addWallAtCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 6 && uti[1] == 13) {
            if (utc[1].x() < utc[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                addTowers(parent, tc0, tc1);
                // horizontal tc1 - tc0
                addWallAtCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addWallAtCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addWallAtCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                addTowers(parent, tc0, tc1);
                // horizontal tc1 - tc0
                addWallAtCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addWallAtCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addWallAtCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            }
        }
        else {
            Logger.error("Invalid S-shape detected");
        }
    }

    public void addTShape3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f join;
        if (utc[2].x() == utc[0].x() && utc[1].x() > utc[2].x()) {
            join = vec_2f(utc[0].x(), utc[1].y());
        }
        else if (utc[0].y() == utc[2].y() && utc[1].y() > utc[0].y()) {
            join = vec_2f(utc[1].x(), utc[0].y());
        }
        else if (utc[0].y() == utc[1].y() && utc[2].y() < utc[0].y()) {
            join = vec_2f(utc[2].x(), utc[0].y());
        }
        else if (utc[2].x() == utc[1].x() && utc[0].x() < utc[1].x()) {
            join = vec_2f(utc[1].x(), utc[0].y());
        }
        else {
            Logger.error("Invalid T-shape obstacle: {}", obstacle);
            return;
        }
        addTowers(parent, utc);
        if (utc[0].x() == join.x()) {
            addWallAtCenter(parent, utc[0].midpoint(join), TS, utc[0].manhattanDist(join));
        } else if (utc[0].y() == join.y()) {
            addWallAtCenter(parent, utc[0].midpoint(join), utc[0].manhattanDist(join), TS);
        }
        if (utc[1].x() == join.x()) {
            addWallAtCenter(parent, utc[1].midpoint(join), TS, utc[1].manhattanDist(join));
        } else if (utc[1].y() == join.y()) {
            addWallAtCenter(parent, utc[1].midpoint(join), utc[1].manhattanDist(join), TS);
        }
        if (utc[2].x() == join.x()) {
            addWallAtCenter(parent, utc[2].midpoint(join), TS, utc[2].manhattanDist(join));
        } else if (utc[2].y() == join.y()) {
            addWallAtCenter(parent, utc[2].midpoint(join), utc[2].manhattanDist(join), TS);
        }
    }

    //TODO This handles only the normal orientation
    public void addTShapeTwoRows3D(Group parent, Obstacle obstacle) {
        Vector2f leg = obstacle.uTurnCenters()[0];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f cornerSE = obstacle.cornerCenter(11);
        Vector2f cornerNE = obstacle.cornerCenter(13);
        addTowers(parent, leg, cornerNW, cornerNE, cornerSW, cornerSE);
        addWall(parent, cornerNW, cornerSW);
        addWall(parent, cornerNE, cornerSE);
        addWall(parent, cornerNW, cornerNE);
        addWall(parent, cornerSW, cornerSE);
        addWall(parent, leg, vec_2f(leg.x(), cornerSW.y()));
    }

    // fallback obstacle builder

    protected void addGenericObstacle3D(Group parent, Obstacle obstacle, double thickness){
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = createCompositeWallCenteredAt(p.midpoint(q), thickness, length);
                parent.getChildren().add(wall);
            } else if (segment.isHorizontalLine()) {
                Node wall = createCompositeWallCenteredAt(p.midpoint(q), length + thickness, thickness);
                parent.getChildren().add(wall);
            } else if (segment.isNWCorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(parent, p.plus(-r, 0), p, q, thickness);
                } else {
                    addGenericShapeCorner(parent, p.plus(0, -r), q, p, thickness);
                }
            } else if (segment.isSWCorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(parent, p.plus(0, r), q, p, thickness);
                } else {
                    addGenericShapeCorner(parent, p.plus(-r, 0), p, q, thickness);
                }
            } else if (segment.isSECorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(parent, p.plus(r, 0), p, q, thickness);
                } else {
                    addGenericShapeCorner(parent, p.plus(0, r), q, p, thickness);
                }
            } else if (segment.isNECorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(parent, p.plus(0, -r), q, p, thickness);
                } else {
                    addGenericShapeCorner(parent, p.plus(r, 0), p, q, thickness);
                }
            }
            p = q;
        }
    }

    protected void addGenericShapeCorner(Group parent, Vector2f corner, Vector2f horEndPoint, Vector2f vertEndPoint, double thickness) {
        Node hWall = createCompositeWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), thickness);
        Node vWall = createCompositeWallCenteredAt(corner.midpoint(vertEndPoint), thickness, corner.manhattanDist(vertEndPoint));
        Node cWall = createCompositeCornerWall(corner, 0.5 * thickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }
}
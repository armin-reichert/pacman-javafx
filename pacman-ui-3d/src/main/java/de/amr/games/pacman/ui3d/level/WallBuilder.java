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
public class WallBuilder {

    private PhongMaterial wallBaseMaterial = new PhongMaterial();
    private PhongMaterial wallTopMaterial = new PhongMaterial();
    private DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(1.0);
    private double wallTopHeight;

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
        var base = new Box(sizeX, sizeY, wallBaseHeightPy.get());
        base.depthProperty().bind(wallBaseHeightPy);
        base.setMaterial(wallBaseMaterial);
        base.setMouseTransparent(true);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5).add(wallTopHeight));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, wallTopHeight);
        top.setMaterial(wallTopMaterial);
        top.setMouseTransparent(true);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1).add(wallTopHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    public Group compositeCornerWall(Vector2f center, double radius) {
        int divisions = 24;

        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), divisions);
        base.setMaterial(wallBaseMaterial);
        base.setMouseTransparent(true);
        base.heightProperty().bind(wallBaseHeightPy);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5).add(wallTopHeight));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, wallTopHeight, divisions);
        top.setMaterial(wallTopMaterial);
        top.setMouseTransparent(true);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5* wallTopHeight).multiply(-1).add(wallTopHeight));
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

        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), divisions);
        base.setMaterial(wallBaseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5).add(wallTopHeight));
        base.heightProperty().bind(wallBaseHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        base.setMouseTransparent(true);

        Cylinder top = new Cylinder(radius, wallTopHeight, divisions);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1).add(wallTopHeight));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        top.setMouseTransparent(true);

        return new Group(base, top);
    }

    private void towers(Group parent, Vector2f... centers) {
        for (Vector2f center : centers) {
            parent.getChildren().add(compositeCircularWall(center, HTS));
        }
    }

    private void wall(Group parent, Vector2f p, Vector2f q) {
        if (p.x() == q.x()) { // vertical wall
            wallAtCenter(parent, p.midpoint(q), TS, p.manhattanDist(q));
        } else if (p.y() == q.y()) { // horizontal wall
            wallAtCenter(parent, p.midpoint(q), p.manhattanDist(q), TS);
        } else {
            Logger.error("Can only add horizontal or vertical castle walls, p={}, q={}", p, q);
        }
    }

    private void wallAtCenter(Group parent, Vector2f center, double sizeX, double sizeY) {
        parent.getChildren().add(compositeWallCenteredAt(center, sizeX, sizeY));
    }

    // Standard 3D obstacles

    public void addOShape3D(Group parent, Obstacle obstacle, boolean fillCenter) {
        switch (obstacle.encoding()) {
            // 1-tile circle
            case "dgfe" -> towers(parent, obstacle.cornerCenter(0));

            // oval with one small side and 2 towers
            case "dcgfce", "dgbfeb" -> {
                Vector2f[] c = obstacle.uTurnCenters();
                towers(parent, c);
                wall(parent, c[0], c[1]);
            }

            // larger oval with 4 towers
            case "dcgbfceb" -> {
                Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6);
                towers(parent, t);
                for (int i = 0; i < t.length; ++i) {
                    int next = i < t.length - 1 ? i + 1 : 0;
                    wall(parent, t[i], t[next]);
                }
                if (fillCenter) {
                    double height = t[0].manhattanDist(t[1]) - TS, width = t[0].manhattanDist(t[3]) - TS;
                    wallAtCenter(parent, t[0].midpoint(t[2]), width, height);
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
        towers(parent, utc);
        towers(parent, corner);
        wall(parent, utc[0], corner);
        wall(parent, utc[1], corner);
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
                towers(parent, utc);
                towers(parent, spineTop);
                wall(parent, spineTop, utc[0]);
                wall(parent, spineMiddle, utc[1]);
                wall(parent, spineTop, utc[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[0].y();
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                Vector2f spineRight = vec_2f(utc[2].x(), spineY);
                towers(parent, utc);
                towers(parent, spineRight);
                wall(parent, utc[0], spineRight);
                wall(parent, spineMiddle, utc[1]);
                wall(parent, spineRight, utc[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[2].y();
                Vector2f spineLeft = vec_2f(utc[0].x(), spineY);
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                towers(parent, utc);
                towers(parent, spineLeft);
                wall(parent, spineLeft, utc[2]);
                wall(parent, spineLeft, utc[0]);
                wall(parent, spineMiddle, utc[1]);
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
                towers(parent, towerNW, towerSW, towerSE, towerNE);
                wall(parent, towerNW, towerNE);
                wall(parent, towerSW, towerSE);
                wallAtCenter(parent, topJoin.midpoint(bottomJoin), TS, topJoin.manhattanDist(bottomJoin));
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
                towers(parent, towerNW, towerSW, towerSE, towerNE);
                wallAtCenter(parent, leftJoin, TS, towerNW.manhattanDist(towerSW));
                wallAtCenter(parent, rightJoin, TS, towerNE.manhattanDist(towerSE));
                wallAtCenter(parent, center, leftJoin.manhattanDist(rightJoin), TS);
            }
        }
    }

    public void addCross3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        towers(parent, utc);
        wall(parent, utc[0], utc[2]);
        wall(parent, utc[1], utc[3]);
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
            wallAtCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            wallAtCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            wallAtCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 2 && uti[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            wallAtCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            wallAtCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            wallAtCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            wallAtCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            wallAtCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            wallAtCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else if (uti[0] == 0 && uti[1] == 7) {
            // U open at left side, d[0]=top, d[1]=bottom
            oc0 = obstacle.cornerCenter(12); // right top
            oc1 = obstacle.cornerCenter(10); // right bottom
            wallAtCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            wallAtCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            wallAtCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else {
            Logger.info("Invalid U-shape detected: {}", obstacle);
            return;
        }
        towers(parent, c0, c1, oc0, oc1);
    }

    //TODO rework and simplify
    public void addSShape3D(Group parent, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f[] utc = obstacle.uTurnCenters(); // count=2
        towers(parent, utc);
        Vector2f tc0, tc1;
        if (uti[0] == 0 && uti[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            towers(parent, tc0, tc1);
            wallAtCenter(parent, utc[0].midpoint(tc0), utc[0].manhattanDist(tc0), TS);
            wallAtCenter(parent, utc[1].midpoint(tc1), utc[1].manhattanDist(tc1), TS);
            // vertical wall
            wallAtCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            towers(parent, tc0, tc1);
            wallAtCenter(parent, tc0.midpoint(utc[1]), tc0.manhattanDist(utc[1]), TS);
            wallAtCenter(parent, utc[0].midpoint(tc1), utc[0].manhattanDist(tc1), TS);
            // vertical wall
            wallAtCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 6 && uti[1] == 13) {
            if (utc[1].x() < utc[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                towers(parent, tc0, tc1);
                // horizontal tc1 - tc0
                wallAtCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                wallAtCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                wallAtCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                towers(parent, tc0, tc1);
                // horizontal tc1 - tc0
                wallAtCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                wallAtCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                wallAtCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
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
        towers(parent, utc);
        if (utc[0].x() == join.x()) {
            wallAtCenter(parent, utc[0].midpoint(join), TS, utc[0].manhattanDist(join));
        } else if (utc[0].y() == join.y()) {
            wallAtCenter(parent, utc[0].midpoint(join), utc[0].manhattanDist(join), TS);
        }
        if (utc[1].x() == join.x()) {
            wallAtCenter(parent, utc[1].midpoint(join), TS, utc[1].manhattanDist(join));
        } else if (utc[1].y() == join.y()) {
            wallAtCenter(parent, utc[1].midpoint(join), utc[1].manhattanDist(join), TS);
        }
        if (utc[2].x() == join.x()) {
            wallAtCenter(parent, utc[2].midpoint(join), TS, utc[2].manhattanDist(join));
        } else if (utc[2].y() == join.y()) {
            wallAtCenter(parent, utc[2].midpoint(join), utc[2].manhattanDist(join), TS);
        }
    }

    //TODO This handles only the normal orientation
    public void addT2RowsShape3D(Group parent, Obstacle obstacle) {
        Vector2f leg = obstacle.uTurnCenters()[0];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f cornerSE = obstacle.cornerCenter(11);
        Vector2f cornerNE = obstacle.cornerCenter(13);
        towers(parent, leg, cornerNW, cornerNE, cornerSW, cornerSE);
        wall(parent, cornerNW, cornerSW);
        wall(parent, cornerNE, cornerSE);
        wall(parent, cornerNW, cornerNE);
        wall(parent, cornerSW, cornerSE);
        wall(parent, leg, vec_2f(leg.x(), cornerSW.y()));
    }

    // fallback obstacle builder

    private void addGenericObstacle3D(Group parent, Obstacle obstacle, double thickness){
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), thickness, length);
                parent.getChildren().add(wall);
            } else if (segment.isHorizontalLine()) {
                Node wall = compositeWallCenteredAt(p.midpoint(q), length + thickness, thickness);
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

    private void addGenericShapeCorner(Group parent, Vector2f corner, Vector2f horEndPoint, Vector2f vertEndPoint, double thickness) {
        Node hWall = compositeWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), thickness);
        Node vWall = compositeWallCenteredAt(corner.midpoint(vertEndPoint), thickness, corner.manhattanDist(vertEndPoint));
        Node cWall = compositeCornerWall(corner, 0.5 * thickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    // dispatcher

    public void addObstacle3D(Group parent, Obstacle obstacle, double thickness) {
        //TODO handle special cases elsewhere, maybe in game-specific code?
        switch (obstacle.encoding()) {
            // Tengen BIG map #1, upside T at top, center
            case "dcfbdcgbfcebgce" -> addTengen_BigMap1_UpsideT(parent, obstacle);

            // Tengen BIG map #2, large desk-like obstacle on the bottom
            case "dgbecgbfebgcdbecfbdgbfcdbfeb" -> addTengen_BigMap2_DeskLike(parent, obstacle);

            // Tengen BIG map #3, large double-T obstacle on the top
            case "dcgbecgfcdbecgfcdbfceb" -> addTengen_BigMap3_DoubleTOnTop(parent, obstacle);

            // Tengen BIG map #5, bowl-like obstacle on the top
            case "dcgbecgbfcdbfcebdcfbgceb" -> addTengen_BigMap5_Bowl(parent, obstacle);

            // Tengen BIG map #5, double-F on left side
            case "dcgfcdbfebgcdbfebgcdbfeb" -> addTengen_BigMap5_DoubleFLeft(parent, obstacle);

            // Tengen BIG map #5, double-F on right side
            case "dgbecfbdgbecfbdgbecgfceb" -> addTengen_BigMap5_DoubleFRight(parent, obstacle);

            // Tengen BIG map #5, plane-like obstacle middle bottom
            case "dcfbdgbecfbdgbfebgcdbfebgce" -> addTengen_BigMap5_PlaneLike(parent, obstacle);

            // Tengen BIG map #6, obstacle left-top
            case "dcgfcdbfcebgce" -> addTengen_BigMap6_LeftTopObstacle(parent, obstacle);

            // Tengen BIG map #6, obstacle right-top
            case "dcfbdcgbecgfce" -> addTengen_BigMap6_RightTopObstacle(parent, obstacle);

            // Tengen BIG map #8, huge 62-segment obstacle on top
            case "dcgbecgbecgfcdbfcdbecgbecgfcdbfcdbfcebdcgbecfbgcebdcfbgcdbfceb" -> addTengen_BigMap8_62SegmentObstacle(parent, obstacle);

            // Tengen BIG map #8, big-bowl obstacle middle bottom
            case "dcgbecgbfcdbfcedcfbdcfbgcebgce" -> addTengen_BigMap8_BigBowl(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbfebgcdbfceb" -> addTengen_BigMap8_SeaHorseLeft(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbecfbdgbfceb" -> addTengen_BigMap8_SeaHorseRight(parent, obstacle);

            // Tengen BIG map #9, inward turned legs (left+right)
            case "dcgbfebgcdbecfbdgbfceb" -> addTengen_BigMap9_InwardLegs(parent, obstacle);

            // Tengen BIG map #10, table upside-down, at top of maze
            case "dcfbdgbfebgcedcfbgce" -> addTengen_BigMap10_TableUpsideDown(parent, obstacle);

            // Tengen BIG map #11, Tour Eiffel-like
            case "dcfbdcgfcdbecgfcebgce" -> addTengen_BigMap11_TourEiffel(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at left side at bottom of maze
            case "dcfbdgbfcdbfebgce" -> addTengen_StrangeMap1_Leg_Left(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at right side at bottom of maze
            case "dcfbdgbecgbfebgce" -> addTengen_StrangeMap1_Leg_Right(parent, obstacle);

            // Tengen STRANGE map #1, Y-shaped obstacle at center at bottom of maze
            case "dgbecgbecgfcdbfcdbfebdcfbgceb" -> addTengen_StrangeMap1_YShape(parent, obstacle);

            // Tengen STRANGE map #2, bowl-like obstacle at center at top of maze
            case "dgbecgbfcdbfebdcfbgceb" -> addTengen_StrangeMap2_Bowl(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at left/top
            case "dcgbecgfcebgcdbfeb" -> addTengen_StrangeMap2_Gallows_Left(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at right/top
            case "dgbecfbdcgfcdbfceb" -> addTengen_StrangeMap2_Gallows_Right(parent, obstacle);

            // Tengen STRANGE map #3. large hat-like onstacle center/top
            case "dfbdcfbdcgfdbfcdbecgbegfcebgcebgeb" -> addTengen_StrangeMap3_Hat(parent, obstacle);

            // Tengen STRANGE map #3, mushroom center-bottom
            case "dcgbecgbfcdbfceb" -> addTengen_StrangeMap3_Mushroom(parent, obstacle);

            // Tengen STRANGE map #3, glasses center-bottom
            case "dcgbfcebdcfbgceb" -> addTengen_StrangeMap3_Glasses(parent, obstacle);

            default -> addGenericObstacle3D(parent, obstacle, thickness);
        }
    }

    // Obstacles not handled by standard obstacle types:

    private void addTengen_BigMap1_UpsideT(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 8, 10);
        towers(parent, t);
        wall(parent, t[1], t[2]);
        wall(parent, t[3], t[4]);
        wall(parent, t[0], t[2].midpoint(t[3]));
        wallAtCenter(parent, t[1].midpoint(t[3]), t[1].manhattanDist(t[4]), 2 * TS);
    }

    private void addTengen_BigMap2_DeskLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topL = c[0], innerBottomL = c[1], innerBottomR = c[2], topR = c[3];
        Vector2f outerBottomL = innerBottomL.minus(4 * TS, 0);
        Vector2f outerBottomR = innerBottomR.plus(4 * TS, 0);
        towers(parent, topL, topR, innerBottomL, outerBottomL, innerBottomR, outerBottomR);
        wall(parent, topL, topR);
        wall(parent, outerBottomL, innerBottomL);
        wall(parent, outerBottomR, innerBottomR);
        wall(parent, outerBottomL, outerBottomL.minus(0, 6 * TS));
        wall(parent, outerBottomR, outerBottomR.minus(0, 6 * TS));
    }

    private void addTengen_BigMap3_DoubleTOnTop(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerNE = cornerNW.plus(12 * TS, 0);
        Vector2f cornerSW = cornerNW.plus(0, TS);
        Vector2f cornerSE = cornerNE.plus(0, TS);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f bottomL = c[0], bottomR = c[1];
        towers(parent, cornerNW, cornerNE, cornerSW, cornerSE, bottomL, bottomR);
        wall(parent, cornerNW, cornerSW);
        wall(parent, cornerNE, cornerSE);
        wall(parent, cornerNW, cornerNE);
        wall(parent, cornerSW, cornerSE);
        wall(parent, bottomL, bottomL.minus(0, 3 * TS));
        wall(parent, bottomR, bottomR.minus(0, 3 * TS));
    }

    private void addTengen_BigMap5_Bowl(Group parent, Obstacle obstacle) {
        Vector2f leftCornerNW = obstacle.cornerCenter(0);
        Vector2f leftCornerSW = leftCornerNW.plus(0, TS);
        Vector2f leftCornerNE = leftCornerNW.plus(2 * TS, 0);
        Vector2f rightCornerNW = leftCornerNW.plus(8 * TS, 0);
        Vector2f rightCornerNE = rightCornerNW.plus(2 * TS, 0);
        Vector2f rightCornerSE = rightCornerNE.plus(0, TS);
        Vector2f leftBottom = leftCornerNW.plus(2 * TS, 4 * TS);
        Vector2f rightBottom = leftBottom.plus(6 * TS, 0);
        towers(parent, leftCornerNW, leftCornerSW, leftCornerNE, rightCornerNW, rightCornerNE, rightCornerSE, leftBottom, rightBottom);
        wall(parent, leftCornerNW, leftCornerNE);
        wall(parent, leftCornerNW, leftCornerSW);
        wall(parent, leftCornerNE, leftBottom);
        wall(parent, rightCornerNW, rightCornerNE);
        wall(parent, rightCornerNE, rightCornerSE);
        wall(parent, rightCornerNW, rightBottom);
        wall(parent, leftBottom, rightBottom);
    }

    private void addTengen_BigMap5_DoubleFLeft(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topRight = c[3], middleRight = c[2], bottomRight = c[1];
        towers(parent, cornerNW, cornerSW, topRight, middleRight, bottomRight);
        wall(parent, cornerNW, cornerSW);
        wall(parent, cornerNW, topRight);
        wall(parent, middleRight.minus(3 * TS, 0), middleRight);
        wall(parent, bottomRight.minus(3 * TS, 0), bottomRight);
        wall(parent, cornerNW, cornerSW);
    }

    private void addTengen_BigMap5_DoubleFRight(Group parent, Obstacle obstacle) {
        Vector2f cornerNE = obstacle.cornerCenter(22);
        Vector2f cornerSE = obstacle.cornerCenter(20);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topLeft = c[0], middleLeft = c[1], bottomLeft = c[2];
        towers(parent, cornerNE, cornerSE, topLeft, middleLeft, bottomLeft);
        wall(parent, cornerNE, cornerSE);
        wall(parent, cornerNE, topLeft);
        wall(parent, middleLeft.plus(3 *TS, 0), middleLeft);
        wall(parent, bottomLeft.plus(3 *TS, 0), bottomLeft);
        wall(parent, cornerNE, cornerSE);
    }

    private void addTengen_BigMap5_PlaneLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f nose = c[4], leftWing = c[0], rightWing = c[3], leftBack = c[1], rightBack = c[2];
        towers(parent, nose, leftWing, leftBack, rightWing, rightBack);
        wall(parent, nose, leftBack.midpoint(rightBack));
        wall(parent, leftWing, rightWing);
        wall(parent, leftBack, rightBack);
    }

    private void addTengen_BigMap8_62SegmentObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 60, 58, 2, 6, 10, 15, 50, 48, 21, 25, 30, 34, 36, 38, 40);
        Vector2f[] h = new Vector2f[8];
        h[0] = vec_2f(t[4].x(),  t[2].y());
        h[1] = vec_2f(t[4].x(),  t[3].y());
        h[2] = vec_2f(t[5].x(),  t[4].y());
        h[3] = vec_2f(t[10].x(), t[9].y());
        h[4] = vec_2f(t[11].x(), t[12].y());
        h[5] = vec_2f(t[11].x(), t[15].y());
        h[6] = vec_2f(t[4].x(),  t[0].y());
        h[7] = vec_2f(t[11].x(), t[13].y());

        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, h[0], h[6]);
        wall(parent, t[0], t[3]);
        wall(parent, t[3], h[1]);
        wall(parent, t[1], t[2]);
        wall(parent, t[2], h[0]);
        wall(parent, h[0], h[1]);
        wall(parent, h[1], t[4]);
        wall(parent, t[4], t[6]);
        wall(parent, h[2], t[5]);
        wall(parent, t[6], t[7]);
        wall(parent, t[7], t[8]);
        wall(parent, t[8], t[9]);
        wall(parent, t[9], t[11]);
        wall(parent, h[3], t[10]);
        wall(parent, t[11], h[4]);
        wall(parent, h[4], t[12]);
        wall(parent, h[4], h[5]);
        wall(parent, h[5], t[15]);
        wall(parent, h[5], h[7]);
        wall(parent, t[15], t[14]);
        wall(parent, t[14], t[13]);
        wall(parent, t[13], t[12]);
        wall(parent, t[12], h[4]);
    }

    private void addTengen_BigMap8_BigBowl(Group parent, Obstacle obstacle) {
        Vector2f[] p = new Vector2f[8];
        p[0] = obstacle.uTurnCenters()[1];
        p[1] = p[0].plus(0, 2 * TS);
        p[2] = p[1].plus(3 * TS, 0);
        p[3] = p[2].plus(0, 6 * TS);
        p[4] = p[3].plus(12 * TS, 0);
        p[5] = p[4].minus(0, 6 * TS);
        p[6] = p[5].plus(3 * TS, 0);
        p[7] = p[6].minus(0, 2 * TS);
        towers(parent, p);
        for (int i = 0; i < p.length; ++i) {
            if (i + 1 < p.length) wall(parent, p[i], p[i+1]);
        }
    }

    private void addTengen_BigMap8_SeaHorseLeft(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 11, 13);
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[0], t[4]);
        wall(parent, t[1], t[2]);
        wall(parent, t[4], t[3]);
        wall(parent, t[3], t[3].minus(2*TS, 0));
    }

    private void addTengen_BigMap8_SeaHorseRight(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 8, 2, 11, 13);
        towers(parent, t);
        wall(parent, t[0], t[2]);
        wall(parent, t[0], t[4]);
        wall(parent, t[3], t[4]);
        wall(parent, t[3], t[1]);
        wall(parent, t[2], t[2].plus(2*TS, 0));
    }

    private void addTengen_BigMap9_InwardLegs(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f toeLeft = utc[0], toeRight = utc[1];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f heelLeft = obstacle.cornerCenter(2);
        Vector2f heelRight = obstacle.cornerCenter(18);
        Vector2f cornerNE = obstacle.cornerCenter(20);
        towers(parent, cornerNW, cornerNE, heelLeft, toeLeft, heelRight, toeRight);
        wall(parent, cornerNW, cornerNE);
        wall(parent, cornerNW, heelLeft);
        wall(parent, heelLeft, toeLeft);
        wall(parent, cornerNE, heelRight);
        wall(parent, heelRight, toeRight);
    }

    private void addTengen_BigMap10_TableUpsideDown(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 8, 12);
        Vector2f h0 = vec_2f(t[0].x(), t[1].y()), h1 = vec_2f(t[3].x(), t[1].y());
        towers(parent, t);
        wall(parent, t[1], t[2]);
        wall(parent, t[0], h0);
        wall(parent, t[3], h1);
    }

    private void addTengen_BigMap11_TourEiffel(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f baseLeft = utc[0], baseRight = utc[1], top = utc[2];
        Vector2f platformLeft = obstacle.cornerCenter(4);
        Vector2f platformRight = obstacle.cornerCenter(16);
        Vector2f topBase = vec_2f(top.x(), platformLeft.y());
        towers(parent, top, platformLeft, platformRight, baseLeft, baseRight);
        wall(parent, top, topBase);
        wall(parent, platformLeft, platformRight);
        wall(parent, platformLeft, baseLeft);
        wall(parent, platformRight, baseRight);
    }

    private void addTengen_StrangeMap1_Leg_Left(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 7, 11);
        Vector2f h = vec_2f(t[0].x(), t[3].y());
        towers(parent, t);
        wall(parent, t[0], t[2]);
        wall(parent, t[2], t[1]);
        wall(parent, h, t[3]);
    }

    private void addTengen_StrangeMap1_Leg_Right(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 11);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        towers(parent, t);
        wall(parent, t[0], t[2]);
        wall(parent, t[2], t[3]);
        wall(parent, h, t[1]);
    }

    private void addTengen_StrangeMap1_YShape(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 27, 5, 9, 14, 18, 21);
        Vector2f h = vec_2f(t[3].x(), t[2].y());
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[1], t[2]);
        wall(parent, t[2], t[4]);
        wall(parent, t[4], t[6]);
        wall(parent, t[6], t[5]);
        wall(parent, t[3], h);
    }

    private void addTengen_StrangeMap2_Bowl(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 20, 5, 7, 14, 11);
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[1], t[2]);
        wall(parent, t[2], t[3]);
        wall(parent, t[3], t[4]);
        wall(parent, t[4], t[5]);
    }

    private void addTengen_StrangeMap2_Gallows_Right(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 16, 7, 14, 9);
        var h0 = vec_2f(t[1].x(), t[2].y());
        var h1 = vec_2f(t[2].x(), t[3].y());
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[1], t[3]);
        wall(parent, h0, t[2]);
        wall(parent, t[2], t[4]);
        wall(parent, h1, t[3]);
        // fill hole
        wallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void addTengen_StrangeMap2_Gallows_Left(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 15, 2, 9, 6);
        var h0 = vec_2f(t[0].x(), t[3].y());
        var h1 = vec_2f(t[3].x(), t[2].y());
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[0], t[2]);
        wall(parent, h0, t[3]);
        wall(parent, t[3], t[4]);
        wall(parent, t[2], h1);
        // fill hole
        wallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void addTengen_StrangeMap3_Hat(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 32, 3, 29, 7, 25, 9, 13, 19, 22);
        Vector2f[] h = new Vector2f[6];
        h[0] = t[0].plus(0, TS);
        h[1] = t[1].plus(0, TS);
        h[2] = t[2].plus(TS, 0);
        h[3] = t[3].minus(TS, 0);
        h[4] = t[7].minus(TS, 0);
        h[5] = t[8].plus(TS, 0);
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[0], h[0]);
        wall(parent, t[1], h[1]);
        wall(parent, h[0], h[1]);
        wall(parent, t[2], t[3]);
        wall(parent, t[2], h[4]);
        wall(parent, h[2], t[7]);
        wall(parent, t[4], t[7]);
        wall(parent, t[4], t[6]);
        wall(parent, h[3], t[8]);
        wall(parent, t[3], h[5]);
        wall(parent, t[8], t[5]);
        wall(parent, t[5], t[9]);
    }

    private void addTengen_BigMap6_LeftTopObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 7, 9);
        Vector2f[] h = { vec_2f(t[1].x(), t[2].y()), vec_2f(t[1].x(), t[3].y()) };
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, h[0], t[2]);
        wall(parent, t[2], t[3]);
        wall(parent, t[3], h[1]);
    }

    private void addTengen_BigMap6_RightTopObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 10);
        Vector2f[] h = { vec_2f(t[0].x(), t[1].y()), vec_2f(t[0].x(), t[2].y()) };
        towers(parent, t);
        wall(parent, t[0], t[3]);
        wall(parent, h[0], t[1]);
        wall(parent, t[1], t[2]);
        wall(parent, t[2], h[1]);
    }

    private void addTengen_StrangeMap3_Mushroom(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 8, 12, 14);
        Vector2f[] h = { vec_2f(t[2].x(), t[1].y()), vec_2f(t[3].x(), t[1].y()) };
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[1], h[0]);
        wall(parent, h[0], t[2]);
        wall(parent, t[2], t[3]);
        wall(parent, t[3], h[1]);
        wall(parent, h[1], t[4]);
        wall(parent, t[4], t[5]);
        wall(parent, t[5], t[0]);
        wallAtCenter(parent, h[0].midpoint(h[1]), 2 * TS, 3 * TS);
    }

    private void addTengen_StrangeMap3_Glasses(Group parent, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6, 8, 14);
        towers(parent, t);
        wall(parent, t[0], t[1]);
        wall(parent, t[1], t[2]);
        wall(parent, t[2], t[3]);
        wall(parent, t[3], t[4]);
        wall(parent, t[5], t[0]);
        wallAtCenter(parent, t[1].midpoint(t[5]), 6 * TS, 2 * TS);
        wallAtCenter(parent, t[2].midpoint(t[4]), 6 * TS, 2 * TS);
    }

}
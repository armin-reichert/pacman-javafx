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

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
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

    public void addOShape3D(Group parent, Obstacle obstacle, boolean fillCenter) {
        switch (obstacle.encoding()) {
            // 1-tile circle
            case "dgfe" -> addTower(parent, obstacle.cornerCenter(0));

            // oval with one small side and 2 towers
            case "dcgfce", "dgbfeb" -> {
                Vector2f[] c = obstacle.uTurnCenters();
                addTower(parent, c[0]);
                addTower(parent, c[1]);
                addCastle(parent, c[0], c[1]);
            }

            // larger oval with 4 towers
            case "dcgbfceb" -> {
                var towers = new Vector2f[] {
                    obstacle.cornerCenter(0), obstacle.cornerCenter(2), obstacle.cornerCenter(4), obstacle.cornerCenter(6)
                };
                for (int i = 0; i < towers.length; ++i) {
                    addTower(parent, towers[i]);
                    int next = i < towers.length - 1 ? i + 1 : 0;
                    addCastle(parent, towers[i], towers[next]);
                }
                if (fillCenter) {
                    Vector2f center = towers[0].midpoint(towers[2]);
                    double height = towers[0].manhattanDist(towers[1]) - TS, width = towers[0].manhattanDist(towers[3]) - TS;
                    addCastleWallWithCenter(parent, center, width, height);
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
            ? new Vector2f(utc[1].x(), utc[0].y())
            : new Vector2f(utc[0].x(), utc[1].y());
        addTower(parent, utc[0]);
        addTower(parent, utc[1]);
        addTower(parent, corner);
        addCastle(parent, utc[0], corner);
        addCastle(parent, utc[1], corner);
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
                Vector2f spineTop = new Vector2f(spineX, utc[0].y());
                Vector2f spineMiddle = new Vector2f(spineX, utc[1].y());
                for (var tower : utc) {
                    addTower(parent, tower);
                }
                addTower(parent, spineTop);
                addCastle(parent, spineTop, utc[0]);
                addCastle(parent, spineMiddle, utc[1]);
                addCastle(parent, spineTop, utc[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[0].y();
                Vector2f spineMiddle = new Vector2f(utc[1].x(), spineY);
                Vector2f spineRight = new Vector2f(utc[2].x(), spineY);
                for (var tower : utc) {
                    addTower(parent, tower);
                }
                addTower(parent, spineRight);
                addCastle(parent, utc[0], spineRight);
                addCastle(parent, spineMiddle, utc[1]);
                addCastle(parent, spineRight, utc[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[2].y();
                Vector2f spineLeft = new Vector2f(utc[0].x(), spineY);
                Vector2f spineMiddle = new Vector2f(utc[1].x(), spineY);
                for (var tower : utc) {
                    addTower(parent, tower);
                }
                addTower(parent, spineLeft);
                addCastle(parent, spineLeft, utc[2]);
                addCastle(parent, spineLeft, utc[0]);
                addCastle(parent, spineMiddle, utc[1]);
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
                addTower(parent, towerNW);
                addTower(parent, towerSW);
                addTower(parent, towerSE);
                addTower(parent, towerNE);
                addCastle(parent, towerNW, towerNE);
                addCastle(parent, towerSW, towerSE);
                addCastleWallWithCenter(parent, topJoin.midpoint(bottomJoin), TS, topJoin.manhattanDist(bottomJoin));
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
                addTower(parent, towerNW);
                addTower(parent, towerSW);
                addTower(parent, towerSE);
                addTower(parent, towerNE);
                addCastleWallWithCenter(parent, leftJoin, TS, towerNW.manhattanDist(towerSW));
                addCastleWallWithCenter(parent, rightJoin, TS, towerNE.manhattanDist(towerSE));
                addCastleWallWithCenter(parent, center, leftJoin.manhattanDist(rightJoin), TS);
            }
        }
    }

    public void addCross3D(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        for (Vector2f tower : utc) {
            addTower(parent, tower);
        }
        addCastle(parent, utc[0], utc[2]);
        addCastle(parent, utc[1], utc[3]);
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
            addCastleWallWithCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addCastleWallWithCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 2 && uti[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            addCastleWallWithCenter(parent, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addCastleWallWithCenter(parent, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            addCastleWallWithCenter(parent, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addCastleWallWithCenter(parent, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addCastleWallWithCenter(parent, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else if (uti[0] == 0 && uti[1] == 7) {
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

    //TODO rework and simplify
    public void addSShape3D(Group parent, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f[] utc = obstacle.uTurnCenters(); // count=2
        addTower(parent, utc[0]);
        addTower(parent, utc[1]);
        Vector2f tc0, tc1;
        if (uti[0] == 0 && uti[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            addTower(parent, tc0);
            addTower(parent, tc1);
            addCastleWallWithCenter(parent, utc[0].midpoint(tc0), utc[0].manhattanDist(tc0), TS);
            addCastleWallWithCenter(parent, utc[1].midpoint(tc1), utc[1].manhattanDist(tc1), TS);
            // vertical wall
            addCastleWallWithCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            addTower(parent, tc0);
            addTower(parent, tc1);
            addCastleWallWithCenter(parent, tc0.midpoint(utc[1]), tc0.manhattanDist(utc[1]), TS);
            addCastleWallWithCenter(parent, utc[0].midpoint(tc1), utc[0].manhattanDist(tc1), TS);
            // vertical wall
            addCastleWallWithCenter(parent, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 6 && uti[1] == 13) {
            if (utc[1].x() < utc[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                addTower(parent, tc0);
                addTower(parent, tc1);
                // horizontal tc1 - tc0
                addCastleWallWithCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWallWithCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addCastleWallWithCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                addTower(parent, tc0);
                addTower(parent, tc1);
                // horizontal tc1 - tc0
                addCastleWallWithCenter(parent, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addCastleWallWithCenter(parent, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addCastleWallWithCenter(parent, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
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
            join = new Vector2f(utc[0].x(), utc[1].y());
        }
        else if (utc[0].y() == utc[2].y() && utc[1].y() > utc[0].y()) {
            join = new Vector2f(utc[1].x(), utc[0].y());
        }
        else if (utc[0].y() == utc[1].y() && utc[2].y() < utc[0].y()) {
            join = new Vector2f(utc[2].x(), utc[0].y());
        }
        else if (utc[2].x() == utc[1].x() && utc[0].x() < utc[1].x()) {
            join = new Vector2f(utc[1].x(), utc[0].y());
        }
        else {
            Logger.error("Invalid T-shape obstacle: {}", obstacle);
            return;
        }
        addTower(parent, utc[0]);
        addTower(parent, utc[1]);
        addTower(parent, utc[2]);

        if (utc[0].x() == join.x()) {
            addCastleWallWithCenter(parent, utc[0].midpoint(join), TS, utc[0].manhattanDist(join));
        } else if (utc[0].y() == join.y()) {
            addCastleWallWithCenter(parent, utc[0].midpoint(join), utc[0].manhattanDist(join), TS);
        }
        if (utc[1].x() == join.x()) {
            addCastleWallWithCenter(parent, utc[1].midpoint(join), TS, utc[1].manhattanDist(join));
        } else if (utc[1].y() == join.y()) {
            addCastleWallWithCenter(parent, utc[1].midpoint(join), utc[1].manhattanDist(join), TS);
        }
        if (utc[2].x() == join.x()) {
            addCastleWallWithCenter(parent, utc[2].midpoint(join), TS, utc[2].manhattanDist(join));
        } else if (utc[2].y() == join.y()) {
            addCastleWallWithCenter(parent, utc[2].midpoint(join), utc[2].manhattanDist(join), TS);
        }
    }

    //TODO This handles only the normal orientation
    public void addT2RowsShape3D(Group parent, Obstacle obstacle) {
        Vector2f leg = obstacle.uTurnCenters()[0];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f cornerSE= obstacle.cornerCenter(11);
        Vector2f cornerNE = obstacle.cornerCenter(13);
        addTower(parent, leg);
        addTower(parent, cornerNW);
        addTower(parent, cornerNE);
        addTower(parent, cornerSW);
        addTower(parent, cornerSE);
        addCastle(parent, cornerNW, cornerSW);
        addCastle(parent, cornerNE, cornerSE);
        addCastle(parent, cornerNW, cornerNE);
        addCastle(parent, cornerSW, cornerSE);
        addCastle(parent, leg, new Vector2f(leg.x(), cornerSW.y()));
    }

    private void addTower(Group parent, Vector2f center) {
        parent.getChildren().add(compositeCircularWall(center, HTS));
    }

    private void addCastle(Group parent, Vector2f p, Vector2f q) {
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

            // Tengen BIG map #6, huge 62-segment obstacle on top
            case "dcgbecgbecgfcdbfcdbecgbecgfcdbfcdbfcebdcgbecfbgcebdcfbgcdbfceb" -> addTengen_BigMap8_62SegmentObstacle(parent, obstacle);

            // Tengen BIG map #8, big-bowl obstacle middle bottom
            case "dcgbecgbfcdbfcedcfbdcfbgcebgce" -> addTengen_BigMap8_BigBowl(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbfebgcdbfceb" -> addTengen_BigMap8_SeaHorseLeft(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbecfbdgbfceb" -> addTengen_BigMap8_SeaHorseRight(parent, obstacle);

            // Tengen BIG map #9, inward turned legs (left+right)
            case "dcgbfebgcdbecfbdgbfceb" -> addTengen_BigMap9_InwardLegs(parent, obstacle);

            // Tengen BIG map #11, Tour Eiffel-like
            case "dcfbdcgfcdbecgfcebgce" -> addTengen_BigMap11_TourEiffel(parent, obstacle);

            default -> addGenericObstacle3D(parent, obstacle, thickness);
        }
    }

    private void addGenericObstacle3D (Group parent, Obstacle obstacle,double thickness){
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

    // other obstacles not handled by predefined types:

    private void addTengen_BigMap1_UpsideT(Group parent, Obstacle obstacle) {
        Vector2f top = obstacle.uTurnCenters()[0];
        Vector2f cornerNW = obstacle.cornerCenter(4);
        Vector2f cornerSW = obstacle.cornerCenter(6);
        Vector2f cornerSE = obstacle.cornerCenter(8);
        Vector2f cornerNE = obstacle.cornerCenter(10);
        addTower(parent, top);
        addTower(parent, cornerNW);
        addTower(parent, cornerSW);
        addTower(parent, cornerSE);
        addTower(parent, cornerNE);
        addCastle(parent, cornerNW, cornerSW);
        addCastle(parent, cornerNE, cornerSE);
        float width = cornerNW.manhattanDist(cornerNE), height = 2 * TS;
        addCastleWallWithCenter(parent, cornerNW.midpoint(cornerSE), width, height);
        addCastle(parent, top, cornerSW.midpoint(cornerSE));
    }

    private void addTengen_BigMap2_DeskLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topL = c[0], innerBottomL = c[1], innerBottomR = c[2], topR = c[3];
        Vector2f outerBottomL = innerBottomL.minus(4 * TS, 0);
        Vector2f outerBottomR = innerBottomR.plus(4 * TS, 0);
        addTower(parent, topL);
        addTower(parent, topR);
        addTower(parent, innerBottomL);
        addTower(parent, outerBottomL);
        addTower(parent, innerBottomR);
        addTower(parent, outerBottomR);
        addCastle(parent, topL, topR);
        addCastle(parent, outerBottomL, innerBottomL);
        addCastle(parent, outerBottomR, innerBottomR);
        addCastle(parent, outerBottomL, outerBottomL.minus(0, 6 * TS));
        addCastle(parent, outerBottomR, outerBottomR.minus(0, 6 * TS));
    }

    private void addTengen_BigMap3_DoubleTOnTop(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerNE = cornerNW.plus(12 * TS, 0);
        Vector2f cornerSW = cornerNW.plus(0, TS);
        Vector2f cornerSE = cornerNE.plus(0, TS);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f bottomL = c[0], bottomR = c[1];
        addTower(parent, cornerNW);
        addTower(parent, cornerNE);
        addTower(parent, cornerSW);
        addTower(parent, cornerSE);
        addTower(parent, bottomL);
        addTower(parent, bottomR);
        addCastle(parent, cornerNW, cornerSW);
        addCastle(parent, cornerNE, cornerSE);
        addCastle(parent, cornerNW, cornerNE);
        addCastle(parent, cornerSW, cornerSE);
        addCastle(parent, bottomL, bottomL.minus(0, 3 * TS));
        addCastle(parent, bottomR, bottomR.minus(0, 3 * TS));
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

        addTower(parent, leftCornerNW);
        addTower(parent, leftCornerSW);
        addTower(parent, leftCornerNE);

        addTower(parent, rightCornerNW);
        addTower(parent, rightCornerNE);
        addTower(parent, rightCornerSE);

        addTower(parent, leftBottom);
        addTower(parent, rightBottom);

        addCastle(parent, leftCornerNW, leftCornerNE);
        addCastle(parent, leftCornerNW, leftCornerSW);
        addCastle(parent, leftCornerNE, leftBottom);

        addCastle(parent, rightCornerNW, rightCornerNE);
        addCastle(parent, rightCornerNE, rightCornerSE);
        addCastle(parent, rightCornerNW, rightBottom);

        addCastle(parent, leftBottom, rightBottom);
    }

    private void addTengen_BigMap5_DoubleFLeft(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topRight = c[3], middleRight = c[2], bottomRight = c[1];

        addTower(parent, cornerNW);
        addTower(parent, cornerSW);
        addTower(parent, topRight);
        addTower(parent, middleRight);
        addTower(parent, bottomRight);

        addCastle(parent, cornerNW, cornerSW);
        addCastle(parent, cornerNW, topRight);
        addCastle(parent, middleRight.minus(3 *TS, 0), middleRight);
        addCastle(parent, bottomRight.minus(3 *TS, 0), bottomRight);
        addCastle(parent, cornerNW, cornerSW);
    }

    private void addTengen_BigMap5_DoubleFRight(Group parent, Obstacle obstacle) {
        Vector2f cornerNE = obstacle.cornerCenter(22);
        Vector2f cornerSE = obstacle.cornerCenter(20);
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f topLeft = c[0], middleLeft = c[1], bottomLeft = c[2];

        addTower(parent, cornerNE);
        addTower(parent, cornerSE);
        addTower(parent, topLeft);
        addTower(parent, middleLeft);
        addTower(parent, bottomLeft);

        addCastle(parent, cornerNE, cornerSE);
        addCastle(parent, cornerNE, topLeft);
        addCastle(parent, middleLeft.plus(3 *TS, 0), middleLeft);
        addCastle(parent, bottomLeft.plus(3 *TS, 0), bottomLeft);
        addCastle(parent, cornerNE, cornerSE);
    }

    private void addTengen_BigMap5_PlaneLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f nose = c[4], leftWing = c[0], rightWing = c[3], leftBack = c[1], rightBack = c[2];

        addTower(parent, nose);
        addTower(parent, leftWing);
        addTower(parent, leftBack);
        addTower(parent, rightWing);
        addTower(parent, rightBack);

        addCastle(parent, nose, leftBack.midpoint(rightBack));
        addCastle(parent, leftWing, rightWing);
        addCastle(parent, leftBack, rightBack);
    }

    private void addTengen_BigMap8_62SegmentObstacle(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f legLeft = utc[0], legRight = utc[1];

        // left part
        Vector2f cornerNWLeft = obstacle.cornerCenter(0);
        Vector2f cornerNELeft = cornerNWLeft.plus(5*TS, 0);
        Vector2f cornerSWLeft = cornerNWLeft.plus(0, 4*TS);
        Vector2f cornerSELeft = cornerNELeft.plus(0, TS);
        Vector2f pLeft = cornerSWLeft.plus(2*TS, 3*TS);
        Vector2f qLeft = pLeft.plus(0, -3*TS);

        // corridor connecting left and right part
        Vector2f corridorBottomLeft = legLeft.plus(3*TS, -3*TS);
        Vector2f corridorTopLeft = corridorBottomLeft.plus(0, -7*TS);
        Vector2f corridorTopRight = corridorTopLeft.plus(6*TS, 0);
        Vector2f corridorBottomRight = corridorTopRight.plus(0, 7*TS);

        addTower(parent, cornerNWLeft);
        addTower(parent, cornerNELeft);
        addTower(parent, cornerSWLeft);
        addTower(parent, cornerSELeft);
        addTower(parent, qLeft);
        addTower(parent, pLeft);
        addCastle(parent, cornerNWLeft, cornerNELeft);
        addCastle(parent, cornerNWLeft, cornerSWLeft);
        addCastle(parent, cornerNELeft, cornerSELeft);
        addCastle(parent, cornerSWLeft, qLeft);
        addCastle(parent, pLeft, qLeft);

        addCastle(parent, qLeft, corridorBottomLeft);
        addTower(parent, legLeft);
        addCastle(parent, legLeft, pLeft.midpoint(corridorBottomLeft));
        addCastle(parent, pLeft, corridorBottomLeft);
        addTower(parent, corridorBottomLeft);
        addCastle(parent, corridorBottomLeft, corridorTopLeft);
        addTower(parent, corridorTopLeft);
        addCastle(parent, corridorTopLeft, corridorTopRight);
        addTower(parent, corridorTopRight);
        addCastle(parent, corridorTopRight, corridorBottomRight);
        addTower(parent, corridorBottomRight);
        addTower(parent, legLeft);

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
        for (int i = 0; i < p.length; ++i) {
            addTower(parent, p[i]);
            if (i + 1 < p.length) addCastle(parent, p[i], p[i+1]);
        }
    }

    private void addTengen_BigMap8_SeaHorseLeft(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f cornerSW = obstacle.cornerCenter(2);
        Vector2f foot = obstacle.cornerCenter(4);
        Vector2f nose = obstacle.cornerCenter(11);
        Vector2f cornerNE = obstacle.cornerCenter(13);
        addTower(parent, cornerNW);
        addTower(parent, cornerSW);
        addTower(parent, foot);
        addTower(parent, nose);
        addTower(parent, cornerNE);
        addCastle(parent, cornerNW, cornerSW);
        addCastle(parent, cornerNW, cornerNE);
        addCastle(parent, cornerSW, foot);
        addCastle(parent, cornerNE, nose);
        addCastle(parent, nose, nose.minus(2*TS, 0));
    }

    private void addTengen_BigMap8_SeaHorseRight(Group parent, Obstacle obstacle) {
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f foot = obstacle.cornerCenter(8);
        Vector2f nose = obstacle.cornerCenter(2);
        Vector2f cornerSE = obstacle.cornerCenter(11);
        Vector2f cornerNE = obstacle.cornerCenter(13);
        addTower(parent, cornerNW);
        addTower(parent, nose);
        addTower(parent, cornerSE);
        addTower(parent, foot);
        addTower(parent, cornerNE);
        addCastle(parent, cornerNW, nose);
        addCastle(parent, cornerNW, cornerNE);
        addCastle(parent, nose, nose.plus(2*TS, 0));
        addCastle(parent, cornerSE, cornerNE);
        addCastle(parent, cornerSE, foot);
    }

    private void addTengen_BigMap9_InwardLegs(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f toeLeft = utc[0], toeRight = utc[1];
        Vector2f cornerNW = obstacle.cornerCenter(0);
        Vector2f heelLeft = obstacle.cornerCenter(2);
        Vector2f heelRight = obstacle.cornerCenter(18);
        Vector2f cornerNE = obstacle.cornerCenter(20);
        addTower(parent, cornerNW);
        addTower(parent, cornerNE);
        addTower(parent, heelLeft);
        addTower(parent, toeLeft);
        addTower(parent, heelRight);
        addTower(parent, toeRight);
        addCastle(parent, cornerNW, cornerNE);
        addCastle(parent, cornerNW, heelLeft);
        addCastle(parent, heelLeft, toeLeft);
        addCastle(parent, cornerNE, heelRight);
        addCastle(parent, heelRight, toeRight);
    }

    private void addTengen_BigMap11_TourEiffel(Group parent, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        Vector2f baseLeft = utc[0], baseRight = utc[1], top = utc[2];
        Vector2f platformLeft = obstacle.cornerCenter(4);
        Vector2f platformRight = obstacle.cornerCenter(16);
        Vector2f topBase = new Vector2f(top.x(), platformLeft.y());
        addTower(parent, top);
        addTower(parent, platformLeft);
        addTower(parent, platformRight);
        addTower(parent, baseLeft);
        addTower(parent, baseRight);
        addCastle(parent, top, topBase);
        addCastle(parent, platformLeft, platformRight);
        addCastle(parent, platformLeft, baseLeft);
        addCastle(parent, platformRight, baseRight);
    }
}
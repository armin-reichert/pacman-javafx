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
            }
            case "dcgfce", "dgbfeb" -> { // oval with one small side (2 U-turns)
                Vector2f[] c = obstacle.uTurnCenters();
                addTower(parent, c[0]);
                addTower(parent, c[1]);
                addCastleWall(parent, c[0], c[1]);
            }
            case "dcgbfceb" -> { // oval without U-turns
                var towers = new Vector2f[] {
                    obstacle.cornerCenter(0), obstacle.cornerCenter(2), obstacle.cornerCenter(4), obstacle.cornerCenter(6)
                };
                for (int i = 0; i < towers.length; ++i) {
                    addTower(parent, towers[i]);
                    int next = i < towers.length - 1 ? i + 1 : 0;
                    addCastleWall(parent, towers[i], towers[next]);
                }
                if (fillCenter) {
                    Vector2f center = towers[0].midpoint(towers[2]);
                    double height = towers[0].manhattanDist(towers[1]) - TS, width = towers[0].manhattanDist(towers[3]) - TS;
                    addCastleWallWithCenter(parent, center, width, height);
                }
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
        addCastleWall(parent, c[0], corner);
        addCastleWall(parent, c[1], corner);
    }

    public void addFShape3D(Group parent, Obstacle obstacle) {
        String encoding = obstacle.encoding();
        Vector2f[] c = obstacle.uTurnCenters();
        switch (encoding) {
            case "dcgfcdbfebgdbfeb",
                 "dgbefbdgbecgfceb",
                 "dcgfcdbfebgcdbfeb",
                 "dgbecfbdgbecgfceb"-> {
                Arrays.sort(c, (p, q) -> Float.compare(p.y(), q.y()));
                float spineX = c[2].x();
                Vector2f spineTop = new Vector2f(spineX, c[0].y());
                Vector2f spineMiddle = new Vector2f(spineX, c[1].y());
                for (var tower : c) {
                    addTower(parent, tower);
                }
                addTower(parent, spineTop);
                addCastleWall(parent, spineTop, c[0]);
                addCastleWall(parent, spineMiddle, c[1]);
                addCastleWall(parent, spineTop, c[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(c, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = c[0].y();
                Vector2f spineMiddle = new Vector2f(c[1].x(), spineY);
                Vector2f spineRight = new Vector2f(c[2].x(), spineY);
                for (var tower : c) {
                    addTower(parent, tower);
                }
                addTower(parent, spineRight);
                addCastleWall(parent, c[0], spineRight);
                addCastleWall(parent, spineMiddle, c[1]);
                addCastleWall(parent, spineRight, c[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(c, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = c[2].y();
                Vector2f spineLeft = new Vector2f(c[0].x(), spineY);
                Vector2f spineMiddle = new Vector2f(c[1].x(), spineY);
                for (var tower : c) {
                    addTower(parent, tower);
                }
                addTower(parent, spineLeft);
                addCastleWall(parent, spineLeft, c[2]);
                addCastleWall(parent, spineLeft, c[0]);
                addCastleWall(parent, spineMiddle, c[1]);
            }
        }
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
                addCastleWall(parent, towerNW, towerNE);
                addCastleWall(parent, towerSW, towerSE);
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
        addCastleWall(parent, c[0], c[2]);
        addCastleWall(parent, c[1], c[3]);
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
        addCastleWall(parent, cornerNW, cornerSW);
        addCastleWall(parent, cornerNE, cornerSE);
        addCastleWall(parent, cornerNW, cornerNE);
        addCastleWall(parent, cornerSW, cornerSE);
        addCastleWall(parent, leg, new Vector2f(leg.x(), cornerSW.y()));
    }

    private void addTower(Group parent, Vector2f center) {
        parent.getChildren().add(compositeCircularWall(center, HTS));
    }

    private void addCastleWall(Group parent, Vector2f p, Vector2f q) {
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
            // Tengen BIG map #8, big-bowl obstacle middle bottom
            case "dcgbecgbfcdbfcedcfbdcfbgcebgce" -> addTengen_BigMap8_BigBowl(parent, obstacle);
            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbfebgcdbfceb" -> addTengen_BigMap8_SeaHorseLeft(parent, obstacle);
            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbecfbdgbfceb" -> addTengen_BigMap8_SeaHorseRight(parent, obstacle);
            // Tengen BIG map #9, inward turned legs (left+right)
            case "dcgbfebgcdbecfbdgbfceb" -> addTengen_BigMap9_InwardLegs(parent, obstacle);

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
        addCastleWall(parent, cornerNW, cornerSW);
        addCastleWall(parent, cornerNE, cornerSE);
        float width = cornerNW.manhattanDist(cornerNE), height = 2 * TS;
        addCastleWallWithCenter(parent, cornerNW.midpoint(cornerSE), width, height);
        addCastleWall(parent, top, cornerSW.midpoint(cornerSE));
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
        addCastleWall(parent, topL, topR);
        addCastleWall(parent, outerBottomL, innerBottomL);
        addCastleWall(parent, outerBottomR, innerBottomR);
        addCastleWall(parent, outerBottomL, outerBottomL.minus(0, 6 * TS));
        addCastleWall(parent, outerBottomR, outerBottomR.minus(0, 6 * TS));
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
        addCastleWall(parent, cornerNW, cornerSW);
        addCastleWall(parent, cornerNE, cornerSE);
        addCastleWall(parent, cornerNW, cornerNE);
        addCastleWall(parent, cornerSW, cornerSE);
        addCastleWall(parent, bottomL, bottomL.minus(0, 3 * TS));
        addCastleWall(parent, bottomR, bottomR.minus(0, 3 * TS));
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

        addCastleWall(parent, leftCornerNW, leftCornerNE);
        addCastleWall(parent, leftCornerNW, leftCornerSW);
        addCastleWall(parent, leftCornerNE, leftBottom);

        addCastleWall(parent, rightCornerNW, rightCornerNE);
        addCastleWall(parent, rightCornerNE, rightCornerSE);
        addCastleWall(parent, rightCornerNW, rightBottom);

        addCastleWall(parent, leftBottom, rightBottom);
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

        addCastleWall(parent, cornerNW, cornerSW);
        addCastleWall(parent, cornerNW, topRight);
        addCastleWall(parent, middleRight.minus(3 *TS, 0), middleRight);
        addCastleWall(parent, bottomRight.minus(3 *TS, 0), bottomRight);
        addCastleWall(parent, cornerNW, cornerSW);
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

        addCastleWall(parent, cornerNE, cornerSE);
        addCastleWall(parent, cornerNE, topLeft);
        addCastleWall(parent, middleLeft.plus(3 *TS, 0), middleLeft);
        addCastleWall(parent, bottomLeft.plus(3 *TS, 0), bottomLeft);
        addCastleWall(parent, cornerNE, cornerSE);
    }

    private void addTengen_BigMap5_PlaneLike(Group parent, Obstacle obstacle) {
        Vector2f[] c = obstacle.uTurnCenters();
        Vector2f nose = c[4], leftWing = c[0], rightWing = c[3], leftBack = c[1], rightBack = c[2];

        addTower(parent, nose);
        addTower(parent, leftWing);
        addTower(parent, leftBack);
        addTower(parent, rightWing);
        addTower(parent, rightBack);

        addCastleWall(parent, nose, leftBack.midpoint(rightBack));
        addCastleWall(parent, leftWing, rightWing);
        addCastleWall(parent, leftBack, rightBack);
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
            if (i + 1 < p.length) addCastleWall(parent, p[i], p[i+1]);
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
        addCastleWall(parent, cornerNW, cornerSW);
        addCastleWall(parent, cornerNW, cornerNE);
        addCastleWall(parent, cornerSW, foot);
        addCastleWall(parent, cornerNE, nose);
        addCastleWall(parent, nose, nose.minus(2*TS, 0));
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
        addCastleWall(parent, cornerNW, nose);
        addCastleWall(parent, cornerNW, cornerNE);
        addCastleWall(parent, nose, nose.plus(2*TS, 0));
        addCastleWall(parent, cornerSE, cornerNE);
        addCastleWall(parent, cornerSE, foot);
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
        addCastleWall(parent, cornerNW, cornerNE);
        addCastleWall(parent, cornerNW, heelLeft);
        addCastleWall(parent, heelLeft, toeLeft);
        addCastleWall(parent, cornerNE, heelRight);
        addCastleWall(parent, heelRight, toeRight);
    }

}
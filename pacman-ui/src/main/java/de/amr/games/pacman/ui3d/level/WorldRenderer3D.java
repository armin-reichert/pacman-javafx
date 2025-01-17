/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.ObstacleType;
import de.amr.games.pacman.model.GameWorld;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.Arrays;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui2d.lib.Ufx.opaqueColor;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * A 3D printer for creating all artifacts in a 3D world.
 *
 * @author Armin Reichert
 */
public class WorldRenderer3D {

    public final static byte TAG_WALL_BASE      = 0x01;
    public final static byte TAG_WALL_TOP       = 0x02;
    public final static byte TAG_OUTER_WALL     = 0x04;
    public final static byte TAG_CORNER         = 0x08;
    public final static byte TAG_INNER_OBSTACLE = 0x10;

    public static void addTags(Node node, byte... tags) {
        if (node.getUserData() == null) {
            node.setUserData((byte) 0);
        }
        byte value = (byte) node.getUserData();
        for (byte tag : tags) {
            value |= tag;
        }
        node.setUserData(value);
    }

    public static boolean isTagged(Node node, byte tag) {
        if (node.getUserData() != null) {
            byte value = (byte) node.getUserData();
            return (value & tag) != 0;
        }
        return false;
    }

    protected static final int CYLINDER_DIVISIONS = 24;

    protected PhongMaterial wallBaseMaterial = new PhongMaterial();
    protected PhongMaterial wallTopMaterial = new PhongMaterial();
    protected PhongMaterial cornerMaterial = new PhongMaterial();

    protected DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(1.0);
    protected float wallTopHeight;
    protected float wallThickness = 1;
    protected boolean oShapeFilled = true;

    public void setCornerMaterial(PhongMaterial material) {
        this.cornerMaterial = material;
    }

    public void setWallBaseMaterial(PhongMaterial material) {
        wallBaseMaterial = material;
    }

    public void setWallTopMaterial(PhongMaterial material) {
        wallTopMaterial = material;
    }

    public void setWallBaseHeightProperty(DoubleProperty py) {
        wallBaseHeightPy = py;
    }

    public void setWallTopHeight(float height) {
        wallTopHeight = height;
    }

    public void setWallThickness(float wallThickness) {
        this.wallThickness = wallThickness;
    }

    public void setOShapeFilled(boolean value) {
        this.oShapeFilled = value;
    }

    /**
     * Analyzes the given obstacle by its encoding and creates a 3D representation consisting
     * only of (groups of) Cylinder and Box primitives. If the obstacle cannot be identified,
     * a generic 3D representation is created.
     *
     * @param parent the group where the 3D shapes are added to
     * @param obstacle an obstacle
     */
    public void renderObstacle3D(Group parent, Obstacle obstacle) {
        ObstacleType type = obstacle.computeType();
        if (type == ObstacleType.ANY) {
            addUncategorizedObstacle3D(parent, obstacle);
            return;
        }
        // each obstacle has its own group
        Group og = new Group();
        switch (type) {
            case CROSS ->                 render_Cross(og, obstacle);
            case F ->                     render_F(og, obstacle);
            case GAMMA_SPIKED ->          render_Gamma_Spiked(og, obstacle);
            case GAMMA_SPIKED_MIRRORED -> render_Gamma_Spiked_Mirrored(og, obstacle);
            case H ->                     render_H(og, obstacle);
            case L ->                     render_L(og, obstacle);
            case L_SPIKED ->              render_L_Spiked(og, obstacle);
            case L_SPIKED_MIRRORED ->     render_L_Spiked_Mirrored(og, obstacle);
            case LL ->                    render_LL(og, obstacle);
            case LL_MIRRORED ->           render_LL_Mirrored(og, obstacle);
            case LLL ->                   render_LLL(og, obstacle);
            case LLL_MIRRORED ->          render_LLL_Mirrored(og, obstacle);
            case O ->                     render_O(og, obstacle);
            case OPEN_SQUARE_SE ->        render_OpenSquare_SE(og, obstacle);
            case OPEN_SQUARE_SW ->        render_OpenSquare_SW(og, obstacle);
            case OPEN_SQUARE_NE ->        render_OpenSquare_NE(og, obstacle);
            case OPEN_SQUARE_NW ->        render_OpenSquare_NW(og, obstacle);
            case S ->                     render_S(og, obstacle);
            case SPACESHIP_DOWN ->        render_Spaceship_Down(og, obstacle);
            case SPACESHIP_UP ->          render_Spaceship_Up(og, obstacle);
            case SPACESHIP_LEFT ->        render_Spaceship_Left(og, obstacle);
            case SPACESHIP_RIGHT ->       render_Spaceship_Right(og, obstacle);
            case T ->                     render_T(og, obstacle);
            case T_TWO_ROWS ->            render_T_TwoRows(og, obstacle);
            case U ->                     render_U(og, obstacle);

            //TODO these belong elsewhere
            case JUNIOR_4_LEFT_OF_HOUSE ->  render_Junior_4_LeftOfHouse(og, obstacle);
            case JUNIOR_4_RIGHT_OF_HOUSE -> render_Junior_4_RightOfHouse(og, obstacle);
        }
        addTags(og, TAG_INNER_OBSTACLE);
        parent.getChildren().add(og);
    }

    public Node createWallCenteredAt(Vector2f center, double sizeX, double sizeY) {
        var base = new Box(sizeX, sizeY, wallBaseHeightPy.get());
        base.depthProperty().bind(wallBaseHeightPy);
        base.setMaterial(wallBaseMaterial);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        addTags(base, TAG_WALL_BASE);

        var top = new Box(sizeX, sizeY, wallTopHeight);
        top.setMaterial(wallTopMaterial);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        addTags(top, TAG_WALL_TOP);

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    public Node createWallBetweenTiles(Vector2i beginTile, Vector2i endTile) {
        if (beginTile.y() == endTile.y()) { // horizontal wall
            Vector2i left  = beginTile.x() < endTile.x() ? beginTile : endTile;
            Vector2i right = beginTile.x() < endTile.x() ? endTile : beginTile;
            Vector2f center = left.plus(right).scaled((float) HTS).plus(HTS, HTS);
            int length = TS * (right.x() - left.x());
            return createWallCenteredAt(center, length + wallThickness, wallThickness);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2f center = top.plus(bottom).scaled((float) HTS).plus(HTS, HTS);
            int length = TS * (bottom.y() - top.y());
            return createWallCenteredAt(center, wallThickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Node createHorizontalWallBetween(Vector2f p, Vector2f q) {
        return createWallCenteredAt(p.midpoint(q), p.manhattanDist(q) + wallThickness, wallThickness);
    }

    public Node createVerticalWallBetween(Vector2f p, Vector2f q) {
        return createWallCenteredAt(p.midpoint(q), wallThickness, p.manhattanDist(q));
    }

    public Node createCircularWall(Vector2f center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(cornerMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.heightProperty().bind(wallBaseHeightPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);
        addTags(base, TAG_WALL_BASE, TAG_CORNER);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);
        addTags(top, TAG_WALL_TOP, TAG_CORNER);

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    protected void addTowers(Group parent, Vector2f... centers) {
        for (Vector2f center : centers) {
            Node tower = createCircularWall(center, HTS);
            parent.getChildren().add(tower);
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
        parent.getChildren().add(createWallCenteredAt(center, sizeX, sizeY));
    }

    // Standard 3D obstacles

    public void render_O(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            // 1-tile circle
            case "dgfe" -> addTowers(g, obstacle.cornerCenter(0));

            // oval with one small side and 2 towers
            case "dcgfce", "dgbfeb" -> {
                Vector2f[] c = obstacle.uTurnCenters();
                addTowers(g, c);
                addWall(g, c[0], c[1]);
            }

            // larger oval with 4 "towers"
            case "dcgbfceb" -> {
                Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6);
                addTowers(g, t);
                for (int i = 0; i < t.length; ++i) {
                    int next = i < t.length - 1 ? i + 1 : 0;
                    addWall(g, t[i], t[next]);
                }
                if (oShapeFilled) {
                    double height = t[0].manhattanDist(t[1]) - TS, width = t[0].manhattanDist(t[3]) - TS;
                    addWallAtCenter(g, t[0].midpoint(t[2]), width, height);
                }
            }

            default -> Logger.error("Invalid O-shape detected: {}", obstacle);
        }
    }

    public void render_L(Group g, Obstacle obstacle) {
        Vector2f[] utc = obstacle.uTurnCenters();
        int[] d = obstacle.uTurnIndices().toArray();
        ObstacleSegment firstUTurn = obstacle.segment(d[0]);
        Vector2f corner = firstUTurn.isRoundedSECorner() || firstUTurn.isRoundedNWCorner()
            ? vec_2f(utc[1].x(), utc[0].y())
            : vec_2f(utc[0].x(), utc[1].y());
        addTowers(g, utc);
        addTowers(g, corner);
        addWall(g, utc[0], corner);
        addWall(g, utc[1], corner);
    }

    public void render_L_Spiked(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 11);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[2]);
        addWall(g, t[1], h);
        addWall(g, t[2], t[3]);
    }

    private void render_L_Spiked_Mirrored(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 7, 11);
        Vector2f h = vec_2f(t[0].x(), t[3].y());
        addTowers(g, t);
        addWall(g, t[0], t[2]);
        addWall(g, h, t[3]);
        addWall(g, t[1], t[2]);
    }

    public void render_LL(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 8, 13);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[4]);
        addWall(g, t[4], t[2]);
        addWall(g, t[2], t[3]);
    }

    public void render_LL_Mirrored(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 8, 11, 15);
        addTowers(g, t);
        addWall(g, t[0], t[4]);
        addWall(g, t[1], t[4]);
        addWall(g, t[1], t[3]);
        addWall(g, t[3], t[2]);
    }

    private void render_LLL(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 21, 6, 17, 10, 12);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[2]);
        addWall(g, t[2], t[3]);
        addWall(g, t[3], t[4]);
        addWall(g, t[4], t[5]);
        addWall(g, t[5], t[6]);
    }

    private void render_LLL_Mirrored(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 8, 12, 15, 19, 23);
        addTowers(g, t);
        addWall(g, t[0], t[6]);
        addWall(g, t[6], t[1]);
        addWall(g, t[1], t[5]);
        addWall(g, t[5], t[2]);
        addWall(g, t[2], t[4]);
        addWall(g, t[4], t[3]);
    }


    public void render_F(Group g, Obstacle obstacle) {
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
                addTowers(g, utc);
                addTowers(g, spineTop);
                addWall(g, spineTop, utc[0]);
                addWall(g, spineMiddle, utc[1]);
                addWall(g, spineTop, utc[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[0].y();
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                Vector2f spineRight = vec_2f(utc[2].x(), spineY);
                addTowers(g, utc);
                addTowers(g, spineRight);
                addWall(g, utc[0], spineRight);
                addWall(g, spineMiddle, utc[1]);
                addWall(g, spineRight, utc[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(utc, (p, q) -> Float.compare(p.x(), q.x()));
                float spineY = utc[2].y();
                Vector2f spineLeft = vec_2f(utc[0].x(), spineY);
                Vector2f spineMiddle = vec_2f(utc[1].x(), spineY);
                addTowers(g, utc);
                addTowers(g, spineLeft);
                addWall(g, spineLeft, utc[2]);
                addWall(g, spineLeft, utc[0]);
                addWall(g, spineMiddle, utc[1]);
            }
        }
    }

    private void render_Gamma_Spiked(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 14);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[2]);
        addWall(g, t[1], h);
        addWall(g, t[0], t[3]);
    }

    private void render_Gamma_Spiked_Mirrored(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 5, 10, 15);
        Vector2f h = vec_2f(t[3].x(), t[2].y());
        addTowers(g, t);
        addWall(g, t[0], t[3]);
        addWall(g, t[3], t[1]);
        addWall(g, h, t[2]);
    }

    public void render_H(Group g, Obstacle obstacle) {
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
                addTowers(g, towerNW, towerSW, towerSE, towerNE);
                addWall(g, towerNW, towerNE);
                addWall(g, towerSW, towerSE);
                addWallAtCenter(g, topJoin.midpoint(bottomJoin), TS, topJoin.manhattanDist(bottomJoin));
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
                addTowers(g, towerNW, towerSW, towerSE, towerNE);
                addWallAtCenter(g, leftJoin, TS, towerNW.manhattanDist(towerSW));
                addWallAtCenter(g, rightJoin, TS, towerNE.manhattanDist(towerSE));
                addWallAtCenter(g, center, leftJoin.manhattanDist(rightJoin), TS);
            }
        }
    }

    // Note that this can also be a cross where the horizontal parts are not aligned vertically!
    public void render_Cross(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 14);
        Vector2f[] h = { vec_2f(t[0].x(), t[3].y()), vec_2f(t[0].x(), t[1].y()) };
        addTowers(g, t);
        addWall(g, t[0], t[2]);
        addWall(g, h[0], t[3]);
        addWall(g, h[1], t[1]);
    }

    //TODO rework and simplify
    public void render_U(Group g, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f c0 = obstacle.cornerCenter(uti[0]);
        Vector2f c1 = obstacle.cornerCenter(uti[1]);
        // find centers on opposite side of U-turns
        Vector2f oc0, oc1;
        if (uti[0] == 6 && uti[1] == 13) {
            // U in normal orientation, open on top
            oc0 = obstacle.cornerCenter(4); // right leg
            oc1 = obstacle.cornerCenter(2); // left leg
            addTowers(g, c0, c1, oc0, oc1);
            addWallAtCenter(g, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addWallAtCenter(g, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addWallAtCenter(g, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 2 && uti[1] == 9) {
            // U vertically mirrored, open at bottom d[0]=left, d[1]=right
            oc0 = obstacle.cornerCenter(0); // left leg
            oc1 = obstacle.cornerCenter(12); // right leg
            addTowers(g, c0, c1, oc0, oc1);
            addWallAtCenter(g, c0.midpoint(oc0), TS, c0.manhattanDist(oc0));
            addWallAtCenter(g, c1.midpoint(oc1), TS, c1.manhattanDist(oc1));
            addWallAtCenter(g, oc0.midpoint(oc1), oc0.manhattanDist(oc1), TS);
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // U open at right side, d[0]=bottom, d[1]=top
            oc0 = obstacle.cornerCenter(2); // left bottom
            oc1 = obstacle.cornerCenter(0); // right top
            addTowers(g, c0, c1, oc0, oc1);
            addWallAtCenter(g, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addWallAtCenter(g, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addWallAtCenter(g, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else if (uti[0] == 0 && uti[1] == 7) {
            // U open at left side, d[0]=top, d[1]=bottom
            oc0 = obstacle.cornerCenter(12); // right top
            oc1 = obstacle.cornerCenter(10); // right bottom
            addTowers(g, c0, c1, oc0, oc1);
            addWallAtCenter(g, c0.midpoint(oc0), c0.manhattanDist(oc0), TS);
            addWallAtCenter(g, c1.midpoint(oc1), c1.manhattanDist(oc1), TS);
            addWallAtCenter(g, oc0.midpoint(oc1), TS, oc0.manhattanDist(oc1));
        }
        else {
            Logger.info("Invalid U-shape detected: {}", obstacle);
        }
    }

    //TODO rework and simplify
    public void render_S(Group g, Obstacle obstacle) {
        int[] uti = obstacle.uTurnIndices().toArray();
        Vector2f[] utc = obstacle.uTurnCenters(); // count=2
        addTowers(g, utc);
        Vector2f tc0, tc1;
        if (uti[0] == 0 && uti[1] == 7) {
            // S-shape mirrored vertically
            tc0 = obstacle.cornerCenter(12);
            tc1 = obstacle.cornerCenter(5);
            addTowers(g, tc0, tc1);
            addWallAtCenter(g, utc[0].midpoint(tc0), utc[0].manhattanDist(tc0), TS);
            addWallAtCenter(g, utc[1].midpoint(tc1), utc[1].manhattanDist(tc1), TS);
            // vertical wall
            addWallAtCenter(g, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 4 && uti[1] == 11) {
            // normal S-shape orientation
            tc0 = obstacle.cornerCenter(0);
            tc1 = obstacle.cornerCenter(7);
            addTowers(g, tc0, tc1);
            addWallAtCenter(g, tc0.midpoint(utc[1]), tc0.manhattanDist(utc[1]), TS);
            addWallAtCenter(g, utc[0].midpoint(tc1), utc[0].manhattanDist(tc1), TS);
            // vertical wall
            addWallAtCenter(g, tc0.midpoint(tc1), TS, tc0.manhattanDist(tc1));
        }
        else if (uti[0] == 6 && uti[1] == 13) {
            if (utc[1].x() < utc[0].x()) {
                // S-shape rotated by 90 degrees
                tc0 = obstacle.cornerCenter(9);
                tc1 = obstacle.cornerCenter(2);
                addTowers(g, tc0, tc1);
                // horizontal tc1 - tc0
                addWallAtCenter(g, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addWallAtCenter(g, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addWallAtCenter(g, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            } else {
                // S-shape mirrored and rotated by 90 degrees
                tc0 = obstacle.cornerCenter(4);
                tc1 = obstacle.cornerCenter(11);
                addTowers(g, tc0, tc1);
                // horizontal tc1 - tc0
                addWallAtCenter(g, tc1.midpoint(tc0), tc1.manhattanDist(tc0), TS);
                // vertical c1 - tc1 and tc0 - c0
                addWallAtCenter(g, utc[1].midpoint(tc1), TS, utc[1].manhattanDist(tc1));
                addWallAtCenter(g, tc0.midpoint(utc[0]), TS, tc0.manhattanDist(utc[0]));
            }
        }
        else {
            Logger.error("Invalid S-shape detected");
        }
    }

    public void render_T(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            case "dgbecgfcdbfeb" -> {
                // T in normal orientation
                Vector2f[] t = obstacle.cornerCenters(0, 5, 10);
                Vector2f h = vec_2f(t[1].x(), t[0].y());
                addTowers(g, t);
                addWall(g, t[0], t[2]);
                addWall(g, h, t[1]);
            }
            case "dcfbdgbfebgce" -> {
                // T mirrored vertically
                Vector2f[] t = obstacle.cornerCenters(0, 4, 7);
                Vector2f h = vec_2f(t[0].x(), t[1].y());
                addTowers(g, t);
                addWall(g, h, t[0]);
                addWall(g, t[1], t[2]);
            }
            case "dcgfcdbfebgce" -> {
                // T with leg pointing right
                Vector2f[] t = obstacle.cornerCenters(0, 2, 7);
                Vector2f h = vec_2f(t[0].x(), t[2].y());
                addTowers(g, t);
                addWall(g, t[0], t[1]);
                addWall(g, h, t[2]);
            }
            case "dcfbdgbecgfce" -> {
                // T with leg pointing left
                Vector2f[] t = obstacle.cornerCenters(0, 4, 9);
                Vector2f h = vec_2f(t[0].x(), t[1].y());
                addTowers(g, t);
                addWall(g, t[0], t[2]);
                addWall(g, h, t[1]);
            }
        }
    }

    //TODO This handles only the normal orientation
    public void render_T_TwoRows(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 11, 13);
        Vector2f h = vec_2f(t[2].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[0], t[4]);
        addWall(g, t[1], t[3]);
        addWall(g, t[3], t[4]);
        addWall(g, h, t[2]);
    }

    private void render_Spaceship_Right(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 7, 10, 14, 19);
        Vector2f h = vec_2f(t[2].x(), t[3].y());
        addTowers(g, t);
        addWall(g, t[0], t[4]);
        addWall(g, h,    t[3]);
        addWall(g, t[1], t[2]);
        addWall(g, t[2], t[4]);
    }

    private void render_Spaceship_Left(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 9, 11, 18);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[2]);
        addWall(g, t[2], t[3]);
        addWall(g, h, t[1]);
        addWall(g, t[0], t[4]);
    }

    private void render_Spaceship_Up(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 4, 6, 13, 16);
        Vector2f h = vec_2f(t[0].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], h);
        addWall(g, t[1], t[4]);
        addWall(g, t[1], t[2]);
        addWall(g, t[4], t[3]);
    }

    private void render_Spaceship_Down(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 11, 13);
        Vector2f h = vec_2f(t[2].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[3]);
        addWall(g, h, t[2]);
        addWall(g, t[3], t[4]);
    }

    private void render_OpenSquare_SE(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 13, 16);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[2]);
        addWall(g, t[0], t[4]);
        addWall(g, t[3], t[4]);
    }

    private void render_OpenSquare_SW(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 11, 14, 16);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[0], t[4]);
        addWall(g, t[2], t[3]);
        addWall(g, t[3], t[4]);
    }

    private void render_OpenSquare_NE(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 4, 6, 15);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[2]);
        addWall(g, t[2], t[3]);
        addWall(g, t[0], t[4]);
    }

    private void render_OpenSquare_NW(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 9, 12, 14, 16);
        addTowers(g, t);
        addWall(g, t[0], t[4]);
        addWall(g, t[4], t[3]);
        addWall(g, t[3], t[2]);
        addWall(g, t[2], t[1]);
    }

    // Junior Pac-Man maze obstacles. TODO: move elsewhere

    private void render_Junior_4_LeftOfHouse(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 2, 6, 9, 13);
        Vector2f h = vec_2f(t[4].x(), t[1].y());
        addTowers(g, t);
        addWall(g, t[0], t[1]);
        addWall(g, t[1], t[3]);
        addWall(g, t[3], t[2]);
        addWall(g, t[4], h);
    }


    private void render_Junior_4_RightOfHouse(Group g, Obstacle obstacle) {
        Vector2f[] t = obstacle.cornerCenters(0, 6, 11, 13, 18);
        Vector2f h = vec_2f(t[1].x(), t[2].y());
        addTowers(g, t);
        addWall(g, t[0], t[4]);
        addWall(g, t[4], t[2]);
        addWall(g, t[1], h);
        addWall(g, t[2], t[3]);
    }

    // fallback obstacle builder

    protected void addUncategorizedObstacle3D(Group g, Obstacle obstacle){
        int r = HTS;
        Vector2f p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2f q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = createWallCenteredAt(p.midpoint(q), wallThickness, length);
                g.getChildren().add(wall);
            } else if (segment.isHorizontalLine()) {
                Node wall = createWallCenteredAt(p.midpoint(q), length + wallThickness, wallThickness);
                g.getChildren().add(wall);
            } else if (segment.isNWCorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(g, p.plus(-r, 0), p, q);
                } else {
                    addGenericShapeCorner(g, p.plus(0, -r), q, p);
                }
            } else if (segment.isSWCorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(g, p.plus(0, r), q, p);
                } else {
                    addGenericShapeCorner(g, p.plus(-r, 0), p, q);
                }
            } else if (segment.isSECorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(g, p.plus(r, 0), p, q);
                } else {
                    addGenericShapeCorner(g, p.plus(0, r), q, p);
                }
            } else if (segment.isNECorner()) {
                if (segment.ccw()) {
                    addGenericShapeCorner(g, p.plus(0, -r), q, p);
                } else {
                    addGenericShapeCorner(g, p.plus(r, 0), p, q);
                }
            }
            p = q;
        }
    }

    protected void addGenericShapeCorner(Group parent, Vector2f corner, Vector2f horEndPoint, Vector2f vertEndPoint) {
        Node hWall = createWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), wallThickness);
        Node vWall = createWallCenteredAt(corner.midpoint(vertEndPoint), wallThickness, corner.manhattanDist(vertEndPoint));
        Node cWall = createCompositeCornerWall(corner, 0.5 * wallThickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    private Group createCompositeCornerWall(Vector2f center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(wallBaseMaterial);
        base.heightProperty().bind(wallBaseHeightPy);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5* wallTopHeight).multiply(-1));
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    public Door3D addGhostHouse(
        Group parent,
        GameWorld world,
        Color houseBaseColor, Color houseTopColor, Color doorsColor, float wallOpacity,
        DoubleProperty wallBaseHeightPy, float wallTopHeight, float wallThickness,
        BooleanProperty houseLightOnPy)
    {
        Vector2i houseSize = world.houseSize();
        setWallBaseHeightProperty(wallBaseHeightPy);
        setWallTopHeight(wallTopHeight);
        setWallThickness(wallThickness);
        setWallBaseMaterial(coloredMaterial(opaqueColor(houseBaseColor, wallOpacity)));
        setWallTopMaterial(coloredMaterial(houseTopColor));

        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = world.houseTopLeftTile().x(), xMax = xMin + tilesX - 1;
        int yMin = world.houseTopLeftTile().y(), yMax = yMin + tilesY - 1;
        Vector2i leftDoorTile = world.houseLeftDoorTile(), rightDoorTile = world.houseRightDoorTile();

        var door3D = new Door3D(leftDoorTile, rightDoorTile, doorsColor, wallBaseHeightPy.get());
        door3D.drawModePy.bind(PY_3D_DRAW_MODE);

        parent.getChildren().addAll(
            createWallBetweenTiles(vec_2i(xMin, yMin), vec_2i(leftDoorTile.x() - 1, yMin)),
            createWallBetweenTiles(vec_2i(rightDoorTile.x() + 1, yMin), vec_2i(xMax, yMin)),
            createWallBetweenTiles(vec_2i(xMin, yMin), vec_2i(xMin, yMax)),
            createWallBetweenTiles(vec_2i(xMax, yMin), vec_2i(xMax, yMax)),
            createWallBetweenTiles(vec_2i(xMin, yMax), vec_2i(xMax, yMax))
        );

        // pixel coordinates
        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;

        var light = new PointLight();
        light.lightOnProperty().bind(houseLightOnPy);
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightPy.multiply(-1));

        parent.getChildren().add(light);

        return door3D;
    }
}
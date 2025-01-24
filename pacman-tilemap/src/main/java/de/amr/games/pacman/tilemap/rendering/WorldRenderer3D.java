/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.ObstacleType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.Arrays;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * A 3D printer for creating all artifacts in a 3D world.
 *
 * @author Armin Reichert
 */
public class WorldRenderer3D {

    protected static final int CYLINDER_DIVISIONS = 24;

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

    public static PhongMaterial coloredMaterial(Color color) {
        assertNotNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    protected PhongMaterial wallBaseMaterial = new PhongMaterial();
    protected PhongMaterial wallTopMaterial = new PhongMaterial();
    protected PhongMaterial cornerMaterial = new PhongMaterial();

    protected DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(3.5);
    protected float wallTopHeight = 0.2f;
    protected float wallThickness = 2;
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
            default -> Logger.error("No 3D renderer implemented for obstacle type {}", type);
            case COIN ->                  render_Coin(og, obstacle);
            case CROSS ->                 render_Cross(og, obstacle);
            case F ->                     render_F(og, obstacle);
            case GAMMA_SPIKED ->          render_Gamma_Spiked(og, obstacle);
            case GAMMA_SPIKED_MIRRORED -> render_Gamma_Spiked_Mirrored(og, obstacle);
            case H ->                     render_H(og, obstacle);
            case I ->                     render_I(og, obstacle);
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
            case S_SMALL ->               render_S_Small(og, obstacle);
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

    public Node createWallCenteredAt(Vector2i center, double sizeX, double sizeY) {
        var base = new Box(sizeX, sizeY, wallBaseHeightPy.get());
        base.depthProperty().bind(wallBaseHeightPy);
        base.setMaterial(wallBaseMaterial);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        addTags(base, TAG_WALL_BASE);

        var top = new Box(sizeX, sizeY, wallTopHeight);
        top.setMaterial(wallTopMaterial);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
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
            Vector2i center = left.plus(right).scaled(HTS).plus(HTS, HTS);
            int length = TS * (right.x() - left.x());
            return createWallCenteredAt(center, length + wallThickness, wallThickness);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2i center = top.plus(bottom).scaled(HTS).plus(HTS, HTS);
            int length = TS * (bottom.y() - top.y());
            return createWallCenteredAt(center, wallThickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
    }

    public Node createHorizontalWallBetween(Vector2i p, Vector2i q) {
        return createWallCenteredAt(p.midpoint(q), p.manhattanDist(q) + wallThickness, wallThickness);
    }

    public Node createVerticalWallBetween(Vector2i p, Vector2i q) {
        return createWallCenteredAt(p.midpoint(q), wallThickness, p.manhattanDist(q));
    }

    public Node createCircularWall(Vector2i center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(cornerMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.heightProperty().bind(wallBaseHeightPy);
        addTags(base, TAG_WALL_BASE, TAG_CORNER);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));
        addTags(top, TAG_WALL_TOP, TAG_CORNER);

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    protected void addTowers(Group parent, Vector2i... centers) {
        for (Vector2i center : centers) {
            Node tower = createCircularWall(center, HTS);
            parent.getChildren().add(tower);
        }
    }

    protected void addWall(Group parent, Vector2i p, Vector2i q) {
        if (p.x() == q.x()) { // vertical wall
            addWallAtCenter(parent, p.midpoint(q), TS, p.manhattanDist(q));
        } else if (p.y() == q.y()) { // horizontal wall
            addWallAtCenter(parent, p.midpoint(q), p.manhattanDist(q), TS);
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p, q);
        }
    }

    protected void addWalls(Group parent, Vector2i... wallEndPoints) {
        if (wallEndPoints.length == 0 || wallEndPoints.length % 2 == 1) {
            throw new IllegalArgumentException("There must be an even, non-zero number of wall end points");
        }
        for (int i = 0; i < wallEndPoints.length / 2; ++i) {
            addWall(parent, wallEndPoints[2*i], wallEndPoints[2*i+1]);
        }
     }

    protected void addWallAtCenter(Group parent, Vector2i center, double sizeX, double sizeY) {
        parent.getChildren().add(createWallCenteredAt(center, sizeX, sizeY));
    }

    // Standard 3D obstacles

    public void render_Coin(Group g, Obstacle obstacle) {
        addTowers(g, obstacle.cornerCenter(0));
    }

    public void render_I(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 3);
        addTowers(g, t);
        addWall(g, t[0], t[1]);
    }

    public void render_O(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[2], t[2], t[3], t[3], t[0]);
        if (oShapeFilled) {
            double width = Math.abs(t[0].x() - t[2].x()) - TS;
            double height = Math.abs(t[0].y() - t[2].y()) - TS;
            addWallAtCenter(g, t[0].midpoint(t[2]), width, height);
        }
    }

    //TODO rework
    public void render_L(Group g, Obstacle obstacle) {
        int[] uti = obstacle.uTurnSegmentIndices().toArray();
        Vector2i[] t = Arrays.stream(uti).mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        ObstacleSegment s_0 = obstacle.segment(uti[0]);
        Vector2i knee = s_0.isRoundedSECorner() || s_0.isRoundedNWCorner() ? vec_2i(t[1].x(), t[0].y()) : vec_2i(t[0].x(), t[1].y());
        addTowers(g, t);
        addTowers(g, knee);
        addWalls(g, t[0], knee, knee, t[1]);
    }

    public void render_L_Spiked(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 11);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[2], t[1], h, t[2], t[3]);
    }

    private void render_L_Spiked_Mirrored(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 11);
        Vector2i h = vec_2i(t[0].x(), t[3].y());
        addTowers(g, t);
        addWalls(g, t[0], t[2], h, t[3], t[1], t[2]);
    }

    public void render_LL(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 8, 13);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[4], t[4], t[2], t[2], t[3]);
    }

    public void render_LL_Mirrored(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 8, 11, 15);
        addTowers(g, t);
        addWalls(g, t[0], t[4], t[1], t[4], t[1], t[3], t[3], t[2]);
    }

    private void render_LLL(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 21, 6, 17, 10, 12);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[2], t[2], t[3], t[3], t[4], t[4], t[5], t[5], t[6]);
    }

    private void render_LLL_Mirrored(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 8, 12, 15, 19, 23);
        addTowers(g, t);
        addWalls(g, t[0], t[6], t[6], t[1], t[1], t[5], t[5], t[2], t[2], t[4], t[4], t[3]);
    }

    //TODO rework
    public void render_F(Group g, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        switch (obstacle.encoding()) {
            case "dcgfcdbfebgdbfeb", "dgbefbdgbecgfceb", "dcgfcdbfebgcdbfeb", "dgbecfbdgbecgfceb"-> {
                Arrays.sort(u, (p, q) -> Float.compare(p.y(), q.y()));
                int spineX = u[2].x();
                Vector2i spineTop = vec_2i(spineX, u[0].y());
                Vector2i spineMiddle = vec_2i(spineX, u[1].y());
                addTowers(g, u);
                addTowers(g, spineTop);
                addWall(g, spineTop, u[0]);
                addWall(g, spineMiddle, u[1]);
                addWall(g, spineTop, u[2]);
            }
            case "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce" -> {
                Arrays.sort(u, (p, q) -> Float.compare(p.x(), q.x()));
                int spineY = u[0].y();
                Vector2i spineMiddle = vec_2i(u[1].x(), spineY);
                Vector2i spineRight = vec_2i(u[2].x(), spineY);
                addTowers(g, u);
                addTowers(g, spineRight);
                addWall(g, u[0], spineRight);
                addWall(g, spineMiddle, u[1]);
                addWall(g, spineRight, u[2]);
            }
            case "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce" -> {
                Arrays.sort(u, (p, q) -> Float.compare(p.x(), q.x()));
                int spineY = u[2].y();
                Vector2i spineLeft = vec_2i(u[0].x(), spineY);
                Vector2i spineMiddle = vec_2i(u[1].x(), spineY);
                addTowers(g, u);
                addTowers(g, spineLeft);
                addWall(g, spineLeft, u[2]);
                addWall(g, spineLeft, u[0]);
                addWall(g, spineMiddle, u[1]);
            }
        }
    }

    private void render_Gamma_Spiked(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 14);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[2], t[1], h, t[0], t[3]);
    }

    private void render_Gamma_Spiked_Mirrored(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 10, 15);
        Vector2i h = vec_2i(t[3].x(), t[2].y());
        addTowers(g, t);
        addWalls(g, t[0], t[3], t[3], t[1], h, t[2]);
    }

    //TODO make complete
    public void render_H(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {

            // little H rotated 90 degrees
            case "dgefdgbfegdfeb" -> Logger.error("Little-H obstacle creation still missing!");

            // little H in normal orientation
            case "dcgfdegfcedfge" -> Logger.error("Little-H obstacle creation still missing!");

            // H in normal orientation
            case "dcgfcdbecgfcedcfbgce" -> {
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 9, 12);
                Vector2i[] h = { vec_2i(t[0].x(), t[0].midpoint(t[1]).y()), vec_2i(t[2].x(), t[2].midpoint(t[3]).y()) };
                addTowers(g, t);
                addWalls(g, t[0], t[1], t[2], t[3], h[0], h[1]);
            }

            // H rotated by 90 degrees
            case "dgbecfbdgbfebgcdbfeb" -> {
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 7, 10, 17);
                Vector2i[] h = { vec_2i(t[0].midpoint(t[3]).x(), t[0].y()), vec_2i(t[1].midpoint(t[2]).x(), t[1].y()) };
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], h[0], h[1]);
            }
        }
    }

    // Note that this can also be a cross where the horizontal parts are not aligned vertically!
    public void render_Cross(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 14);
        Vector2i[] h = { vec_2i(t[0].x(), t[3].y()), vec_2i(t[0].x(), t[1].y()) };
        addTowers(g, t);
        addWalls(g, t[0], t[2], h[0], t[3], h[1], t[1]);
    }

    //TODO rework and simplify
    public void render_U(Group g, Obstacle obstacle) {
        int[] uti = obstacle.uTurnSegmentIndices().toArray();
        Vector2i c0 = obstacle.cornerCenter(uti[0]);
        Vector2i c1 = obstacle.cornerCenter(uti[1]);
        // find centers on opposite side of U-turns
        Vector2i oc0, oc1;
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

    public void render_S(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            case "dcfbdgbfcdbfeb" -> {
                // normal orientation
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 11);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[0], t[2]);
            }
            case "dgbecgbfebgceb" -> {
                // mirrored horizontally
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 7, 12);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[3], t[1]);
            }
            case "dcgbecgfcebgce" -> {
                // rot 90 counter-clockwise
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 9);
                addTowers(g, t);
                addWalls(g, t[0], t[1], t[1], t[3], t[2], t[3]);
            }
            case "dcfbdcgfcdbfce" -> {
                // rot 90 counter-clockwise mirrored horizontally
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 11);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[1], t[3]);
            }
        }
    }

    public void render_S_Small(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            case "dcfdgbfcdfeb" -> {
                // mini-S normal orientation
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 3, 6, 9);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[0], t[2]);
            }
            case "dgecgbfegceb" -> {
                // mini-S normal orientation mirrored horizontally
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 10);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[3], t[1]);
            }
            case "dcgbegfcebge" -> {
                // mini-S rot 90 counter-clockwise
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 5, 8);
                addTowers(g, t);
                addWalls(g, t[0], t[1], t[1], t[3], t[2], t[3]);
            }
            case "dfbdcgfdbfce" -> {
                // mini-S rot 90 counter-clockwise mirrored horizontally
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 3, 5, 9);
                addTowers(g, t);
                addWalls(g, t[0], t[3], t[1], t[2], t[1], t[3]);
            }
        }
    }

    public void render_T(Group g, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            case "dgbecgfcdbfeb" -> {
                // T in normal orientation
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 10);
                Vector2i h = vec_2i(t[1].x(), t[0].y());
                addTowers(g, t);
                addWalls(g, t[0], t[2], h, t[1]);
            }
            case "dcfbdgbfebgce" -> {
                // T mirrored vertically
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7);
                Vector2i h = vec_2i(t[0].x(), t[1].y());
                addTowers(g, t);
                addWalls(g, h, t[0], t[1], t[2]);
            }
            case "dcgfcdbfebgce" -> {
                // T with leg pointing right
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 7);
                Vector2i h = vec_2i(t[0].x(), t[2].y());
                addTowers(g, t);
                addWalls(g, t[0], t[1], h, t[2]);
            }
            case "dcfbdgbecgfce" -> {
                // T with leg pointing left
                Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9);
                Vector2i h = vec_2i(t[0].x(), t[1].y());
                addTowers(g, t);
                addWalls(g, t[0], t[2], h, t[1]);
            }
        }
    }

    //TODO This handles only the normal orientation
    public void render_T_TwoRows(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 11, 13);
        Vector2i h = vec_2i(t[2].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[0], t[4], t[1], t[3], t[3], t[4], h, t[2]);
    }

    private void render_Spaceship_Right(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 7, 10, 14, 19);
        Vector2i h = vec_2i(t[2].x(), t[3].y());
        addTowers(g, t);
        addWalls(g, t[0], t[4], h, t[3], t[1], t[2], t[2], t[4]);
    }

    private void render_Spaceship_Left(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 11, 18);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[2], t[2], t[3], h, t[1], t[0], t[4]);
    }

    private void render_Spaceship_Up(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 13, 16);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], h, t[1], t[4], t[1], t[2], t[4], t[3]);
    }

    private void render_Spaceship_Down(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 11, 13);
        Vector2i h = vec_2i(t[2].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[3], h, t[2], t[3], t[4]);
    }

    private void render_OpenSquare_SE(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 13, 16);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[2], t[0], t[4], t[3], t[4]);
    }

    private void render_OpenSquare_SW(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 11, 14, 16);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[0], t[4], t[2], t[3], t[3], t[4]);
    }

    private void render_OpenSquare_NE(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6, 15);
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[2], t[2], t[3], t[0], t[4]);
    }

    private void render_OpenSquare_NW(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 9, 12, 14, 16);
        addTowers(g, t);
        addWalls(g, t[0], t[4], t[4], t[3], t[3], t[2], t[2], t[1]);
    }

    // Junior Pac-Man maze obstacles. TODO: move elsewhere

    private void render_Junior_4_LeftOfHouse(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 9, 13);
        Vector2i h = vec_2i(t[4].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[1], t[1], t[3], t[3], t[2], t[4], h);
    }

    private void render_Junior_4_RightOfHouse(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 6, 11, 13, 18);
        Vector2i h = vec_2i(t[1].x(), t[2].y());
        addTowers(g, t);
        addWalls(g, t[0], t[4], t[4], t[2], t[1], h, t[2], t[3]);
    }

    // generic obstacle builder

    protected void addGenericObstacle3D(Group g, Obstacle obstacle){
        int r = HTS;
        Vector2i p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2i q = p.plus(segment.vector());
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

    protected void addGenericShapeCorner(Group parent, Vector2i corner, Vector2i horEndPoint, Vector2i vertEndPoint) {
        Node hWall = createWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), wallThickness);
        Node vWall = createWallCenteredAt(corner.midpoint(vertEndPoint), wallThickness, corner.manhattanDist(vertEndPoint));
        Node cWall = createCompositeCornerWall(corner, 0.5 * wallThickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    private Group createCompositeCornerWall(Vector2i center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(wallBaseMaterial);
        base.heightProperty().bind(wallBaseHeightPy);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(wallTopMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5* wallTopHeight).multiply(-1));

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    // Tengen maps

    public void addUncategorizedObstacle3D(Group parent, Obstacle obstacle) {
        switch (obstacle.encoding()) {
            // Tengen BIG map #1, upside T at top, center
            case "dcfbdcgbfcebgce" -> render_BigMap1_UpsideT(parent, obstacle);

            // Tengen BIG map #2, large desk-like obstacle on the bottom
            case "dgbecgbfebgcdbecfbdgbfcdbfeb" -> render_BigMap2_DeskLike(parent, obstacle);

            // Tengen BIG map #3, large double-T obstacle on the top
            case "dcgbecgfcdbecgfcdbfceb" -> render_BigMap3_DoubleTOnTop(parent, obstacle);

            // Tengen BIG map #5, bowl-like obstacle on the top
            case "dcgbecgbfcdbfcebdcfbgceb" -> render_BigMap5_Bowl(parent, obstacle);

            // Tengen BIG map #5, double-F on left side
            case "dcgfcdbfebgcdbfebgcdbfeb" -> render_BigMap5_DoubleFLeft(parent, obstacle);

            // Tengen BIG map #5, double-F on right side
            case "dgbecfbdgbecfbdgbecgfceb" -> render_BigMap5_DoubleFRight(parent, obstacle);

            // Tengen BIG map #5, plane-like obstacle middle bottom
            case "dcfbdgbecfbdgbfebgcdbfebgce" -> render_BigMap5_PlaneLike(parent, obstacle);

            // Tengen BIG map #6, obstacle left-top
            case "dcgfcdbfcebgce" -> render_BigMap6_LeftTopObstacle(parent, obstacle);

            // Tengen BIG map #6, obstacle right-top
            case "dcfbdcgbecgfce" -> render_BigMap6_RightTopObstacle(parent, obstacle);

            // Tengen BIG map #8, huge 62-segment obstacle on top
            case "dcgbecgbecgfcdbfcdbecgbecgfcdbfcdbfcebdcgbecfbgcebdcfbgcdbfceb" -> render_BigMap8_62SegmentObstacle(parent, obstacle);

            // Tengen BIG map #8, big-bowl obstacle middle bottom
            case "dcgbecgbfcdbfcedcfbdcfbgcebgce" -> render_BigMap8_BigBowl(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbfebgcdbfceb" -> render_BigMap8_SeaHorseLeft(parent, obstacle);

            // Tengen BIG map #8, sea-horse obstacle left
            case "dcgbecfbdgbfceb" -> render_BigMap8_SeaHorseRight(parent, obstacle);

            // Tengen BIG map #9, inward turned legs (left+right)
            case "dcgbfebgcdbecfbdgbfceb" -> render_BigMap9_InwardLegs(parent, obstacle);

            // Tengen BIG map #10, table upside-down, at top of maze
            case "dcfbdgbfebgcedcfbgce" -> render_BigMap10_TableUpsideDown(parent, obstacle);

            // Tengen BIG map #11, Tour Eiffel-like
            case "dcfbdcgfcdbecgfcebgce" -> render_BigMap11_TourEiffel(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at left side at bottom of maze
            case "dcfbdgbfcdbfebgce" -> render_StrangeMap1_Leg_Left(parent, obstacle);

            // Tengen STRANGE map #1, leg-like obstacle at right side at bottom of maze
            case "dcfbdgbecgbfebgce" -> render_StrangeMap1_Leg_Right(parent, obstacle);

            // Tengen STRANGE map #1, Y-shaped obstacle at center at bottom of maze
            case "dgbecgbecgfcdbfcdbfebdcfbgceb" -> render_StrangeMap1_YShape(parent, obstacle);

            // Tengen STRANGE map #2, bowl-like obstacle at center at top of maze
            case "dgbecgbfcdbfebdcfbgceb" -> render_StrangeMap2_Bowl(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at left/top
            case "dcgbecgfcebgcdbfeb" -> render_StrangeMap2_Gallows_Left(parent, obstacle);

            // Tengen STRANGE map #2, gallow-like obstacle at right/top
            case "dgbecfbdcgfcdbfceb" -> render_StrangeMap2_Gallows_Right(parent, obstacle);

            // Tengen STRANGE map #3, large hat-like onstacle center/top
            case "dfbdcfbdcgfdbfcdbecgbegfcebgcebgeb" -> render_StrangeMap3_Hat(parent, obstacle);

            // Tengen STRANGE map #3, mushroom center-bottom
            case "dcgbecgbfcdbfceb" -> render_StrangeMap3_Mushroom(parent, obstacle);

            // Tengen STRANGE map #3 and #7, U-shape with stronger "legs"
            case "dcgbfcebdcfbgceb" -> render_StrangeMap3_7_StrongLeggedUShape(parent, obstacle);

            // Tengen STRANGE map #4, huge obstacle
            case "dgbecfbdcgbfcdbecgbfcebgcdbfebdgbecfbgcebgcedcfbdcfbgcdbfeb" -> render_StrangeMap4_Huge(parent, obstacle);

            // Tengen STRANGE map #7: top-left strange obstacle
            case "dcfbdgbecgbecfbdgbfcdbfeb" -> render_Strange7_TopLeft(parent, obstacle);

            // Tengen STRANGE map #7: top-right strange obstacle
            case "dgbecgbfebgcdbfcdbfebgceb" -> render_Strange7_TopRight(parent, obstacle);

            // Tengen STRANGE map #7: boots pointing left
            case "dcfbdgbfceb" -> render_Boot_PointingLeft(parent, obstacle);

            // Tengen STRANGE map #7: boots pointing left
            case "dcgbfebgceb" -> render_Boot_PointingRight(parent, obstacle);

            // STRANGE map #8: rectangle with two arms at top-right corner, left maze side
            case "dcfbdcgbfcdbfebgce" -> render_RectangleWithTwoArmsAtTopRightCorner(parent, obstacle);

            // STRANGE map #8: rectangle with two arms at top-left corner, right maze side
            case "dcfbdgbecgbfcebgce" -> render_RectangleWithTwoArmsAtTopLeftCorner(parent, obstacle);

            // STRANGE map #8: rectangle with one arm at top-right corner, left maze side
            case "dcgbfcdbfeb" -> render_RectangleWithArmAtTopRightCorner(parent, obstacle);

            // STRANGE map #8: rectangle with one arm at top-left corner, right maze side
            case "dgbecgbfceb" -> render_RectangleWithArmAtTopLeftCorner(parent, obstacle);

            // STRANGE map #9: large obstacle left maze side
            case "dcfbdgbecfbdgbecfbdgbfebgcdbfebgcdbfeb" -> render_Strange9_LargeObstacleLeft(parent, obstacle);

            // STRANGE map #9: large obstacle right maze side
            case "dgbecfbdgbecfbdgbfebgcdbfebgcdbfebgceb" -> render_Strange9_LargeObstacleRight(parent, obstacle);

            // STRANGE map #10: spaceship top, center
            case "dcfbdcgbecgfcdbfcebgceb" -> render_Strange10_SpaceShipTopCenter(parent, obstacle);

            // STRANGE map #10: island left of spaceship
            case "dcgegegfcegceb" -> render_Strange10_IslandLeftOfSpaceship(parent, obstacle);

            // STRANGE map #10: island right of spaceship
            case "dcfdcgfdfdfceb" -> render_Strange10_IslandRightOfSpaceship(parent, obstacle);

            // STRANGE map #10: left of house
            case "dcfbdgbecgfcdfceb" -> render_Strange10_RectangleWithTwoLegsAtLowerLeftEdge(parent, obstacle);

            // STRANGE map #10: right of house
            case "dcgecgfcdbfebgceb" -> render_Strange10_RectangleWithTwoLegsAtLowerRightEdge(parent, obstacle);

            // STRANGE map #10: obstacle below house
            case "dfdgbecgfcdbfegeb" -> render_Strange10_ObstacleBelowHouse(parent, obstacle);

            // STRANGE map #10: pistol-like shape pointing left
            case "dgbecgbfcegeb" -> render_Strange10_PistolPointingLeft(parent, obstacle);

            // STRANGE map #10: pistol-like shape pointing right
            case "dfdcgbfcdbfeb" -> render_Strange10_PistolPointingRight(parent, obstacle);

            // STRANGE map #12: dog-like looking left
            case "dcgbecgfcdbecgfcebgeb" -> render_Strange12_DogLookingLeft(parent, obstacle);

            // STRANGE map #12: dog-like looking right
            case "dfbdcgfcdbecgfcdbfceb" -> render_Strange12_DogLookingRight(parent, obstacle);

            // STRANGE map #14: Gamma-like with wider arms
            case "dcgbfcdbfceb" -> render_Strange14_Gamma(parent, obstacle);

            // STRANGE map #14: Gamma-like with wider arms, mirrored
            case "dcgbecgbfceb" -> render_Strange14_Gamma_Mirrored(parent, obstacle);

            // STRANGE map #15: obstacle left of house
            case "dgbecfbdgbecgbfebgceb" -> render_Strange15_LeftOfHouse(parent, obstacle);

            // STRANGE map #15: obstacle right of house
            case "dcfbdgbfcdbfebgcdbfeb" -> render_Strange15_RightOfHouse(parent, obstacle);


            // other obstacle types
            case "dcgbfcedcfbgcedcfbgce" -> render_dcgbfcedcfbgcedcfbgce(parent, obstacle);

            case "dgbecfbdgbfebgceb" -> render_dgbecfbdgbfebgceb(parent, obstacle);

            case "dcfbdgbfebgcdbfeb" -> render_dcfbdgbfebgcdbfeb(parent, obstacle);
            
            case "dcgbecgfceb" -> render_dcgbecgfceb(parent, obstacle);

            case "dcgfcdbfceb" -> render_dcgfcdbfceb(parent, obstacle);

            default -> addGenericObstacle3D(parent, obstacle);
        }
    }

    private void render_dcgfcdbfceb(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 7, 9);
        Vector2i h = vec_2i(t[0].x(), t[2].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], t[3], t[2], t[3], t[2], h);
        addWallAtCenter(parent, t[0].midpoint(t[2]), TS, TS);
    }

    private void render_dcgbecgfceb(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 9);
        Vector2i h = vec_2i(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], t[3], t[3], t[2], t[1], h);
        addWallAtCenter(parent, t[0].midpoint(h), TS, TS);
    }

    private void render_dcgbfcedcfbgcedcfbgce(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6, 13);
        Vector2i h = vec_2i(t[4].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], t[2], t[3], t[4], h);
    }

    private void render_dgbecfbdgbfebgceb(Group g, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 7, 10, 15);
        Vector2i h = vec_2i(t[3].x(), t[1].y());
        addTowers(g, t);
        addWalls(g, t[0], t[3], t[3], h, t[1], t[2]);
    }

    private void render_dcfbdgbfebgcdbfeb(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 14);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[3], t[0], h, t[1], t[2]);
    }

    private void render_Boot_PointingLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 9);
        Vector2i h0 = vec_2i(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], h0, t[1], t[2], t[2], t[3], t[3], t[0]);
        addWallAtCenter(parent, t[0].midpoint(t[2]), t[0].manhattanDist(t[3]), t[3].manhattanDist(t[2]));
    }

    private void render_Boot_PointingRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 9);
        Vector2i h0 = vec_2i(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], h0, t[3], t[3], t[0]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[0].manhattanDist(t[3]), t[0].manhattanDist(t[1]));
    }

    private void render_BigMap1_UpsideT(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 8, 10);
        addTowers(parent, t);
        addWalls(parent, t[1], t[2], t[3], t[4], t[0], t[2].midpoint(t[3]));
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[1].manhattanDist(t[4]), 2 * TS);
    }

    private void render_BigMap2_DeskLike(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i topL = u[0], innerBottomL = u[1], innerBottomR = u[2], topR = u[3];
        Vector2i outerBottomL = innerBottomL.minus(4 * TS, 0);
        Vector2i outerBottomR = innerBottomR.plus(4 * TS, 0);
        addTowers(parent, topL, topR, innerBottomL, outerBottomL, innerBottomR, outerBottomR);
        addWall(parent, topL, topR);
        addWall(parent, outerBottomL, innerBottomL);
        addWall(parent, outerBottomR, innerBottomR);
        addWall(parent, outerBottomL, outerBottomL.minus(0, 6 * TS));
        addWall(parent, outerBottomR, outerBottomR.minus(0, 6 * TS));
    }

    private void render_BigMap3_DoubleTOnTop(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i cornerNW = obstacle.cornerCenter(0);
        Vector2i cornerNE = cornerNW.plus(12 * TS, 0);
        Vector2i cornerSW = cornerNW.plus(0, TS);
        Vector2i cornerSE = cornerNE.plus(0, TS);
        Vector2i bottomL = u[0], bottomR = u[1];
        addTowers(parent, cornerNW, cornerNE, cornerSW, cornerSE, bottomL, bottomR);
        addWall(parent, cornerNW, cornerSW);
        addWall(parent, cornerNE, cornerSE);
        addWall(parent, cornerNW, cornerNE);
        addWall(parent, cornerSW, cornerSE);
        addWall(parent, bottomL, bottomL.minus(0, 3 * TS));
        addWall(parent, bottomR, bottomR.minus(0, 3 * TS));
    }

    private void render_BigMap5_Bowl(Group parent, Obstacle obstacle) {
        Vector2i leftCornerNW = obstacle.cornerCenter(0);
        Vector2i leftCornerSW = leftCornerNW.plus(0, TS);
        Vector2i leftCornerNE = leftCornerNW.plus(2 * TS, 0);
        Vector2i rightCornerNW = leftCornerNW.plus(8 * TS, 0);
        Vector2i rightCornerNE = rightCornerNW.plus(2 * TS, 0);
        Vector2i rightCornerSE = rightCornerNE.plus(0, TS);
        Vector2i leftBottom = leftCornerNW.plus(2 * TS, 4 * TS);
        Vector2i rightBottom = leftBottom.plus(6 * TS, 0);
        addTowers(parent, leftCornerNW, leftCornerSW, leftCornerNE, rightCornerNW, rightCornerNE, rightCornerSE, leftBottom, rightBottom);
        addWall(parent, leftCornerNW, leftCornerNE);
        addWall(parent, leftCornerNW, leftCornerSW);
        addWall(parent, leftCornerNE, leftBottom);
        addWall(parent, rightCornerNW, rightCornerNE);
        addWall(parent, rightCornerNE, rightCornerSE);
        addWall(parent, rightCornerNW, rightBottom);
        addWall(parent, leftBottom, rightBottom);
    }

    private void render_BigMap5_DoubleFLeft(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i cornerNW = obstacle.cornerCenter(0);
        Vector2i cornerSW = obstacle.cornerCenter(2);
        Vector2i topRight = u[3], middleRight = u[2], bottomRight = u[1];
        addTowers(parent, cornerNW, cornerSW, topRight, middleRight, bottomRight);
        addWall(parent, cornerNW, cornerSW);
        addWall(parent, cornerNW, topRight);
        addWall(parent, middleRight.minus(3 * TS, 0), middleRight);
        addWall(parent, bottomRight.minus(3 * TS, 0), bottomRight);
        addWall(parent, cornerNW, cornerSW);
    }

    private void render_BigMap5_DoubleFRight(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i cornerNE = obstacle.cornerCenter(22);
        Vector2i cornerSE = obstacle.cornerCenter(20);
        Vector2i topLeft = u[0], middleLeft = u[1], bottomLeft = u[2];
        addTowers(parent, cornerNE, cornerSE, topLeft, middleLeft, bottomLeft);
        addWall(parent, cornerNE, cornerSE);
        addWall(parent, cornerNE, topLeft);
        addWall(parent, middleLeft.plus(3 *TS, 0), middleLeft);
        addWall(parent, bottomLeft.plus(3 *TS, 0), bottomLeft);
        addWall(parent, cornerNE, cornerSE);
    }

    private void render_BigMap5_PlaneLike(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i nose = u[4], leftWing = u[0], rightWing = u[3], leftBack = u[1], rightBack = u[2];
        addTowers(parent, nose, leftWing, leftBack, rightWing, rightBack);
        addWall(parent, nose, leftBack.midpoint(rightBack));
        addWall(parent, leftWing, rightWing);
        addWall(parent, leftBack, rightBack);
    }

    private void render_BigMap8_62SegmentObstacle(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 60, 58, 2, 6, 10, 15, 50, 48, 21, 25, 30, 34, 36, 38, 40);
        Vector2i[] h = new Vector2i[8];
        h[0] = vec_2i(t[4].x(),  t[2].y());
        h[1] = vec_2i(t[4].x(),  t[3].y());
        h[2] = vec_2i(t[5].x(),  t[4].y());
        h[3] = vec_2i(t[10].x(), t[9].y());
        h[4] = vec_2i(t[11].x(), t[12].y());
        h[5] = vec_2i(t[11].x(), t[15].y());
        h[6] = vec_2i(t[4].x(),  t[0].y());
        h[7] = vec_2i(t[11].x(), t[13].y());

        addTowers(parent, t);
        addWall(parent, t[0], t[1]);
        addWall(parent, h[0], h[6]);
        addWall(parent, t[0], t[3]);
        addWall(parent, t[3], h[1]);
        addWall(parent, t[1], t[2]);
        addWall(parent, t[2], h[0]);
        addWall(parent, h[0], h[1]);
        addWall(parent, h[1], t[4]);
        addWall(parent, t[4], t[6]);
        addWall(parent, h[2], t[5]);
        addWall(parent, t[6], t[7]);
        addWall(parent, t[7], t[8]);
        addWall(parent, t[8], t[9]);
        addWall(parent, t[9], t[11]);
        addWall(parent, h[3], t[10]);
        addWall(parent, t[11], h[4]);
        addWall(parent, h[4], t[12]);
        addWall(parent, h[4], h[5]);
        addWall(parent, h[5], t[15]);
        addWall(parent, h[5], h[7]);
        addWall(parent, t[15], t[14]);
        addWall(parent, t[14], t[13]);
        addWall(parent, t[13], t[12]);
        addWall(parent, t[12], h[4]);
    }

    private void render_BigMap8_BigBowl(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i[] p = new Vector2i[8];
        p[0] = u[1];
        p[1] = p[0].plus(0, 2 * TS);
        p[2] = p[1].plus(3 * TS, 0);
        p[3] = p[2].plus(0, 6 * TS);
        p[4] = p[3].plus(12 * TS, 0);
        p[5] = p[4].minus(0, 6 * TS);
        p[6] = p[5].plus(3 * TS, 0);
        p[7] = p[6].minus(0, 2 * TS);
        addTowers(parent, p);
        for (int i = 0; i < p.length; ++i) {
            if (i + 1 < p.length) addWall(parent, p[i], p[i+1]);
        }
    }

    private void render_BigMap8_SeaHorseLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 11, 13);
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], t[4], t[1], t[2], t[4], t[3], t[3], t[3].minus(2*TS, 0));
    }

    private void render_BigMap8_SeaHorseRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 8, 2, 11, 13);
        addTowers(parent, t);
        addWalls(parent, t[0], t[2], t[0], t[4], t[3], t[4], t[3], t[1], t[2], t[2].plus(2*TS, 0));
    }

    private void render_BigMap9_InwardLegs(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i toeLeft = u[0], toeRight = u[1];
        Vector2i cornerNW = obstacle.cornerCenter(0);
        Vector2i heelLeft = obstacle.cornerCenter(2);
        Vector2i heelRight = obstacle.cornerCenter(18);
        Vector2i cornerNE = obstacle.cornerCenter(20);
        addTowers(parent, cornerNW, cornerNE, heelLeft, toeLeft, heelRight, toeRight);
        addWall(parent, cornerNW, cornerNE);
        addWall(parent, cornerNW, heelLeft);
        addWall(parent, heelLeft, toeLeft);
        addWall(parent, cornerNE, heelRight);
        addWall(parent, heelRight, toeRight);
    }

    private void render_BigMap10_TableUpsideDown(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 8, 12);
        Vector2i h0 = vec_2i(t[0].x(), t[1].y()), h1 = vec_2i(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[1], t[2], t[0], h0, t[3], h1);
    }

    private void render_BigMap11_TourEiffel(Group parent, Obstacle obstacle) {
        Vector2i[] u = obstacle.uTurnSegmentIndices().mapToObj(obstacle::cornerCenter).toArray(Vector2i[]::new);
        Vector2i baseLeft = u[0], baseRight = u[1], top = u[2];
        Vector2i platformLeft = obstacle.cornerCenter(4);
        Vector2i platformRight = obstacle.cornerCenter(16);
        Vector2i topBase = vec_2i(top.x(), platformLeft.y());
        addTowers(parent, top, platformLeft, platformRight, baseLeft, baseRight);
        addWall(parent, top, topBase);
        addWall(parent, platformLeft, platformRight);
        addWall(parent, platformLeft, baseLeft);
        addWall(parent, platformRight, baseRight);
    }

    private void render_StrangeMap1_Leg_Left(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 11);
        Vector2i h = vec_2i(t[0].x(), t[3].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[2], t[2], t[1], h, t[3]);
    }

    private void render_StrangeMap1_Leg_Right(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 11);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[2], t[2], t[3], h, t[1]);
    }

    private void render_StrangeMap1_YShape(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 27, 5, 9, 14, 18, 21);
        Vector2i h = vec_2i(t[3].x(), t[2].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], t[2], t[4], t[4], t[6], t[6], t[5], t[3], h);
    }

    private void render_StrangeMap2_Bowl(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 20, 5, 7, 14, 11);
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], t[2], t[3], t[3], t[4], t[4], t[5]);
    }

    private void render_StrangeMap2_Gallows_Right(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 16, 7, 14, 9);
        var h0 = vec_2i(t[1].x(), t[2].y());
        var h1 = vec_2i(t[2].x(), t[3].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[3], h0, t[2], t[2], t[4], h1, t[3]);
        // fill hole
        addWallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void render_StrangeMap2_Gallows_Left(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 15, 2, 9, 6);
        var h0 = vec_2i(t[0].x(), t[3].y());
        var h1 = vec_2i(t[3].x(), t[2].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], t[2], h0, t[3], t[3], t[4], t[2], h1);
        // fill hole
        addWallAtCenter(parent, h0.midpoint(h1), 2*TS, 3*TS);
    }

    private void render_StrangeMap3_Hat(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 32, 3, 29, 7, 25, 9, 13, 19, 22);
        Vector2i[] h = new Vector2i[6];
        h[0] = t[0].plus(0, TS);
        h[1] = t[1].plus(0, TS);
        h[2] = t[2].plus(TS, 0);
        h[3] = t[3].minus(TS, 0);
        h[4] = t[7].minus(TS, 0);
        h[5] = t[8].plus(TS, 0);
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], h[0], t[1], h[1], h[0], h[1], t[2], t[3], t[2], h[4], h[2], t[7],
            t[4], t[7], t[4], t[6], h[3], t[8], t[3], h[5], t[8], t[5], t[5], t[9]);
    }

    private void render_BigMap6_LeftTopObstacle(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 7, 9);
        Vector2i[] h = { vec_2i(t[1].x(), t[2].y()), vec_2i(t[1].x(), t[3].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], h[0], t[2], t[2], t[3], t[3], h[1]);
    }

    private void render_BigMap6_RightTopObstacle(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 10);
        Vector2i[] h = { vec_2i(t[0].x(), t[1].y()), vec_2i(t[0].x(), t[2].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[3], h[0], t[1], t[1], t[2], t[2], h[1]);
    }

    private void render_StrangeMap3_Mushroom(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 8, 12, 14);
        Vector2i[] h = { vec_2i(t[2].x(), t[1].y()), vec_2i(t[3].x(), t[1].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], h[0], h[0], t[2], t[2], t[3], t[3], h[1], h[1], t[4], t[4], t[5], t[5], t[0]);
        addWallAtCenter(parent, h[0].midpoint(h[1]), 2 * TS, 3 * TS);
    }

    private void render_StrangeMap3_7_StrongLeggedUShape(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6, 8, 14);
        Vector2i[] h = { vec_2i(t[5].x(), t[1].y()), vec_2i(t[4].x(), t[2].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], t[2], t[3], t[3], t[4], t[4], h[1], t[5], t[0], t[5], h[0]);
        addWallAtCenter(parent, t[1].midpoint(t[5]), t[0].manhattanDist(t[5]), t[0].manhattanDist(t[1]));
        addWallAtCenter(parent, t[2].midpoint(t[4]), t[3].manhattanDist(t[4]), t[2].manhattanDist(t[3]));
    }

    private void render_StrangeMap4_Huge(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 57, 7, 9, 11, 48, 39, 43, 17, 19, 21, 27, 30);
        Vector2i[] h = {
                t[0].plus(2 * TS, 0),
                vec_2i(t[0].x() + 2 * TS, t[2].y()),
                vec_2i(t[7].x(), t[5].y()),
                t[10].minus(2 * TS, 0),
                t[11].minus(2 * TS, 0)
        };
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], h[0], h[1], h[1], t[2], t[2], t[3], t[3], t[4], t[4], t[5], t[5], t[6], h[2],
            t[7], t[6], t[8], t[8], t[9], t[9], t[10], t[10], h[3], h[3], h[4], t[11], t[12]);
        // fill holes
        addWallAtCenter(parent, h[1].midpoint(t[3]), 3*TS, 3*TS);
        addWallAtCenter(parent, h[3].midpoint(t[9]), 3*TS, 3*TS);
    }

    private void render_Strange7_TopLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 15, 18, 22);
        Vector2i[] h = { vec_2i(t[0].x(), t[1].y()), vec_2i(t[2].x(), t[1].y()), vec_2i(t[0].x(), t[2].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[1], h[0], t[2], h[2], t[3], t[4], t[0], t[5]);
        addWallAtCenter(parent, h[1].midpoint(h[2]), 3*TS, 2*TS);
    }

    private void render_Strange7_TopRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 7, 14, 18, 23);
        Vector2i[] h = { vec_2i(t[1].x(), t[4].y()), vec_2i(t[3].x(), t[4].y()), vec_2i(t[1].x(), t[3].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[5], t[4], h[0], t[3], h[2], t[1], t[2], t[1], t[5]);
        addWallAtCenter(parent, h[1].midpoint(h[2]), 3*TS, 2*TS);
    }

    private void render_RectangleWithTwoArmsAtTopRightCorner(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 8, 12);
        addTowers(parent, t);
        addWalls(parent, t[1], t[2], t[1], t[4], t[2], t[3], t[3], t[0]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[2].manhattanDist(t[3]), t[1].manhattanDist(t[2]));
    }

    private void render_RectangleWithTwoArmsAtTopLeftCorner(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 11, 13);
        addTowers(parent, t);
        addWalls(parent, t[0], t[2], t[1], t[4], t[4], t[3], t[3], t[2]);
        addWallAtCenter(parent, t[2].midpoint(t[4]), t[2].manhattanDist(t[3]), t[3].manhattanDist(t[4]));
    }

    private void render_RectangleWithArmAtTopRightCorner(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 8);
        Vector2i h = vec_2i(t[2].x(), t[0].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[3], t[0], t[1], t[1], t[2], t[2], h);
        addWallAtCenter(parent, t[0].midpoint(t[2]), t[1].manhattanDist(t[2]), t[0].manhattanDist(t[1]));
    }

    private void render_RectangleWithArmAtTopLeftCorner(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 7, 9);
        Vector2i h = vec_2i(t[1].x(), t[0].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[3], h, t[1], t[1], t[2], t[2], t[3]);
        addWallAtCenter(parent, t[1].midpoint(t[3]), t[1].manhattanDist(t[2]), t[2].manhattanDist(t[3]));
    }

    private void render_Strange9_LargeObstacleLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 11, 18, 21, 28, 35);
        Vector2i[] h = { vec_2i(t[0].x(), t[1].y()), vec_2i(t[0].x(), t[2].y()), vec_2i(t[0].x(), t[5].y()), vec_2i(t[0].x(), t[3].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[6], t[1], h[0], t[2], h[1], h[2], t[5], t[0], t[5], t[3], t[4], t[0], h[3]);
    }

    private void render_Strange9_LargeObstacleRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 7, 14, 17, 24, 31, 36);
        Vector2i[] h = { vec_2i(t[6].x(), t[5].y()), vec_2i(t[6].x(), t[4].y()), vec_2i(t[6].x(), t[1].y()), vec_2i(t[6].x(), t[2].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[6], h[0], t[5], h[1], t[4], t[1], h[2], t[2], t[3], t[6], h[3]);
    }

    private void render_Strange10_SpaceShipTopCenter(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 6, 10, 15, 17, 21);
        Vector2i[] h = { vec_2i(t[0].x(), t[1].y()), vec_2i(t[6].x(), t[1].y()), vec_2i(t[3].x(), t[2].y()) };
        addTowers(parent, t);
        addWalls(parent, t[0], t[6], t[0], h[0], t[6], h[1], t[1], t[5], t[1], t[2], t[3], h[2], t[4], t[5]);
        addWallAtCenter(parent, t[0].midpoint(h[1]), t[0].manhattanDist(t[6]), t[0].manhattanDist(h[0]));
        addWallAtCenter(parent, t[1].midpoint(t[4]), t[1].manhattanDist(t[5]), t[1].manhattanDist(t[2]) + TS);
    }

    private void render_Strange10_IslandLeftOfSpaceship(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6, 9, 12);
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[0], t[5], t[5], t[2], t[2], t[4], t[4], t[3], t[1], t[1].plus(TS, 0));
    }

    private void render_Strange10_IslandRightOfSpaceship(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 3, 5, 8, 10, 12);
        addTowers(parent, t);
        addWalls(parent, t[0], t[3], t[0], t[5], t[5], t[4], t[1], t[3], t[1], t[2], t[4], t[4].minus(TS, 0));
    }

    private void render_Strange10_RectangleWithTwoLegsAtLowerLeftEdge(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 9, 13, 15);
        addTowers(parent, t);
        addWalls(parent, t[0], t[2], t[1], t[3], t[3], t[4], t[0], t[4]);
    }

    private void render_Strange10_RectangleWithTwoLegsAtLowerRightEdge(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 5, 10, 15);
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[0], t[1], t[1], t[3], t[2], t[4]);
    }

    private void render_Strange10_ObstacleBelowHouse(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 7, 12, 15);
        Vector2i[] h = {
                vec_2i(t[0].x(), t[1].y()),
                vec_2i(t[2].x(), t[1].y()),
                vec_2i(t[4].x(), t[1].y()),
        };
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[1], t[3], t[0], t[4], t[0], h[0], t[2], h[1], t[4], h[2]);
    }

    private void render_Strange10_PistolPointingLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 5, 7, 9, 11);
        Vector2i h = vec_2i(t[4].x(), t[3].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[4], t[1], t[1], t[2], t[2], t[3], t[3], h);
    }

    private void render_Strange10_PistolPointingRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 6, 10);
        Vector2i h = vec_2i(t[0].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[0], t[3], t[1], h, t[1], t[2], t[2], t[3]);
    }

    private void render_Strange12_DogLookingLeft(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 13, 16, 19);
        Vector2i h = vec_2i(t[2].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[5], t[0], t[1], t[1], t[4], t[2], h, t[3], t[4], t[5], t[5].plus(0, TS));
    }

    private void render_Strange12_DogLookingRight(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 3, 5, 12, 17, 19);
        Vector2i h = vec_2i(t[3].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[5], t[1], t[4], t[1], t[2], t[3], h, t[4], t[5], t[0], t[0].plus(0, TS));
    }

    private void render_Strange15_LeftOfHouse(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 7, 12, 14, 19);
        Vector2i h = vec_2i(t[2].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[4], t[2], t[2], t[3], t[1], h);
    }

    private void render_Strange15_RightOfHouse(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 4, 7, 11, 18);
        Vector2i h = vec_2i(t[0].x(), t[3].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[4], t[0], t[2], t[1], t[2], t[3], h);
    }


    private void render_Strange14_Gamma(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 4, 8, 10);
        Vector2i h = vec_2i(t[2].x(), t[3].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], t[2], t[2], h, h, t[3], t[3], t[4], t[4], t[0]);
        addWallAtCenter(parent, t[0].midpoint(h), t[1].manhattanDist(t[2]), t[3].manhattanDist(t[4]));
    }

    private void render_Strange14_Gamma_Mirrored(Group parent, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCentersAtSegments(0, 2, 6, 8, 10);
        Vector2i h = vec_2i(t[2].x(), t[1].y());
        addTowers(parent, t);
        addWalls(parent, t[0], t[1], t[1], h, h, t[2], t[2], t[3], t[3], t[4], t[4], t[0]);
        addWallAtCenter(parent, t[4].midpoint(h), t[2].manhattanDist(t[3]), t[0].manhattanDist(t[1]));
    }
}
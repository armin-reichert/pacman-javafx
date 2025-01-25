/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.ObstacleType;
import de.amr.games.pacman.lib.tilemap.PolygonToRectSet;
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
import java.util.List;

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

    public Node createWallCenteredAt(Vector2f center, double sizeX, double sizeY) {
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
            return createWallCenteredAt(center.toVector2f(), length + wallThickness, wallThickness);
        }
        else if (beginTile.x() == endTile.x()) { // vertical wall
            Vector2i top    = beginTile.y() < endTile.y() ? beginTile : endTile;
            Vector2i bottom = beginTile.y() < endTile.y() ? endTile : beginTile;
            Vector2i center = top.plus(bottom).scaled(HTS).plus(HTS, HTS);
            int length = TS * (bottom.y() - top.y());
            return createWallCenteredAt(center.toVector2f(), wallThickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(beginTile, endTile));
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
        parent.getChildren().add(createWallCenteredAt(center.toVector2f(), sizeX, sizeY));
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
            // render single-wall obstacles using generic renderer
            if (obstacle.isClosed() && !obstacle.hasDoubleWalls() && obstacle.segment(0).isRoundedCorner() ) {
                render_Generic(parent, obstacle);
            } else {
                addGenericObstacle3D(parent, obstacle);
            }
            return;
        }
        // each obstacle has its own group
        Group og = new Group();
        switch (type) {
            default ->           render_Generic(og, obstacle);
            case COIN ->         render_Coin(og, obstacle);
            case F ->            render_F(og, obstacle);
            case H ->            render_H(og, obstacle);
            case O ->            render_O(og, obstacle);
            case S_SMALL ->      render_S_Small(og, obstacle);
        }
        addTags(og, TAG_INNER_OBSTACLE);
        parent.getChildren().add(og);
    }

    // Standard 3D obstacles

    public void render_Coin(Group g, Obstacle obstacle) {
        addTowers(g, obstacle.cornerCenter(0));
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

    // generic obstacle rendering

    protected void render_Generic(Group g, Obstacle obstacle) {
        String encoding = obstacle.encoding();
        Logger.info("Render generic: obstacle type={} encoding={}", ObstacleType.identify(encoding).orElse(ObstacleType.ANY), encoding);
        Vector2i[] cornerCenters = obstacle.cornerCenters();
        addTowers(g, cornerCenters);
        List<RectArea> rectangles = PolygonToRectSet.apply(obstacle);
        for (RectArea r : rectangles) {
            Vector2f center = vec_2f( r.x() + r.width() * 0.5f, r.y() + r.height() * 0.5f );
            g.getChildren().add(createWallCenteredAt(center, r.width(), r.height()));
        }
    }

    /**
     * Renders outer walls and non-single-wall obstacle inside.
     */
    protected void addGenericObstacle3D(Group g, Obstacle obstacle){
        int r = HTS;
        Vector2i p = obstacle.startPoint();
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            double length = segment.vector().length();
            Vector2i q = p.plus(segment.vector());
            if (segment.isVerticalLine()) {
                Node wall = createWallCenteredAt(p.midpoint(q).toVector2f(), wallThickness, length);
                g.getChildren().add(wall);
            } else if (segment.isHorizontalLine()) {
                Node wall = createWallCenteredAt(p.midpoint(q).toVector2f(), length + wallThickness, wallThickness);
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
        Node hWall = createWallCenteredAt(corner.midpoint(horEndPoint).toVector2f(), corner.manhattanDist(horEndPoint), wallThickness);
        Node vWall = createWallCenteredAt(corner.midpoint(vertEndPoint).toVector2f(), wallThickness, corner.manhattanDist(vertEndPoint));
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
}
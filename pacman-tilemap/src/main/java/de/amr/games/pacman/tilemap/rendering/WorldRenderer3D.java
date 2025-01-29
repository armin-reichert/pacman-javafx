/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.PolygonToRectConversion;
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

/**
 * A 3D printer for creating all artifacts in a 3D world.
 *
 * @author Armin Reichert
 */
public class WorldRenderer3D {

    private static final int CYLINDER_DIVISIONS = 24;

    // Tags
    public static final byte TAG_WALL_BASE      = 0x01;
    public static final byte TAG_WALL_TOP       = 0x02;
    public static final byte TAG_OUTER_WALL     = 0x04;
    public static final byte TAG_CORNER         = 0x08;
    public static final byte TAG_INNER_OBSTACLE = 0x10;

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

    private PhongMaterial wallBaseMaterial = new PhongMaterial();
    private PhongMaterial wallTopMaterial = new PhongMaterial();
    private PhongMaterial cornerBaseMaterial = new PhongMaterial();
    private PhongMaterial cornerTopMaterial = new PhongMaterial();

    private DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(3.5);
    private float wallTopHeight = 0.2f;
    private float wallThickness = 2;
    private boolean oShapeFilled = true;

    public void setCornerBaseMaterial(PhongMaterial material) {
        this.cornerBaseMaterial = material;
    }

    public void setCornerTopMaterial(PhongMaterial cornerTopMaterial) {
        this.cornerTopMaterial = cornerTopMaterial;
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

    public Node createWallBetweenTiles(Vector2i t1, Vector2i t2) {
        Vector2i center = t1.plus(t2).scaled(HTS).plus(HTS, HTS);
        if (t1.y() == t2.y()) { // horizontal wall
            int length = TS * Math.abs((t2.x() - t1.x()));
            return createWallCenteredAt(center.toVector2f(), length + wallThickness, wallThickness);
        }
        else if (t1.x() == t2.x()) { // vertical wall
            int length = TS * Math.abs((t2.y() - t1.y()));
            return createWallCenteredAt(center.toVector2f(), wallThickness, length);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(t1, t2));
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

    public Node createCircularWall(Vector2i center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(cornerBaseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.heightProperty().bind(wallBaseHeightPy);
        addTags(base, TAG_WALL_BASE, TAG_CORNER);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(cornerTopMaterial);
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

    private void addTowers(Group parent, Vector2i... centers) {
        for (Vector2i center : centers) {
            Node tower = createCircularWall(center, HTS);
            parent.getChildren().add(tower);
        }
    }

    private void addWall(Group parent, Vector2i p, Vector2i q) {
        if (p.x() == q.x()) { // vertical wall
            addWallAtCenter(parent, p.midpoint(q), TS, p.manhattanDist(q));
        } else if (p.y() == q.y()) { // horizontal wall
            addWallAtCenter(parent, p.midpoint(q), p.manhattanDist(q), TS);
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p, q);
        }
    }

    private void addWallAtCenter(Group parent, Vector2f center, double sizeX, double sizeY) {
        parent.getChildren().add(createWallCenteredAt(center, sizeX, sizeY));
    }

    /**
     * Creates a 3D representation for the given obstacle.
     * <p>
     * For each closed, single-wall obstacles, a group of Cylinder and Box primitives is created. For all other obstacles,
     * a sequence of walls and cylinders as corners is created.
     *
     * @param parent the group into which the 3D shapes are added
     * @param obstacle an obstacle
     */
    public void renderObstacle3D(Group parent, Obstacle obstacle) {
        String encoding = obstacle.encoding();
        Logger.info("Render 3D obstacle with encoding '{}'", encoding);
        if (obstacle.isClosed() && !obstacle.hasDoubleWalls()) {
            Group og = new Group();
            addTags(og, TAG_INNER_OBSTACLE);
            parent.getChildren().add(og);
            switch (encoding) {
                case "dgfe" ->     addTowers(og, obstacle.cornerCenter(0)); // single tower
                case "dcgbfceb" -> render_O(og, obstacle); // filled or hollow O-shaped obstacle
                default ->         render_ClosedSingleWallObstacle(og, obstacle);
            }
        } else {
            render_DoubleStrokeObstacle(parent, obstacle);
        }
    }

    public void render_O(Group og, Obstacle obstacle) {
        Vector2i[] t = obstacle.cornerCenters();
        addTowers(og, t);
        addWall(og, t[0], t[1]);
        addWall(og, t[1], t[2]);
        addWall(og, t[2], t[3]);
        addWall(og, t[3], t[0]);
        if (oShapeFilled) {
            double width  = Math.abs(t[0].x() - t[2].x()) - TS;
            double height = Math.abs(t[0].y() - t[2].y()) - TS;
            addWallAtCenter(og, t[0].midpoint(t[2]), width, height);
        }
    }

    private void render_ClosedSingleWallObstacle(Group g, Obstacle obstacle) {
        addTowers(g, obstacle.cornerCenters());
        for (RectArea rect : PolygonToRectConversion.convert(obstacle)) {
            g.getChildren().add(createWallCenteredAt(rect.center(), rect.width(), rect.height()));
        }
    }

    /**
     * Renders outer walls and non-single-wall obstacle inside.
     */
    private void render_DoubleStrokeObstacle(Group g, Obstacle obstacle){
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

    private void addGenericShapeCorner(Group parent, Vector2i corner, Vector2i horEndPoint, Vector2i vertEndPoint) {
        Node hWall = createWallCenteredAt(corner.midpoint(horEndPoint), corner.manhattanDist(horEndPoint), wallThickness);
        Node vWall = createWallCenteredAt(corner.midpoint(vertEndPoint), wallThickness, corner.manhattanDist(vertEndPoint));
        Node cWall = createCompositeCornerWall(corner, 0.5 * wallThickness);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    private Group createCompositeCornerWall(Vector2i center, double radius) {
        Cylinder base = new Cylinder(radius, wallBaseHeightPy.get(), CYLINDER_DIVISIONS);
        base.setMaterial(cornerBaseMaterial);
        base.heightProperty().bind(wallBaseHeightPy);
        base.translateZProperty().bind(wallBaseHeightPy.multiply(-0.5));
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);

        Cylinder top = new Cylinder(radius, wallTopHeight, CYLINDER_DIVISIONS);
        top.setMaterial(cornerTopMaterial);
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
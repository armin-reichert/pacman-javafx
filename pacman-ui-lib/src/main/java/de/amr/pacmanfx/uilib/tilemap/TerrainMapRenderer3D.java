/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

/**
 * Renders 3D terrain.
 */
public class TerrainMapRenderer3D {

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

    public void addWallBetween(Group parent, Vector2i p1, Vector2i p2, double wallThickness) {
        if (p1.x() == p2.x()) { // vertical wall
            parent.getChildren().add(createWallCenteredAt(p1.midpoint(p2), wallThickness, Math.abs(p1.y() - p2.y())));
        } else if (p1.y() == p2.y()) { // horizontal wall
            parent.getChildren().add(createWallCenteredAt(p1.midpoint(p2), Math.abs(p1.x() - p2.x()), wallThickness));
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p1, p2);
        }
    }

    public Group createWallBetweenTiles(Vector2i t1, Vector2i t2) {
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

    /**
     * Creates a 3D representation for the given obstacle.
     * <p>
     * For each closed obstacle, a group of Cylinder and Box primitives is created. For all other obstacles,
     * a sequence of walls with cylinders as corners is created.
     *
     * @param parent the group into which the 3D shapes are added
     * @param obstacle an obstacle
     */
    public void renderObstacle3D(Group parent, Obstacle obstacle, boolean worldBorder) {
        String encoding = obstacle.encoding();
        Logger.debug("Render 3D obstacle with encoding '{}'", encoding);
        if (obstacle.isClosed() && !worldBorder) {
            Group og = new Group();
            addTags(og, TAG_INNER_OBSTACLE);
            parent.getChildren().add(og);
            //TODO provide more general solution for polygons with holes
            if ("dcgbfceb".equals(encoding) && !oShapeFilled) { // O-shape with hole
                Vector2i[] cornerCenters = obstacle.cornerCenters();
                for (Vector2i center : cornerCenters) {
                    og.getChildren().add(createCircularWall(center, HTS));
                }
                addWallBetween(og, cornerCenters[0], cornerCenters[1], TS);
                addWallBetween(og, cornerCenters[1], cornerCenters[2], TS);
                addWallBetween(og, cornerCenters[2], cornerCenters[3], TS);
                addWallBetween(og, cornerCenters[3], cornerCenters[0], TS);
            } else {
                render_ClosedSingleWallObstacle(parent, obstacle);
            }
        } else {
            render_UnfilledObstacle(parent, obstacle);
        }
    }

    private void render_ClosedSingleWallObstacle(Group parent, Obstacle obstacle) {
        for (Vector2i center : obstacle.cornerCenters()) {
            parent.getChildren().add(createCircularWall(center, HTS));
        }
        obstacle.innerAreaRectangles()
            .forEach(r -> parent.getChildren().add( createWallCenteredAt(r.center(), r.width(), r.height()) ));
    }

    private void render_UnfilledObstacle(Group parent, Obstacle obstacle){
        int r = HTS;
        for (ObstacleSegment segment : obstacle.segments()) {
            boolean counterClockwise = segment.ccw();
            Vector2i start = segment.startPoint(), end = segment.endPoint();
            if (segment.isStraightLine()) {
                addWallBetween(parent, start, end, wallThickness);
            } else if (segment.isNWCorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(-r, 0), start, end);
                } else {
                    addCornerShape(parent, start.plus(0, -r), end, start);
                }
            } else if (segment.isSWCorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(0, r), end, start);
                } else {
                    addCornerShape(parent, start.plus(-r, 0), start, end);
                }
            } else if (segment.isSECorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(r, 0), start, end);
                } else {
                    addCornerShape(parent, start.plus(0, r), end, start);
                }
            } else if (segment.isNECorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(0, -r), end, start);
                } else {
                    addCornerShape(parent, start.plus(r, 0), start, end);
                }
            }
        }
    }

    private void addCornerShape(Group parent, Vector2i cornerCenter, Vector2i horEndPoint, Vector2i vertEndPoint) {
        Node hWall = createWallCenteredAt(cornerCenter.midpoint(horEndPoint), cornerCenter.manhattanDist(horEndPoint), wallThickness);
        Node vWall = createWallCenteredAt(cornerCenter.midpoint(vertEndPoint), wallThickness, cornerCenter.manhattanDist(vertEndPoint));
        Group cWall = createCircularWall(cornerCenter, 0.5 * wallThickness);
        addTags(cWall.getChildren().getFirst(), TAG_CORNER);
        addTags(cWall.getChildren().getLast(), TAG_CORNER);
        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    public Group createCircularWall(Vector2i center, double radius) {
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
        top.translateZProperty().bind(wallBaseHeightPy.add(0.5 * wallTopHeight).multiply(-1));

        addTags(base, TAG_WALL_BASE);
        addTags(top, TAG_WALL_TOP);

        Group wall = new Group(base, top);
        wall.setTranslateX(center.x());
        wall.setTranslateY(center.y());
        wall.setMouseTransparent(true);
        return wall;
    }

    public Group createWallCenteredAt(Vector2f center, double sizeX, double sizeY) {
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
}
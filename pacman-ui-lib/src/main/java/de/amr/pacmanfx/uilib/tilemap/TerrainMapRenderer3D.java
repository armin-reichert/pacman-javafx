/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.function.Consumer;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

/**
 * Renders 3D terrain.
 */
public class TerrainMapRenderer3D {

    private int cylinderDivisions = 32; // default=64
    private boolean oShapeFilled = true;

    private Consumer<Wall3D> wallCreatedCallback = wall3D -> Logger.debug(() -> "Wall created: " + wall3D);
    private Consumer<Wall3D> cornerCreatedCallback = wall3D -> Logger.debug(() -> "Corner created: " + wall3D);

    public TerrainMapRenderer3D() {}

    public void setWallCreatedCallback(Consumer<Wall3D> wallCreatedCallback) {
        this.wallCreatedCallback = wallCreatedCallback;
    }

    public void setCornerCreatedCallback(Consumer<Wall3D> cornerCreatedCallback) {
        this.cornerCreatedCallback = cornerCreatedCallback;
    }

    public void setCylinderDivisions(int cylinderDivisions) {
        this.cylinderDivisions = cylinderDivisions;
    }

    public void setOShapeFilled(boolean value) {
        this.oShapeFilled = value;
    }

    public void addWallBetween(Group parent, Vector2i p1, Vector2i p2, double wallThickness, PhongMaterial wallBaseMaterial, PhongMaterial wallTopMaterial) {
        if (p1.x() == p2.x()) { // vertical wall
            parent.getChildren().add(
                createWallCenteredAt(p1.midpoint(p2), wallThickness, Math.abs(p1.y() - p2.y()), wallBaseMaterial, wallTopMaterial)
            );
        } else if (p1.y() == p2.y()) { // horizontal wall
            parent.getChildren().add(
                createWallCenteredAt(p1.midpoint(p2), Math.abs(p1.x() - p2.x()), wallThickness, wallBaseMaterial, wallTopMaterial));
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p1, p2);
        }
    }

    public Wall3D createWallBetweenTiles(
        Vector2i t1,
        Vector2i t2,
        double wallThickness,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        Vector2i center = t1.plus(t2).scaled(HTS).plus(HTS, HTS);
        if (t1.y() == t2.y()) { // horizontal wall
            int length = TS * Math.abs((t2.x() - t1.x()));
            return createWallCenteredAt(center.toVector2f(), length + wallThickness,
                    wallThickness, baseMaterial, topMaterial);
        }
        else if (t1.x() == t2.x()) { // vertical wall
            int length = TS * Math.abs((t2.y() - t1.y()));
            return createWallCenteredAt(center.toVector2f(), wallThickness, length, baseMaterial, topMaterial);
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(t1, t2));
    }

    /**
     * Creates a 3D representation for the given obstacle.
     * <p>
     * For each closed obstacle, a group of Cylinder and Box primitives is created. For all other obstacles,
     * a sequence of walls with cylinders as corners is created.
     */
    public void renderObstacle3D(
        Group parent,
        Obstacle obstacle,
        boolean worldBorder,
        double wallThickness,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        String encoding = obstacle.encoding();
        Logger.debug("Render 3D obstacle with encoding '{}'", encoding);
        if (obstacle.isClosed() && !worldBorder) {
            Group obstacleGroup = new Group();
            parent.getChildren().add(obstacleGroup);
            //TODO provide more general solution for polygons with holes
            if ("dcgbfceb".equals(encoding) && !oShapeFilled) { // O-shape with hole
                Vector2i[] cornerCenters = obstacle.cornerCenters();
                for (Vector2i center : cornerCenters) {
                    obstacleGroup.getChildren().add(
                        createCircularWall(center, HTS, baseMaterial, topMaterial));
                }
                addWallBetween(obstacleGroup, cornerCenters[0], cornerCenters[1], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[1], cornerCenters[2], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[2], cornerCenters[3], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[3], cornerCenters[0], TS, baseMaterial, topMaterial);
            } else {
                render_ClosedSingleWallObstacle(parent, obstacle, baseMaterial, topMaterial);
            }
        } else {
            render_UnfilledObstacle(parent, obstacle, wallThickness, baseMaterial, topMaterial);
        }
    }

    private void render_ClosedSingleWallObstacle(
        Group parent,
        Obstacle obstacle,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        for (Vector2i center : obstacle.cornerCenters()) {
            parent.getChildren().add(createCircularWall(center, HTS, baseMaterial, topMaterial));
        }
        obstacle.innerAreaRectangles().forEach(r -> parent.getChildren().add(
                createWallCenteredAt(r.center(), r.width(), r.height(), baseMaterial, topMaterial)
        ));
    }

    private void render_UnfilledObstacle(
        Group parent,
        Obstacle obstacle,
        double wallThickness,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        int r = HTS;
        for (ObstacleSegment segment : obstacle.segments()) {
            boolean counterClockwise = segment.ccw();
            Vector2i start = segment.startPoint(), end = segment.endPoint();
            if (segment.isStraightLine()) {
                addWallBetween(parent, start, end, wallThickness, baseMaterial, topMaterial);
            } else if (segment.isNWCorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(-r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCornerShape(parent, start.plus(0, -r), end, start, wallThickness, baseMaterial, topMaterial);
                }
            } else if (segment.isSWCorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(0, r), end, start, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCornerShape(parent, start.plus(-r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                }
            } else if (segment.isSECorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCornerShape(parent, start.plus(0, r), end, start, wallThickness, baseMaterial, topMaterial);
                }
            } else if (segment.isNECorner()) {
                if (counterClockwise) {
                    addCornerShape(parent, start.plus(0, -r), end, start, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCornerShape(parent, start.plus(r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                }
            }
        }
    }

    private void addCornerShape(
            Group parent,
            Vector2i cornerCenter,
            Vector2i horEndPoint,
            Vector2i vertEndPoint,
            double wallThickness,
            PhongMaterial baseMaterial,
            PhongMaterial topMaterial)
    {
        Wall3D hWall = createWallCenteredAt(
            cornerCenter.midpoint(horEndPoint),
            cornerCenter.manhattanDist(horEndPoint),
            wallThickness,
            baseMaterial, topMaterial);

        Wall3D vWall = createWallCenteredAt(
            cornerCenter.midpoint(vertEndPoint),
            wallThickness,
            cornerCenter.manhattanDist(vertEndPoint),
            baseMaterial, topMaterial);

        Wall3D cWall = createCircularWall(cornerCenter, 0.5 * wallThickness, baseMaterial, topMaterial);

        parent.getChildren().addAll(hWall, vWall, cWall);
    }

    public Wall3D createCircularWall(
            Vector2i center,
            double radius,
            PhongMaterial baseMaterial,
            PhongMaterial topMaterial)
    {
        Cylinder base = new Cylinder(radius, Wall3D.DEFAULT_BASE_HEIGHT, cylinderDivisions);
        base.setMaterial(baseMaterial);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);

        Cylinder top = new Cylinder(radius, Wall3D.DEFAULT_TOP_HEIGHT, cylinderDivisions);
        top.setMaterial(topMaterial);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);

        Wall3D wall3D = new Wall3D(base, top);
        wall3D.setTranslateX(center.x());
        wall3D.setTranslateY(center.y());
        wall3D.setMouseTransparent(true);

        if (cornerCreatedCallback != null) {
            cornerCreatedCallback.accept(wall3D);
        }
        return wall3D;
    }

    public Wall3D createWallCenteredAt(
        Vector2f center,
        double sizeX,
        double sizeY,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        var base = new Box(sizeX, sizeY, Wall3D.DEFAULT_BASE_HEIGHT);
        base.setMaterial(baseMaterial);

        var top = new Box(sizeX, sizeY, Wall3D.DEFAULT_TOP_HEIGHT);
        top.setMaterial(topMaterial);

        Wall3D wall3D = new Wall3D(base, top);
        wall3D.setTranslateX(center.x());
        wall3D.setTranslateY(center.y());
        wall3D.setMouseTransparent(true);

        if (wallCreatedCallback != null) {
            wallCreatedCallback.accept(wall3D);
        }
        return wall3D;
    }
}
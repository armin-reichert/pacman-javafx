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

    private Consumer<Wall3D> onWallCreated = wall3D -> Logger.debug(() -> "Wall created: " + wall3D);

    public TerrainMapRenderer3D() {}

    public void setOnWallCreated(Consumer<Wall3D> callback) {
        onWallCreated = callback;
    }

    public void setCylinderDivisions(int cylinderDivisions) {
        this.cylinderDivisions = cylinderDivisions;
    }

    public void addWallBetween(Group parent, Vector2i p1, Vector2i p2, double wallThickness, PhongMaterial wallBaseMaterial, PhongMaterial wallTopMaterial) {
        if (p1.x() == p2.x()) { // vertical wall
            parent.getChildren().add(
                createBoxWall(p1.midpoint(p2), wallThickness, Math.abs(p1.y() - p2.y()), wallBaseMaterial, wallTopMaterial)
            );
        } else if (p1.y() == p2.y()) { // horizontal wall
            parent.getChildren().add(
                createBoxWall(p1.midpoint(p2), Math.abs(p1.x() - p2.x()), wallThickness, wallBaseMaterial, wallTopMaterial));
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
        Vector2f center = t1.midpoint(t2).scaled(TS).plus(HTS, HTS);
        if (t1.y() == t2.y()) { // horizontal wall
            int length = TS * Math.abs((t2.x() - t1.x()));
            return createBoxWall(center, length + wallThickness, wallThickness, baseMaterial, topMaterial);
        }
        else if (t1.x() == t2.x()) { // vertical wall
            int length = TS * Math.abs((t2.y() - t1.y()));
            return createBoxWall(center, wallThickness, length, baseMaterial, topMaterial);
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
        if (obstacle.isClosed() && !worldBorder) {
            var obstacleGroup = new Group();
            //TODO provide general solution for obstacles with holes
            if ("dcgbfceb".equals(obstacle.encoding())) { // O-shape with hole
                Vector2i[] cornerCenters = obstacle.cornerCenters();
                for (Vector2i center : cornerCenters) {
                    obstacleGroup.getChildren().add(
                        createCylinderWall(center, HTS, baseMaterial, topMaterial));
                }
                addWallBetween(obstacleGroup, cornerCenters[0], cornerCenters[1], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[1], cornerCenters[2], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[2], cornerCenters[3], TS, baseMaterial, topMaterial);
                addWallBetween(obstacleGroup, cornerCenters[3], cornerCenters[0], TS, baseMaterial, topMaterial);
            } else {
                renderClosedFilledObstacle(parent, obstacle, baseMaterial, topMaterial);
            }
            parent.getChildren().add(obstacleGroup);
        } else {
            renderUnfilledObstacle(parent, obstacle, wallThickness, baseMaterial, topMaterial);
        }
    }

    private void renderClosedFilledObstacle(
        Group parent,
        Obstacle obstacle,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        // Place a cylinder at each corner
        for (Vector2i center : obstacle.cornerCenters()) {
            parent.getChildren().add(
                createCylinderWall(center, HTS, baseMaterial, topMaterial));
        }
        // Fill area with boxes
        obstacle.innerAreaRectangles().forEach(rect ->
            parent.getChildren().add(
                createBoxWall(rect.center(), rect.width(), rect.height(), baseMaterial, topMaterial)
            )
        );
    }

    private void renderUnfilledObstacle(
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
            }
            else if (segment.isNWCorner()) {
                if (counterClockwise) {
                    addCorner(parent, start.plus(-r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCorner(parent, start.plus(0, -r), end, start, wallThickness, baseMaterial, topMaterial);
                }
            }
            else if (segment.isSWCorner()) {
                if (counterClockwise) {
                    addCorner(parent, start.plus(0, r), end, start, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCorner(parent, start.plus(-r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                }
            }
            else if (segment.isSECorner()) {
                if (counterClockwise) {
                    addCorner(parent, start.plus(r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCorner(parent, start.plus(0, r), end, start, wallThickness, baseMaterial, topMaterial);
                }
            }
            else if (segment.isNECorner()) {
                if (counterClockwise) {
                    addCorner(parent, start.plus(0, -r), end, start, wallThickness, baseMaterial, topMaterial);
                } else {
                    addCorner(parent, start.plus(r, 0), start, end, wallThickness, baseMaterial, topMaterial);
                }
            }
        }
    }

    private void addCorner(
        Group parent,
        Vector2i cornerCenter,
        Vector2i horizontalEndPoint,
        Vector2i verticalEndPoint,
        double wallThickness,
        PhongMaterial baseMaterial,
        PhongMaterial topMaterial)
    {
        Wall3D horizontalWall = createBoxWall(
            cornerCenter.midpoint(horizontalEndPoint),
            cornerCenter.manhattanDist(horizontalEndPoint),
            wallThickness,
            baseMaterial, topMaterial);

        Wall3D verticalWall = createBoxWall(
            cornerCenter.midpoint(verticalEndPoint),
            wallThickness,
            cornerCenter.manhattanDist(verticalEndPoint),
            baseMaterial, topMaterial);

        Wall3D cornerWall = createCylinderWall(cornerCenter, 0.5 * wallThickness, baseMaterial, topMaterial);
        parent.getChildren().addAll(horizontalWall, cornerWall, verticalWall);
    }

    public Wall3D createCylinderWall(
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

        if (onWallCreated != null) {
            onWallCreated.accept(wall3D);
        }
        return wall3D;
    }

    public Wall3D createBoxWall(
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

        if (onWallCreated != null) {
            onWallCreated.accept(wall3D);
        }
        return wall3D;
    }
}
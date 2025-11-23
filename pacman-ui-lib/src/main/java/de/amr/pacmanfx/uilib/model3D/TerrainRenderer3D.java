/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.Obstacle;
import de.amr.pacmanfx.lib.worldmap.ObstacleSegment;
import javafx.util.Callback;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

/**
 * Renders 3D terrain. To add the created walls to some group, use the callback function.
 * <p>Example:
 * <pre>
 *     Material baseMaterial = ...;
 *     Material topMaterial = ...;
 *     Group parent = new Group();
 *     var r3D = new TerrainRenderer();
 *     r3D.setOnWallCreated(wall -> {
 *        wall.setBaseMaterial(baseMaterial);
 *        wall.setTopMaterial(topMaterial;
 *        parent.getChildren().addAll(wall.base(), wall.top());
 *        return wall;
 *     });
 * </pre>
 * </p>
 */
public class TerrainRenderer3D {

    private static Wall3D keepUnchanged(Wall3D wall3D) {
        Logger.trace("Wall3D created: {}", wall3D);
        return wall3D;
    }

    private Callback<Wall3D, Wall3D> onWallCreated = TerrainRenderer3D::keepUnchanged;

    public TerrainRenderer3D() {}

    /**
     * @param callback a callback function which is applied to each wall on creation
     *                 or {@code null} if no action is required on creation
     */
    public void setOnWallCreated(Callback<Wall3D, Wall3D> callback) {
        onWallCreated = callback != null ? callback : TerrainRenderer3D::keepUnchanged;
    }

    public Wall3D createBoxWall(Vector2f center, double sizeX, double sizeY) {
        Wall3D wall3D = Wall3D.createBoxWall(center, sizeX, sizeY);
        return onWallCreated.call(wall3D);
    }

    public Wall3D createCylinderWall(Vector2f center, double radius) {
        Wall3D wall3D = Wall3D.createCylinderWall(center, radius);
        return onWallCreated.call(wall3D);
    }

    public void createWallBetween(Vector2f p1, Vector2f p2, double wallThickness) {
        if (p1.x() == p2.x()) { // vertical wall
            createBoxWall(p1.midpoint(p2), wallThickness, p1.manhattanDist(p2));
        } else if (p1.y() == p2.y()) { // horizontal wall
            createBoxWall(p1.midpoint(p2), p1.manhattanDist(p2), wallThickness);
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p1, p2);
        }
    }

    public Wall3D createWallBetweenTileCoordinates(Vector2i t1, Vector2i t2, double wallThickness) {
        Vector2f center = t1.midpoint(t2).scaled(TS).plus(HTS, HTS);
        if (t1.x() == t2.x()) { // vertical wall
            return createBoxWall(center, wallThickness, TS * t1.manhattanDist(t2));
        } else if (t1.y() == t2.y()) { // horizontal wall
            return createBoxWall(center, TS * t1.manhattanDist(t2) + wallThickness, wallThickness);
        } else {
            throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(t1, t2));
        }
    }

    /**
     * Creates a 3D representation for the given obstacle.
     * <p>
     * For each closed obstacle, a group of Cylinder and Box primitives is created. For all other obstacles,
     * a sequence of walls with cylinders as corners is created.
     */
    public void renderObstacle3D(
        Obstacle obstacle,
        boolean border,
        double wallThickness,
        double cornerRadius)
    {
        if (obstacle.isClosed() && !border) {
            //TODO provide general solution for obstacles with holes
            if ("dcgbfceb".equals(obstacle.encoding())) { // O-shape with hole
                Vector2f[] corners = obstacle.cornerCenterPoints();
                for (Vector2f corner : corners) {
                    createCylinderWall(corner, HTS);
                }
                createWallBetween(corners[0], corners[1], TS);
                createWallBetween(corners[1], corners[2], TS);
                createWallBetween(corners[2], corners[3], TS);
                createWallBetween(corners[3], corners[0], TS);
            } else {
                // My way (c) for creating closed obstacles:
                // Create a cylindric wall at each corner, use the rectangular partition of the inner area to
                // fill the inner area with boxes.
                for (Vector2f corner : obstacle.cornerCenterPoints()) {
                    createCylinderWall(corner, cornerRadius);
                }
                obstacle.innerAreaRectangles().forEach(r -> createBoxWall(r.center(), r.width(), r.height()));
            }
        } else {
            renderSegmentPath(obstacle, wallThickness);
        }
    }

    private void renderSegmentPath(Obstacle obstacle, double wallThickness) {
        float r = HTS;
        for (ObstacleSegment segment : obstacle.segments()) {
            boolean counterClockwise = segment.ccw();
            Vector2f start = segment.startPoint().toVector2f(), end = segment.endPoint().toVector2f();
            if (segment.isStraightLine()) {
                createWallBetween(start, end, wallThickness);
            }
            else if (segment.isNWCorner()) {
                if (counterClockwise) {
                    createCornerWalls(start.plus(-r, 0), start, end, wallThickness);
                } else {
                    createCornerWalls(start.plus(0, -r), end, start, wallThickness);
                }
            }
            else if (segment.isSWCorner()) {
                if (counterClockwise) {
                    createCornerWalls(start.plus(0, r), end, start, wallThickness);
                } else {
                    createCornerWalls(start.plus(-r, 0), start, end, wallThickness);
                }
            }
            else if (segment.isSECorner()) {
                if (counterClockwise) {
                    createCornerWalls(start.plus(r, 0), start, end, wallThickness);
                } else {
                    createCornerWalls(start.plus(0, r), end, start, wallThickness);
                }
            }
            else if (segment.isNECorner()) {
                if (counterClockwise) {
                    createCornerWalls(start.plus(0, -r), end, start, wallThickness);
                } else {
                    createCornerWalls(start.plus(r, 0), start, end, wallThickness);
                }
            }
        }
    }

    private void createCornerWalls(Vector2f center, Vector2f endPointH, Vector2f endPointV, double wallThickness) {
        createBoxWall(center.midpoint(endPointH), center.manhattanDist(endPointH), wallThickness);
        createCylinderWall(center, 0.5 * wallThickness);
        createBoxWall(center.midpoint(endPointV), wallThickness, center.manhattanDist(endPointV));
    }
}
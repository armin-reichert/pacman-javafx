/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.ObstacleSegment;
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

    private Callback<Wall3D, Wall3D> onWallCreated = wall3D -> wall3D;

    public TerrainRenderer3D() {}

    /**
     * @param callback a callback function which is applied to each wall on creation
     *                 or {@code null} if no action is required on creation
     */
    public void setOnWallCreated(Callback<Wall3D, Wall3D> callback) {
        onWallCreated = callback;
    }

    public void addWallBetween(
        Vector2i p1,
        Vector2i p2,
        double wallThickness)
    {
        if (p1.x() == p2.x()) { // vertical wall
            createBoxWall(p1.midpoint(p2), wallThickness, p1.manhattanDist(p2));
        } else if (p1.y() == p2.y()) { // horizontal wall
            createBoxWall(p1.midpoint(p2), p1.manhattanDist(p2), wallThickness);
        } else {
            Logger.error("Cannot add horizontal/vertical wall between {} and {}", p1, p2);
        }
    }

    public Wall3D createWallBetweenTiles(
        Vector2i tile1,
        Vector2i tile2,
        double wallThickness)
    {
        Vector2f center = tile1.midpoint(tile2).scaled(TS).plus(HTS, HTS);
        if (tile1.y() == tile2.y()) { // horizontal wall
            return createBoxWall(center, TS * tile1.manhattanDist(tile2) + wallThickness, wallThickness);
        }
        else if (tile1.x() == tile2.x()) { // vertical wall
            return createBoxWall(center, wallThickness, TS * tile1.manhattanDist(tile2));
        }
        throw new IllegalArgumentException("Cannot build wall between tiles %s and %s".formatted(tile1, tile2));
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
                Vector2i[] corners = obstacle.cornerCenters();
                for (Vector2i corner : corners) {
                    createCylinderWall(corner, HTS);
                }
                addWallBetween(corners[0], corners[1], TS);
                addWallBetween(corners[1], corners[2], TS);
                addWallBetween(corners[2], corners[3], TS);
                addWallBetween(corners[3], corners[0], TS);
            } else {
                renderFilledObstacle(obstacle, cornerRadius);
            }
        } else {
            renderSegmentPath(obstacle, wallThickness);
        }
    }

    private void renderFilledObstacle(Obstacle obstacle, double cornerRadius) {
        for (Vector2i corner : obstacle.cornerCenters()) {
            createCylinderWall(corner, cornerRadius);
        }
        obstacle.innerAreaRectangles().forEach(r -> createBoxWall(r.center(), r.width(), r.height()));
    }

    private void renderSegmentPath(Obstacle obstacle, double wallThickness) {
        int r = HTS;
        for (ObstacleSegment segment : obstacle.segments()) {
            boolean counterClockwise = segment.ccw();
            Vector2i start = segment.startPoint(), end = segment.endPoint();
            if (segment.isStraightLine()) {
                addWallBetween(start, end, wallThickness);
            }
            else if (segment.isNWCorner()) {
                if (counterClockwise) {
                    addCorner(start.plus(-r, 0), start, end, wallThickness);
                } else {
                    addCorner(start.plus(0, -r), end, start, wallThickness);
                }
            }
            else if (segment.isSWCorner()) {
                if (counterClockwise) {
                    addCorner(start.plus(0, r), end, start, wallThickness);
                } else {
                    addCorner(start.plus(-r, 0), start, end, wallThickness);
                }
            }
            else if (segment.isSECorner()) {
                if (counterClockwise) {
                    addCorner(start.plus(r, 0), start, end, wallThickness);
                } else {
                    addCorner(start.plus(0, r), end, start, wallThickness);
                }
            }
            else if (segment.isNECorner()) {
                if (counterClockwise) {
                    addCorner(start.plus(0, -r), end, start, wallThickness);
                } else {
                    addCorner(start.plus(r, 0), start, end, wallThickness);
                }
            }
        }
    }

    private void addCorner(Vector2i center, Vector2i endPointH, Vector2i endPointV, double wallThickness) {
        createBoxWall(center.midpoint(endPointH), center.manhattanDist(endPointH), wallThickness);
        createBoxWall(center.midpoint(endPointV), wallThickness, center.manhattanDist(endPointV));
        createCylinderWall(center, 0.5 * wallThickness);
    }

    public Wall3D createCylinderWall(Vector2i center, double radius) {
        Wall3D wall3D = Wall3D.createCylinderWall(center, radius);
        return onWallCreated != null ? onWallCreated.call(wall3D) : wall3D;
    }

    public Wall3D createBoxWall(Vector2f center, double sizeX, double sizeY) {
        Wall3D wall3D = Wall3D.createBoxWall(center, sizeX, sizeY);
        return onWallCreated != null ? onWallCreated.call(wall3D) : wall3D;
    }
}
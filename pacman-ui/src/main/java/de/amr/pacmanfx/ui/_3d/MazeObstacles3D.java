/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.uilib.model3D.TerrainRenderer3D;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import org.tinylog.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;

public class MazeObstacles3D extends Group implements Disposable {

    private final TerrainRenderer3D renderer3D = new TerrainRenderer3D();

    public void renderObstacles(GameLevel level, float wallThickness, float cornerRadius, MazeMaterials3D materials, DoubleProperty wallBaseHeight) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final House house = terrain.optHouse().orElse(null);
        final var wall3DCount = new AtomicInteger(0);
        renderer3D.setOnWallCreated(wall3D -> {
            wall3DCount.incrementAndGet();
            wall3D.setBaseMaterial(materials.wallBase());
            wall3D.setTopMaterial(materials.wallTop());
            wall3D.bindBaseHeight(wallBaseHeight);
            getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(tileAt(startPoint))) {
                renderer3D.renderObstacle3D(obstacle, isWorldBorder(terrain, obstacle), wallThickness, cornerRadius);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);
    }

    @Override
    public void dispose() {
        renderer3D.setOnWallCreated(null);
        getChildren().clear();
    }

    private static boolean isWorldBorder(TerrainLayer terrain, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == terrain.emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == terrain.numCols() * TS;
        }
    }
}

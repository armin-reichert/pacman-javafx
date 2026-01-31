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
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.TerrainRenderer3D;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import org.tinylog.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

public class MazeObstacles3D implements Disposable {

    private final PreferencesManager prefs;
    private final TerrainRenderer3D terrainRenderer3D = new TerrainRenderer3D();

    public MazeObstacles3D(PreferencesManager prefs) {
        this.prefs = requireNonNull(prefs);
    }

    private void configureRenderer(Group parent, MazeMaterials3D materials, DoubleProperty wallBaseHeight, AtomicInteger wall3DCount) {
        terrainRenderer3D.setOnWallCreated(wall3D -> {
            wall3DCount.incrementAndGet();
            wall3D.setBaseMaterial(materials.wallBase());
            wall3D.setTopMaterial(materials.wallTop());
            wall3D.bindBaseHeight(wallBaseHeight);
            parent.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });
    }

    private void renderObstacles(TerrainLayer terrain, float wallThickness, float cornerRadius) {
        final House house = terrain.optHouse().orElse(null);
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(tileAt(startPoint))) {
                final boolean border = isWorldBorder(terrain, obstacle);
                terrainRenderer3D.renderObstacle3D(obstacle, border, wallThickness, cornerRadius);
            }
        }
    }

    public void addObstacles(Group parent, MazeMaterials3D materials, DoubleProperty wallBaseHeight, GameLevel level) {
        var wall3DCount = new AtomicInteger(0);
        configureRenderer(parent, materials, wallBaseHeight, wall3DCount);
        final float wallThickness = prefs.getFloat("3d.obstacle.wall_thickness");
        final float cornerRadius = prefs.getFloat("3d.obstacle.corner_radius");
        final var stopWatch = new StopWatch();
        renderObstacles(level.worldMap().terrainLayer(), wallThickness, cornerRadius);
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);
    }

    @Override
    public void dispose() {
        terrainRenderer3D.setOnWallCreated(null);
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

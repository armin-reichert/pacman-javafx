/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3.entities;

import de.amr.basics.StopWatch;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelEntity;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.config.Floor3DSettings;
import de.amr.pacmanfx.ui.config.Maze3DSettings;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.world.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.world.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

/**
 * Renders the complete 3D representation of a Pac-Man maze for a single level.
 */
public class Maze3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);
    private final ObjectProperty<Color> floorColor = new SimpleObjectProperty<>(Color.valueOf("#1a1a1a"));

    private final TerrainLayer terrain;
    private final Group particlesGroup = new Group();
    private Box floor3D;
    private MazeHouse3D house3D;
    private Map<String, PhongMaterial> materials;

    public Maze3D(TerrainLayer terrain) {
        this.terrain = requireNonNull(terrain);
    }

    @Override
    public void init(GameContext gameContext, GameLevel level) {}

    @Override
    public void update(GameContext gameContext, GameLevel level) {
        house3D.update(gameContext, level);
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (house3D != null) {
            house3D.dispose();
            house3D = null;
        }

        cleanupGroup(particlesGroup, true);
        cleanupGroup(this, true);
    }

    public void build(
        ObjectProperty<DrawMode> drawMode,
        Map<String, PhongMaterial> materials,
        Maze3DSettings mazeConfig,
        Floor3DSettings floorConfig3D)
    {
        this.materials = materials;
        buildFloor(drawMode, floorConfig3D);
        addObstacles(drawMode, mazeConfig.obstacleWallThickness(), mazeConfig.obstacleCornerRadius());
    }

    public Map<String, PhongMaterial> materials() {
        return materials;
    }

    public void setHouse3D(MazeHouse3D house3D) {
        this.house3D = requireNonNull(house3D);
    }

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public ObjectProperty<Color> floorColorProperty() {
        return floorColor;
    }

    public Box floor() {
        return floor3D;
    }

    public double floorTop() {
        return floor().getTranslateZ() - 0.5 * floor().getDepth();
    }

    public MazeHouse3D house() {
        return house3D;
    }

    public Group particlesGroup() {
        return particlesGroup;
    }

    private void addObstacles(ObjectProperty<DrawMode> drawMode, float wallThickness, float cornerRadius) {
        final TerrainRenderer3D renderer3D = new TerrainRenderer3D();
        final House house = terrain.optHouse().orElse(null);
        final var wallCount = new AtomicInteger(0);
        renderer3D.setOnWallCreated(wall3D -> {
            wallCount.incrementAndGet();
            wall3D.setBaseMaterial(materials.get("wallBaseMaterial"));
            wall3D.setTopMaterial(materials.get("wallTopMaterial"));
            wall3D.bindBaseHeight(wallBaseHeight);
            wall3D.base().drawModeProperty().bindBidirectional(drawMode);
            wall3D.top() .drawModeProperty().bindBidirectional(drawMode);
            getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(WorldMap.computeTileAt(startPoint))) {
                renderer3D.renderObstacle3D(obstacle, isWorldBorder(obstacle), wallThickness, cornerRadius);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Building {} composite walls took {} milliseconds", wallCount, passedTimeMillis);
    }

    private boolean isWorldBorder(Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == WorldMap.TS || start.y() == terrain.emptyRowsOverMaze() * WorldMap.TS + WorldMap.HTS;
        } else {
            return start.x() == 0 || start.x() == terrain.numCols() * WorldMap.TS;
        }
    }

    private void buildFloor(ObjectProperty<DrawMode> drawMode, Floor3DSettings floorConfig) {
        final Vector2i terrainSize = terrain.sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        final float thickness = floorConfig.thickness();

        floor3D = new Box(width, height, thickness);
        floor3D.drawModeProperty().bindBidirectional(drawMode);
        floor3D.setMaterial(materials.get("floorMaterial"));

        floor3D.setTranslateX(0.5 * width - floorConfig.padding());
        floor3D.setTranslateY(0.5 * height);
        floor3D.setTranslateZ(0.5 * thickness);

        getChildren().add(floor3D);
    }
}
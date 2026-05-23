/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.entities;

import de.amr.basics.StopWatch;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.config.WorldConfig;
import de.amr.pacmanfx.ui.config.FloorConfig3D;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.d3.Factory3D;
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
import org.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Renders the complete 3D representation of a Pac-Man maze for a single level.
 */
public class Maze3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    private static void addObstacles(
        Group group,
        TerrainLayer terrain,
        float wallThickness, float cornerRadius,
        Map<String, PhongMaterial> materials,
        DoubleProperty wallBaseHeight)
    {
        final TerrainRenderer3D renderer3D = new TerrainRenderer3D();
        final House house = terrain.optHouse().orElse(null);
        final var wall3DCount = new AtomicInteger(0);
        renderer3D.setOnWallCreated(wall3D -> {
            wall3DCount.incrementAndGet();
            wall3D.setBaseMaterial(materials.get("wallBaseMaterial"));
            wall3D.setTopMaterial(materials.get("wallTopMaterial"));
            wall3D.bindBaseHeight(wallBaseHeight);
            wall3D.base().drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
            wall3D.top().drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
            group.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(computeTileAt(startPoint))) {
                renderer3D.renderObstacle3D(obstacle, isWorldBorder(terrain, obstacle), wallThickness, cornerRadius);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);
    }

    private static boolean isWorldBorder(TerrainLayer terrain, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == terrain.emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == terrain.numCols() * TS;
        }
    }

    /** Base height of walls in world units. Can be externally bound. */
    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);

    /** Opacity applied to all wall materials. Can be externally bound. */
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private final ObjectProperty<Color> floorColor = new SimpleObjectProperty<>(Color.GRAY);

    private final Map<String, PhongMaterial> materials;
    private Box floor3D;
    private MazeHouse3D house3D;

    private final Group particlesGroup = new Group();

    /**
     * Creates a new 3D maze for the given level.
     *
     * @param terrain       the game level terrain
     * @param factory3D     the factory for 3D entities
     * @param worldConfig  3D configuration
     * @param colorScheme   the map color scheme
     *
     * @throws NullPointerException if any required argument is {@code null}
     */
    public Maze3D(
        TerrainLayer terrain,
        Factory3D factory3D,
        WorldConfig worldConfig,
        WorldMapColorScheme colorScheme)
    {
        requireNonNull(terrain);
        requireNonNull(factory3D);
        requireNonNull(worldConfig);

        materials = factory3D.createMazeMaterials(colorScheme, wallOpacity, floorColor);

        createAndAddFloor3D(worldConfig.floor(), terrain);
        createAndAddObstacles3D(worldConfig.maze(), terrain);
    }

    public void setHouse3D(MazeHouse3D house3D) {
        this.house3D = requireNonNull(house3D);
    }

    @Override
    public void update(GameLevel level) {
        house3D.update(level);
    }

    /** @return the property controlling the base height of all walls */
    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    /** @return the property controlling the opacity of all wall materials */
    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public ObjectProperty<Color> floorColorProperty() {
        return floorColor;
    }

    /** @return the shared materials used by all maze components */
    public Map<String, PhongMaterial> materials() {
        return materials;
    }

    /** @return the floor component */
    public Box floor() {
        return floor3D;
    }

    /** @return the ghost house component, or {@code null} if the map has no house */
    public MazeHouse3D house() {
        return house3D;
    }

    /** @return the group for dynamic particle effects (energizer explosions etc.) */
    public Group particlesGroup() {
        return particlesGroup;
    }

    public double floorTop() {
        return floor().getTranslateZ() - 0.5 * floor().getDepth();
    }

    /**
     * Disposes all 3D resources created by this maze.
     * <p>
     * Unbinds properties, disposes all subcomponents, disposes every {@link Wall3D} among the direct children,
     * and clears the scene-graph children. After calling {@code dispose()}, this instance must not be used again.
     */
    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        materials.clear();

        if (house3D != null) {
            house3D.dispose();
            house3D = null;
        }

        cleanupGroup(particlesGroup, true);
        cleanupGroup(this, true);

        Logger.info("Disposed 3D maze");
    }

    // Private area

    private void createAndAddObstacles3D(MazeConfig3D mazeConfig, TerrainLayer terrain) {
        final float wallThickness = mazeConfig.obstacleWallThickness();
        final float cornerRadius  = mazeConfig.obstacleCornerRadius();
        addObstacles(this, terrain, wallThickness, cornerRadius, materials, wallBaseHeight);
    }

    private void createAndAddFloor3D(FloorConfig3D floorConfig, TerrainLayer terrain) {
        final Vector2i terrainSize = terrain.sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        final float thickness = floorConfig.thickness();

        floor3D = new Box(width, height, thickness);
        floor3D.drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
        floor3D.setMaterial(materials.get("floorMaterial"));

        floor3D.setTranslateX(0.5 * width - floorConfig.padding());
        floor3D.setTranslateY(0.5 * height);
        floor3D.setTranslateZ(0.5 * thickness);

        getChildren().add(floor3D);
    }
}
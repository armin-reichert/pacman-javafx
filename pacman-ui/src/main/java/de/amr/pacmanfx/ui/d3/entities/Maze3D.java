/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.entities;

import de.amr.basics.Disposable;
import de.amr.basics.StopWatch;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.config.FloorConfig3D;
import de.amr.pacmanfx.ui.config.HouseConfig3D;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.MazeMaterials3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.world.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.world.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Renders the complete 3D representation of a Pac-Man maze for a single level.
 * <p>
 * {@code Maze3D} is the top-level container that assembles and manages all visual 3D elements:
 * <ul>
 *   <li>{@link MazeMaterials3D} – shared materials and property bindings</li>
 *   <li>{@link MazeFloor3D} – the floor plane</li>
 *   <li>{@link MazeHouse3D} – the ghost house (if the map contains one)</li>
 * </ul>
 * <p>
 * It also exposes a dedicated {@link #particlesGroup()} for dynamic effects such as energizer explosions.
 * <p>
 * This class extends {@link Group} so it can be added directly to a JavaFX scene graph.
 * It implements {@link Disposable} to ensure all 3D resources are released when a level ends.
 * <p>
 * <strong>Note:</strong> {@code floor3D}, {@code food3D} and {@code particlesGroup} are created
 * but <em>not</em> added as children by this class; the caller is responsible for adding them.
 *
 * @see MazeFloor3D
 * @see MazeHouse3D
 * @see MazeMaterials3D
 */
public class Maze3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    private static void addObstacles(
        Group group, WorldMap worldMap,
        float wallThickness, float cornerRadius, MazeMaterials3D materials, DoubleProperty wallBaseHeight)
    {
        final TerrainRenderer3D renderer3D = new TerrainRenderer3D();
        final House house = worldMap.terrainLayer().optHouse().orElse(null);
        final var wall3DCount = new AtomicInteger(0);
        renderer3D.setOnWallCreated(wall3D -> {
            wall3DCount.incrementAndGet();
            wall3D.setBaseMaterial(materials.wallBase());
            wall3D.setTopMaterial(materials.wallTop());
            wall3D.bindBaseHeight(wallBaseHeight);
            wall3D.base().drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
            wall3D.top().drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
            group.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : worldMap.terrainLayer().obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(tileAt(startPoint))) {
                renderer3D.renderObstacle3D(obstacle, isWorldBorder(worldMap.terrainLayer(), obstacle), wallThickness, cornerRadius);
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

    private MazeMaterials3D materials3D;
    private Box floor3D;
    private MazeHouse3D house3D;

    private final Group particlesGroup = new Group();

    /**
     * Creates a new 3D maze for the given level.
     *
     * @param level         the game level whose world map is rendered
     * @param factory3D     the factory for 3D entities
     * @param entityConfig  3D configuration
     * @param colorScheme   the map color scheme
     * @param animations registry for animations used by 3D components
     *
     * @throws NullPointerException if any required argument is {@code null}
     */
    public Maze3D(
        GameLevel level,
        Factory3D factory3D,
        EntityConfig entityConfig,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animations)
    {
        requireNonNull(level);
        requireNonNull(factory3D);
        requireNonNull(entityConfig);
        requireNonNull(animations);

        materials3D = factory3D.createMazeMaterials(colorScheme, wallOpacity, floorColor);
        createAndAddFloor3D(entityConfig.floor(), level);
        createAndAddObstacles3D(entityConfig.maze(), level);
        house3D = createArcadeHouse3D(animations, entityConfig.house(), level.worldMap(), colorScheme)
            .orElseThrow(IllegalStateException::new);
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
    public MazeMaterials3D materials() {
        return materials3D;
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
     * Unbinds properties, disposes all sub-components, disposes every {@link Wall3D} among the direct children,
     * and clears the scene-graph children. After calling {@code dispose()}, this instance must not be used again.
     */
    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        materials3D = null;

        if (house3D != null) {
            house3D.dispose();
            house3D = null;
        }

        cleanupGroup(particlesGroup, true);
        cleanupGroup(this, true);

        Logger.info("Disposed 3D maze");
    }

    // Private area

    private void createAndAddObstacles3D(MazeConfig3D mazeConfig, GameLevel level) {
        final float wallThickness = mazeConfig.obstacleWallThickness();
        final float cornerRadius  = mazeConfig.obstacleCornerRadius();
        addObstacles(this, level.worldMap(), wallThickness, cornerRadius, materials3D, wallBaseHeight);
    }

    private void createAndAddFloor3D(FloorConfig3D floorConfig, GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        final float thickness = floorConfig.thickness();
//        floor3D = new MazeFloor3D(materials3D.floor(), width, height, thickness);

        floor3D = new Box(width, height, thickness);
        floor3D.drawModeProperty().bind(GameUIConstants.PROPERTY_3D_DRAW_MODE);
        floor3D.setMaterial(materials3D.floor());

        floor3D.setTranslateX(0.5 * width - floorConfig.padding());
        floor3D.setTranslateY(0.5 * height);
        floor3D.setTranslateZ(0.5 * thickness);
        getChildren().add(floor3D);
    }

    private Optional<MazeHouse3D> createArcadeHouse3D(
        AnimationRegistry animations, HouseConfig3D houseConfig, WorldMap worldMap, WorldMapColorScheme colorScheme) {
        return worldMap.terrainLayer().optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .map(arcadeHouse -> new MazeHouse3D(colorScheme, houseConfig, animations, arcadeHouse));
    }
}
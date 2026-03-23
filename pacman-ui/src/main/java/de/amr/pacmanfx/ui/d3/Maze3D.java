/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.config.FloorConfig3D;
import de.amr.pacmanfx.ui.config.HouseConfig3D;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.world.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Renders the complete 3D representation of a Pac-Man maze for a single level.
 * <p>
 * {@code Maze3D} is the top-level container that assembles and manages all visual 3D elements:
 * <ul>
 *   <li>{@link MazeMaterials3D} – shared materials and property bindings</li>
 *   <li>{@link MazeFloor3D} – the floor plane</li>
 *   <li>{@link MazeObstacles3D} – walls, corners and static obstacles</li>
 *   <li>{@link MazeHouse3D} – the ghost house (if the map contains one)</li>
 *   <li>{@link MazeFood3D} – pellets, energizers and related animations</li>
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
 * @see MazeObstacles3D
 * @see MazeHouse3D
 * @see MazeFood3D
 * @see MazeMaterials3D
 */
public class Maze3D extends Group implements DisposableGraphicsObject {

    /** Base height of walls in world units. Can be externally bound. */
    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);

    /** Opacity applied to all wall materials. Can be externally bound. */
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private MazeMaterials3D materials3D;
    private MazeObstacles3D obstacles3D;
    private MazeFloor3D floor3D;
    private MazeHouse3D house3D;

    private final Group particlesGroup = new Group();

    /**
     * Creates a new 3D maze for the given level.
     *
     * @param level         the game level whose world map is rendered
     * @param factory3D     the factory for 3D entities
     * @param entityConfig  3D configuration
     * @param colorScheme   the map color scheme
     * @param animationRegistry registry for animations used by 3D components
     *
     * @throws NullPointerException if any required argument is {@code null}
     */
    public Maze3D(
        GameLevel level,
        Factory3D factory3D,
        EntityConfig entityConfig,
        WorldMapColorScheme colorScheme,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(factory3D);
        requireNonNull(entityConfig);
        requireNonNull(level);

        createMaterials(colorScheme);
        createFloor3D(entityConfig.floor(), level);
        createObstacles3D(entityConfig.maze(), level);
        createArcadeHouse3D(animationRegistry, entityConfig.house(), level, colorScheme);
    }

    /** @return the property controlling the base height of all walls */
    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    /** @return the property controlling the opacity of all wall materials */
    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    /** @return the shared materials used by all maze components */
    public MazeMaterials3D materials() {
        return materials3D;
    }

    /** @return the floor component */
    public MazeFloor3D floor() {
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

    /** @return the Z-coordinate of the top surface of the floor */
    public double floorTop() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
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

        if (materials3D != null) { materials3D.dispose(); materials3D = null; }
        if (floor3D != null)     { floor3D.dispose();     floor3D = null; }
        if (obstacles3D != null) { obstacles3D.dispose(); obstacles3D = null; }
        if (house3D != null)     { house3D.dispose();     house3D = null; }

        cleanupGroup(particlesGroup, true);
        cleanupGroup(this, true);

        Logger.info("Disposed 3D maze");
    }

    // ──────────────────────────────────────────────────────────────
    // Private creation helpers (no Javadoc changes needed)
    // ──────────────────────────────────────────────────────────────

    private void createMaterials(WorldMapColorScheme colorScheme) {
        materials3D = MazeMaterials3D.create(colorScheme, wallOpacityProperty());
    }

    private void createObstacles3D(MazeConfig3D mazeConfig, GameLevel level) {
        obstacles3D = new MazeObstacles3D();
        getChildren().add(obstacles3D);
        final float wallThickness = mazeConfig.obstacleWallThickness();
        final float cornerRadius  = mazeConfig.obstacleCornerRadius();
        obstacles3D.renderObstacles(level, wallThickness, cornerRadius, materials3D, wallBaseHeight);
    }

    private void createFloor3D(FloorConfig3D floorConfig, GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        floor3D = new MazeFloor3D(materials3D.floor(), width, height, floorConfig.thickness(), floorConfig.padding());
    }

    private void createArcadeHouse3D(AnimationRegistry animationRegistry, HouseConfig3D houseConfig, GameLevel level, WorldMapColorScheme colorScheme) {
        level.worldMap().terrainLayer().optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .ifPresentOrElse(arcadeHouse -> house3D = new MazeHouse3D(colorScheme, houseConfig, animationRegistry, arcadeHouse),
                () -> Logger.error("Currently only Arcade house is supported"));
    }
}
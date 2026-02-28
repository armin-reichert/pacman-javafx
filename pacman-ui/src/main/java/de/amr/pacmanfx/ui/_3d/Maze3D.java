/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Renders the 3D representation of a Pac-Man maze.
 * <p>
 * A {@code Maze3D} aggregates all visual 3D components of a level:
 * <ul>
 *   <li>{@link MazeMaterials3D} – shared materials and opacity bindings</li>
 *   <li>{@link MazeFloor3D} – the floor plane beneath the maze</li>
 *   <li>{@link MazeObstacles3D} – walls, corners, and other static geometry</li>
 *   <li>{@link MazeHouse3D} – the ghost house, if present in the map</li>
 *   <li>{@link MazeFood3D} – pellets, energizers, and edible items</li>
 * </ul>
 * The class is responsible for constructing these components, applying
 * color schemes, reading 3D preferences, and managing their lifecycle.
 * <p>
 * A {@code Maze3D} is a JavaFX {@link Group} and can be added directly to a scene graph.
 * It implements {@link Disposable} to ensure all 3D resources are released when no longer needed.
 */
public class Maze3D extends Group implements Disposable {

    private final PreferencesManager prefs;
    private final WorldMapColorScheme colorScheme;
    private final AnimationRegistry animationRegistry;

    /** Base height of walls in world units. Can be externally bound. */
    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);

    /** Opacity applied to all wall materials. Can be externally bound. */
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private MazeMaterials3D materials3D;
    private MazeObstacles3D obstacles3D;
    private MazeFloor3D floor3D;
    private MazeHouse3D house3D;
    private MazeFood3D food3D;

    private final Group particlesGroup = new Group();

    /**
     * Creates a new 3D maze for the given level.
     *
     * @param uiConfig         the game UI configuration
     * @param prefs            the UI preferences
     * @param level            the game level whose world map is rendered
     * @param animationRegistry registry for registering animations used by 3D components
     * @param ghostMaterials   materials used for rendering ghost-related 3D elements
     * @throws NullPointerException if any required argument is {@code null}
     */
    public Maze3D(UIConfig uiConfig, PreferencesManager prefs, GameLevel level, AnimationRegistry animationRegistry, List<PhongMaterial> ghostMaterials) {
        requireNonNull(uiConfig);
        this.prefs = requireNonNull(prefs);
        this.animationRegistry = requireNonNull(animationRegistry);
        requireNonNull(ghostMaterials);
        this.colorScheme = adjustColorScheme(uiConfig.colorScheme(level.worldMap()));

        createMaterials();
        createFloor3D(level);
        createObstacles3D(level);
        level.worldMap().terrainLayer().optHouse()
            .filter(ArcadeHouse.class::isInstance)
            .map(ArcadeHouse.class::cast)
            .ifPresentOrElse(
                this::createHouse3D,
                () -> Logger.error("For creating 3D house, currently only Arcade house is supported"));
        createMazeFood3D(level, ghostMaterials);
    }

    /**
     * Ensures sufficient contrast when the wall fill color is extremely dark.
     * This prevents walls from visually merging with the floor.
     *
     * @param proposedColorScheme the original color scheme
     * @return a possibly adjusted color scheme with improved contrast
     */
    private WorldMapColorScheme adjustColorScheme(WorldMapColorScheme proposedColorScheme) {
        final boolean isFillColorDark = Color.valueOf(proposedColorScheme.wallFill()).getBrightness() < 0.1;
        return isFillColorDark
            ? new WorldMapColorScheme(PlayScene3D.DARK_WALL_FILL_COLOR, proposedColorScheme.wallStroke(),
                proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    /** @return the property controlling the base height of all walls */
    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    /** @return the property controlling the opacity of all wall materials */
    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    /** @return the effective color scheme used for rendering the maze */
    public WorldMapColorScheme colorScheme() {
        return colorScheme;
    }

    /** @return the shared 3D materials used by maze components */
    public MazeMaterials3D materials() {
        return materials3D;
    }

    /** @return the 3D floor component */
    public MazeFloor3D floor() {
        return floor3D;
    }

    /** @return the 3D ghost house component, or {@code null} if the map has no house */
    public MazeHouse3D house() {
        return house3D;
    }

    /** @return the 3D food component (pellets, energizers, etc.) */
    public MazeFood3D food() {
        return food3D;
    }

    public Group particlesGroup() {
        return particlesGroup;
    }

    public double floorTop() {
        return floor3D.getTranslateZ() - 0.5 * floor3D.getDepth();
    }

    /**
     * Disposes all 3D resources created by this maze.
     * <p>
     * This includes unbinding properties, disposing materials, floor, obstacles,
     * house, food, and all {@link Wall3D} instances added as children.
     * After disposal, the maze should not be used again.
     */
    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (materials3D != null) {
            materials3D.dispose();
            materials3D = null;
        }
        if (floor3D != null) {
            floor3D.dispose();
            floor3D = null;
        }
        if (obstacles3D != null) {
            obstacles3D.dispose();
            obstacles3D = null;
        }
        if (house3D != null) {
            house3D.dispose();
            house3D = null;
        }
        if (food3D != null) {
            food3D.dispose();
            food3D = null;
        }
        getChildren().forEach(Wall3D::dispose);
        getChildren().clear();
        Logger.info("Disposed 3D maze");
    }

    /** Creates all shared materials used by the maze and binds them to the opacity property. */
    private void createMaterials() {
        materials3D = MazeMaterials3D.create(colorScheme, wallOpacityProperty());
    }

    /**
     * Creates and renders all static obstacles (walls, corners, etc.) of the maze.
     *
     * @param level the level whose world map defines the obstacle layout
     */
    private void createObstacles3D(GameLevel level) {
        obstacles3D = new MazeObstacles3D();
        getChildren().add(obstacles3D);
        final float wallThickness = PlayScene3D.OBSTACLE_WALL_THICKNESS;
        final float cornerRadius = PlayScene3D.OBSTACLE_CORNER_RADIUS;
        obstacles3D.renderObstacles(level, wallThickness, cornerRadius, materials3D, wallBaseHeight);
    }

    /**
     * Creates the 3D floor plane based on world map dimensions and user preferences.
     *
     * @param level the level whose world map determines the floor size
     */
    private void createFloor3D(GameLevel level) {
        final Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        final float width = worldSizePx.x() + 2 * PlayScene3D.FLOOR_PADDING;
        final float height = worldSizePx.y();
        floor3D = new MazeFloor3D(materials3D.floor(), width, height, PlayScene3D.FLOOR_THICKNESS, PlayScene3D.FLOOR_PADDING);
    }

    /**
     * Creates the 3D ghost house and adds it to the scene graph.
     *
     * @param house the house model from the world map
     */
    private void createHouse3D(ArcadeHouse house) {
        house3D = new MazeHouse3D(colorScheme, animationRegistry, house);
        getChildren().add(house3D.root());
    }

    /**
     * Creates the 3D food layer (pellets, energizers, bonus items).
     *
     * @param level          the level providing pellet and energizer positions
     * @param ghostMaterials materials used for ghost-related visual effects
     */
    private void createMazeFood3D(GameLevel level, List<PhongMaterial> ghostMaterials) {
        food3D = new MazeFood3D(colorScheme, animationRegistry, level, ghostMaterials, this);
    }
}

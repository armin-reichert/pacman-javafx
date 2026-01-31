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
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import de.amr.pacmanfx.uilib.model3D.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_FLOOR_COLOR;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;
import static java.util.Objects.requireNonNull;

public class Maze3D extends Group implements Disposable {

    /** Normalized wall top color for very dark colors */
    private static final String DARK_GRAY = "0x2a2a2a";

    private final GameUI ui;
    private final GameLevel level;
    private final AnimationRegistry animationRegistry;

    private final DoubleProperty wallBaseHeight = new SimpleDoubleProperty(Wall3D.DEFAULT_BASE_HEIGHT);
    private final DoubleProperty wallOpacity = new SimpleDoubleProperty(1);

    private WorldMapColorScheme colorScheme;
    private MazeFloor3D mazeFloor3D;
    private ArcadeHouse3D house3D;
    private MazeFood3D mazeFood3D;

    private int wall3DCount;

    private PhongMaterial floorMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    public Maze3D(GameUI ui, GameLevel level, AnimationRegistry animationRegistry, List<PhongMaterial> ghostMaterials) {
        this.ui = ui;
        this.level = level;
        this.animationRegistry = animationRegistry;

        createWorldMapColorScheme(level.worldMap());
        createMaterials();
        createFloor3D();
        createObstacles3D(level.worldMap());
        level.worldMap().terrainLayer().optHouse().ifPresent(house -> {
            createHouse3D(house);
            getChildren().add(house3D);
        });
        createMazeFood3D(ghostMaterials);
    }

    public WorldMapColorScheme colorScheme() {
        return colorScheme;
    }

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public PhongMaterial wallTopMaterial() {
        return wallTopMaterial;
    }

    public MazeFloor3D mazeFloor3D() {
        return mazeFloor3D;
    }

    public ArcadeHouse3D house3D() {
        return house3D;
    }

    public MazeFood3D mazeFood3D() {
        return mazeFood3D;
    }

    @Override
    public void dispose() {
        wallBaseHeight.unbind();
        wallOpacity.unbind();

        if (floorMaterial != null) {
            floorMaterial.diffuseColorProperty().unbind();
            floorMaterial.specularColorProperty().unbind();
            floorMaterial = null;
        }
        if (wallBaseMaterial != null) {
            wallBaseMaterial.diffuseColorProperty().unbind();
            wallBaseMaterial.specularColorProperty().unbind();
            wallBaseMaterial = null;
        }
        if (wallTopMaterial != null) {
            wallTopMaterial.diffuseColorProperty().unbind();
            wallTopMaterial.specularColorProperty().unbind();
            wallTopMaterial = null;
        }
        if (mazeFloor3D != null) {
            mazeFloor3D.dispose();
            mazeFloor3D = null;
        }
        if (house3D != null) {
            house3D.openProperty().removeListener(this::handleHouseOpenChange);
            house3D.dispose();
            house3D = null;
            Logger.info("Disposed 3D house");
        }
        Logger.info("Removed 'house open' listener");

        getChildren().forEach(Wall3D::dispose);


        mazeFood3D.dispose();
        getChildren().clear();
        Logger.info("Disposed 3D maze");
    }

    private void createWorldMapColorScheme(WorldMap worldMap) {
        final WorldMapColorScheme proposedColorScheme = ui.currentConfig().colorScheme(worldMap);
        requireNonNull(proposedColorScheme);
        // Add some contrast with dark floor if wall fill color is very dark
        final boolean veryDarkWallFillColor = Color.valueOf(proposedColorScheme.wallFill()).getBrightness() < 0.1;
        colorScheme = veryDarkWallFillColor
            ? new WorldMapColorScheme(DARK_GRAY, proposedColorScheme.wallStroke(), proposedColorScheme.door(), proposedColorScheme.pellet())
            : proposedColorScheme;
    }

    private void createMaterials() {
        floorMaterial = Ufx.colorSensitivePhongMaterial(PROPERTY_3D_FLOOR_COLOR);
        floorMaterial.setSpecularPower(128);

        final ObservableValue<Color> diffuseColorProperty = wallOpacityProperty()
            .map(opacity -> colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue()));
        wallBaseMaterial = Ufx.colorSensitivePhongMaterial(diffuseColorProperty);
        wallBaseMaterial.setSpecularPower(64);

        wallTopMaterial = defaultPhongMaterial(Color.valueOf(colorScheme.wallFill()));
    }

    private void createObstacles3D(WorldMap worldMap) {
        final float wallThickness = ui.prefs().getFloat("3d.obstacle.wall_thickness");
        final float cornerRadius = ui.prefs().getFloat("3d.obstacle.corner_radius");
        final House house = worldMap.terrainLayer().optHouse().orElse(null);
        final var stopWatch = new StopWatch();
        final var terrainRenderer3D = new TerrainRenderer3D();
        terrainRenderer3D.setOnWallCreated(wall3D -> {
            ++wall3DCount;
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            wall3D.bindBaseHeight(wallBaseHeightProperty());
            getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        wall3DCount = 0;
        for (Obstacle obstacle : worldMap.terrainLayer().obstacles()) {
            final Vector2i obstacleStartTile = tileAt(obstacle.startPoint().toVector2f());
            // exclude house placeholder obstacle
            if (house == null || !house.contains(obstacleStartTile)) {
                terrainRenderer3D.renderObstacle3D(obstacle, isWorldBorder(worldMap, obstacle), wallThickness, cornerRadius);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Built {} composite walls in {} milliseconds", wall3DCount, passedTimeMillis);
    }

    private void createFloor3D() {
        mazeFloor3D = new MazeFloor3D(ui.prefs(), level, floorMaterial);
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == worldMap.terrainLayer().emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private void createHouse3D(House house) {
        final Vector2i[] ghostRevivalTiles = {
                house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
                house.ghostRevivalTile(PINK_GHOST_SPEEDY),
                house.ghostRevivalTile(ORANGE_GHOST_POKEY)
        };
        // Note: revival tile is the left of the pair of tiles in the house where the ghost is placed. The center
        //       of the 3D shape is one tile to the right and a half tile to the bottom from the tile origin.
        final Vector2f[] ghostRevivalPositions = Stream.of(ghostRevivalTiles)
                .map(tile -> tile.scaled((float) TS).plus(TS, HTS))
                .toArray(Vector2f[]::new);

        house3D = new ArcadeHouse3D(
            animationRegistry,
            house,
            ghostRevivalPositions,
            ui.prefs().getFloat("3d.house.base_height"),
            ui.prefs().getFloat("3d.house.wall_thickness"),
            ui.prefs().getFloat("3d.house.opacity")
        );
        house3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        house3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        house3D.setDoorColor(Color.valueOf(colorScheme.door()));
        house3D.wallBaseHeightProperty().set(ui.prefs().getFloat("3d.house.base_height"));
        house3D.openProperty().addListener(this::handleHouseOpenChange);
        house3D.setDoorSensitivity(ui.prefs().getFloat("3d.house.sensitivity"));
    }

    private void createMazeFood3D(List<PhongMaterial> ghostMaterials) {
        mazeFood3D = new MazeFood3D(ui.prefs(), animationRegistry, level, colorScheme, ghostMaterials, house3D.swirls());
    }

    private void handleHouseOpenChange(ObservableValue<? extends Boolean> obs, boolean wasOpen, boolean isOpen) {
        if (isOpen && house3D != null) {
            house3D.doorsOpenCloseAnimation().playFromStart();
        }
    }
}

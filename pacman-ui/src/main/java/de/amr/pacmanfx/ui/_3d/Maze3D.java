/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
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
    private MazeHouse3D mazeHouse3D;
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
        level.worldMap().terrainLayer().optHouse().ifPresent(this::createHouse3D);
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

    public MazeHouse3D mazeHouse3D() {
        return mazeHouse3D;
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
        if (mazeHouse3D != null) {
            mazeHouse3D.dispose();
            mazeHouse3D = null;
        }
        if (mazeFood3D != null) {
            mazeFood3D.dispose();
            mazeFood3D = null;
        }
        getChildren().forEach(Wall3D::dispose);
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
        mazeHouse3D = new MazeHouse3D(ui.prefs(), colorScheme, animationRegistry, house);
        getChildren().add(mazeHouse3D);
    }

    private void createMazeFood3D(List<PhongMaterial> ghostMaterials) {
        mazeFood3D = new MazeFood3D(ui.prefs(), colorScheme, animationRegistry, level, ghostMaterials,
            mazeHouse3D.arcadeHouse3D().swirls());
    }
}

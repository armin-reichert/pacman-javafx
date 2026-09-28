package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.StopWatch;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.obstacle.Obstacle;
import de.amr.pacmanfx.ui.settings.world.Floor3DSettings;
import de.amr.pacmanfx.ui.settings.world.Maze3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.view3d.TerrainRenderer3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.tinylog.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MapView3DFactory {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;
    public static final int WALL_TOP_SPECULAR_POWER = 128;

    public MapView3D createMapView3D(
        Predicate<Vector2f> obstacleStartPointIgnored,
        TerrainLayer terrain,
        WorldSettings worldSettings,
        WorldMapColorScheme colorScheme)
    {
        requireNonNull(obstacleStartPointIgnored);
        requireNonNull(terrain);
        requireNonNull(worldSettings);
        requireNonNull(colorScheme);

        final var mapView3D = new MapView3D(terrain, createMazeMaterials(colorScheme));
        buildFloor(mapView3D, terrain, worldSettings.floor());
        addObstacles(mapView3D, terrain, worldSettings.maze(), obstacleStartPointIgnored);
        bindWallBaseMaterialColor(mapView3D, mapView3D.materials().wallBaseMaterial(), Color.valueOf(colorScheme.wallStroke()));

        return mapView3D;
    }

    private void buildFloor(MapView3D mapView3D, TerrainLayer terrain, Floor3DSettings floorConfig) {
        final Vector2i terrainSize = terrain.sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        final float thickness = floorConfig.thickness();

        final Box floor3D = new Box(width, height, thickness);
        floor3D.drawModeProperty().bindBidirectional(mapView3D.drawModeProperty());
        floor3D.setMaterial(mapView3D.materials().floorMaterial());

        floor3D.setTranslateX(0.5 * width - floorConfig.padding());
        floor3D.setTranslateY(0.5 * height);
        floor3D.setTranslateZ(0.5 * thickness);

        mapView3D.setFloor3D(floor3D);

        final PhongMaterial floorMaterial = mapView3D.materials().floorMaterial();
        floorMaterial.diffuseColorProperty().bind(mapView3D.floorColorProperty());
        floorMaterial.specularColorProperty().bind(mapView3D.floorColorProperty().map(Color::brighter));
    }

    private void addObstacles(
        MapView3D mapView3D, TerrainLayer terrain, Maze3DSettings maze3DSettings,
        Predicate<Vector2f> obstacleStartPointIgnored) {
        final float wallThickness = maze3DSettings.obstacleWallThickness();
        final TerrainRenderer3D renderer3D = new TerrainRenderer3D();
        final AtomicInteger wallCount = new AtomicInteger(0);
        renderer3D.setOnWallCreatedCallback(wall3D -> {
            wallCount.incrementAndGet();
            wall3D.setBaseMaterial(mapView3D.materials().wallBaseMaterial());
            wall3D.setTopMaterial(mapView3D.materials().wallTopMaterial());
            wall3D.bindBaseHeight(mapView3D.wallBaseHeightProperty());
            wall3D.base().drawModeProperty().bindBidirectional(mapView3D.drawModeProperty());
            wall3D.top() .drawModeProperty().bindBidirectional(mapView3D.drawModeProperty());
            mapView3D.root().getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (obstacleStartPointIgnored.test(startPoint)) continue;
            renderer3D.renderObstacle3D(obstacle, isWorldBorder(terrain, obstacle), wallThickness, 4);
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Building {} composite walls took {} milliseconds", wallCount, passedTimeMillis);

    }

    private boolean isWorldBorder(TerrainLayer terrain, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == terrain.emptyRowsOverMaze() * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == terrain.numCols() * TS;
        }
    }

    private MapView3D.Materials createMazeMaterials(WorldMapColorScheme colorScheme) {
        final PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));
        wallTopMaterial.setSpecularPower(WALL_TOP_SPECULAR_POWER);

        return new MapView3D.Materials(floorMaterial, wallBaseMaterial, wallTopMaterial);
    }

    private void bindWallBaseMaterialColor(MapView3D mapView3D, PhongMaterial wallBaseMaterial, Color wallStrokeColor) {
        wallBaseMaterial.diffuseColorProperty().bind(mapView3D.wallOpacityProperty()
            .map(opacity -> Ufx.colorWithOpacity(wallStrokeColor, opacity.doubleValue()))
        );
    }
}

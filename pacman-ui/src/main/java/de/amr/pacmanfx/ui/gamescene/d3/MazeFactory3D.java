package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.StopWatch;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.obstacle.Obstacle;
import de.amr.pacmanfx.ui.settings.world.Floor3DSettings;
import de.amr.pacmanfx.ui.settings.world.House3DSettings;
import de.amr.pacmanfx.ui.settings.world.Maze3DSettings;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.world.TerrainRenderer3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.tinylog.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class MazeFactory3D {

    public static final int FLOOR_SPECULAR_POWER = 128;
    public static final int WALL_BASE_SPECULAR_POWER = 64;
    public static final int WALL_TOP_SPECULAR_POWER = 128;

    public Maze3D createMaze3D(
        House house,
        TerrainLayer terrain,
        WorldSettings worldSettings,
        WorldMapColorScheme colorScheme)
    {
        requireNonNull(house);
        requireNonNull(terrain);
        requireNonNull(worldSettings);
        requireNonNull(colorScheme);

        final var maze3D = new Maze3D(terrain, createMazeMaterials(colorScheme));
        createHouse3D(house, worldSettings.house(), colorScheme);
        buildFloor(maze3D, terrain, worldSettings.floor());
        addObstacles(maze3D, house, terrain, worldSettings.maze());
        bindWallBaseMaterialColor(maze3D, maze3D.materials().wallBaseMaterial(), Color.valueOf(colorScheme.wallStroke()));

        return maze3D;
    }

    private void buildFloor(Maze3D maze3D, TerrainLayer terrain,  Floor3DSettings floorConfig) {
        final Vector2i terrainSize = terrain.sizeInPixel();
        final float width = terrainSize.x() + 2 * floorConfig.padding();
        final float height = terrainSize.y();
        final float thickness = floorConfig.thickness();

        final Box floor3D = new Box(width, height, thickness);
        floor3D.drawModeProperty().bindBidirectional(maze3D.drawModeProperty());
        floor3D.setMaterial(maze3D.materials().floorMaterial());

        floor3D.setTranslateX(0.5 * width - floorConfig.padding());
        floor3D.setTranslateY(0.5 * height);
        floor3D.setTranslateZ(0.5 * thickness);

        maze3D.setFloor3D(floor3D);

        final PhongMaterial floorMaterial = maze3D.materials().floorMaterial();
        floorMaterial.diffuseColorProperty().bind(maze3D.floorColorProperty());
        floorMaterial.specularColorProperty().bind(maze3D.floorColorProperty().map(Color::brighter));
    }

    private void addObstacles(Maze3D maze3D, House house, TerrainLayer terrain, Maze3DSettings maze3DSettings) {
        final float wallThickness = maze3DSettings.obstacleWallThickness();
        final TerrainRenderer3D renderer3D = new TerrainRenderer3D();
        final AtomicInteger wallCount = new AtomicInteger(0);
        renderer3D.setOnWallCreated(wall3D -> {
            wallCount.incrementAndGet();
            wall3D.setBaseMaterial(maze3D.materials().wallBaseMaterial());
            wall3D.setTopMaterial(maze3D.materials().wallTopMaterial());
            wall3D.bindBaseHeight(maze3D.wallBaseHeightProperty());
            wall3D.base().drawModeProperty().bindBidirectional(maze3D.drawModeProperty());
            wall3D.top() .drawModeProperty().bindBidirectional(maze3D.drawModeProperty());
            maze3D.root().getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });

        final var stopWatch = new StopWatch();
        // render all obstacles found in map except the house placeholder obstacle
        for (Obstacle obstacle : terrain.obstacles()) {
            final Vector2f startPoint = obstacle.startPoint().toVector2f();
            if (house == null || !house.contains(PositionSystem.computeTileAt(startPoint))) {
                renderer3D.renderObstacle3D(obstacle, isWorldBorder(terrain, obstacle), wallThickness, 4);
            }
        }
        final var passedTimeMillis = stopWatch.passedTime().toMillis();
        Logger.info("Building {} composite walls took {} milliseconds", wallCount, passedTimeMillis);

    }

    private boolean isWorldBorder(TerrainLayer terrain, Obstacle obstacle) {
        final Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == WorldMap.TS || start.y() == terrain.emptyRowsOverMaze() * WorldMap.TS + WorldMap.HTS;
        } else {
            return start.x() == 0 || start.x() == terrain.numCols() * WorldMap.TS;
        }
    }

    private void createHouse3D(House house, House3DSettings config3D, WorldMapColorScheme colorScheme) {
        if (!house.hasComp(House3DViewComp.class)) {
            final var view3D = new House3DViewComp(
                house.floorplan(),
                config3D.baseHeight(),
                config3D.wallThickness(),
                config3D.opacity()
            );
            house.setComp(House3DViewComp.class, view3D);
        }

        // apply color scheme
        final var view3D = house.reqComp(House3DViewComp.class);
        view3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        view3D.wallBaseHeightProperty().set(config3D.baseHeight());
        view3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        view3D.setDoorColor(Color.valueOf(colorScheme.door()));
        view3D.setDoorSensitivity(config3D.sensitivity());
    }

    private Maze3D.Materials createMazeMaterials(WorldMapColorScheme colorScheme) {
        final PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.setSpecularPower(FLOOR_SPECULAR_POWER);

        final PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.setSpecularPower(WALL_BASE_SPECULAR_POWER);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));
        wallTopMaterial.setSpecularPower(WALL_TOP_SPECULAR_POWER);

        return new Maze3D.Materials(
            floorMaterial,
            wallBaseMaterial,
            wallTopMaterial
        );
    }

    private void bindWallBaseMaterialColor(Maze3D maze3D, PhongMaterial wallBaseMaterial, Color wallStrokeColor) {
        wallBaseMaterial.diffuseColorProperty().bind(maze3D.wallOpacityProperty()
            .map(opacity -> Ufx.colorWithOpacity(wallStrokeColor, opacity.doubleValue()))
        );
    }
}

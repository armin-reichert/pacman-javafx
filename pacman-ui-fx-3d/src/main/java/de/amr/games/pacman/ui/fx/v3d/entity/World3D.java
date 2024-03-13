/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.NO_TEXTURE;

/**
 * 3D-model for the world in a game level. Walls and doors are created from 2D information specified by a floor plan.
 *
 * @author Armin Reichert
 */
public class World3D {

    private static final double FLOOR_THICKNESS = 0.4;

    public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);
    public final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity", 0.5);
    public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(this, "wallThickness", 1.0);

    public final ObjectProperty<String> floorTexturePy = new SimpleObjectProperty<>(this, "floorTexture", NO_TEXTURE) {
        @Override
        protected void invalidated() {
            updateFloorMaterial();
        }
    };

    public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            updateFloorMaterial();
        }
    };

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final World world;
    private final FloorPlan floorPlan;
    private final Group root = new Group();
    private final Group floorGroup = new Group();
    private final Group wallsGroup = new Group();
    private final PointLight houseLight;

    private final MazeFactory factory;
    private final Map<String, PhongMaterial> floorTextures;

    public World3D(World world, FloorPlan floorPlan, Map<String, PhongMaterial> floorTextures, MazeFactory factory) {
        checkNotNull(world);
        checkNotNull(floorPlan);
        checkNotNull(floorTextures);
        checkNotNull(factory);

        this.world = world;
        this.floorPlan = floorPlan;
        this.floorTextures = floorTextures;
        this.factory = factory;
        factory.wallOpacityPy.bind(wallOpacityPy);

        Vector2f houseCenter = world.house().topLeftTile().toFloatVec().scaled(TS).plus(world.house().size().toFloatVec().scaled(HTS));
        houseLight = new PointLight();
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(houseCenter.x());
        houseLight.setTranslateY(houseCenter.y());
        houseLight.setTranslateZ(-TS);

        buildFloor();
        buildWorld();

        root.getChildren().addAll(floorGroup, wallsGroup, houseLight);
    }

    public Node root() {
        return root;
    }

    public PointLight houseLighting() {
        return houseLight;
    }

    private void buildFloor() {
        var sizeX = world.numCols() * TS - 1;
        var sizeY = world.numRows() * TS - 1;
        var sizeZ = FLOOR_THICKNESS;
        var floor = new Box(sizeX, sizeY, sizeZ);
        floor.drawModeProperty().bind(drawModePy);
        floorGroup.getChildren().add(floor);
        floorGroup.getTransforms().add(new Translate(0.5 * sizeX, 0.5 * sizeY, 0.5 * sizeZ));
        updateFloorMaterial();
    }

    private Box floor() {
        return (Box) floorGroup.getChildren().getFirst();
    }

    private void updateFloorMaterial() {
        floor().setMaterial(floorTextures.getOrDefault("texture." + floorTexturePy.get(), coloredMaterial(floorColorPy.get())));
    }

    private WallData createWallData() {
        var wallData = new WallData();
        wallData.brickSize = (float) TS / floorPlan.resolution();
        return wallData;
    }

    private void buildWorld() {
        wallsGroup.getChildren().clear();
        addCorners(createWallData());
        addHorizontalWalls(createWallData());
        addVerticalWalls(createWallData());
        Logger.info("3D world created (resolution={}, wall height={})", floorPlan.resolution(), wallHeightPy.get());
    }


    private void addHorizontalWalls(WallData wallData) {
        wallData.type = FloorPlan.HWALL;
        wallData.numBricksY = 1;
        for (int y = 0; y < floorPlan.sizeY(); ++y) {
            wallData.x = -1;
            wallData.y = y;
            wallData.numBricksX = 0;
            for (int x = 0; x < floorPlan.sizeX(); ++x) {
                if (floorPlan.cell(x, y) == FloorPlan.HWALL) {
                    if (wallData.numBricksX == 0) {
                        wallData.x = x;
                    }
                    wallData.numBricksX++;
                } else if (wallData.numBricksX > 0) {
                    addWall(wallData);
                    wallData.numBricksX = 0;
                }
            }
            if (wallData.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
                addWall(wallData);
            }
        }
    }

    private void addVerticalWalls(WallData wallData) {
        wallData.type = FloorPlan.VWALL;
        wallData.numBricksX = 1;
        for (int x = 0; x < floorPlan.sizeX(); ++x) {
            wallData.x = x;
            wallData.y = -1;
            wallData.numBricksY = 0;
            for (int y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.VWALL) {
                    if (wallData.numBricksY == 0) {
                        wallData.y = y;
                    }
                    wallData.numBricksY++;
                } else if (wallData.numBricksY > 0) {
                    addWall(wallData);
                    wallData.numBricksY = 0;
                }
            }
            if (wallData.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
                addWall(wallData);
            }
        }
    }

    private void addCorners(WallData wallData) {
        wallData.type = FloorPlan.CORNER;
        wallData.numBricksX = 1;
        wallData.numBricksY = 1;
        for (int x = 0; x < floorPlan.sizeX(); ++x) {
            for (int y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.CORNER) {
                    wallData.x = x;
                    wallData.y = y;
                    addWall(wallData);
                }
            }
        }
    }

    private boolean isWallInsideHouse(WallData wallData, House house) {
        int resolution = floorPlan.resolution();
        Vector2i bottomRightTile = house.topLeftTile().plus(house.size());
        double xMin = house.topLeftTile().x() * resolution;
        double yMin = house.topLeftTile().y() * resolution;
        double xMax = bottomRightTile.x() * resolution - resolution;
        double yMax = bottomRightTile.y() * resolution - resolution;
        return wallData.x > xMin && wallData.y > yMin && wallData.x <= xMax && wallData.y <= yMax;
    }

    private void addWall(WallData wallData) {
        boolean partOfHouse = world.house().contains(floorPlan.tileOfCell(wallData.x, wallData.y));
        if (partOfHouse) {
            // only outer house wall gets built
            if (!isWallInsideHouse(wallData, world.house())) {
                wallsGroup.getChildren().add(factory.createHouseWall(wallData));
            }
        } else {
            wallsGroup.getChildren().add(factory.createMazeWall(wallData, wallThicknessPy, wallHeightPy));
        }
    }
}
/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.animation.FoodOscillation;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * 3D-model for the world in a game level. Walls and doors are created from 2D information specified by a floor plan.
 *
 * @author Armin Reichert
 */
public class World3D {
    private static final double FLOOR_THICKNESS = 0.25;

    private static class WallData {
        byte type; // see FloorPlan
        int x;
        int y;
        int numBricksX;
        int numBricksY;
        float brickSize;
    }

    public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);

    public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(this, "wallThickness", 1.0);

    public final ObjectProperty<String> floorTexturePy = new SimpleObjectProperty<>(this, "floorTexture",
        PacManGames3dUI.NO_TEXTURE) {
        @Override
        protected void invalidated() {
            Logger.trace("Floor texture change detected");
            updateFloorMaterial(floor());
        }
    };

    public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            Logger.trace("Floor color change detected");
            updateFloorMaterial(floor());
        }
    };

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Theme theme;
    private final Model3D pelletModel3D;
    private final World world;
    private final Group root = new Group();
    private final Group floorGroup = new Group();
    private final Group wallsGroup = new Group();
    private final List<DoorWing3D> doorWings3D = new ArrayList<>();
    private final Group doorGroup = new Group();
    private final PointLight houseLight;
    private final Group foodGroup = new Group();
    private final FoodOscillation foodOscillation;

    private final Color foodColor;
    private final Color doorColor;
    private final PhongMaterial baseMaterial;
    private final PhongMaterial topMaterial;
    private final PhongMaterial houseMaterial;

    public World3D(World world, Theme theme, Model3D pelletModel3D, Color foodColor, Color wallBaseColor,
                   Color wallTopColor, Color doorColor) {

        checkNotNull(world);
        checkNotNull(theme);
        checkNotNull(pelletModel3D);
        checkNotNull(foodColor);
        checkNotNull(wallBaseColor);
        checkNotNull(wallTopColor);
        checkNotNull(doorColor);

        this.world = world;
        this.theme = theme;
        this.pelletModel3D = pelletModel3D;
        this.foodColor = foodColor;
        this.doorColor = doorColor;
        this.baseMaterial = ResourceManager.coloredMaterial(wallBaseColor);
        this.topMaterial = ResourceManager.coloredMaterial(wallTopColor);

        //TODO this should not depend on specific color value
        var ghostHouseColor = wallBaseColor.equals(Color.rgb(222, 222, 255))
            ? Color.rgb(200, 200, 255) : wallBaseColor;
        this.houseMaterial = ResourceManager.coloredMaterial(ResourceManager.color(ghostHouseColor, 0.25));

        this.houseLight = createGhostHouseLight(wallBaseColor);
        this.foodOscillation = new FoodOscillation(foodGroup);

        buildFloor();
        buildWorld(4);
        addFood();

        root.getChildren().addAll(floorGroup, wallsGroup, doorGroup, houseLight, foodGroup);
    }

    private PointLight createGhostHouseLight(Color lightColor) {
        var light = new PointLight();
        light.setColor(lightColor);
        light.setMaxRange(3 * TS);
        Vector2f center = world.house().seat("middle");
        light.setTranslateX(center.x() + HTS);
        light.setTranslateY(center.y());
        light.setTranslateZ(-TS);
        return light;
    }

    public Node getRoot() {
        return root;
    }

    public PointLight houseLighting() {
        return houseLight;
    }

    public FoodOscillation foodOscillation() {
        return foodOscillation;
    }

    private void buildFloor() {
        var sizeX = world.numCols() * TS - 1;
        var sizeY = world.numRows() * TS - 1;
        var sizeZ = FLOOR_THICKNESS;
        var floor = new Box(sizeX, sizeY, sizeZ);
        floor.drawModeProperty().bind(drawModePy);
        floorGroup.getChildren().add(floor);
        floorGroup.getTransforms().add(new Translate(0.5 * sizeX, 0.5 * sizeY, 0.5 * sizeZ));
        updateFloorMaterial(floor);
    }

    private Box floor() {
        return (Box) floorGroup.getChildren().get(0);
    }

    private void updateFloorMaterial(Box floor) {
        String key = floorTexturePy.get();
        PhongMaterial texture = theme.get("texture." + key);
        if (texture == null) {
            texture = ResourceManager.coloredMaterial(floorColorPy.get());
        }
        floor.setMaterial(texture);
    }

    private WallData createWallData(int resolution) {
        var wallData = new WallData();
        wallData.brickSize = (float) TS / resolution;
        return wallData;
    }

    private void buildWorld(int resolution) {
        var floorPlan = new FloorPlan(world, resolution);
        wallsGroup.getChildren().clear();
        addCorners(floorPlan, createWallData(resolution));
        addHorizontalWalls(floorPlan, createWallData(resolution));
        addVerticalWalls(floorPlan, createWallData(resolution));
        addDoorWing(world.house().door().leftWing(), doorColor);
        addDoorWing(world.house().door().rightWing(), doorColor);
        Logger.info("3D world created (resolution={}, wall height={})", floorPlan.getResolution(), wallHeightPy.get());
    }

    public Stream<DoorWing3D> doorWings3D() {
        return doorWings3D.stream();
    }

    private void addDoorWing(Vector2i tile, Color doorWingColor) {
        var doorWing3D = new DoorWing3D(tile, doorWingColor);
        doorWing3D.drawModePy.bind(drawModePy);
        doorWings3D.add(doorWing3D);
        doorGroup.getChildren().add(doorWing3D.getRoot());
    }

    private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
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
                    addCompositeWall(floorPlan, wallData);
                    wallData.numBricksX = 0;
                }
            }
            if (wallData.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
                addCompositeWall(floorPlan, wallData);
            }
        }
    }

    private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
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
                    addCompositeWall(floorPlan, wallData);
                    wallData.numBricksY = 0;
                }
            }
            if (wallData.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
                addCompositeWall(floorPlan, wallData);
            }
        }
    }

    private void addCorners(FloorPlan floorPlan, WallData wallData) {
        wallData.type = FloorPlan.CORNER;
        wallData.numBricksX = 1;
        wallData.numBricksY = 1;
        for (int x = 0; x < floorPlan.sizeX(); ++x) {
            for (int y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.CORNER) {
                    wallData.x = x;
                    wallData.y = y;
                    addCompositeWall(floorPlan, wallData);
                }
            }
        }
    }

    private void addCompositeWall(FloorPlan floorPlan, WallData wallData) {
        final double topHeight = 0.5;
        final double ghostHouseHeight = 9.0;
        final Vector2i tile = floorPlan.tile(wallData.x, wallData.y);

        if (world.house().contains(tile)) {
            addHouseWall(floorPlan, wallData);
            return;
        }

        Box base = createWallOfType(wallData);
        Box middle = createWallOfType(wallData);
        Box top = createWallOfType(wallData);
        Group wall = new Group(middle, top);

        base.setMaterial(topMaterial);
        base.setDepth(topHeight);
        base.translateZProperty()
            .bind(middle.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));

        middle.depthProperty().bind(wallHeightPy);
        middle.translateZProperty().bind(wallHeightPy.multiply(-0.5));
        middle.setMaterial(baseMaterial);

        top.setMaterial(topMaterial);
        top.setDepth(topHeight);
        top.translateZProperty()
            .bind(middle.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));

        wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
        wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
        wall.setUserData(wallData);

        wallsGroup.getChildren().add(wall);
    }

    private Box createWallOfType(WallData wallData) {
        return switch (wallData.type) {
            case FloorPlan.HWALL -> horizontalWall(wallData);
            case FloorPlan.VWALL -> verticalWall(wallData);
            case FloorPlan.CORNER -> corner();
            default -> throw new IllegalStateException("Unknown wall type: " + wallData.type);
        };
    }

    private void addHouseWall(FloorPlan floorPlan, WallData wallData) {
        final double topHeight = 0.5;
        final double houseHeight = 9.0;
        final Vector2i tile = floorPlan.tile(wallData.x, wallData.y);

        Box base = createWallOfType(wallData);
        Box top = createWallOfType(wallData);
        Group wall = new Group(base, top);

        base.setDepth(houseHeight);
        base.setTranslateZ(-0.5 * houseHeight);
        base.setMaterial(houseMaterial);

        top.setMaterial(topMaterial);
        top.setDepth(topHeight);
        top.setTranslateZ(-houseHeight - 0.2);

        wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
        wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
        wall.setUserData(wallData);

        wallsGroup.getChildren().add(wall);
    }

    private Box horizontalWall(WallData wallData) {
        Box wall = new Box();
        // without ...+1 there are gaps. why?
        wall.setWidth((wallData.numBricksX + 1) * wallData.brickSize);
        wall.heightProperty().bind(wallThicknessPy);
        wall.drawModeProperty().bind(drawModePy);
        return wall;
    }

    private Box verticalWall(WallData wallData) {
        Box wall = new Box();
        wall.widthProperty().bind(wallThicknessPy);
        // without ...+1 there are gaps. why?
        wall.setHeight((wallData.numBricksY + 1) * wallData.brickSize);
        wall.drawModeProperty().bind(drawModePy);
        return wall;
    }

    private Box corner() {
        Box corner = new Box();
        corner.widthProperty().bind(wallThicknessPy);
        corner.heightProperty().bind(wallThicknessPy);
        corner.drawModeProperty().bind(drawModePy);
        return corner;
    }

    // Food

    private void addFood() {
        var foodMaterial = ResourceManager.coloredMaterial(foodColor);
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            Eatable3D food3D = world.isEnergizerTile(tile)
                ? createEnergizer3D(tile, foodMaterial)
                : createNormalPellet3D(tile, foodMaterial);
            foodGroup.getChildren().add(food3D.getRoot());
        });
    }

    private Pellet3D createNormalPellet3D(Vector2i tile, PhongMaterial material) {
        var pellet3D = new Pellet3D(pelletModel3D, 1.0);
        pellet3D.getRoot().setMaterial(material);
        pellet3D.placeAtTile(tile);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(Vector2i tile, PhongMaterial material) {
        var energizer3D = new Energizer3D(3.5);
        energizer3D.getRoot().setMaterial(material);
        energizer3D.placeAtTile(tile);
        var squirting = new Squirting(root) {
            @Override
            protected boolean reachesEndPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.getRoot());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(ResourceManager.coloredMaterial(foodColor.desaturate()));
        energizer3D.setEatenAnimation(squirting);
        return energizer3D;
    }

    /**
     * @return all 3D pellets, including energizers
     */
    public Stream<Eatable3D> eatables3D() {
        return foodGroup.getChildren().stream().map(Node::getUserData).map(Eatable3D.class::cast);
    }

    public Stream<Energizer3D> energizers3D() {
        return eatables3D().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
    }

    public Optional<Eatable3D> eatableAt(Vector2i tile) {
        checkTileNotNull(tile);
        return eatables3D().filter(eatable -> eatable.tile().equals(tile)).findFirst();
    }
}
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
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * 3D-model for the world in a game level. Walls and doors are created from 2D information specified by a floor plan.
 *
 * @author Armin Reichert
 */
public class World3D {

    public final DoubleProperty wallHeightPy         = new SimpleDoubleProperty(this, "wallHeight", 2.0);
    public final DoubleProperty wallOpacityPy        = new SimpleDoubleProperty(this, "wallOpacity", 0.5);
    public final DoubleProperty wallThicknessPy      = new SimpleDoubleProperty(this, "wallThickness", 1.0);
    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final World world;
    private final MazeFactory factory;
    private final FloorPlan floorPlan;

    private final Group root = new Group();
    private final Group floorGroup = new Group();
    private final Group wallsGroup = new Group();
    private final PointLight houseLight = new PointLight();
    private final Floor3D floor3D;

    public World3D(World world, FloorPlan floorPlan, Map<String, PhongMaterial> floorTextures, MazeFactory factory) {
        checkNotNull(world);
        checkNotNull(floorPlan);
        checkNotNull(floorTextures);
        checkNotNull(factory);

        this.world = world;
        this.floorPlan = floorPlan;
        this.factory = factory;
        factory.wallOpacityPy.bind(wallOpacityPy);

        root.getChildren().addAll(floorGroup, wallsGroup, houseLight);

        Vector2f houseCenter = world.house().topLeftTile().toFloatVec().scaled(TS).plus(world.house().size().toFloatVec().scaled(HTS));
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(houseCenter.x());
        houseLight.setTranslateY(houseCenter.y());
        houseLight.setTranslateZ(-TS);

        floor3D = new Floor3D(world.numCols() * TS - 1, world.numRows() * TS - 1, 0.4, floorTextures);
        floor3D.drawModeProperty().bind(drawModePy);
        floorGroup.getChildren().add(floor3D);
        floorGroup.getTransforms().add(new Translate(0.5 * floor3D.getWidth(), 0.5 * floor3D.getHeight(), 0.5 * floor3D.getDepth()));

        addCorners();
        addHorizontalWalls();
        addVerticalWalls();

        Logger.info("3D world created (resolution={}, wall height={})", floorPlan.resolution(), wallHeightPy.get());
    }

    public Node root() {
        return root;
    }

    public Floor3D floor() {
        return floor3D;
    }

    public void setHouseLightOn(boolean state) {
        houseLight.setLightOn(state);
    }

    private void addHorizontalWalls() {
        var wd = new WallData();
        wd.type = FloorPlan.HWALL;
        wd.numBricksY = 1;
        for (short y = 0; y < floorPlan.sizeY(); ++y) {
            wd.x = -1;
            wd.y = y;
            wd.numBricksX = 0;
            for (short x = 0; x < floorPlan.sizeX(); ++x) {
                if (floorPlan.cell(x, y) == FloorPlan.HWALL) {
                    if (wd.numBricksX == 0) {
                        wd.x = x;
                    }
                    wd.numBricksX++;
                } else if (wd.numBricksX > 0) {
                    addWall(wd);
                    wd.numBricksX = 0;
                }
            }
            if (wd.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
                addWall(wd);
            }
        }
    }

    private void addVerticalWalls() {
        var wd = new WallData();
        wd.type = FloorPlan.VWALL;
        wd.numBricksX = 1;
        for (short x = 0; x < floorPlan.sizeX(); ++x) {
            wd.x = x;
            wd.y = -1;
            wd.numBricksY = 0;
            for (short y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.VWALL) {
                    if (wd.numBricksY == 0) {
                        wd.y = y;
                    }
                    wd.numBricksY++;
                } else if (wd.numBricksY > 0) {
                    addWall(wd);
                    wd.numBricksY = 0;
                }
            }
            if (wd.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
                addWall(wd);
            }
        }
    }

    private void addCorners() {
        var wd = new WallData();
        wd.type = FloorPlan.CORNER;
        wd.numBricksX = 1;
        wd.numBricksY = 1;
        for (short x = 0; x < floorPlan.sizeX(); ++x) {
            for (short y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.CORNER) {
                    wd.x = x;
                    wd.y = y;
                    addWall(wd);
                }
            }
        }
    }

    private void addWall(WallData wd) {
        boolean partOfHouse = world.house().contains(floorPlan.tileOfCell(wd.x, wd.y));
        if (!partOfHouse) {
            wallsGroup.getChildren().add(factory.createMazeWall(wd, wallThicknessPy, wallHeightPy));
        } else if (!isWallInsideHouse(wd, world.house())) {
            // only outer house wall gets built
            wallsGroup.getChildren().add(factory.createHouseWall(wd));
        }
    }

    private boolean isWallInsideHouse(WallData wd, House house) {
        int res = floorPlan.resolution();
        Vector2i bottomRightTile = house.topLeftTile().plus(house.size());
        double xMin = house.topLeftTile().x() * res;
        double yMin = house.topLeftTile().y() * res;
        double xMax = (bottomRightTile.x() - 1) * res;
        double yMax = (bottomRightTile.y() - 1) * res;
        return wd.x > xMin && wd.y > yMin && wd.x <= xMax && wd.y <= yMax;
    }
}
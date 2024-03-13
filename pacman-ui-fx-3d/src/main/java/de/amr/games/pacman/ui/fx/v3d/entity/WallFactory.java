/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.model.world.FloorPlan;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.opaqueColor;

/**
 * @author Armin Reichert
 */
public class WallFactory {

    private static final double HOUSE_WALL_THICKNESS  = 0.2;
    private static final double HOUSE_WALL_OPACITY    = 0.25;
    private static final double HOUSE_WALL_TOP_HEIGHT  = 1.0;
    private static final double HOUSE_WALL_BASE_HEIGHT = 8.0;


    private static Color darker(Color color) {
        return color.deriveColor(0, 1.0, 0.85, 1.0);
    }

    private final DoubleProperty houseWallThicknessPy = new SimpleDoubleProperty(this, "houseWallThickness", HOUSE_WALL_THICKNESS);

    public final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity", 0.5) {
        @Override
        protected void invalidated() {
            Color color = opaqueColor(darker(wallMiddleColor), wallOpacityPy.get());
            wallMiddleMaterial.setDiffuseColor(color);
            wallMiddleMaterial.setSpecularColor(color.brighter());
        }
    };

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private Color wallBaseColor;
    private Color wallMiddleColor;
    private Color wallTopColor;

    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallMiddleMaterial;
    private PhongMaterial wallTopMaterial;

    private PhongMaterial houseMaterial;


    public WallFactory() {
        setWallBaseColor(Color.BLACK);
        setWallMiddleColor(Color.RED);
        setWallTopColor(Color.GOLD);
    }

    public void setWallBaseColor(Color color) {
        this.wallBaseColor = color;
        wallBaseMaterial = coloredMaterial(color);
    }

    public Color wallBaseColor() {
        return wallBaseColor;
    }

    public void setWallMiddleColor(Color color) {
        this.wallMiddleColor = color;
        wallMiddleMaterial = coloredMaterial(opaqueColor(darker(color), wallOpacityPy.get()));
        houseMaterial = coloredMaterial(opaqueColor(darker(color), HOUSE_WALL_OPACITY));
    }

    public Color wallMiddleColor() {
        return wallMiddleColor;
    }

    public void setWallTopColor(Color color) {
        this.wallTopColor = color;
        wallTopMaterial = coloredMaterial(color);
    }

    public Color wallTopColor() {
        return wallTopColor;
    }

    public Group createMazeWall(WallData wallData, DoubleProperty wallThicknessPy, DoubleProperty wallHeightPy) {
        final double baseHeight = 0.25;
        final double topHeight = 0.1;

        Box base = createBlock(wallData, wallBaseMaterial, wallThicknessPy);
        Box mid = createBlock(wallData, wallMiddleMaterial, wallThicknessPy);
        Box top = createBlock(wallData, wallTopMaterial, wallThicknessPy);
        Group wall = new Group(base, mid, top);

        base.setDepth(baseHeight);
        top.setDepth(topHeight);
        mid.depthProperty().bind(wallHeightPy.subtract(baseHeight + topHeight));

        top.translateZProperty().bind(mid.depthProperty().add(topHeight).multiply(-0.5));
        base.translateZProperty().bind(mid.depthProperty().add(baseHeight).multiply(0.5));

        wall.setUserData(wallData);
        wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
        wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
        wall.translateZProperty().bind(wallHeightPy.multiply(-0.5));

        return wall;
    }

    public Group createHouseWall(WallData wallData) {
        Box base = createBlock(wallData, houseMaterial, houseWallThicknessPy);
        Box top = createBlock(wallData, wallTopMaterial, houseWallThicknessPy);
        Group wall = new Group(base, top);

        base.setDepth(HOUSE_WALL_BASE_HEIGHT);
        top.setDepth(HOUSE_WALL_TOP_HEIGHT);
        top.setTranslateZ(-0.58 * HOUSE_WALL_BASE_HEIGHT); // why does 0.5 flicker?

        wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
        wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
        wall.setTranslateZ(-0.5 * (HOUSE_WALL_BASE_HEIGHT + HOUSE_WALL_TOP_HEIGHT));
        wall.setUserData(wallData);

        return wall;
    }


    public Box createBlock(WallData wallData, Material material, DoubleProperty thicknessPy) {
        return switch (wallData.type) {
            case FloorPlan.HWALL -> createHBlock(wallData, material, thicknessPy);
            case FloorPlan.VWALL -> createVBlock(wallData, material, thicknessPy);
            case FloorPlan.CORNER -> createCornerBlock(material, thicknessPy);
            default -> throw new IllegalStateException("Unknown wall type: " + wallData.type);
        };
    }

    private Box createHBlock(WallData wallData, Material material, DoubleProperty thicknessPy) {
        Box block = new Box();
        block.setMaterial(material);
        // without ...+1 there are gaps. why?
        block.setWidth((wallData.numBricksX + 1) * wallData.brickSize);
        block.heightProperty().bind(thicknessPy);
        block.drawModeProperty().bind(drawModePy);
        return block;
    }

    private Box createVBlock(WallData wallData, Material material, DoubleProperty thicknessPy) {
        Box block = new Box();
        block.setMaterial(material);
        block.widthProperty().bind(thicknessPy);
        // without ...+1 there are gaps. why?
        block.setHeight((wallData.numBricksY + 1) * wallData.brickSize);
        block.drawModeProperty().bind(drawModePy);
        return block;
    }

    private Box createCornerBlock(Material material, DoubleProperty thicknessPy) {
        Box block = new Box();
        block.setMaterial(material);
        block.widthProperty().bind(thicknessPy);
        block.heightProperty().bind(thicknessPy);
        block.drawModeProperty().bind(drawModePy);
        return block;
    }
}
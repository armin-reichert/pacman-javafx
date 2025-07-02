/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.opaqueColor;
import static java.util.Objects.requireNonNull;

/**
 * 3D Arcade style house.
 */
public class ArcadeHouse3D extends Group {

    private Door3D door3D;
    private PointLight light;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    public ArcadeHouse3D(
        AnimationManager animationManager,
        House house,
        TerrainMapRenderer3D r3D,
        Color houseBaseColor,
        Color houseTopColor,
        Color doorColor,
        float wallOpacity,
        DoubleProperty wallBaseHeightPy,
        float wallTopHeight,
        float wallThickness,
        BooleanProperty houseLightOnPy)
    {
        requireNonNull(animationManager);
        requireNonNull(house);
        requireNonNull(r3D);
        requireNonNull(houseBaseColor);
        requireNonNull(houseTopColor);
        requireNonNull(doorColor);
        requireNonNull(wallBaseHeightPy);
        requireNonNull(houseLightOnPy);

        wallBaseMaterial = coloredPhongMaterial(opaqueColor(houseBaseColor, wallOpacity));
        wallTopMaterial = coloredPhongMaterial(houseTopColor);
        Vector2i houseSize = house.sizeInTiles();
        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallTopHeight(wallTopHeight);
        r3D.setWallThickness(wallThickness);
        r3D.setWallBaseMaterial(wallBaseMaterial);
        r3D.setWallTopMaterial(wallTopMaterial);

        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = house.minTile().x(), xMax = xMin + tilesX - 1;
        int yMin = house.minTile().y(), yMax = yMin + tilesY - 1;
        Vector2i leftDoorTile = house.leftDoorTile(), rightDoorTile = house.rightDoorTile();
        door3D = new Door3D(animationManager, leftDoorTile, rightDoorTile, doorColor, wallBaseHeightPy.get());

        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;
        light = new PointLight();
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightPy.multiply(-1));
        light.lightOnProperty().bind(houseLightOnPy);

        getChildren().addAll(
            light,
            door3D,
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(leftDoorTile.x() - 1, yMin)),
            r3D.createWallBetweenTiles(Vector2i.of(rightDoorTile.x() + 1, yMin), Vector2i.of(xMax, yMin)),
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(xMin, yMax)),
            r3D.createWallBetweenTiles(Vector2i.of(xMax, yMin), Vector2i.of(xMax, yMax)),
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMax), Vector2i.of(xMax, yMax))
        );
    }

    public void destroy() {
        getChildren().clear();
        wallBaseMaterial = null;
        wallTopMaterial = null;
        door3D.destroy();
        door3D = null;
        light.lightOnProperty().unbind();
        light.translateZProperty().unbind();
        light = null;
    }

    public Door3D door3D() {
        return door3D;
    }
}
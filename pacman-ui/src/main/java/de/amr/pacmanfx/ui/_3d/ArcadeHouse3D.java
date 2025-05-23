/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.opaqueColor;

public class ArcadeHouse3D {

    private final Group root = new Group();
    private final Door3D door3D;

    public ArcadeHouse3D(
            GameLevel level,
            TerrainMapRenderer3D r3D,
            Color houseBaseColor, Color houseTopColor, Color doorColor, float wallOpacity,
            DoubleProperty wallBaseHeightPy, float wallTopHeight, float wallThickness,
            BooleanProperty houseLightOnPy)
    {
        Vector2i houseSize = level.houseSizeInTiles();
        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallTopHeight(wallTopHeight);
        r3D.setWallThickness(wallThickness);
        r3D.setWallBaseMaterial(coloredPhongMaterial(opaqueColor(houseBaseColor, wallOpacity)));
        r3D.setWallTopMaterial(coloredPhongMaterial(houseTopColor));

        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = level.houseMinTile().x(), xMax = xMin + tilesX - 1;
        int yMin = level.houseMinTile().y(), yMax = yMin + tilesY - 1;
        Vector2i leftDoorTile = level.houseLeftDoorTile(), rightDoorTile = level.houseRightDoorTile();
        door3D = new Door3D(leftDoorTile, rightDoorTile, doorColor, wallBaseHeightPy.get());

        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;
        var light = new PointLight();
        light.lightOnProperty().bind(houseLightOnPy);
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightPy.multiply(-1));

        root.getChildren().addAll(
                light,
                door3D,
                r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(leftDoorTile.x() - 1, yMin)),
                r3D.createWallBetweenTiles(Vector2i.of(rightDoorTile.x() + 1, yMin), Vector2i.of(xMax, yMin)),
                r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(xMin, yMax)),
                r3D.createWallBetweenTiles(Vector2i.of(xMax, yMin), Vector2i.of(xMax, yMax)),
                r3D.createWallBetweenTiles(Vector2i.of(xMin, yMax), Vector2i.of(xMax, yMax))
        );
    }

    public Group getRoot() {
        return root;
    }

    public Door3D getDoor3D() {
        return door3D;
    }
}

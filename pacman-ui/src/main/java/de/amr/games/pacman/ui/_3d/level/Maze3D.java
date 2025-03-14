/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui.GameUIConfiguration;
import de.amr.games.pacman.ui._3d.animation.MaterialColorAnimation;
import de.amr.games.pacman.uilib.WorldMapColoring;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.*;
import static de.amr.games.pacman.uilib.Ufx.*;

public class Maze3D extends Group {

    private final DoubleProperty obstacleBaseHeightPy = new SimpleDoubleProperty(OBSTACLE_BASE_HEIGHT);
    private final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(1);
    private final DoubleProperty houseBaseHeightPy = new SimpleDoubleProperty(HOUSE_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy = new SimpleBooleanProperty(false);

    private final Door3D door3D;
    private final MaterialColorAnimation materialColorAnimation;

    public Maze3D(GameUIConfiguration configuration3D, GameLevel level, WorldMapColoring coloring) {
        Logger.info("Build world 3D. Map URL='{}'", URLDecoder.decode(level.map().url().toExternalForm(), StandardCharsets.UTF_8));

        Color wallBaseColor = coloring.stroke();
        // need some contrast with floor if fill color is black
        Color wallTopColor = coloring.fill().equals(Color.BLACK) ? Color.grayRgb(42) : coloring.fill();

        PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
                () -> opaqueColor(wallBaseColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        PhongMaterial wallTopMaterial = new PhongMaterial();
        wallTopMaterial.setDiffuseColor(wallTopColor);
        wallTopMaterial.setSpecularColor(wallTopColor.brighter());

        materialColorAnimation = new MaterialColorAnimation(Duration.seconds(0.25), wallTopMaterial, wallTopColor, wallBaseColor);

        PhongMaterial cornerBaseMaterial = new PhongMaterial();
        cornerBaseMaterial.setDiffuseColor(wallBaseColor); // for now use same color
        cornerBaseMaterial.specularColorProperty().bind(cornerBaseMaterial.diffuseColorProperty().map(Color::brighter));

        PhongMaterial cornerTopMaterial = new PhongMaterial();
        cornerTopMaterial.setDiffuseColor(wallTopColor);
        cornerTopMaterial.specularColorProperty().bind(cornerTopMaterial.diffuseColorProperty().map(Color::brighter));

        TerrainRenderer3D r3D = configuration3D.createTerrainRenderer3D();
        r3D.setWallBaseHeightProperty(obstacleBaseHeightPy);
        r3D.setWallTopHeight(OBSTACLE_TOP_HEIGHT);
        r3D.setWallTopMaterial(wallTopMaterial);
        r3D.setCornerBaseMaterial(cornerBaseMaterial);
        r3D.setCornerTopMaterial(wallTopMaterial); // for now such that power animation also affects corner top

        //TODO check this:
        obstacleBaseHeightPy.set(PY_3D_WALL_HEIGHT.get());

        for (Obstacle obstacle : level.map().obstacles()) {
            if (!level.isPartOfHouse(tileAt(obstacle.startPoint().toVector2f()))) {
                r3D.setWallThickness(OBSTACLE_THICKNESS);
                r3D.setWallBaseMaterial(wallBaseMaterial);
                r3D.setWallTopMaterial(wallTopMaterial);
                r3D.renderObstacle3D(this, obstacle, isWorldBorder(level.map(), obstacle));
            }
        }

        // House
        houseBaseHeightPy.set(HOUSE_BASE_HEIGHT);
        door3D = addGhostHouse(
                this, level, r3D,
                coloring.fill(), coloring.stroke(), coloring.door(),
                HOUSE_OPACITY,
                houseBaseHeightPy, HOUSE_WALL_TOP_HEIGHT, HOUSE_WALL_THICKNESS,
                houseLightOnPy);
        getChildren().add(door3D); //TODO check this

        PY_3D_WALL_HEIGHT.addListener((py, ov, nv) -> obstacleBaseHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == GameLevel.EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private Door3D addGhostHouse(
        Group parent,
        GameLevel level,
        TerrainRenderer3D r3D,
        Color houseBaseColor, Color houseTopColor, Color doorsColor, float wallOpacity,
        DoubleProperty wallBaseHeightPy, float wallTopHeight, float wallThickness,
        BooleanProperty houseLightOnPy)
    {
        Vector2i houseSize = level.houseSizeInTiles();
        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallTopHeight(wallTopHeight);
        r3D.setWallThickness(wallThickness);
        r3D.setWallBaseMaterial(coloredMaterial(opaqueColor(houseBaseColor, wallOpacity)));
        r3D.setWallTopMaterial(coloredMaterial(houseTopColor));

        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = level.houseMinTile().x(), xMax = xMin + tilesX - 1;
        int yMin = level.houseMinTile().y(), yMax = yMin + tilesY - 1;
        Vector2i leftDoorTile = level.houseLeftDoorTile(), rightDoorTile = level.houseRightDoorTile();

        var door3D = new Door3D(leftDoorTile, rightDoorTile, doorsColor, wallBaseHeightPy.get());

        parent.getChildren().addAll(
            r3D.createWallBetweenTiles(vec_2i(xMin, yMin), vec_2i(leftDoorTile.x() - 1, yMin)),
            r3D.createWallBetweenTiles(vec_2i(rightDoorTile.x() + 1, yMin), vec_2i(xMax, yMin)),
            r3D.createWallBetweenTiles(vec_2i(xMin, yMin), vec_2i(xMin, yMax)),
            r3D.createWallBetweenTiles(vec_2i(xMax, yMin), vec_2i(xMax, yMax)),
            r3D.createWallBetweenTiles(vec_2i(xMin, yMax), vec_2i(xMax, yMax))
        );

        // pixel coordinates
        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;

        var light = new PointLight();
        light.lightOnProperty().bind(houseLightOnPy);
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightPy.multiply(-1));

        parent.getChildren().add(light);

        return door3D;
    }

    public void playMaterialAnimation() {
        materialColorAnimation.play();
    }

    public void stopMaterialAnimation() {
        materialColorAnimation.stop();
        materialColorAnimation.jumpTo(Duration.ZERO);
    }

    public void setHouseLightOn(boolean on) {
        houseLightOnPy.set(on);
    }

    public Door3D door3D() {
        return door3D;
    }

    public Animation wallsDisappearAnimation(double seconds) {
        var totalDuration = Duration.seconds(seconds);
        var houseDisappears = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33), new KeyValue(houseBaseHeightPy, 0, Interpolator.EASE_IN)));
        var obstaclesDisappear = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_IN)));
        var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
        animation.setOnFinished(e -> setVisible(false));
        return animation;
    }

    public Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
            new KeyFrame(Duration.millis(125), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_BOTH)));
        animation.setAutoReverse(true);
        animation.setCycleCount(2 * numFlashes);
        return animation;
    }
}
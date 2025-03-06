/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.uilib.WorldMapColoring;
import de.amr.games.pacman.ui._3d.animation.MaterialColorAnimation;
import de.amr.games.pacman.ui._3d.scene3d.GameConfiguration3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D.TAG_WALL_BASE;
import static de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D.isTagged;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.*;
import static de.amr.games.pacman.uilib.Ufx.*;

public class Maze3D extends Group {

    private static final int EMPTY_ROWS_OVER_MAZE = 3;

    private static final String OSHAPES_FILLED_PROPERTY_NAME = "rendering_oshapes_filled";

    private final DoubleProperty obstacleBaseHeightPy = new SimpleDoubleProperty(OBSTACLE_BASE_HEIGHT);
    private final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(1);
    private final DoubleProperty houseBaseHeightPy = new SimpleDoubleProperty(HOUSE_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy = new SimpleBooleanProperty(false);

    private final Door3D door3D;

    private final PhongMaterial wallBaseMaterial;

    // experimental
    private final PhongMaterial highlightMaterial = coloredMaterial(Color.YELLOW);
    private final Set<Group> obstacleGroups;
    private final MaterialColorAnimation materialColorAnimation;

    public Maze3D(GameConfiguration3D configuration3D, GameWorld world, WorldMapColoring coloring) {
        Logger.info("Build world 3D. Map URL='{}'", URLDecoder.decode(world.map().url().toExternalForm(), StandardCharsets.UTF_8));

        Color wallBaseColor = coloring.stroke();
        // need some contrast with floor if fill color is black
        Color wallTopColor = coloring.fill().equals(Color.BLACK) ? Color.grayRgb(42) : coloring.fill();

        wallBaseMaterial = new PhongMaterial();
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

        //TODO just a temporary solution until I find something better
        if (world.map().hasProperty(LayerID.TERRAIN, OSHAPES_FILLED_PROPERTY_NAME)) {
            Object value = world.map().getProperty(LayerID.TERRAIN, OSHAPES_FILLED_PROPERTY_NAME);
            try {
                r3D.setOShapeFilled(Boolean.parseBoolean(String.valueOf(value)));
            } catch (Exception x) {
                Logger.error("Map property '{}}' is not a valid boolean value: {}", OSHAPES_FILLED_PROPERTY_NAME, value);
            }
        }

        for (Obstacle obstacle : world.map().obstacles()) {
            if (!world.isPartOfHouse(tileAt(obstacle.startPoint().toVector2f()))) {
                r3D.setWallThickness(OBSTACLE_THICKNESS);
                r3D.setWallBaseMaterial(wallBaseMaterial);
                r3D.setWallTopMaterial(wallTopMaterial);
                r3D.renderObstacle3D(this, obstacle, isWorldBorder(world.map(), obstacle));
            }
        }

        // House
        houseBaseHeightPy.set(HOUSE_BASE_HEIGHT);
        door3D = addGhostHouse(
                this, world, r3D,
                coloring.fill(), coloring.stroke(), coloring.door(),
                HOUSE_OPACITY,
                houseBaseHeightPy, HOUSE_WALL_TOP_HEIGHT, HOUSE_WALL_THICKNESS,
                houseLightOnPy);
        getChildren().add(door3D); //TODO check this

        // experimental
        obstacleGroups = lookupAll("*").stream()
                .filter(Group.class::isInstance)
                .map(Group.class::cast)
                .filter(group -> isTagged(group, TerrainRenderer3D.TAG_INNER_OBSTACLE))
                .collect(Collectors.toSet());

        PY_3D_WALL_HEIGHT.addListener((py, ov, nv) -> obstacleBaseHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private Door3D addGhostHouse(
        Group parent,
        GameWorld world,
        TerrainRenderer3D r3D,
        Color houseBaseColor, Color houseTopColor, Color doorsColor, float wallOpacity,
        DoubleProperty wallBaseHeightPy, float wallTopHeight, float wallThickness,
        BooleanProperty houseLightOnPy)
    {
        Vector2i houseSize = world.houseSizeInTiles();
        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallTopHeight(wallTopHeight);
        r3D.setWallThickness(wallThickness);
        r3D.setWallBaseMaterial(coloredMaterial(opaqueColor(houseBaseColor, wallOpacity)));
        r3D.setWallTopMaterial(coloredMaterial(houseTopColor));

        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = world.houseMinTile().x(), xMax = xMin + tilesX - 1;
        int yMin = world.houseMinTile().y(), yMax = yMin + tilesY - 1;
        Vector2i leftDoorTile = world.houseLeftDoorTile(), rightDoorTile = world.houseRightDoorTile();

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

    public void highlightObstacleNearPac(Pac3D pac3D, Pac pac) {
        Bounds pacSensitiveArea = new BoundingBox(pac.posX(), pac.posY(), 0, 3*TS, 3*TS, TS);
        for (Group obstacleGroup : obstacleGroups) {
            Bounds bounds = obstacleGroup.getBoundsInLocal();
            if (bounds.intersects(pacSensitiveArea)) {
                Logger.info("Pac near obstacle {}", obstacleGroup);
                Timeline timeline = new Timeline();
                Set<Node> obstacleParts = obstacleGroup.lookupAll("*").stream()
                        .filter(node -> node instanceof Box || node instanceof Cylinder)
                        .collect(Collectors.toSet());
                for (Node node : obstacleParts) {
                    if (isTagged(node, TAG_WALL_BASE) && node instanceof Shape3D shape3D) {
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1),
                                e -> shape3D.setMaterial(highlightMaterial)));
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1.5),
                                e -> shape3D.setMaterial(wallBaseMaterial)));
                    }
                }
                timeline.play();
                break;
            }
        }
    }
}
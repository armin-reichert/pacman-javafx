/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import de.amr.games.pacman.ui3d.scene3d.GameConfiguration3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
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
import static de.amr.games.pacman.ui2d.lib.Ufx.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.*;
import static de.amr.games.pacman.ui3d.level.WorldRenderer3D.TAG_WALL_BASE;
import static de.amr.games.pacman.ui3d.level.WorldRenderer3D.isTagged;

public class Maze3D extends Group {

    private static final String OSHAPES_FILLED_PROPERTY_NAME = "rendering_oshapes_filled";

    private final DoubleProperty obstacleBaseHeightPy = new SimpleDoubleProperty(OBSTACLE_BASE_HEIGHT);
    private final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(1);
    private final DoubleProperty houseBaseHeightPy = new SimpleDoubleProperty(HOUSE_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy = new SimpleBooleanProperty(false);

    private Door3D door3D;

    // experimental
    private PhongMaterial cornerMaterial;
    private Set<Group> obstacleGroups;
    private PhongMaterial highlightMaterial = new PhongMaterial(Color.YELLOW);

    public void build(GameConfiguration3D configuration3D, GameWorld world, WorldMapColoring coloring) {
        Logger.info("Build world 3D. Map URL='{}'", URLDecoder.decode(world.map().url().toExternalForm(), StandardCharsets.UTF_8));

        Color wallBaseColor = coloring.stroke();
        // need some contrast with floor if fill color is black
        Color wallTopColor = coloring.fill().equals(Color.BLACK) ? Color.grayRgb(30) : coloring.fill();

        var wallTopMaterial = new PhongMaterial();
        wallTopMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
                () -> opaqueColor(wallTopColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallTopMaterial.specularColorProperty().bind(wallTopMaterial.diffuseColorProperty().map(Color::brighter));

        var wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
                () -> opaqueColor(wallBaseColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        cornerMaterial = new PhongMaterial();
        cornerMaterial.setDiffuseColor(wallBaseColor);
        cornerMaterial.specularColorProperty().bind(cornerMaterial.diffuseColorProperty().map(Color::brighter));

        WorldRenderer3D r3D = configuration3D.createWorldRenderer();
        r3D.setWallBaseMaterial(wallBaseMaterial);
        r3D.setWallBaseHeightProperty(obstacleBaseHeightPy);
        r3D.setWallTopMaterial(wallTopMaterial);
        r3D.setWallTopHeight(OBSTACLE_TOP_HEIGHT);
        r3D.setCornerMaterial(cornerMaterial);

        //TODO check this:
        obstacleBaseHeightPy.set(PY_3D_WALL_HEIGHT.get());

        //TODO just a temporary solution until I find something better
        if (world.map().terrain().hasProperty(OSHAPES_FILLED_PROPERTY_NAME)) {
            Object value = world.map().terrain().getProperty(OSHAPES_FILLED_PROPERTY_NAME);
            try {
                r3D.setOShapeFilled(Boolean.parseBoolean(String.valueOf(value)));
            } catch (Exception x) {
                Logger.error("Map property '{}}' is not a valid boolean value: {}", OSHAPES_FILLED_PROPERTY_NAME, value);
            }
        }

        for (Obstacle obstacle : world.map().obstacles()) {
            Logger.info("{}: {}", obstacle.computeType(), obstacle);
            if (!world.isPartOfHouse(tileAt(obstacle.startPoint()))) {
                r3D.setWallThickness(obstacle.hasDoubleWalls() ? BORDER_WALL_THICKNESS : OBSTACLE_THICKNESS);
                r3D.renderObstacle3D(this, obstacle);
            }
        }

        // House
        houseBaseHeightPy.set(HOUSE_BASE_HEIGHT);
        door3D = r3D.addGhostHouse(
                this, world,
                coloring.fill(), coloring.stroke(), coloring.door(),
                HOUSE_OPACITY,
                houseBaseHeightPy, HOUSE_WALL_TOP_HEIGHT, HOUSE_WALL_THICKNESS,
                houseLightOnPy);
        getChildren().add(door3D); //TODO check this

        // experimental
        obstacleGroups = lookupAll("*").stream()
                .filter(Group.class::isInstance)
                .map(Group.class::cast)
                .filter(group -> isTagged(group, WorldRenderer3D.TAG_INNER_OBSTACLE))
                .collect(Collectors.toSet());

        PY_3D_WALL_HEIGHT.addListener((py, ov, nv) -> obstacleBaseHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    public void setHouseLightOn(boolean on) {
        houseLightOnPy.set(on);
    }

    public Door3D door3D() {
        return door3D;
    }

    public Animation wallsDisappearAnimation(double seconds) {
        var totalDuration = Duration.seconds(seconds);
        var obstaclesDisappear = new Timeline(
                new KeyFrame(totalDuration.multiply(0.33),
                        new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_IN)
                ));
        var houseDisappears = new Timeline(
                new KeyFrame(totalDuration.multiply(0.33),
                        new KeyValue(houseBaseHeightPy, 0, Interpolator.EASE_IN)
                ));
        var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
        animation.setOnFinished(e -> setVisible(false));
        return animation;
    }

    public Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
                new KeyFrame(Duration.millis(125), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_BOTH))
        );
        animation.setAutoReverse(true);
        animation.setCycleCount(2 * numFlashes);
        return animation;
    }

    public void highlightObstacleNearPac(Vector2f pacPosition) {
        for (Group obstacleGroup : obstacleGroups) {
            Set<Node> obstacleParts = obstacleGroup.lookupAll("*").stream()
                    .filter(node -> node instanceof Box || node instanceof Cylinder)
                    .collect(Collectors.toSet());
            boolean highlight = false;
            for (Node node : obstacleParts) {
                if (isTagged(node, TAG_WALL_BASE)) {
                    Vector2f nodePosition = vec_2f(node.getTranslateX(), node.getTranslateY());
                    highlight = nodePosition.euclideanDist(pacPosition) < 2 * TS;
                    break;
                }
            }
            for (Node node : obstacleParts) {
                if (isTagged(node, TAG_WALL_BASE) && node instanceof Shape3D shape3D) {
                    shape3D.setMaterial(highlight ? highlightMaterial : cornerMaterial); // TODO
                }
            }
        }
    }
}
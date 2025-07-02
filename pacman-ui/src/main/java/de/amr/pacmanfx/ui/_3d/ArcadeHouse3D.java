/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.Ufx.opaqueColor;
import static java.util.Objects.requireNonNull;

/**
 * 3D Arcade style house.
 */
public class ArcadeHouse3D extends Group {

    private static final int NUM_VERTICAL_BARS = 2;

    private final DoubleProperty barThicknessPy = new SimpleDoubleProperty(0.75);
    private PointLight light;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;


    private Group door;
    private PhongMaterial barMaterial;
    private Group leftWing;
    private Group rightWing;
    private ManagedAnimation doorOpenCloseAnimation;

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
        barMaterial = Ufx.coloredPhongMaterial(doorColor);

        Vector2i houseSize = house.sizeInTiles();
        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = house.minTile().x(), xMax = xMin + tilesX - 1;
        int yMin = house.minTile().y(), yMax = yMin + tilesY - 1;
        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;

        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallTopHeight(wallTopHeight);
        r3D.setWallThickness(wallThickness);
        r3D.setWallBaseMaterial(wallBaseMaterial);
        r3D.setWallTopMaterial(wallTopMaterial);

        door = createDoor(house.leftDoorTile(), house.rightDoorTile(), wallBaseHeightPy.get());

        doorOpenCloseAnimation = new ManagedAnimation(animationManager, "Door_OpenClose") {
            @Override
            protected Animation createAnimation() {
                return new Timeline(
                    new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessPy, 0)),
                    new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessPy, 0.75))
                );
            }
        };

        light = new PointLight();
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightPy.multiply(-1));
        light.lightOnProperty().bind(houseLightOnPy);

        getChildren().addAll(
            light,
            door,
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(house.leftDoorTile().x() - 1, yMin)),
            r3D.createWallBetweenTiles(Vector2i.of(house.rightDoorTile().x() + 1, yMin), Vector2i.of(xMax, yMin)),
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMin), Vector2i.of(xMin, yMax)),
            r3D.createWallBetweenTiles(Vector2i.of(xMax, yMin), Vector2i.of(xMax, yMax)),
            r3D.createWallBetweenTiles(Vector2i.of(xMin, yMax), Vector2i.of(xMax, yMax))
        );
    }

    public void setDoorVisible(boolean visible) {
        door.setVisible(visible);
    }

    public ManagedAnimation doorOpenCloseAnimation() {
        return doorOpenCloseAnimation;
    }

    private Group createDoor(Vector2i leftWingTile, Vector2i rightWingTile, double height) {
        var door = new Group();
        leftWing = createDoorWing(leftWingTile, height);
        rightWing = createDoorWing(rightWingTile, height);
        door.getChildren().addAll(leftWing, rightWing);
        return door;
    }

    private Group createDoorWing(Vector2i tile, double height) {
        var root = new Group();
        root.setTranslateX(tile.x() * TS);
        root.setTranslateY(tile.y() * TS);

        for (int i = 0; i < NUM_VERTICAL_BARS; ++i) {
            var verticalBar = new Cylinder(barThicknessPy.get(), height);
            verticalBar.radiusProperty().bind(barThicknessPy);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            root.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(barThicknessPy.get(), 14);
        horizontalBar.radiusProperty().bind(barThicknessPy);
        horizontalBar.setMaterial(barMaterial);
        horizontalBar.setTranslateX(4);
        horizontalBar.setTranslateY(4);
        horizontalBar.setTranslateZ(0.25 - horizontalBar.getHeight() * 0.5);
        horizontalBar.setRotationAxis(Rotate.Z_AXIS);
        horizontalBar.setRotate(90);
        root.getChildren().add(horizontalBar);

        return root;
    }

    private void destroyDoorWing(Group doorWing) {
        for (Node node : doorWing.getChildren()) {
            if (node instanceof Cylinder bar) {
                bar.radiusProperty().unbind();
                bar.translateZProperty().unbind();
                bar.setMaterial(null);
            }
        }
    }

    public void destroy() {
        getChildren().clear();
        wallBaseMaterial = null;
        wallTopMaterial = null;

        doorOpenCloseAnimation.destroy();
        doorOpenCloseAnimation = null;

        destroyDoorWing(leftWing);
        leftWing = null;

        destroyDoorWing(rightWing);
        rightWing = null;

        door.getChildren().clear();
        door = null;

        barMaterial = null;

        light.lightOnProperty().unbind();
        light.translateZProperty().unbind();
        light = null;
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerExplosionAndRecycling;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * 3D house in Arcade style.
 *
 * <p>At each ghost position inside the house, a swirl attracts the particles emitted by
 * exploding energizers.</p>
 */
public class ArcadeHouse3D extends Group implements Disposable {

    private static final int DOOR_VERTICAL_BAR_COUNT = 2;
    private static final float DOOR_BAR_THICKNESS = 0.75f;

    private final DoubleProperty barThicknessProperty   = new SimpleDoubleProperty(DOOR_BAR_THICKNESS);
    private final BooleanProperty openProperty          = new SimpleBooleanProperty(false);
    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty();

    private final TerrainRenderer3D r3D;

    private PhongMaterial barMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    private Group door;
    private Group leftWing;
    private Group rightWing;
    private PointLight light;

    private Group[] swirls;
    private List<ManagedAnimation> swirlAnimations = new ArrayList<>(3);

    private ManagedAnimation doorOpenCloseAnimation;

    public ArcadeHouse3D(
        AnimationRegistry animationRegistry,
        GameLevel gameLevel,
        House house,
        double baseHeight,
        double wallThickness,
        double opacity,
        Color houseBaseColor,
        Color houseTopColor,
        Color doorColor)
    {
        requireNonNull(animationRegistry);
        requireNonNull(gameLevel);
        requireNonNull(house);
        requireNonNull(houseBaseColor);
        requireNonNull(houseTopColor);
        requireNonNull(doorColor);

        r3D = new TerrainRenderer3D();

        barMaterial      = coloredPhongMaterial(doorColor);
        wallBaseMaterial = coloredPhongMaterial(colorWithOpacity(houseBaseColor, opacity));
        wallTopMaterial  = coloredPhongMaterial(houseTopColor);

        wallBaseHeightProperty.set(baseHeight);

        Vector2i houseSize = house.sizeInTiles();
        int tilesX = houseSize.x(), tilesY = houseSize.y();
        int xMin = house.minTile().x(), xMax = xMin + tilesX - 1;
        int yMin = house.minTile().y(), yMax = yMin + tilesY - 1;
        float centerX = xMin * TS + tilesX * HTS;
        float centerY = yMin * TS + tilesY * HTS;

        // (0)-----(1)left_door-right_door(2)-----(3)
        //  |                                      |
        //  |                                      |
        //  |                                      |
        //  |                                      |
        // (4)------------------------------------(5)

        Vector2i[] p = {
            Vector2i.of(xMin, yMin),
            Vector2i.of(house.leftDoorTile().x() - 1, yMin),
            Vector2i.of(house.rightDoorTile().x() + 1, yMin),
            Vector2i.of(xMax, yMin),
            Vector2i.of(xMin, yMax),
            Vector2i.of(xMax, yMax)
        };

        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            getChildren().addAll(wall3D.top(), wall3D.base());
            return wall3D;
        });
        r3D.createWallBetweenTileCoordinates(p[0], p[1], wallThickness);
        r3D.createWallBetweenTileCoordinates(p[2], p[3], wallThickness);
        r3D.createWallBetweenTileCoordinates(p[3], p[5], wallThickness);
        r3D.createWallBetweenTileCoordinates(p[0], p[4], wallThickness);
        r3D.createWallBetweenTileCoordinates(p[4], p[5], wallThickness);

        door = createDoor(house.leftDoorTile(), house.rightDoorTile(), wallBaseHeightProperty.get());

        doorOpenCloseAnimation = new ManagedAnimation(animationRegistry, "Door_OpenClose") {
            @Override
            protected Animation createAnimationFX() {
                return new Timeline(
                    new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
                    new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, 0.75))
                );
            }
        };

        light = new PointLight();
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightProperty.multiply(-1));

        getChildren().addAll(light, door);

        createSwirls(gameLevel);

        Duration rotationTime = Duration.seconds(EnergizerExplosionAndRecycling.SWIRL_ROTATION_SEC);
        for (int i = 0; i < swirls.length; ++i) {
            final int index = i;
            swirlAnimations.add(new ManagedAnimation(animationRegistry, "Swirl_%d".formatted(i)) {
                @Override
                protected Animation createAnimationFX() {
                    var rotation = new RotateTransition(rotationTime, swirls[index]);
                    rotation.setAxis(Rotate.Z_AXIS);
                    rotation.setFromAngle(0);
                    rotation.setToAngle(360);
                    rotation.setInterpolator(Interpolator.LINEAR);
                    rotation.setCycleCount(Animation.INDEFINITE);
                    return rotation;
                }
            });
        }
    }

    public List<Group> particleSwirls() {
        return List.of(swirls);
    }

    public BooleanProperty openProperty() {return openProperty;}

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeightProperty;
    }

    public void setDoorVisible(boolean visible) {
        door.setVisible(visible);
    }

    public ManagedAnimation doorOpenCloseAnimation() {
        return doorOpenCloseAnimation;
    }

    public PointLight light() {
        return light;
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

        for (int i = 0; i < DOOR_VERTICAL_BAR_COUNT; ++i) {
            var verticalBar = new Cylinder(barThicknessProperty.get(), height);
            verticalBar.radiusProperty().bind(barThicknessProperty);
            verticalBar.setMaterial(barMaterial);
            verticalBar.setTranslateX(i * 4 + 2);
            verticalBar.setTranslateY(4);
            verticalBar.translateZProperty().bind(verticalBar.heightProperty().multiply(-0.5));
            verticalBar.setRotationAxis(Rotate.X_AXIS);
            verticalBar.setRotate(90);
            root.getChildren().add(verticalBar);
        }

        var horizontalBar = new Cylinder(barThicknessProperty.get(), 14);
        horizontalBar.radiusProperty().bind(barThicknessProperty);
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

    private void createSwirls(GameLevel gameLevel) {
        Vector2f[] centers = {
            gameLevel.ghost(CYAN_GHOST_BASHFUL).revivalPosition().plus(HTS, HTS),
            gameLevel.ghost(PINK_GHOST_SPEEDY) .revivalPosition().plus(HTS, HTS),
            gameLevel.ghost(ORANGE_GHOST_POKEY).revivalPosition().plus(HTS, HTS),
        };
        swirls = new Group[centers.length];
        for (int i = 0; i < swirls.length; ++i) {
            swirls[i] = new Group();
            swirls[i].setTranslateX(centers[i].x());
            swirls[i].setTranslateY(centers[i].y());
        }
    }

    public void startSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(ManagedAnimation::playFromStart);
            Logger.info("Swirl animations started");
        }
    }

    public void stopSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(ManagedAnimation::stop);
        }
    }

    @Override
    public void dispose() {
        doorOpenCloseAnimation.stop();
        doorOpenCloseAnimation.dispose();
        doorOpenCloseAnimation = null;

        if (swirlAnimations != null) {
            for (ManagedAnimation swirlAnimation : swirlAnimations) {
                swirlAnimation.dispose();
            }
            swirlAnimations.clear();
            swirlAnimations = null;
            Logger.info("Disposed swirl animations");
        }
        for (Node child : getChildren()) {
            Wall3D.dispose(child); // does nothing if child is not part of 3D wall
        }

        if (swirls != null) {
            for (Group swirl : swirls) {
                swirl.getChildren().forEach(child -> {
                    if (child instanceof EnergizerExplosionAndRecycling.Particle particle) {
                        particle.dispose();
                    }
                });
                swirl.getChildren().clear();
                swirls = null;
            }
            Logger.info("Disposed swirls and their particles");
        }

        getChildren().clear();

        destroyDoorWing(leftWing);
        leftWing = null;

        destroyDoorWing(rightWing);
        rightWing = null;

        door.getChildren().clear();
        door = null;

        barMaterial = null;
        wallBaseMaterial = null;
        wallTopMaterial = null;

        light.translateZProperty().unbind();
        light.lightOnProperty().unbind();
        light = null;

        r3D.setOnWallCreated(null);
    }
}
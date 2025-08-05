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
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
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

    private final DoubleProperty barThicknessProperty   = new SimpleDoubleProperty(0.25);
    private final BooleanProperty openProperty          = new SimpleBooleanProperty(false);
    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty();

    private final float barThickness;
    private int doorVerticalBarCount = 4;

    private final TerrainRenderer3D r3D;

    private PhongMaterial barMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    private Group doors;
    private Group leftDoor;
    private Group rightDoor;
    private PointLight light;
    private float doorSensitivity = 10;

    private Group[] swirls;
    private List<ManagedAnimation> swirlAnimations = new ArrayList<>(3);

    private ManagedAnimation doorsOpenCloseAnimation;

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
        barThickness = 2f / doorVerticalBarCount;
        barThicknessProperty.set(barThickness);

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

        leftDoor  = createDoor(house.leftDoorTile(), wallBaseHeightProperty.get());
        rightDoor = createDoor(house.rightDoorTile(), wallBaseHeightProperty.get());
        doors = new Group(leftDoor, rightDoor);

        doorsOpenCloseAnimation = new ManagedAnimation(animationRegistry, "Doors_OpenClose") {
            @Override
            protected Animation createAnimationFX() {
                return new Timeline(
                    new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
                    new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, barThickness))
                );
            }
        };

        light = new PointLight();
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(wallBaseHeightProperty.multiply(-1));

        getChildren().addAll(light, doors);

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

    private Group createDoor(Vector2i tile, double height) {
        var door = new Group();
        door.setTranslateX(tile.x() * TS);
        door.setTranslateY(tile.y() * TS + HTS);
        float barDistance = (float)TS / doorVerticalBarCount;
        for (int i = 0; i < doorVerticalBarCount; ++i) {
            var vBar = new Cylinder(barThicknessProperty.get(), height);
            vBar.radiusProperty().bind(barThicknessProperty);
            vBar.setMaterial(barMaterial);
            vBar.setRotationAxis(Rotate.X_AXIS);
            vBar.setRotate(90);
            vBar.setTranslateX((i + 0.5) * barDistance);
            vBar.setTranslateY(0);
            vBar.translateZProperty().bind(vBar.heightProperty().multiply(-0.5));
            door.getChildren().add(vBar);
        }

        var hBar = new Cylinder(barThicknessProperty.get(), 2 * TS);
        hBar.radiusProperty().bind(barThicknessProperty);
        hBar.setMaterial(barMaterial);
        hBar.setRotationAxis(Rotate.Z_AXIS);
        hBar.setRotate(90);
        hBar.setTranslateX(HTS);
        hBar.setTranslateY(0);
        hBar.setTranslateZ(-0.5 * (height + barThickness));
        door.getChildren().add(hBar);

        return door;
    }

    public void setDoorSensitivity(float value) {
        this.doorSensitivity = value;
    }

    public void tick(GameLevel gameLevel) {
        boolean accessRequested = gameLevel
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        light.lightOnProperty().set(accessRequested);

        gameLevel.house().ifPresent(house -> {
            boolean ghostNearHouseEntry = gameLevel
                .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(house.entryPosition()) <= doorSensitivity)
                .anyMatch(Ghost::isVisible);
            openProperty().set(ghostNearHouseEntry);
        });
    }

    public List<Group> particleSwirls() {
        return List.of(swirls);
    }

    public BooleanProperty openProperty() {return openProperty;}

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeightProperty;
    }

    public void setDoorsVisible(boolean visible) {
        doors.setVisible(visible);
    }

    public ManagedAnimation doorsOpenCloseAnimation() {
        return doorsOpenCloseAnimation;
    }

    public PointLight light() {
        return light;
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
        doorsOpenCloseAnimation.stop();
        doorsOpenCloseAnimation.dispose();
        doorsOpenCloseAnimation = null;

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

        disposeDoor(leftDoor);
        leftDoor = null;

        disposeDoor(rightDoor);
        rightDoor = null;

        doors.getChildren().clear();
        doors = null;

        barMaterial = null;
        wallBaseMaterial = null;
        wallTopMaterial = null;

        light.translateZProperty().unbind();
        light.lightOnProperty().unbind();
        light = null;

        r3D.setOnWallCreated(null);
    }

    private void disposeDoor(Group door) {
        for (Node node : door.getChildren()) {
            if (node instanceof Cylinder bar) {
                bar.radiusProperty().unbind();
                bar.heightProperty().unbind();
                bar.translateXProperty().unbind();
                bar.translateYProperty().unbind();
                bar.translateZProperty().unbind();
                bar.setMaterial(null);
            }
        }
    }
}
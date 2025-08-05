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
import de.amr.pacmanfx.uilib.Ufx;
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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
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

    private static final int DOOR_VERTICAL_BAR_COUNT = 4;

    private final DoubleProperty barThicknessProperty   = new SimpleDoubleProperty(0.25);
    private final BooleanProperty doorsOpenProperty     = new SimpleBooleanProperty(false);
    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty();

    private final float barThickness;
    private final double wallBaseOpacity;

    private final TerrainRenderer3D r3D;

    private PhongMaterial barMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    private PointLight light;
    private Group doors;
    private Group leftDoor;
    private Group rightDoor;
    private Group[] swirls;

    private float doorSensitivity = 10;

    private ManagedAnimation doorsMeltingAnimation;
    private List<ManagedAnimation> swirlAnimations = new ArrayList<>(3);

    public ArcadeHouse3D(
        AnimationRegistry animationRegistry,
        House house,
        Vector2f[] ghostRevivalPositionCenters,
        double baseHeight,
        double wallThickness,
        double opacity)
    {
        requireNonNull(animationRegistry);
        requireNonNull(house);

        r3D = new TerrainRenderer3D();

        wallBaseOpacity = opacity;
        wallBaseMaterial = coloredPhongMaterial(colorWithOpacity(Color.BLUE, 0.5));
        wallTopMaterial  = coloredPhongMaterial(Color.YELLOW);
        barMaterial      = coloredPhongMaterial(Color.PINK);

        wallBaseHeightProperty.set(baseHeight);
        barThickness = 2f / DOOR_VERTICAL_BAR_COUNT;
        barThicknessProperty.set(barThickness);

        float xMin = house.minTile().x() * TS + HTS, yMin = house.minTile().y() * TS + HTS;
        float xMax = house.maxTile().x() * TS + HTS, yMax = house.maxTile().y() * TS + HTS;

        // (0)----(1) left_door right_door (2)----(3)
        //  |                                      |
        //  |                                      |
        //  |                                      |
        //  |                                      |
        // (4)------------------------------------(5)

        Vector2f p0 = Vector2f.of(xMin, yMin);
        Vector2f p1 = house.leftDoorTile().scaled((float)TS).plus(0, HTS);
        Vector2f p2 = house.rightDoorTile().scaled((float)TS).plus(TS, HTS);
        Vector2f p3 = Vector2f.of(xMax, yMin);
        Vector2f p4 = Vector2f.of(xMin, yMax);
        Vector2f p5 = Vector2f.of(xMax, yMax);

        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            getChildren().addAll(wall3D.top(), wall3D.base());
            return wall3D;
        });
        r3D.createCylinderWall(p0, 0.5 * wallThickness);
        r3D.createCylinderWall(p3, 0.5 * wallThickness);
        r3D.createCylinderWall(p4, 0.5 * wallThickness);
        r3D.createCylinderWall(p5, 0.5 * wallThickness);
        r3D.createWallBetween(p0, p1, wallThickness);
        r3D.createWallBetween(p2, p3, wallThickness);
        r3D.createWallBetween(p3, p5, wallThickness);
        r3D.createWallBetween(p0, p4, wallThickness);
        r3D.createWallBetween(p4, p5, wallThickness);

        leftDoor  = createDoor(house.leftDoorTile(), wallBaseHeightProperty.get());
        rightDoor = createDoor(house.rightDoorTile(), wallBaseHeightProperty.get());
        doors = new Group(leftDoor, rightDoor);

        Vector2f houseCenter = p0.midpoint(p5);
        light = new PointLight();
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(2.5 * TS);
        light.setTranslateX(houseCenter.x());
        light.setTranslateY(houseCenter.y());
        light.translateZProperty().bind(wallBaseHeightProperty.multiply(-1));

        getChildren().addAll(light, doors);

        // These groups are added to their parent in GameLevel3D to ensure correct ordering!
        swirls = new Group[ghostRevivalPositionCenters.length];
        for (int i = 0; i < ghostRevivalPositionCenters.length; ++i) {
            swirls[i] = new Group();
            swirls[i].setTranslateX(ghostRevivalPositionCenters[i].x());
            swirls[i].setTranslateY(ghostRevivalPositionCenters[i].y());
            ManagedAnimation animation = createSwirlAnimation(animationRegistry, "Swirl_%d".formatted(i), swirls[i]);
            swirlAnimations.add(animation);
        }

        doorsMeltingAnimation = new ManagedAnimation(animationRegistry, "Doors_Melting") {
            @Override
            protected Animation createAnimationFX() {
                return new Timeline(
                    new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
                    new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, barThickness))
                );
            }
        };
    }

    private ManagedAnimation createSwirlAnimation(AnimationRegistry animationRegistry, String label, Group swirl) {
        Duration rotationTime = Duration.seconds(EnergizerExplosionAndRecycling.SWIRL_ROTATION_SEC);
        return new ManagedAnimation(animationRegistry, label) {
            @Override
            protected Animation createAnimationFX() {
                var rotation = new RotateTransition(rotationTime, swirl);
                rotation.setAxis(Rotate.Z_AXIS);
                rotation.setFromAngle(0);
                rotation.setToAngle(360);
                rotation.setInterpolator(Interpolator.LINEAR);
                rotation.setCycleCount(Animation.INDEFINITE);
                return rotation;
            }
        };
    }

    public void setWallBaseColor(Color color) {
        requireNonNull(color);
        wallBaseMaterial.setDiffuseColor(Ufx.colorWithOpacity(color, wallBaseOpacity));
    }

    public void setWallTopColor(Color color) {
        requireNonNull(color);
        wallTopMaterial.setDiffuseColor(color);
    }

    public void setDoorColor(Color color) {
        requireNonNull(color);
        barMaterial.setDiffuseColor(color);
    }

    private Group createDoor(Vector2i tile, double height) {
        var door = new Group();
        door.setTranslateX(tile.x() * TS);
        door.setTranslateY(tile.y() * TS + HTS);
        float barDistance = (float)TS / DOOR_VERTICAL_BAR_COUNT;
        for (int i = 0; i < DOOR_VERTICAL_BAR_COUNT; ++i) {
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
            doorsOpenProperty.set(ghostNearHouseEntry);
        });
    }

    public List<Group> swirls() {
        return List.of(swirls);
    }

    public BooleanProperty openProperty() {return doorsOpenProperty;}

    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeightProperty;
    }

    public void setDoorsVisible(boolean visible) {
        doors.setVisible(visible);
    }

    public ManagedAnimation doorsOpenCloseAnimation() {
        return doorsMeltingAnimation;
    }

    public PointLight light() {
        return light;
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
        doorsMeltingAnimation.stop();
        doorsMeltingAnimation.dispose();
        doorsMeltingAnimation = null;

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
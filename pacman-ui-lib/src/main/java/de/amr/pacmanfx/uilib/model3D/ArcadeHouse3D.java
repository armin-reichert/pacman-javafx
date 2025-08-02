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
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * 3D house in Arcade style.
 */
public class ArcadeHouse3D extends Group implements Disposable {

    private static final int NUM_VERTICAL_BARS = 2;
    private static final float BAR_THICKNESS = 0.75f;

    private final DoubleProperty barThicknessProperty = new SimpleDoubleProperty(BAR_THICKNESS);
    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty();

    private TerrainRenderer3D r3D;

    private PhongMaterial barMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    private Group door;
    private Group leftWing;
    private Group rightWing;
    private PointLight light;

    private Group[] particleSwirls;
    private List<ManagedAnimation> particleSwirlAnimations = new ArrayList<>(3);

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

        Vector2i topLeft = Vector2i.of(xMin, yMin);
        Vector2i bottomLeft = Vector2i.of(xMin, yMax);
        Vector2i topRight = Vector2i.of(xMax, yMin);
        Vector2i bottomRight = Vector2i.of(xMax, yMax);
        Vector2i doorLeft = Vector2i.of(house.leftDoorTile().x() - 1, yMin);
        Vector2i doorRight = Vector2i.of(house.rightDoorTile().x() + 1, yMin);

        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
        });
        r3D.createWallBetweenTiles(topLeft, doorLeft, wallThickness).addToGroup(this);
        r3D.createWallBetweenTiles(doorRight, topRight, wallThickness).addToGroup(this);
        r3D.createWallBetweenTiles(topRight, bottomRight, wallThickness).addToGroup(this);
        r3D.createWallBetweenTiles(topLeft, bottomLeft, wallThickness).addToGroup(this);
        r3D.createWallBetweenTiles(bottomLeft, bottomRight, wallThickness).addToGroup(this);

        getChildren().addAll(light, door);

        createParticleSwirls(gameLevel);

        Duration rotationTime = Duration.seconds(EnergizerExplosionAndRecycling.PARTICLE_SWIRL_ROTATION_SEC);
        for (int i = 0; i < particleSwirls.length; ++i) {
            final int index = i;
            particleSwirlAnimations.add(new ManagedAnimation(animationRegistry, "Swirl_%d".formatted(i)) {
                @Override
                protected Animation createAnimationFX() {
                    var rotation = new RotateTransition(rotationTime, particleSwirls[index]);
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
        return List.of(particleSwirls);
    }

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

        for (int i = 0; i < NUM_VERTICAL_BARS; ++i) {
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


    private void createParticleSwirls(GameLevel gameLevel) {
        Vector2f[] centers = {
                gameLevel.ghost(CYAN_GHOST_BASHFUL).revivalPosition().plus(HTS, HTS),
                gameLevel.ghost(PINK_GHOST_SPEEDY).revivalPosition().plus(HTS, HTS),
                gameLevel.ghost(ORANGE_GHOST_POKEY).revivalPosition().plus(HTS, HTS),
        };
        particleSwirls = new Group[3];
        for (int i = 0; i < particleSwirls.length; ++i) {
            particleSwirls[i] = new Group();
            particleSwirls[i].setTranslateX(centers[i].x());
            particleSwirls[i].setTranslateY(centers[i].y());
        }
    }

    public void startSwirlAnimations() {
        if (particleSwirlAnimations != null) {
            particleSwirlAnimations.forEach(ManagedAnimation::playFromStart);
            Logger.info("Swirl animations started");
        }
    }

    public void stopSwirlAnimations() {
        if (particleSwirlAnimations != null) {
            particleSwirlAnimations.forEach(ManagedAnimation::stop);
        }
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

    @Override
    public void dispose() {
        doorOpenCloseAnimation.stop();
        doorOpenCloseAnimation.dispose();
        doorOpenCloseAnimation = null;

        if (particleSwirlAnimations != null) {
            for (ManagedAnimation swirlAnimation : particleSwirlAnimations) {
                swirlAnimation.dispose();
            }
            particleSwirlAnimations.clear();
            particleSwirlAnimations = null;
            Logger.info("Disposed swirl animations");
        }
        for (Node child : getChildren()) {
            Wall3D.dispose(child); // does nothing if child is not part of 3D wall
        }

        if (particleSwirls != null) {
            for (Group swirl : particleSwirls) {
                swirl.getChildren().forEach(child -> {
                    if (child instanceof EnergizerExplosionAndRecycling.Particle particle) {
                        particle.dispose();
                    }
                });
                swirl.getChildren().clear();
                particleSwirls = null;
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
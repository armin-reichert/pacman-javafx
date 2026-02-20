/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.uilib.UfxColors;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.UfxColors.colorWithOpacity;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of the classic Pac‑Man ghost house.
 * <p>
 * This class constructs the house walls, doors, and interior lighting using JavaFX 3D primitives.
 * The geometry is derived from the {@link ArcadeHouse} model and scaled to world coordinates.
 * <p>
 * The house reacts dynamically to game state:
 * <ul>
 *   <li>Doors open when ghosts approach the entry</li>
 *   <li>A point light activates when ghost access is required</li>
 *   <li>A “melting” animation can shrink and regrow the door bars</li>
 * </ul>
 * <p>
 * All created 3D nodes are owned by this group and cleaned up via {@link #dispose()}.
 */
public class ArcadeHouse3D extends Group implements Disposable {

    private static final int DOOR_VERTICAL_BAR_COUNT = 4;

    /** Thickness of the vertical door bars. Animated during the melting effect. */
    private final DoubleProperty barThicknessProperty = new SimpleDoubleProperty(0.25);

    /** Whether the doors should appear open (bars shrink). */
    private final BooleanProperty doorsOpenProperty = new SimpleBooleanProperty(false);

    /** Height of the lower wall segment. */
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

    /** Distance threshold for detecting ghosts near the house entry. */
    private float doorSensitivity = 10;

    /** Animation that shrinks and regrows the door bars. */
    private RegisteredAnimation doorsMeltingAnimation;

    /**
     * Creates a 3D ghost house representation.
     *
     * @param animationRegistry registry used to register the door animation
     * @param house             logical house model defining geometry and door positions
     * @param baseHeight        height of the lower wall segment
     * @param wallThickness     thickness of the wall cylinders
     * @param opacity           opacity of the wall base material
     */
    public ArcadeHouse3D(
        AnimationRegistry animationRegistry,
        ArcadeHouse house,
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

        // Compute house corner coordinates in world space
        float xMin = house.minTile().x() * TS + HTS, yMin = house.minTile().y() * TS + HTS;
        float xMax = house.maxTile().x() * TS + HTS, yMax = house.maxTile().y() * TS + HTS;

        // Define wall corner points
        Vector2f p0 = Vector2f.of(xMin, yMin);
        Vector2f p1 = house.leftDoorTile().scaled((float) TS).plus(0, HTS);
        Vector2f p2 = house.rightDoorTile().scaled((float) TS).plus(TS, HTS);
        Vector2f p3 = Vector2f.of(xMax, yMin);
        Vector2f p4 = Vector2f.of(xMin, yMax);
        Vector2f p5 = Vector2f.of(xMax, yMax);

        // Configure wall creation callback
        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            getChildren().addAll(wall3D.top(), wall3D.base());
            return wall3D;
        });

        // Build walls
        r3D.createCylinderWall(p0, 0.5 * wallThickness);
        r3D.createCylinderWall(p3, 0.5 * wallThickness);
        r3D.createCylinderWall(p4, 0.5 * wallThickness);
        r3D.createCylinderWall(p5, 0.5 * wallThickness);
        r3D.createWallBetween(p0, p1, wallThickness);
        r3D.createWallBetween(p2, p3, wallThickness);
        r3D.createWallBetween(p3, p5, wallThickness);
        r3D.createWallBetween(p0, p4, wallThickness);
        r3D.createWallBetween(p4, p5, wallThickness);

        // Create doors
        leftDoor  = createDoor(house.leftDoorTile(), wallBaseHeightProperty.get());
        rightDoor = createDoor(house.rightDoorTile(), wallBaseHeightProperty.get());
        doors = new Group(leftDoor, rightDoor);

        // Interior light
        Vector2f houseCenter = p0.midpoint(p5);
        light = new PointLight(Color.GHOSTWHITE);
        light.setMaxRange(2.5 * TS);
        light.setTranslateX(houseCenter.x());
        light.setTranslateY(houseCenter.y());
        light.translateZProperty().bind(wallBaseHeightProperty.multiply(-1));

        // Door melting animation
        doorsMeltingAnimation = new RegisteredAnimation(animationRegistry, "Doors_Melting");
        doorsMeltingAnimation.setFactory(() -> new Timeline(
            new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
            new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, barThickness)))
        );
    }

    /**
     * Sets the diffuse color of the lower wall segment.
     */
    public void setWallBaseColor(Color color) {
        requireNonNull(color);
        wallBaseMaterial.setDiffuseColor(UfxColors.colorWithOpacity(color, wallBaseOpacity));
    }

    /**
     * Sets the diffuse color of the upper wall segment.
     */
    public void setWallTopColor(Color color) {
        requireNonNull(color);
        wallTopMaterial.setDiffuseColor(color);
    }

    /**
     * Sets the color of the door bars.
     */
    public void setDoorColor(Color color) {
        requireNonNull(color);
        barMaterial.setDiffuseColor(color);
    }

    /**
     * Creates a door consisting of vertical bars and a horizontal top bar.
     *
     * @param tile   tile coordinate of the door
     * @param height height of the vertical bars
     * @return a group containing the door geometry
     */
    private Group createDoor(Vector2i tile, double height) {
        var door = new Group();
        door.setTranslateX(tile.x() * TS);
        door.setTranslateY(tile.y() * TS + HTS);

        float barDistance = (float) TS / DOOR_VERTICAL_BAR_COUNT;

        // Vertical bars
        for (int i = 0; i < DOOR_VERTICAL_BAR_COUNT; ++i) {
            var vBar = new Cylinder(barThicknessProperty.get(), height);
            vBar.radiusProperty().bind(barThicknessProperty);
            vBar.setMaterial(barMaterial);
            vBar.setRotationAxis(Rotate.X_AXIS);
            vBar.setRotate(90);
            vBar.setTranslateX((i + 0.5) * barDistance);
            vBar.translateZProperty().bind(vBar.heightProperty().multiply(-0.5));
            door.getChildren().add(vBar);
        }

        // Horizontal top bar
        var hBar = new Cylinder(barThicknessProperty.get(), 2 * TS);
        hBar.radiusProperty().bind(barThicknessProperty);
        hBar.setMaterial(barMaterial);
        hBar.setRotationAxis(Rotate.Z_AXIS);
        hBar.setRotate(90);
        hBar.setTranslateX(HTS);
        hBar.setTranslateZ(-0.5 * (height + barThickness));
        door.getChildren().add(hBar);

        return door;
    }

    /**
     * Sets the distance threshold used to detect ghosts near the house entry.
     */
    public void setDoorSensitivity(float value) {
        this.doorSensitivity = value;
    }

    /**
     * Updates the house state based on the current game level.
     * <p>
     * This method:
     * <ul>
     *   <li>Activates the interior light when ghost access is required</li>
     *   <li>Opens the doors when a ghost approaches the entry</li>
     * </ul>
     */
    public void update(GameLevel gameLevel) {
        boolean accessRequested = gameLevel
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        light.lightOnProperty().set(accessRequested);

        gameLevel.worldMap().terrainLayer().optHouse().ifPresent(house -> {
            boolean ghostNearHouseEntry = gameLevel
                .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDist(house.entryPosition()) <= doorSensitivity)
                .anyMatch(Ghost::isVisible);
            doorsOpenProperty.set(ghostNearHouseEntry);
        });
    }

    /** Property controlling whether the doors appear open. */
    public BooleanProperty openProperty() {
        return doorsOpenProperty;
    }

    /** Height property of the lower wall segment. */
    public DoubleProperty wallBaseHeightProperty() {
        return wallBaseHeightProperty;
    }

    /** Returns the group containing both doors. */
    public Group doors() {
        return doors;
    }

    /** Shows or hides the door geometry. */
    public void setDoorsVisible(boolean visible) {
        doors.setVisible(visible);
    }

    /** Returns the animation that melts and regrows the door bars. */
    public RegisteredAnimation doorsOpenCloseAnimation() {
        return doorsMeltingAnimation;
    }

    /** Returns the interior point light. */
    public PointLight light() {
        return light;
    }

    /**
     * Disposes all 3D resources created by this house.
     * <p>
     * This method:
     * <ul>
     *   <li>Unbinds all properties</li>
     *   <li>Stops and disposes animations</li>
     *   <li>Clears materials and geometry</li>
     *   <li>Removes all children from the scene graph</li>
     * </ul>
     */
    @Override
    public void dispose() {
        openProperty().unbind();
        wallBaseHeightProperty().unbind();

        doorsMeltingAnimation.stop();
        doorsMeltingAnimation.dispose();
        doorsMeltingAnimation = null;

        for (Node child : getChildren()) {
            Wall3D.dispose(child);
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

    /**
     * Disposes all cylinders inside a door group.
     */
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

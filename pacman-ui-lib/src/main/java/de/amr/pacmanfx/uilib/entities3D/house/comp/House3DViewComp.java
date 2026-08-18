package de.amr.pacmanfx.uilib.entities3D.house.comp;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.entities.house.comp.HouseFloorplanComp;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.entities3D.world.TerrainRenderer3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.basics.util.Ufx.colorWithOpacity;
import static de.amr.basics.util.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

public class House3DViewComp implements EntityComponent, DisposableGraphicsObject {

    public static final int DOOR_VERTICAL_BAR_COUNT = 4;

    /** Thickness of the vertical door bars. Animated during the melting effect. */
    public final DoubleProperty barThicknessProperty = new SimpleDoubleProperty(0.25);

    /** Height of the lower wall segment. */
    private final DoubleProperty wallBaseHeightProperty = new SimpleDoubleProperty();

    private final float barThickness;
    private final double wallBaseOpacity;

    private final TerrainRenderer3D r3D;

    private PhongMaterial barMaterial;
    private PhongMaterial wallBaseMaterial;
    private PhongMaterial wallTopMaterial;

    private final Group root = new Group();
    private PointLight light;
    private Group doors;
    private Group leftDoor;
    private Group rightDoor;

    /** Distance threshold for detecting ghosts near the house entry. */
    private float doorSensitivity = 10;

    /**
     * Creates a 3D ghost house representation.
     *
     * @param floorplan         floor plan (geometry, door positions)
     * @param baseHeight        height of the lower wall segment
     * @param wallThickness     thickness of the wall cylinders
     * @param opacity           opacity of the wall base material
     */
    public House3DViewComp(
        HouseFloorplanComp floorplan,
        double baseHeight,
        double wallThickness,
        double opacity)
    {
        requireNonNull(floorplan);

        r3D = new TerrainRenderer3D();

        wallBaseOpacity = opacity;
        wallBaseMaterial = coloredPhongMaterial(colorWithOpacity(Color.BLUE, 0.5));
        wallTopMaterial  = coloredPhongMaterial(Color.YELLOW);
        barMaterial      = coloredPhongMaterial(Color.PINK);

        wallBaseHeightProperty.set(baseHeight);
        barThickness = 2f / DOOR_VERTICAL_BAR_COUNT;
        barThicknessProperty.set(barThickness);

        // Compute house corner coordinates in world space
        float xMin = floorplan.minTile().x() * WorldMap.TS + WorldMap.HTS;
        float yMin = floorplan.minTile().y() * WorldMap.TS + WorldMap.HTS;
        float xMax = floorplan.maxTile().x() * WorldMap.TS + WorldMap.HTS;
        float yMax = floorplan.maxTile().y() * WorldMap.TS + WorldMap.HTS;

        // Define wall corner points
        Vector2f p0 = vec2_float(xMin, yMin);
        Vector2f p1 = floorplan.leftDoorTile().scaled((float) WorldMap.TS).plus(0, WorldMap.HTS);
        Vector2f p2 = floorplan.rightDoorTile().scaled((float) WorldMap.TS).plus(WorldMap.TS, WorldMap.HTS);
        Vector2f p3 = vec2_float(xMax, yMin);
        Vector2f p4 = vec2_float(xMin, yMax);
        Vector2f p5 = vec2_float(xMax, yMax);

        // Configure wall creation callback
        r3D.setOnWallCreated(wall3D -> {
            wall3D.bindBaseHeight(wallBaseHeightProperty);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            root.getChildren().addAll(wall3D.top(), wall3D.base());
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
        leftDoor  = createDoor(floorplan.leftDoorTile(), wallBaseHeightProperty.get());
        rightDoor = createDoor(floorplan.rightDoorTile(), wallBaseHeightProperty.get());
        doors = new Group(leftDoor, rightDoor);

        // Interior light
        Vector2f houseCenter = p0.midpoint(p5);
        light = new PointLight(Color.GHOSTWHITE);
        light.setMaxRange(2.5 * WorldMap.TS);
        light.setTranslateX(houseCenter.x());
        light.setTranslateY(houseCenter.y());
        light.translateZProperty().bind(wallBaseHeightProperty.multiply(-1));

    }

    public Group root() {
        return root;
    }

    public PointLight light() {
        return light;
    }

    /**
     * Sets the diffuse color of the lower wall segment.
     */
    public void setWallBaseColor(Color color) {
        requireNonNull(color);
        wallBaseMaterial.setDiffuseColor(Ufx.colorWithOpacity(color, wallBaseOpacity));
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
        door.setTranslateX(tile.x() * WorldMap.TS);
        door.setTranslateY(tile.y() * WorldMap.TS + WorldMap.HTS);

        float barDistance = (float) WorldMap.TS / DOOR_VERTICAL_BAR_COUNT;

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
        var hBar = new Cylinder(barThicknessProperty.get(), 2 * WorldMap.TS);
        hBar.radiusProperty().bind(barThicknessProperty);
        hBar.setMaterial(barMaterial);
        hBar.setRotationAxis(Rotate.Z_AXIS);
        hBar.setRotate(90);
        hBar.setTranslateX(WorldMap.HTS);
        hBar.setTranslateZ(-0.5 * (height + barThickness));
        door.getChildren().add(hBar);

        return door;
    }

    public float doorSensitivity() {
        return doorSensitivity;
    }

    /**
     * Sets the distance threshold used to detect ghosts near the house entry.
     */
    public void setDoorSensitivity(float value) {
        this.doorSensitivity = value;
    }

    public DoubleProperty barThicknessProperty() {
        return barThicknessProperty;
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

    @Override
    public void dispose() {
        r3D.setOnWallCreated(null);
        wallBaseHeightProperty().unbind();
        barMaterial = wallBaseMaterial = wallTopMaterial = null;
        cleanupGroup(doors, true);
        leftDoor = rightDoor = doors = null;
        cleanupLight(light);
        light = null;
        cleanupGroup(root, true);

        //animations.optAnimation(House3DAnimationID.HOUSE_DOORS_MELTING).ifPresent(ManagedAnimation::dispose);
    }
}

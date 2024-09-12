/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameWorld;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public class House3D {

    static final PhongMaterial DEFAULT_MATERIAL = new PhongMaterial();
    static final float WALL_COAT_HEIGHT = 0.1f;

    public final DoubleProperty heightPy = new SimpleDoubleProperty(this, "height", 7);
    public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(this, "wallThickness", 1.5);
    public final BooleanProperty openPy = new SimpleBooleanProperty();
    public final BooleanProperty usedPy = new SimpleBooleanProperty();
    public final ObjectProperty<PhongMaterial> fillMaterialPy  = new SimpleObjectProperty<>(this, "fillMaterial", DEFAULT_MATERIAL);
    public final ObjectProperty<PhongMaterial> strokeMaterialPy  = new SimpleObjectProperty<>(this, "strokeMaterial", DEFAULT_MATERIAL);

    private final Group root = new Group();
    private final Door3D door3D;

    public House3D(GameWorld world) {
        WorldMap map = world.map();

        // tile coordinates
        int xMin = world.houseTopLeftTile().x();
        int xMax = xMin + world.houseSize().x() - 1;
        int yMin = world.houseTopLeftTile().y();
        int yMax = yMin + world.houseSize().y() - 1;

        Vector2i leftDoorTile = world.houseLeftDoorTile(), rightDoorTile = world.houseRightDoorTile();
        root.getChildren().addAll(
            createWall(xMin, yMin, leftDoorTile.x() - 1, yMin),
            createWall(rightDoorTile.x() + 1, yMin, xMax, yMin),
            createWall(xMin, yMin, xMin, yMax),
            createWall(xMax, yMin, xMax, yMax),
            createWall(xMin, yMax, xMax, yMax)
        );

        Color doorColor = getColorFromMap(map.terrain(), GameWorld.PROPERTY_COLOR_DOOR, Color.rgb(254,184,174));
        door3D = new Door3D(leftDoorTile, rightDoorTile, doorColor);
        door3D.drawModePy.bind(PY_3D_DRAW_MODE);

        // pixel coordinates
        float centerX = world.houseTopLeftTile().x() * TS + world.houseSize().x() * HTS;
        float centerY = world.houseTopLeftTile().y() * TS + world.houseSize().y() * HTS;

        var light = new PointLight();
        light.lightOnProperty().bind(usedPy);
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.translateZProperty().bind(heightPy.multiply(-1));
        root.getChildren().add(light);

        openPy.addListener((py, wasOpen, isOpen) -> {
            if (isOpen) {
                door3D.playTraversalAnimation();
            }
        });
    }

    private Node createWall(int x1, int y1, int x2, int y2) {
        return WallBuilder.createWall(v2i(x1, y1), v2i(x2, y2),
            wallThicknessPy.get(), heightPy, WALL_COAT_HEIGHT,
            fillMaterialPy, strokeMaterialPy);
    }

    public Group root() { return root; }

    public Door3D door3D() {
        return door3D;
    }
}
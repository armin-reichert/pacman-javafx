/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public class House3D {

    static final float WALL_COAT_HEIGHT = 0.1f;
    static final float WALL_THICKNESS = 1.5f;

    public final DoubleProperty baseWallHeightPy = new SimpleDoubleProperty(this, "baseWallHeight", 7);
    public final BooleanProperty openPy  = new SimpleBooleanProperty();
    public final BooleanProperty usedPy  = new SimpleBooleanProperty();

    private final Group root = new Group();
    private Door3D door3D;

    private final WorldRenderer3D worldRenderer3D = new WorldRenderer3D();

    public House3D() {
        openPy.addListener((py, wasOpen, isOpen) -> {
            if (isOpen) {
                door3D.playTraversalAnimation();
            }
        });
        worldRenderer3D.setWallBaseHeightProperty(baseWallHeightPy);
        worldRenderer3D.setWallTopHeight(WALL_COAT_HEIGHT);
    }

    public void build(GameWorld world, WorldMapColoring coloring) {
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

        door3D = new Door3D(leftDoorTile, rightDoorTile, coloring.door());
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
        light.translateZProperty().bind(baseWallHeightPy.multiply(-1));
        root.getChildren().add(light);
    }

    public WorldRenderer3D renderer3D() {
        return worldRenderer3D;
    }

    private Node createWall(int x1, int y1, int x2, int y2) {
        return worldRenderer3D.createCompositeWallBetweenTiles(vec_2i(x1, y1), vec_2i(x2, y2), WALL_THICKNESS);
    }

    public Group root() { return root; }

    public Door3D door3D() {
        return door3D;
    }
}
/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.WorldRenderer3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.parseColor;

public class MazePreview3D {

    private final DoubleProperty widthPy = new SimpleDoubleProperty(800);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);
    private final BooleanProperty wireframePy = new SimpleBooleanProperty(false);

    private final Group root = new Group();
    private final Group mazeGroup = new Group();
    private final Group foodGroup = new Group();

    private final WorldRenderer3D r3D;
    private final PerspectiveCamera camera;

    private final DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(3.5);

    public Node root() {
        return root;
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public MazePreview3D(double width, double height) {
        r3D = new WorldRenderer3D();
        root.getChildren().addAll(mazeGroup, foodGroup);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root.getChildren().add(ambientLight);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30

        widthPy.set(width);
        heightPy.set(height);
    }

    public DoubleProperty widthProperty() { return widthPy; }

    public DoubleProperty heightProperty() { return heightPy; }

    public BooleanProperty wireframeProperty() { return wireframePy; }

    public void updateMaze(WorldMap worldMap) {
        TileMap terrain = worldMap.terrain();
        double worldWidth = terrain.numCols() * TS;
        double worldHeight = terrain.numRows() * TS;

        Color wallBaseColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_STROKE, parseColor(COLOR_WALL_STROKE));
        Color wallTopColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_FILL, parseColor(COLOR_WALL_FILL));
        Color doorColor = getColorFromMap(terrain, PROPERTY_COLOR_DOOR, parseColor(COLOR_DOOR));

        mazeGroup.getChildren().clear();

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(WorldRenderer3D.coloredMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallBaseMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setCornerMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setWallTopMaterial(WorldRenderer3D.coloredMaterial(wallTopColor));

        for (Obstacle obstacle : worldMap.obstacles()) {
            r3D.renderObstacle3D(mazeGroup, obstacle);
        }

        var doorMaterial = WorldRenderer3D.coloredMaterial(doorColor);
        terrain.tiles().filter(tile -> terrain.get(tile) == TileEncoding.DOOR).forEach(tile -> {
            Box door = new Box(7, 2, wallBaseHeightPy.get());
            door.setMaterial(doorMaterial);
            door.setTranslateX(tile.x() * TS + HTS);
            door.setTranslateY(tile.y() * TS + HTS);
            door.setTranslateZ(-door.getDepth() * 0.5);
            mazeGroup.getChildren().add(door);
        });

        // exclude normal pellets from wireframe display
        mazeGroup.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframePy.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

        Logger.info("Maze 3D recreated");
    }

    public void updateFood(WorldMap worldMap) {
        TileMap food = worldMap.food();
        Color foodColor = getColorFromMap(food, PROPERTY_COLOR_FOOD, parseColor(COLOR_FOOD));
        var foodMaterial = WorldRenderer3D.coloredMaterial(foodColor);
        foodGroup.getChildren().clear();
        food.tiles().filter(tile -> hasFoodAt(food, tile)).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -4);
            boolean energizer = hasEnergizerAt(food, tile);
            var pellet = new Sphere(energizer ? 4 : 1, 32);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(position.getZ());
            foodGroup.getChildren().add(pellet);
        });
    }

    private boolean hasFoodAt(TileMap food, Vector2i tile) {
        return food.get(tile) != TileEncoding.EMPTY;
    }

    private boolean hasEnergizerAt(TileMap food, Vector2i tile) {
        return food.get(tile) == TileEncoding.ENERGIZER;
    }
}
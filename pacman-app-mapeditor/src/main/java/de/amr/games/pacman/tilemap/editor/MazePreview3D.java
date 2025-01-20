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
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.WorldMap.PROPERTY_COLOR_FOOD;
import static de.amr.games.pacman.lib.tilemap.WorldMap.PROPERTY_COLOR_WALL_FILL;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.parseColor;

public class MazePreview3D {

    public record SubSceneEmbedding(SubScene subScene, MazePreview3D mazePreview3D) {}

    private final DoubleProperty widthPy = new SimpleDoubleProperty(800);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);
    private final BooleanProperty wireframePy = new SimpleBooleanProperty(false);

    private final Group root = new Group();
    private final Group mazeGroup = new Group();
    private final Group foodGroup = new Group();

    private final WorldRenderer3D r3D;
    private final PerspectiveCamera camera;

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

    public void update(WorldMap worldMap) {
        updateMaze(worldMap);
        updateFood(worldMap);
    }

    private void updateMaze(WorldMap worldMap) {
        double worldWidth = worldMap.terrain().numCols() * TS;
        double worldHeight = worldMap.terrain().numRows() * TS;
        TileMap terrainMap = worldMap.terrain();
        Color wallBaseColor = getColorFromMap(terrainMap, WorldMap.PROPERTY_COLOR_WALL_STROKE, parseColor(COLOR_WALL_STROKE));
        Color wallTopColor = getColorFromMap(terrainMap, PROPERTY_COLOR_WALL_FILL, parseColor(COLOR_WALL_FILL));

        mazeGroup.getChildren().clear();

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(WorldRenderer3D.coloredMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        r3D.setWallBaseMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setCornerMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setWallTopMaterial(WorldRenderer3D.coloredMaterial(wallTopColor));
        for (Obstacle obstacle : worldMap.obstacles()) {
            r3D.renderObstacle3D(mazeGroup, obstacle);
        }

        // exclude normal pellets from wireframe display
        mazeGroup.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframePy.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

        Logger.info("Maze 3D recreated");
    }

    public void updateFood(WorldMap worldMap) {
        Color foodColor = getColorFromMap(worldMap.food(), PROPERTY_COLOR_FOOD, parseColor(COLOR_FOOD));
        var foodMaterial = WorldRenderer3D.coloredMaterial(foodColor);
        foodGroup.getChildren().clear();
        worldMap.food().tiles().filter(tile -> hasFood(worldMap, tile)).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
            boolean energizer = hasEnergizer(worldMap, tile);
            var pellet = new Sphere(energizer ? 4 : 1, 32);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(-4);
            foodGroup.getChildren().add(pellet);
        });
    }

    private boolean hasFood(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) != TileEncoding.EMPTY;
    }

    private boolean hasEnergizer(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) == TileEncoding.ENERGIZER;
    }
}
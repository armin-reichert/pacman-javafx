package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.WorldRenderer3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class Preview3D extends Group {

    public record SceneEmbedding(Scene scene, Preview3D preview3D) {}
    public record SubSceneEmbedding(SubScene subScene, Preview3D preview3D) {}

    private final DoubleProperty widthPy = new SimpleDoubleProperty(800);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);

    private final Group root = new Group();
    private final WorldRenderer3D r3D;
    private final PerspectiveCamera camera = new PerspectiveCamera();

    private WorldMap worldMap;

    private Color wallBaseColor;
    private Color wallTopColor;
    private Color foodColor;

    public static SceneEmbedding embedInScene() {
        var preview3D = new Preview3D();
        var scene = new Scene(preview3D.root, preview3D.widthPy.get(), preview3D.heightPy.get(), true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.CORNFLOWERBLUE);
        scene.setCamera(preview3D.camera);
        return new SceneEmbedding(scene, preview3D);
    }

    public Node root() {
        return root;
    }

    public Preview3D() {
        r3D = new WorldRenderer3D();
        root.getChildren().add(createSampleContent());
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(60);
    }

    public DoubleProperty widthProperty() { return widthPy; }

    public DoubleProperty heightProperty() { return heightPy; }

    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
        double worldWidth = worldMap.terrain().numCols() * TS;
        camera.translateXProperty().bind(widthProperty().subtract(worldWidth).multiply(-0.5));
        camera.translateYProperty().bind(heightProperty().multiply(-0.5));
        camera.setTranslateZ(50);
        updateContent();
    }

    public void setColors(Color wallBaseColor, Color wallTopColor, Color foodColor) {
        this.wallBaseColor = wallBaseColor;
        this.wallTopColor = wallTopColor;
        this.foodColor = foodColor;
    }

    private Node createSampleContent() {
        var sphere = new Sphere(200);
        sphere.setMaterial(WorldRenderer3D.coloredMaterial(Color.GREEN));
        return sphere;
    }

    private void updateContent() {
        root.getChildren().clear();

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root.getChildren().add(ambientLight);

        double worldWidth = worldMap.terrain().numCols() * TS;
        double worldHeight = worldMap.terrain().numRows() * TS;

        Group og = new Group();
        root.getChildren().add(og);

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(WorldRenderer3D.coloredMaterial(Color.BLACK));
        og.getChildren().add(floor);

        Group maze = new Group();
        root.getChildren().add(maze);

        r3D.setWallBaseMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setCornerMaterial(WorldRenderer3D.coloredMaterial(wallBaseColor));
        r3D.setWallTopMaterial(WorldRenderer3D.coloredMaterial(wallTopColor));
        for (Obstacle obstacle : worldMap.obstacles()) {
            r3D.renderObstacle3D(maze, obstacle);
        }

        var foodMaterial = WorldRenderer3D.coloredMaterial(foodColor);
        worldMap.food().tiles().filter(tile -> hasFood(worldMap, tile)).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
            var pellet = new Sphere(hasEnergizer(worldMap, tile) ? 4 : 1);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(-4);
            maze.getChildren().add(pellet);
        });
    }

    private boolean hasFood(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) != TileEncoding.EMPTY;
    }

    private boolean hasEnergizer(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) == TileEncoding.ENERGIZER;
    }
}

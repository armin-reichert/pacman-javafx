package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.WorldRenderer3D;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class Preview3D {

    public record SubSceneEmbedding(SubScene subScene, Preview3D preview3D) {}

    private final DoubleProperty widthPy = new SimpleDoubleProperty(800);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);
    private final BooleanProperty wireframePy = new SimpleBooleanProperty(false);

    private final Group root = new Group();
    private final WorldRenderer3D r3D;
    private final PerspectiveCamera camera;

    private WorldMap worldMap;

    private Color wallBaseColor;
    private Color wallTopColor;
    private Color foodColor;

    public Node root() {
        return root;
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public Preview3D(double width, double height) {
        r3D = new WorldRenderer3D();
        root.getChildren().add(createSampleContent());

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

    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
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
            boolean energizer = hasEnergizer(worldMap, tile);
            var pellet = new Sphere(energizer ? 4 : 1, 32);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(-4);
            pellet.setUserData(energizer ? "energizer" : "pellet");
            maze.getChildren().add(pellet);
        });

        // exclude normal pellets from wireframe display
        maze.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .filter(shape3D -> !"pellet".equals(shape3D.getUserData()))
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframePy.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

    }

    private boolean hasFood(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) != TileEncoding.EMPTY;
    }

    private boolean hasEnergizer(WorldMap worldMap, Vector2i tile) {
        return worldMap.food().get(tile) == TileEncoding.ENERGIZER;
    }
}

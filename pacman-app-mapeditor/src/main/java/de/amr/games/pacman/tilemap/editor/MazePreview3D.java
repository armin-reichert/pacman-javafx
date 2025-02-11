/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.parseColor;

public class MazePreview3D extends Group {

    private static final double ACTOR_SIZE = 12.0;

    private static PhongMaterial coloredMaterial(Color color) {
        assertNotNull(color);
        var material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        return material;
    }

    private final DoubleProperty widthPy = new SimpleDoubleProperty(800);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);
    private final BooleanProperty wireframePy = new SimpleBooleanProperty(false);
    private final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);

    private final Group mazeGroup = new Group();
    private final Group foodGroup = new Group();

    private final TerrainRenderer3D r3D;
    private final PerspectiveCamera camera;

    private final DoubleProperty wallBaseHeightPy = new SimpleDoubleProperty(3.5);

    private final Node pacmanShape3D;
    private final Node[] ghostShapes;

    public PerspectiveCamera camera() {
        return camera;
    }

    public MazePreview3D() {
        r3D = new TerrainRenderer3D();

        getChildren().addAll(mazeGroup, foodGroup);
        foodGroup.visibleProperty().bind(foodVisiblePy);
        mazeGroup.visibleProperty().bind(terrainVisiblePy);

        ResourceManager uiLibResources = () -> Ufx.class;
        Model3D pacmanModel3D = new Model3D(uiLibResources.url("model3D/pacman.obj"));
        pacmanShape3D = PacModel3D.createPacShape(pacmanModel3D, ACTOR_SIZE, Color.YELLOW, Color.BLACK, Color.GRAY);

        Model3D ghostModel3D = new Model3D(uiLibResources.url("model3D/ghost.obj"));
        ghostShapes = new Node[4];
        ghostShapes[0] = createGhostShape3D(ghostModel3D, Color.RED, 0);
        ghostShapes[1] = createGhostShape3D(ghostModel3D, Color.PINK, 270);
        ghostShapes[2] = createGhostShape3D(ghostModel3D, Color.CYAN, 90);
        ghostShapes[3] = createGhostShape3D(ghostModel3D, Color.ORANGE, 90);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        getChildren().add(ambientLight);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
    }

    private Node createGhostShape3D(Model3D model3D, Color dressColor, double rotate) {
        MeshView dress = new MeshView(model3D.mesh("Sphere.004_Sphere.034_light_blue_ghost"));
        dress.setMaterial(Ufx.coloredMaterial(dressColor));
        Bounds dressBounds = dress.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dress.getTransforms().add(centeredOverOrigin);

        MeshView pupils = new MeshView(model3D.mesh("Sphere.010_Sphere.039_grey_wall"));
        pupils.setMaterial(Ufx.coloredMaterial(Color.BLUE));

        MeshView eyeballs = new MeshView(model3D.mesh("Sphere.009_Sphere.036_white"));
        eyeballs.setMaterial(Ufx.coloredMaterial(Color.WHITE));
        var eyesGroup = new Group(pupils, eyeballs);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        var dressGroup = new Group(dress);

        Group root = new Group(dressGroup, eyesGroup);
        root.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(rotate, Rotate.Y_AXIS));
        Bounds b = root.getBoundsInLocal();
        root.getTransforms().add(new Scale(ACTOR_SIZE / b.getWidth(), ACTOR_SIZE / b.getHeight(), ACTOR_SIZE / b.getDepth()));

        return root;
    }

    public DoubleProperty widthProperty() { return widthPy; }

    public DoubleProperty heightProperty() { return heightPy; }

    public BooleanProperty wireframeProperty() { return wireframePy; }

    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }

    public void updateMaze(WorldMap worldMap) {
        TileMap terrain = worldMap.terrain();
        double worldWidth = terrain.numCols() * TS;
        double worldHeight = terrain.numRows() * TS;

        Color wallBaseColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_STROKE, parseColor(MS_PACMAN_COLOR_WALL_STROKE));
        Color wallTopColor = getColorFromMap(terrain, PROPERTY_COLOR_WALL_FILL, parseColor(MS_PACMAN_COLOR_WALL_FILL));
        Color doorColor = getColorFromMap(terrain, PROPERTY_COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR));

        mazeGroup.getChildren().clear();

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(coloredMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        r3D.setWallBaseHeightProperty(wallBaseHeightPy);
        r3D.setWallBaseMaterial(coloredMaterial(wallBaseColor));
        r3D.setWallTopMaterial(coloredMaterial(wallTopColor));
        r3D.setCornerBaseMaterial(coloredMaterial(wallBaseColor));
        r3D.setCornerTopMaterial(coloredMaterial(wallTopColor));

        for (Obstacle obstacle : worldMap.obstacles()) {
            r3D.renderObstacle3D(mazeGroup, obstacle);
        }

        var doorMaterial = coloredMaterial(doorColor);
        terrain.tiles().filter(tile -> terrain.get(tile) == TileEncoding.DOOR).forEach(tile -> {
            Box door = new Box(7, 2, wallBaseHeightPy.get());
            door.setMaterial(doorMaterial);
            door.setTranslateX(tile.x() * TS + HTS);
            door.setTranslateY(tile.y() * TS + HTS);
            door.setTranslateZ(-door.getDepth() * 0.5);
            door.visibleProperty().bind(terrainVisiblePy);
            mazeGroup.getChildren().add(door);
        });

        // exclude normal pellets from wireframe display
        mazeGroup.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframePy.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

        addActorShape(pacmanShape3D, terrain, PROPERTY_POS_PAC);
        addActorShape(ghostShapes[0], terrain, PROPERTY_POS_RED_GHOST);
        addActorShape(ghostShapes[1], terrain, PROPERTY_POS_PINK_GHOST);
        addActorShape(ghostShapes[2], terrain, PROPERTY_POS_CYAN_GHOST);
        addActorShape(ghostShapes[3], terrain, PROPERTY_POS_ORANGE_GHOST);
    }

    private void addActorShape(Node actorShape, TileMap terrain, String actorTilePropertyName) {
        Vector2i tile = terrain.getTileProperty(actorTilePropertyName, Vector2i.ZERO);
        Vector2f center = tile.scaled(TS).toVector2f().plus(TS, HTS);
        actorShape.setTranslateX(center.x());
        actorShape.setTranslateY(center.y());
        actorShape.setTranslateZ(-0.5 * ACTOR_SIZE);
        mazeGroup.getChildren().add(actorShape);
    }

    public void updateFood(WorldMap worldMap) {
        TileMap food = worldMap.food();
        Color foodColor = getColorFromMap(food, PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
        var foodMaterial = coloredMaterial(foodColor);
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
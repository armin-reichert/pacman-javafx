/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.model3D.GhostBody;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.ArcadeSprites.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.getColorFromMap;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.parseColor;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static java.util.Objects.requireNonNull;

public class Maze3D extends Group {

    private static final int EMPTY_ROWS_OVER_MAZE = 3;

    public static final float ACTOR_SIZE = 12.0f;
    public static final float HOUSE_WALL_HEIGHT = 12;
    public static final float HOUSE_DOOR_HEIGHT = 10;
    public static final float OBSTACLE_HEIGHT = 4;

    private static PhongMaterial coloredMaterial(Color color) {
        requireNonNull(color);
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

    private final Node pacmanShape3D;
    private final GhostBody[] ghostShapes;

    private final TileMapEditor editor;

    public Maze3D(TileMapEditor editor, Model3DRepository model3DRepository) {
        this.editor = requireNonNull(editor);
        requireNonNull(model3DRepository);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30

        r3D = new TerrainRenderer3D();

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        getChildren().addAll(ambientLight, mazeGroup, foodGroup);

        foodGroup.visibleProperty().bind(foodVisiblePy);

        pacmanShape3D = model3DRepository.createPacBody(ACTOR_SIZE, Color.YELLOW, Color.BLACK, Color.GRAY);
        pacmanShape3D.visibleProperty().bind(editor.actorsVisibleProperty());

        ghostShapes = new GhostBody[4];
        ghostShapes[0] = model3DRepository.createGhostBody(ACTOR_SIZE, Color.RED, 0);
        ghostShapes[1] = model3DRepository.createGhostBody(ACTOR_SIZE, Color.PINK, 90);
        ghostShapes[2] = model3DRepository.createGhostBody(ACTOR_SIZE, Color.CYAN, 270);
        ghostShapes[3] = model3DRepository.createGhostBody(ACTOR_SIZE, Color.ORANGE, 270);
        for (var ghostShape : ghostShapes) {
            ghostShape.visibleProperty().bind(editor.actorsVisibleProperty());
        }
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public void moveTowardsUser(double pixels) {
        setTranslateY(getTranslateY() + pixels);
    }

    public void moveLeft(double pixels) {
        setTranslateX(getTranslateX() - pixels);
    }

    public void moveRight(double pixels) {
        setTranslateX(getTranslateX() + pixels);
    }

    public void rotateBy(double angle) {
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(getRotate() + angle);
    }

    public void toggleWireframe() {
        wireframePy.set(!wireframePy.get());
    }

    public DoubleProperty widthProperty() { return widthPy; }

    public DoubleProperty heightProperty() { return heightPy; }

    public BooleanProperty foodVisibleProperty() { return foodVisiblePy; }

    public BooleanProperty terrainVisibleProperty() { return terrainVisiblePy; }

    public void updateMaze(WorldMap worldMap) {
        double worldWidth = worldMap.numCols() * TS;
        double worldHeight = worldMap.numRows() * TS;

        mazeGroup.getChildren().clear();

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(coloredMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        Color wallBaseColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE,
                parseColor(MS_PACMAN_COLOR_WALL_STROKE));
        Color wallTopColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL,
                parseColor(MS_PACMAN_COLOR_WALL_FILL));

        PhongMaterial wallBaseMaterial = coloredMaterial(wallBaseColor);
        PhongMaterial wallTopMaterial = coloredMaterial(wallTopColor);

        r3D.setOnWallCreated(wall3D -> {
            wall3D.setBaseHeight(OBSTACLE_HEIGHT);
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            wall3D.base().visibleProperty().bind(terrainVisibleProperty());
            wall3D.top().visibleProperty().bind(terrainVisibleProperty());
            mazeGroup.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });
        for (Obstacle obstacle : worldMap.obstacles()) {
            boolean worldBorder = isWorldBorder(worldMap, obstacle);
            r3D.renderObstacle3D(obstacle, worldBorder, 2, HTS);
        }
        r3D.setOnWallCreated(null);

        addHouse(worldMap, wallBaseColor, wallTopColor);

        // exclude normal pellets from wireframe display
        mazeGroup.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframePy.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

        addActorShape(pacmanShape3D, worldMap, WorldMapProperty.POS_PAC);
        addActorShape(ghostShapes[0], worldMap, WorldMapProperty.POS_RED_GHOST);
        addActorShape(ghostShapes[1], worldMap, WorldMapProperty.POS_PINK_GHOST);
        addActorShape(ghostShapes[2], worldMap, WorldMapProperty.POS_CYAN_GHOST);
        addActorShape(ghostShapes[3], worldMap, WorldMapProperty.POS_ORANGE_GHOST);
    }

    private void addHouse(WorldMap worldMap, Color wallBaseColor, Color wallTopColor) {
        Vector2i houseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i houseMaxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
        if (houseMinTile == null || houseMaxTile == null) {
            return;
        }
        Vector2i houseRightUpper = Vector2i.of(houseMaxTile.x(), houseMinTile.y());
        Vector2i houseLeftLower = Vector2i.of(houseMinTile.x(), houseMaxTile.y());

        PhongMaterial wallBaseMaterial = coloredMaterial(colorWithOpacity(wallBaseColor, 0.4));
        PhongMaterial wallTopMaterial = coloredMaterial(wallTopColor);

        r3D.setOnWallCreated(wall3D -> {
            wall3D.setBaseMaterial(wallBaseMaterial);
            wall3D.setTopMaterial(wallTopMaterial);
            wall3D.setBaseHeight(HOUSE_WALL_HEIGHT);
            wall3D.base().visibleProperty().bind(terrainVisibleProperty());
            wall3D.top().visibleProperty().bind(terrainVisibleProperty());
            mazeGroup.getChildren().addAll(wall3D.base(), wall3D.top());
            return wall3D;
        });
        r3D.createWallBetweenTileCoordinates(houseMinTile, houseMinTile.plus(2, 0), Wall3D.DEFAULT_WALL_THICKNESS);
        r3D.createWallBetweenTileCoordinates(houseRightUpper.minus(2, 0), houseRightUpper, Wall3D.DEFAULT_WALL_THICKNESS);
        r3D.createWallBetweenTileCoordinates(houseMinTile, houseLeftLower, Wall3D.DEFAULT_WALL_THICKNESS);
        r3D.createWallBetweenTileCoordinates(houseLeftLower, houseMaxTile, Wall3D.DEFAULT_WALL_THICKNESS);
        r3D.createWallBetweenTileCoordinates(houseMaxTile, houseRightUpper, Wall3D.DEFAULT_WALL_THICKNESS);

        Color doorColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, parseColor(MS_PACMAN_COLOR_DOOR));
        var doorMaterial = coloredMaterial(doorColor);
        Stream.of(houseMinTile.plus(3, 0), houseMinTile.plus(4, 0)).forEach(doorTile -> {
            Box door = new Box(TS + HTS, 2, HOUSE_DOOR_HEIGHT);
            door.setMaterial(doorMaterial);
            door.setTranslateX(doorTile.x() * TS + HTS);
            door.setTranslateY(doorTile.y() * TS + HTS);
            door.setTranslateZ(-door.getDepth() * 0.5);
            door.visibleProperty().bind(terrainVisibleProperty());
            mazeGroup.getChildren().add(door);
        });
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }

    private void addActorShape(Node actorShape, WorldMap worldMap, String actorTilePropertyName) {
        Vector2i tile = worldMap.getTerrainTileProperty(actorTilePropertyName);
        if (tile == null) {
            Logger.warn("Tile property '{}' has no value", actorTilePropertyName);
            return;
        }
        Vector2f center = tile.scaled(TS).toVector2f().plus(TS, HTS);
        actorShape.setTranslateX(center.x());
        actorShape.setTranslateY(center.y());
        actorShape.setTranslateZ(-0.5 * ACTOR_SIZE);
        mazeGroup.getChildren().add(actorShape);
    }

    public void updateFood(WorldMap worldMap) {
        Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
        var foodMaterial = coloredMaterial(foodColor);
        foodGroup.getChildren().clear();
        worldMap.tiles().filter(tile -> hasFoodAt(worldMap, tile)).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -4);
            boolean energizer = hasEnergizerAt(worldMap, tile);
            var pellet = new Sphere(energizer ? 4 : 1, 32);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(position.getZ());
            foodGroup.getChildren().add(pellet);
        });
    }

    private boolean hasFoodAt(WorldMap worldMap, Vector2i tile) {
        return worldMap.content(LayerID.FOOD, tile) != FoodTile.EMPTY.code();
    }

    private boolean hasEnergizerAt(WorldMap worldMap, Vector2i tile) {
        return worldMap.content(LayerID.FOOD, tile) == FoodTile.ENERGIZER.code();
    }
}
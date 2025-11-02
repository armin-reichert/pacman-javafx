/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.Obstacle;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.GhostBody;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.model3D.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.Wall3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.*;
import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;
import static java.util.Objects.requireNonNull;

public class Maze3D extends Group {

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

    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);

    public BooleanProperty actorsVisibleProperty() {
        return actorsVisible;
    }

    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    public ObjectProperty<WorldMap> worldMapProperty() {
        return worldMap;
    }

    public WorldMap worldMap() {
        return worldMap.get();
    }

    private final BooleanProperty wireframe = new SimpleBooleanProperty(false);

    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);

    public BooleanProperty foodVisibleProperty() { return foodVisible; }

    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);

    public BooleanProperty terrainVisibleProperty() { return terrainVisible; }

    public final Runnable actionRotateLeft = () -> rotateBy(-2);

    public final Runnable actionRotateRight = () -> rotateBy(2);

    public final Runnable actionMoveTowardsUser = () -> setTranslateY(getTranslateY() + 10);

    public final Runnable actionMoveAwayFromUser = () -> setTranslateY(getTranslateY() - 10);

    public final Runnable actionMoveRight = () -> setTranslateX(getTranslateX() - 10);

    public final Runnable actionMoveLeft = () -> setTranslateX(getTranslateX() + 10);

    public final Runnable actionToggleWireframe = () -> wireframe.set(!wireframe.get());

    private final Group mazeGroup = new Group();
    private final Group foodGroup = new Group();

    private final TerrainRenderer3D r3D;
    private final PerspectiveCamera camera;

    private final Node pacmanShape3D;
    private final GhostBody[] ghostShapes;

    public Maze3D(PacManModel3DRepository model3DRepository) {
        requireNonNull(model3DRepository);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30

        r3D = new TerrainRenderer3D();

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        getChildren().addAll(ambientLight, mazeGroup, foodGroup);

        foodGroup.visibleProperty().bind(foodVisible);

        pacmanShape3D = model3DRepository.createPacBody(ACTOR_SIZE, Color.YELLOW, Color.BLACK, Color.GRAY);
        pacmanShape3D.visibleProperty().bind(actorsVisibleProperty());

        ghostShapes = new GhostBody[] {
            model3DRepository.createGhostBody(ACTOR_SIZE, Color.RED, 0),
            model3DRepository.createGhostBody(ACTOR_SIZE, Color.PINK, 90),
            model3DRepository.createGhostBody(ACTOR_SIZE, Color.CYAN, 270),
            model3DRepository.createGhostBody(ACTOR_SIZE, Color.ORANGE, 270)
        };
        for (var ghostShape : ghostShapes) {
            ghostShape.visibleProperty().bind(actorsVisibleProperty());
        }

        worldMapProperty().addListener((py, oldMap, newMap) -> {
            if (newMap != null) updateMaze();
        });
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public void rotateBy(double angle) {
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(getRotate() + angle);
    }

    public void updateMaze() {
        final double worldWidth = worldMap().numCols() * TS;
        final double worldHeight = worldMap().numRows() * TS;

        mazeGroup.getChildren().clear();

        // Floor left-upper corner at origin
        Box floor = new Box(worldWidth, worldHeight, 0.1);
        floor.setTranslateX(0.5 * worldWidth);
        floor.setTranslateY(0.5 * worldHeight);
        floor.setMaterial(coloredMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        Color wallBaseColor = EditorUtil.getColorFromMapLayer(worldMap().terrainLayer(),
            DefaultWorldMapPropertyName.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        Color wallTopColor = EditorUtil.getColorFromMapLayer(worldMap().terrainLayer(),
            DefaultWorldMapPropertyName.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);

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
        for (Obstacle obstacle : worldMap().terrainLayer().obstacles()) {
            boolean worldBorder = Ufx.isBorderObstacle(worldMap(), obstacle);
            r3D.renderObstacle3D(obstacle, worldBorder, 2, HTS);
        }
        r3D.setOnWallCreated(null);

        addHouse(wallBaseColor, wallTopColor);

        // exclude normal pellets from wireframe display
        mazeGroup.lookupAll("*").stream()
                .filter(Shape3D.class::isInstance)
                .map(Shape3D.class::cast)
                .forEach(shape3D -> shape3D.drawModeProperty()
                        .bind(wireframe.map(wireframe -> wireframe ? DrawMode.LINE : DrawMode.FILL)));

        addActorShape(pacmanShape3D,  DefaultWorldMapPropertyName.POS_PAC);
        addActorShape(ghostShapes[0], DefaultWorldMapPropertyName.POS_GHOST_1_RED);
        addActorShape(ghostShapes[1], DefaultWorldMapPropertyName.POS_GHOST_2_PINK);
        addActorShape(ghostShapes[2], DefaultWorldMapPropertyName.POS_GHOST_3_CYAN);
        addActorShape(ghostShapes[3], DefaultWorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    private void addHouse(Color wallBaseColor, Color wallTopColor) {
        Vector2i houseMinTile = worldMap().terrainLayer().getTileProperty(DefaultWorldMapPropertyName.POS_HOUSE_MIN_TILE);
        Vector2i houseMaxTile = worldMap().terrainLayer().getTileProperty(DefaultWorldMapPropertyName.POS_HOUSE_MAX_TILE);
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

        Color doorColor = EditorUtil.getColorFromMapLayer(worldMap().terrainLayer(),
            DefaultWorldMapPropertyName.COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
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

    private void addActorShape(Node actorShape, String actorTilePropertyName) {
        Vector2i tile = worldMap().terrainLayer().getTileProperty(actorTilePropertyName);
        if (tile == null) {
            return;
        }
        Vector2f center = tile.scaled(TS).toVector2f().plus(TS, HTS);
        actorShape.setTranslateX(center.x());
        actorShape.setTranslateY(center.y());
        actorShape.setTranslateZ(-0.5 * ACTOR_SIZE);
        mazeGroup.getChildren().add(actorShape);
    }

    public void updateFood() {
        if (worldMap() == null) {
            return;
        }
        Color foodColor = EditorUtil.getColorFromMapLayer(worldMap().foodLayer(),
            DefaultWorldMapPropertyName.COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        var foodMaterial = coloredMaterial(foodColor);
        foodGroup.getChildren().clear();
        worldMap().terrainLayer().tiles().filter(this::hasFoodAt).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -4);
            boolean energizer = hasEnergizerAt(tile);
            var pellet = new Sphere(energizer ? 4 : 1, 32);
            pellet.setMaterial(foodMaterial);
            pellet.setTranslateX(position.getX());
            pellet.setTranslateY(position.getY());
            pellet.setTranslateZ(position.getZ());
            foodGroup.getChildren().add(pellet);
        });
    }

    private boolean hasFoodAt(Vector2i tile) {
        return worldMap().foodLayer().content(tile) != FoodTile.EMPTY.$;
    }

    private boolean hasEnergizerAt(Vector2i tile) {
        return worldMap().foodLayer().content(tile) == FoodTile.ENERGIZER.$;
    }
}
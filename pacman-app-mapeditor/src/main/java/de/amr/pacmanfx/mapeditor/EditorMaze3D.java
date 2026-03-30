/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.actor.MsPacManComponentColors;
import de.amr.pacmanfx.uilib.model3D.actor.PacComponentColors;
import de.amr.pacmanfx.uilib.model3D.actor.PacConfig;
import de.amr.pacmanfx.uilib.model3D.world.TerrainRenderer3D;
import de.amr.pacmanfx.uilib.model3D.world.Wall3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.Vector2i.vec2_int;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.*;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.UfxColors.colorWithOpacity;

public class EditorMaze3D extends Group {

    private static final PacConfig PAC_CONFIG =
        new PacConfig(
            new PacComponentColors(
                Color.YELLOW, // head
                Color.SADDLEBROWN,  // palate
                Color.BLACK // eyes
            ),
            new MsPacManComponentColors(
                Color.RED, // hair bow
                Color.BLUE, // hair bow pearls
                Color.YELLOW.deriveColor(0, 1.0, 0.96, 1.0) // boobs
            ),
            12.0f, 14.0f
        );

    public static final float GHOST_SIZE = 14.0f;
    public static final float HOUSE_WALL_HEIGHT = 12;
    public static final float HOUSE_DOOR_HEIGHT = 10;
    public static final float OBSTACLE_HEIGHT = 4;

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
    private final Group[] ghostShapes;

    public EditorMaze3D() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30

        r3D = new TerrainRenderer3D();

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        getChildren().addAll(ambientLight, mazeGroup, foodGroup);

        foodGroup.visibleProperty().bind(foodVisible);

        pacmanShape3D = TileMapEditorUI.PAC_MAN_MODEL.createPacBody(PAC_CONFIG);
        pacmanShape3D.visibleProperty().bind(actorsVisibleProperty());

        ghostShapes = new Group[] {
            createGhostBody(Color.RED,      0),
            createGhostBody(Color.PINK,    90),
            createGhostBody(Color.CYAN,   270),
            createGhostBody(Color.ORANGE, 270)
        };
        for (var ghostShape : ghostShapes) {
            ghostShape.visibleProperty().bind(actorsVisibleProperty());
        }

        worldMapProperty().addListener((_, _, newMap) -> {
            if (newMap != null) rebuildMaze();
        });
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public void rotateBy(double angle) {
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(getRotate() + angle);
    }

    public void rebuildMaze() {
        final double width  = worldMap().numCols() * TS;
        final double height = worldMap().numRows() * TS;

        mazeGroup.getChildren().clear();

        // Floor: Set left-upper corner at origin (centered at origin by default)
        final Box floor = new Box(width, height, 0.1);
        floor.setTranslateX(0.5 * width);
        floor.setTranslateY(0.5 * height);
        floor.setMaterial(Ufx.coloredPhongMaterial(Color.BLACK));
        mazeGroup.getChildren().add(floor);

        final Color wallBaseColor = UfxMapEditor.getColorFromMapLayer(worldMap().terrainLayer(),
            WorldMapPropertyName.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        final Color wallTopColor = UfxMapEditor.getColorFromMapLayer(worldMap().terrainLayer(),
            WorldMapPropertyName.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);

        PhongMaterial wallBaseMaterial = Ufx.coloredPhongMaterial(wallBaseColor);
        PhongMaterial wallTopMaterial = Ufx.coloredPhongMaterial(wallTopColor);

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
            boolean worldBorder = ObstacleBuilder.isBorderObstacle(obstacle, worldMap());
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

        addActorShape(pacmanShape3D,  WorldMapPropertyName.POS_PAC);
        addActorShape(ghostShapes[0], WorldMapPropertyName.POS_GHOST_1_RED);
        addActorShape(ghostShapes[1], WorldMapPropertyName.POS_GHOST_2_PINK);
        addActorShape(ghostShapes[2], WorldMapPropertyName.POS_GHOST_3_CYAN);
        addActorShape(ghostShapes[3], WorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    private Group createGhostBody(Color dressColor, double rotateY) {
        final Group body = new Group();

        final MeshView dressMeshView = new MeshView(TileMapEditorUI.GHOST_MODEL.dressMesh());
        dressMeshView.setMaterial(coloredPhongMaterial(dressColor));

        final MeshView pupilsMeshView = new MeshView(TileMapEditorUI.GHOST_MODEL.pupilsMesh());
        pupilsMeshView.setMaterial(coloredPhongMaterial(Color.BLUE));

        final MeshView eyeballsMeshView = new MeshView(TileMapEditorUI.GHOST_MODEL.eyeballsMesh());
        eyeballsMeshView.setMaterial(coloredPhongMaterial(Color.WHITE));

        final var dressGroup = new Group(dressMeshView);
        final var eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        body.getChildren().addAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressMeshView.getBoundsInLocal();
        final Bounds bounds = body.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());

        dressMeshView.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        body.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        body.getTransforms().add(new Rotate(rotateY, Rotate.Y_AXIS));
        body.getTransforms().add(new Scale(GHOST_SIZE / bounds.getWidth(), GHOST_SIZE / bounds.getHeight(), GHOST_SIZE / bounds.getDepth()));

        return body;
    }


    private void addHouse(Color wallBaseColor, Color wallTopColor) {
        Vector2i houseMinTile = worldMap().terrainLayer().getTileProperty(WorldMapPropertyName.POS_HOUSE_MIN_TILE);
        Vector2i houseMaxTile = worldMap().terrainLayer().getTileProperty(WorldMapPropertyName.POS_HOUSE_MAX_TILE);
        if (houseMinTile == null || houseMaxTile == null) {
            return;
        }
        Vector2i houseRightUpper = vec2_int(houseMaxTile.x(), houseMinTile.y());
        Vector2i houseLeftLower = vec2_int(houseMinTile.x(), houseMaxTile.y());

        PhongMaterial wallBaseMaterial = Ufx.coloredPhongMaterial(colorWithOpacity(wallBaseColor, 0.4));
        PhongMaterial wallTopMaterial = Ufx.coloredPhongMaterial(wallTopColor);

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

        Color doorColor = UfxMapEditor.getColorFromMapLayer(worldMap().terrainLayer(),
            WorldMapPropertyName.COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        var doorMaterial = Ufx.coloredPhongMaterial(doorColor);
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
        actorShape.setTranslateZ(-0.5 * GHOST_SIZE);
        mazeGroup.getChildren().add(actorShape);
    }

    public void updateFood() {
        if (worldMap() == null) {
            return;
        }
        Color foodColor = UfxMapEditor.getColorFromMapLayer(worldMap().foodLayer(),
            WorldMapPropertyName.COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        var foodMaterial = Ufx.coloredPhongMaterial(foodColor);
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
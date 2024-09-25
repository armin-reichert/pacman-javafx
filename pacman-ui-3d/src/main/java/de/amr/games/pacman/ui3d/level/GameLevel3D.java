/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    static final int   LIVES_COUNTER_MAX     = 5;
    static final float LIVE_SHAPE_SIZE       = 10;
    static final float FLOOR_THICKNESS       = 0.5f;
    static final float OBSTACLE_HEIGHT       = 5.5f;
    static final float OBSTACLE_COAT_HEIGHT  = 0.1f;
    static final float OBSTACLE_THICKNESS    = 0.5f;
    static final float BORDER_WALL_HEIGHT    = 6.0f;
    static final float BORDER_WALL_THICKNESS = 2.5f;
    static final float HOUSE_HEIGHT          = 12.0f;
    static final float HOUSE_OPACITY         = 0.4f;
    static final float HOUSE_SENSITIVITY     = 1.5f * TS;
    static final float PAC_SIZE              = 14.0f;
    static final float GHOST_SIZE            = 13.5f;
    static final float ENERGIZER_RADIUS      = 3.5f;
    static final float PELLET_RADIUS         = 1.0f;

    static final PhongMaterial DEFAULT_MATERIAL = new PhongMaterial();

    private final StringProperty floorTextureNamePy = new SimpleStringProperty(this, "floorTextureName", NO_TEXTURE);
    private final DoubleProperty borderWallHeightPy = new SimpleDoubleProperty(this, "borderWallHeight", BORDER_WALL_HEIGHT);
    private final DoubleProperty obstacleHeightPy   = new SimpleDoubleProperty(this, "obstacleHeight", OBSTACLE_HEIGHT);
    private final DoubleProperty wallOpacityPy      = new SimpleDoubleProperty(this, "wallOpacity",1.0);

    private final DoubleProperty  houseHeightPy = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);
    private final BooleanProperty houseUsedPy   = new SimpleBooleanProperty(this, "houseUsed", false);
    private final BooleanProperty houseOpenPy   = new SimpleBooleanProperty(this, "houseOpen", false);

    private final ObjectProperty<Color> floorColorPy      = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
    private final ObjectProperty<Color> wallFillColorPy   = new SimpleObjectProperty<>(this, "wallFillColor", Color.BLUE);
    private final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(this, "wallStrokeColor", Color.LIGHTBLUE);

    private final ObjectProperty<PhongMaterial> wallFillMaterialPy   = new SimpleObjectProperty<>(this, "wallFillMaterial", DEFAULT_MATERIAL);
    private final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial", DEFAULT_MATERIAL);

    public final IntegerProperty livesCounterPy = new SimpleIntegerProperty(0);

    private final GameContext context;

    private final Group root = new Group();
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Message3D message3D;
    private final Pac3D pac3D;
    private final List<MutableGhost3D> ghosts3D;
    private final Map<Vector2i, Pellet3D> pellets3D = new HashMap<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private House3D house3D;
    private final LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;

    public GameLevel3D(GameContext context) {
        this.context = checkNotNull(context);

        final GameVariant gameVariant = context.game().variant();
        final AssetStorage assets = context.assets();

        pac3D = Factory3D.createPac3D(gameVariant, assets, context.game().pac(), PAC_SIZE);
        ghosts3D = context.game().ghosts().map(ghost -> Factory3D.createMutableGhost3D(gameVariant, assets, ghost, GHOST_SIZE)).toList();
        livesCounter3D = Factory3D.createLivesCounter3D(gameVariant, assets, LIVES_COUNTER_MAX, LIVE_SHAPE_SIZE, context.game().hasCredit());
        livesCounter3D.livesCountPy.bind(livesCounterPy);
        message3D = Factory3D.createMessage3D(assets);

        wallFillMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(opaqueColor(wallFillColorPy.get(), wallOpacityPy.get())), wallFillColorPy, wallOpacityPy));

        wallStrokeMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(wallStrokeColorPy.get()), wallStrokeColorPy));

        buildWorld3D(context.game().world());
        addFood3D(context.game().world());

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        root.getChildren().addAll(pac3D.shape3D(), pac3D.shape3D().light());
        ghosts3D.forEach(ghost3D -> root.getChildren().add(ghost3D.root()));
        root.getChildren().addAll(message3D, livesCounter3D, worldGroup);

        PY_3D_WALL_HEIGHT.addListener((py,ov,nv) -> obstacleHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    /**
     * Updates level from game state.
     */
    public void update() {
        pac3D.update(context);
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));

        boolean houseAccessRequired = context.game()
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);

        boolean ghostNearHouseEntry = context.game()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(context.game().world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);

        houseUsedPy.set(houseAccessRequired);
        houseOpenPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, context.game().lives() - 1);
        if (!context.game().pac().isVisible() && context.gameState() == GameState.READY) {
            livesCounterPy.set(symbolsDisplayed + 1);
        } else {
            livesCounterPy.set(symbolsDisplayed);
        }
    }

    private void buildWorld3D(GameWorld world) {
        //TODO check this
        obstacleHeightPy.set(PY_3D_WALL_HEIGHT.get());

        TileMap terrain = world.map().terrain();
        wallStrokeColorPy.set(getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_STROKE, Color.rgb(33, 33, 255)));
        wallFillColorPy.set(getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_FILL, Color.rgb(0, 0, 0)));

        Box floor = createFloor(terrain.numCols() * TS - 1, terrain.numRows() * TS - 1);

        terrain.doubleStrokePaths()
            .filter(path -> !context.game().world().isPartOfHouse(path.startTile()))
            .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path,
                borderWallHeightPy, BORDER_WALL_THICKNESS, OBSTACLE_COAT_HEIGHT,
                wallFillMaterialPy, wallStrokeMaterialPy));

        terrain.singleStrokePaths()
            .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path,
                obstacleHeightPy, OBSTACLE_THICKNESS, OBSTACLE_COAT_HEIGHT,
                wallFillMaterialPy, wallStrokeMaterialPy));

        house3D = new House3D(world);
        house3D.heightPy.bind(houseHeightPy);
        house3D.fillMaterialPy.bind(wallFillColorPy.map(fillColor -> opaqueColor(fillColor, HOUSE_OPACITY)).map(Ufx::coloredMaterial));
        house3D.strokeMaterialPy.bind(wallStrokeMaterialPy);
        house3D.usedPy.bind(houseUsedPy);
        house3D.openPy.bind(houseOpenPy);

        mazeGroup.getChildren().add(house3D.root());
        worldGroup.getChildren().addAll(floor, mazeGroup);
        root.getChildren().add(house3D.door3D());
    }

    private Box createFloor(double sizeX, double sizeY) {
        var floor = new Box(sizeX, sizeY, FLOOR_THICKNESS);
        // Place floor such that left-upper corner is at origin and floor surface is at z=0
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().bind(floor.depthProperty().multiply(0.5));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor.materialProperty().bind(Bindings.createObjectBinding(
            () -> createFloorMaterial(floorColorPy.get(), floorTextureNamePy.get(), context.assets().get("floorTextures")),
            floorColorPy, floorTextureNamePy
        ));
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);
        return floor;
    }

    private PhongMaterial createFloorMaterial(Color color, String textureName, Map<String, PhongMaterial> textures) {
        return NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
            ? coloredMaterial(color)
            : textures.get(textureName);
    }

    private void addFood3D(GameWorld world) {
        TileMap foodMap = world.map().food();
        Color foodColor = getColorFromMap(foodMap, GameWorld.PROPERTY_COLOR_FOOD, Color.WHITE);
        Material foodMaterial = coloredMaterial(foodColor);
        Model3D pelletModel3D = context.assets().get("model3D.pellet");
        foodMap.tiles().filter(world::hasFoodAt).forEach(tile -> {
            Point3D position = new Point3D(tile.x() * TS + HTS, tile.y() * TS + HTS, -6);
            if (world.isEnergizerPosition(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                energizer3D.shape3D().setMaterial(foodMaterial);
                energizer3D.setTile(tile);
                energizer3D.setPosition(position);
                var squirting = new Squirting(root, Duration.seconds(2));
                squirting.setDropReachesFinalPosition(drop ->
                    drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY()));
                squirting.createDrops(15, 46, foodMaterial, position);
                energizer3D.setEatenAnimation(squirting);
                root.getChildren().add(energizer3D.shape3D());
                energizers3D.add(energizer3D);
            } else {
                var pellet3D = new Pellet3D(pelletModel3D, PELLET_RADIUS);
                pellet3D.shape3D().setMaterial(foodMaterial);
                pellet3D.setTile(tile);
                pellet3D.setPosition(position);
                root.getChildren().add(pellet3D.shape3D());
                pellets3D.put(tile, pellet3D);
            }
        });
        energizers3D.trimToSize();
    }

    public void showAnimatedMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        double halfHeight = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(halfHeight); // just under floor
        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(halfHeight + 0.5 * obstacleHeightPy.get()));
        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveDownAnimation.setDelay(Duration.seconds(displaySeconds));
        moveDownAnimation.setToZ(halfHeight);
        moveDownAnimation.setOnFinished(e -> message3D.setVisible(false));
        new SequentialTransition(moveUpAnimation, moveDownAnimation).play();
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            worldGroup.getChildren().remove(bonus3D);
        }
        bonus3D = new Bonus3D(bonus,
            context.spriteSheet().subImage(context.spriteSheet().bonusSymbolSprite(bonus.symbol())),
            context.spriteSheet().subImage(context.spriteSheet().bonusValueSprite(bonus.symbol())));
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
    }

    public RotateTransition levelRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), root);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Animation wallsDisappearAnimation(double seconds) {
        var totalDuration = Duration.seconds(seconds);
        var obstaclesDisappear = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(obstacleHeightPy, 0, Interpolator.EASE_IN)
            ));
        var houseDisappears = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(houseHeightPy, 0, Interpolator.EASE_IN)
            ));
        var borderWallsDisappear = new Timeline(
            new KeyFrame(totalDuration.multiply(0.33),
                new KeyValue(borderWallHeightPy, 0, Interpolator.EASE_IN)
            )
        );
        var animation  = new SequentialTransition(houseDisappears, obstaclesDisappear, borderWallsDisappear);
        animation.setOnFinished(e -> mazeGroup.setVisible(false));
        return animation;
    }

    public Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
            new KeyFrame(Duration.millis(125), new KeyValue(obstacleHeightPy, 0, Interpolator.EASE_BOTH))
        );
        animation.setAutoReverse(true);
        animation.setCycleCount(2*numFlashes);
        return animation;
    }

    public Group root() { return root; }

    public Pac3D pac3D() { return pac3D; }

    public List<MutableGhost3D> ghosts3D() { return ghosts3D; }

    public MutableGhost3D ghost3D(byte id) { return ghosts3D.get(id); }

    public Optional<Bonus3D> bonus3D() { return Optional.ofNullable(bonus3D); }

    public LivesCounter3D livesCounter3D() { return livesCounter3D; }

    public House3D house3D() { return house3D; }

    public Stream<Pellet3D> pellets3D() { return pellets3D.values().stream(); }

    public Stream<Energizer3D> energizers3D() { return energizers3D.stream(); }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        checkTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    public Optional<Pellet3D> pellet3D(Vector2i tile) {
        checkTileNotNull(tile);
        return Optional.ofNullable(pellets3D.get(tile));
    }
}
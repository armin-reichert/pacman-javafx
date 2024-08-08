/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.mapeditor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.GameParameters3D.*;

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

    private final StringProperty  floorTextureNamePy  = new SimpleStringProperty(this, "floorTextureName", NO_TEXTURE);
    private final DoubleProperty  borderWallHeightPy  = new SimpleDoubleProperty(this, "borderWallHeight", BORDER_WALL_HEIGHT);
    private final DoubleProperty  obstacleHeightPy    = new SimpleDoubleProperty(this, "obstacleHeight", OBSTACLE_HEIGHT);
    private final DoubleProperty  wallOpacityPy       = new SimpleDoubleProperty(this, "wallOpacity",1.0);

    private final DoubleProperty  houseHeightPy       = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);
    private final BooleanProperty houseUsedPy         = new SimpleBooleanProperty(this, "houseUsed", false);
    private final BooleanProperty houseOpenPy         = new SimpleBooleanProperty(this, "houseOpen", false);

    private final ObjectProperty<Color> floorColorPy      = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
    private final ObjectProperty<Color> wallFillColorPy   = new SimpleObjectProperty<>(this, "wallFillColor", Color.BLUE);
    private final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(this, "wallStrokeColor", Color.LIGHTBLUE);

    private final ObjectProperty<PhongMaterial> wallFillMaterialPy   = new SimpleObjectProperty<>(this, "wallFillMaterial", DEFAULT_MATERIAL);
    private final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial", DEFAULT_MATERIAL);

    private final GameContext context;

    private final Group root = new Group();
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Pac3D pac3D;
    private final List<MutableGhost3D> ghosts3D;
    private final Map<Vector2i, Pellet3D> pellets3D = new HashMap<>();
    private final ArrayList<Energizer3D> energizers3D = new ArrayList<>();
    private House3D house3D;
    private final LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;
    private Message3D message3D;

    public GameLevel3D(GameContext context) {
        this.context = checkNotNull(context);

        GameModel game  = context.game();
        GameWorld world = game.world();
        AssetMap assets = context.assets();

        pac3D = game.variant() == GameVariant.MS_PACMAN
            ? new MsPacMan3D(context, game.pac(), PAC_SIZE, assets.get("model3D.pacman"))
            : new PacMan3D(context, game.pac(), PAC_SIZE, assets.get("model3D.pacman"));
        pac3D.drawModeProperty().bind(PY_3D_DRAW_MODE);

        ghosts3D = game.ghosts().map(ghost -> new MutableGhost3D(assets.get("model3D.ghost"), assets, ghost, GHOST_SIZE)).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));

        livesCounter3D = createLivesCounter3D();
        updateLivesCounter();

        createMessage3D();

        wallFillMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(opaqueColor(wallFillColorPy.get(), wallOpacityPy.get())), wallFillColorPy, wallOpacityPy));

        wallStrokeMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(wallStrokeColorPy.get()), wallStrokeColorPy));

        buildWorld3D(world);
        addFood3D(world);

        Color lightColor = context.game().variant() == GameVariant.MS_PACMAN
                ? context.assets().color("ms_pacman.color.head").desaturate()
                : context.assets().color("pacman.color.head").desaturate();
        PointLight pacLight = createPacLight(pac3D, lightColor);

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        root.getChildren().addAll(pac3D.root(), pacLight);
        ghosts3D.forEach(ghost3D -> root.getChildren().add(ghost3D.root()));
        root.getChildren().addAll(message3D, livesCounter3D, worldGroup);
    }

    /**
     * Updates level from game state.
     */
    public void update() {
        var game = context.game();
        pac3D.update();
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));

        boolean ghostNeedsHouseAccess = game.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean ghostNearHouseEntry = game.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(game.world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);
        houseUsedPy.set(ghostNeedsHouseAccess);
        houseOpenPy.set(ghostNearHouseEntry);

        updateLivesCounter();
    }

    private void updateLivesCounter() {
        //TODO reconsider this:
        if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
            livesCounter3D.livesCountPy.set(context.game().lives());
        } else {
            livesCounter3D.livesCountPy.set(context.game().lives() - 1);
        }
    }

    private void buildWorld3D(GameWorld world) {
        TileMap terrain = world.map().terrain();
        terrain.computeTerrainPaths();

        PY_3D_WALL_HEIGHT.addListener((py,ov,nv) -> obstacleHeightPy.set(nv.doubleValue()));
        obstacleHeightPy.set(PY_3D_WALL_HEIGHT.get());

        wallOpacityPy.bind(PY_3D_WALL_OPACITY);

        wallStrokeColorPy.set(getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_STROKE, Color.rgb(33, 33, 255)));
        wallFillColorPy.set(getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_FILL, Color.rgb(0, 0, 0)));

        Box floor = createFloor(terrain);

        terrain.doubleStrokePaths()
            .filter(path -> !context.game().world().isPartOfHouse(path.startTile()))
            .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path, borderWallHeightPy, BORDER_WALL_THICKNESS, OBSTACLE_COAT_HEIGHT,
                wallFillMaterialPy, wallStrokeMaterialPy));

        terrain.singleStrokePaths()
            .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path, obstacleHeightPy, OBSTACLE_THICKNESS, OBSTACLE_COAT_HEIGHT,
                wallFillMaterialPy, wallStrokeMaterialPy));

        house3D = new House3D(world);
        house3D.heightPy.bind(houseHeightPy);
        house3D.fillMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(opaqueColor(wallFillColorPy.get(), HOUSE_OPACITY)), wallFillColorPy
        ));
        house3D.strokeMaterialPy.bind(wallStrokeMaterialPy);
        house3D.usedPy.bind(houseUsedPy);
        house3D.openPy.bind(houseOpenPy);

        mazeGroup.getChildren().add(house3D.root());
        worldGroup.getChildren().addAll(floor, mazeGroup);
        //TODO check this, get transparency right
        root.getChildren().add(house3D.door3D());
    }

    private PointLight createPacLight(Pac3D pac3D, Color color) {
        var light = new PointLight();
        light.setColor(color);
        light.lightOnProperty().bind(pac3D.lightOnProperty());
        light.maxRangeProperty().bind(pac3D.lightRangeProperty());
        light.translateXProperty().bind(pac3D.root().translateXProperty());
        light.translateYProperty().bind(pac3D.root().translateYProperty());
        light.translateZProperty().bind(pac3D.root().translateZProperty().subtract(PAC_SIZE));
        return light;
    }

    private Box createFloor(TileMap terrain) {
        double sizeX = terrain.numCols() * TS - 1, sizeY = terrain.numRows() * TS - 1;
        var floor = new Box(sizeX, sizeY, FLOOR_THICKNESS);
        // Place floor such that left-upper corner is at origin and floor surface is at z=0
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().bind(floor.depthProperty().multiply(0.5));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor.materialProperty().bind(Bindings.createObjectBinding(
            () -> {
                Color floorColor = floorColorPy.get();
                String textureName = floorTextureNamePy.get();
                Map<String, PhongMaterial> textures = context.assets().get("floorTextures");
                return NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
                    ? coloredMaterial(floorColor)
                    : textures.get(textureName);
            }, floorColorPy, floorTextureNamePy
        ));
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);
        return floor;
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

    private Node createLivesCounterShape(GameVariant variant) {
        AssetMap assets = context.assets();
        return switch (variant) {
            case MS_PACMAN -> new Group(
                PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), LIVE_SHAPE_SIZE,
                    assets.color("ms_pacman.color.head"),
                    assets.color("ms_pacman.color.eyes"),
                    assets.color("ms_pacman.color.palate")),
                PacModel3D.createFemaleParts(LIVE_SHAPE_SIZE,
                    assets.color("ms_pacman.color.hairbow"),
                    assets.color("ms_pacman.color.hairbow.pearls"),
                    assets.color("ms_pacman.color.boobs"))
            );
            case PACMAN, PACMAN_XXL ->
                 PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), LIVE_SHAPE_SIZE,
                    assets.color("pacman.color.head"),
                    assets.color("pacman.color.eyes"),
                    assets.color("pacman.color.palate")
                );
        };
    }

    private LivesCounter3D createLivesCounter3D() {
        Node[] shapes = new Node[LIVES_COUNTER_MAX];
        for (int i = 0; i < shapes.length; ++i) {
            shapes[i] = createLivesCounterShape(context.game().variant());
        }
        var counter3D = new LivesCounter3D(shapes, 10);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(context.gameController().hasCredit());
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(context.gameController().hasCredit());
        return counter3D;
    }

    public void addLevelCounter3D(List<Byte> symbols) {
        TileMap terrain = context.game().world().map().terrain();
        double spacing = 2 * TS;
        // this is the *right* edge of the level counter:
        double x = terrain.numCols() * TS - spacing;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(spacing);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : symbols) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            switch (context.game().variant()) {
                case MS_PACMAN -> {
                    var ss = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                    material.setDiffuseMap(ss.subImage(ss.bonusSymbolSprite(symbol)));
                }
                case PACMAN, PACMAN_XXL -> {
                    var ss = (PacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                    material.setDiffuseMap(ss.subImage(ss.bonusSymbolSprite(symbol)));
                }
            }
            cube.setMaterial(material);

            var spinning = new RotateTransition(Duration.seconds(6), cube);
            spinning.setAxis(Rotate.X_AXIS);
            spinning.setCycleCount(Animation.INDEFINITE);
            spinning.setByAngle(360);
            spinning.setRate(n % 2 == 0 ? 1 : -1);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.play();

            n += 1;
        }
        root.getChildren().add(levelCounter3D);
    }

    private void createMessage3D() {
        message3D = new Message3D("", context.assets().font("font.arcade", 6), Color.YELLOW, Color.WHITE);
        message3D.setRotation(Rotate.X_AXIS, 90);
        message3D.setVisible(false);
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
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                var ss = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) context.spriteSheet(context.game().variant());
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
        }
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
        var duration = Duration.seconds(seconds);
        var phase1 = new Timeline(
            new KeyFrame(duration.multiply(0.33),
                new KeyValue(obstacleHeightPy, 0, Interpolator.EASE_IN)
            ));
        var phase2 = new Timeline(
            new KeyFrame(duration.multiply(0.33),
                new KeyValue(houseHeightPy, 0, Interpolator.EASE_IN)
            ));
        var phase3 = new Timeline(
            new KeyFrame(duration.multiply(0.33),
                new KeyValue(borderWallHeightPy, 0, Interpolator.EASE_IN)
            )
        );
        var animation  = new SequentialTransition(phase1, phase2, phase3);
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
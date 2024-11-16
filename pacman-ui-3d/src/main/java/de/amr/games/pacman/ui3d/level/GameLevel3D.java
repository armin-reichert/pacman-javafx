/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManTengenGameMapConfig;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.GameAssets3D;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    static final int   LIVES_COUNTER_MAX     = 5;
    static final float LIVES_SHAPE_SIZE      = 10.0f;
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

    static Pac3D createPac3D(GameVariant variant, AssetStorage assets, GameSound sounds, Pac pac) {
        String prefix = assetPrefix(variant) + ".";
        Pac3D pac3D = switch (variant) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new MsPacMan3D(variant, pac, PAC_SIZE, assets, sounds);
            case PACMAN, PACMAN_XXL          -> new PacMan3D(variant, pac, PAC_SIZE, assets, sounds);
        };
        pac3D.shape3D().light().setColor(assets.color(prefix + "pac.color.head").desaturate());
        pac3D.shape3D().drawModeProperty().bind(PY_3D_DRAW_MODE);
        return pac3D;
    }

    static MutableGhost3D createMutableGhost3D(AssetStorage assets, String assetPrefix, Ghost ghost, int numFlashes) {
        Shape3D dressShape    = new MeshView(assets.get("model3D.ghost.mesh.dress"));
        Shape3D pupilsShape   = new MeshView(assets.get("model3D.ghost.mesh.pupils"));
        Shape3D eyeballsShape = new MeshView(assets.get("model3D.ghost.mesh.eyeballs"));
        return new MutableGhost3D(dressShape, pupilsShape, eyeballsShape, assets, assetPrefix, ghost, GHOST_SIZE, numFlashes);
    }

    static LivesCounter3D createLivesCounter3D(GameVariant variant, AssetStorage assets,  boolean canStartNewGame) {
        Node[] shapes = IntStream.range(0, LIVES_COUNTER_MAX).mapToObj(i -> createLivesCounterShape(variant, assets)).toArray(Node[]::new);
        var counter3D = new LivesCounter3D(shapes, 10);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(canStartNewGame);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(canStartNewGame);
        return counter3D;
    }

    static Node createLivesCounterShape(GameVariant variant, AssetStorage assets) {
        String assetPrefix = assetPrefix(variant) + ".";
        return switch (variant) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new Group(
                PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), LIVES_SHAPE_SIZE,
                    assets.color(assetPrefix + "pac.color.head"),
                    assets.color(assetPrefix + "pac.color.eyes"),
                    assets.color(assetPrefix + "pac.color.palate")
                ),
                PacModel3D.createFemaleParts(LIVES_SHAPE_SIZE,
                    assets.color(assetPrefix + "pac.color.hairbow"),
                    assets.color(assetPrefix + "pac.color.hairbow.pearls"),
                    assets.color(assetPrefix + "pac.color.boobs")
                )
            );
            case PACMAN, PACMAN_XXL ->
                PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), LIVES_SHAPE_SIZE,
                    assets.color(assetPrefix + "pac.color.head"),
                    assets.color(assetPrefix + "pac.color.eyes"),
                    assets.color(assetPrefix + "pac.color.palate")
                );
        };
    }

    private static Node createLevelCounter3D(GameSpriteSheet spriteSheet, List<Byte> symbols, double x, double y) {
        double spacing = 2 * TS;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : symbols) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(spriteSheet.subImage(spriteSheet.bonusSymbolSprite(symbol)));
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
        return levelCounter3D;
    }

    static Message3D createMessage3D(AssetStorage assets) {
        var message3D = new Message3D("", assets.font("font.arcade", 6), Color.YELLOW, Color.WHITE);
        message3D.setRotation(Rotate.X_AXIS, 90);
        message3D.setVisible(false);
        return message3D;
    }

    private final StringProperty floorTextureNamePy = new SimpleStringProperty(this, "floorTextureName", GameAssets3D.NO_TEXTURE);
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
    private final LivesCounter3D livesCounter3D;
    private House3D house3D;
    private Bonus3D bonus3D;

    public GameLevel3D(GameContext context) {
        this.context = context;
        final GameVariant variant = context.gameVariant();
        final GameModel game = context.game();
        final GameLevel level = game.level().orElseThrow();
        final GameWorld world = level.world();
        final AssetStorage assets = context.assets();

        pac3D = createPac3D(variant, assets, context.sound(), level.pac());
        ghosts3D = level.ghosts().map(ghost -> createMutableGhost3D(assets, assetPrefix(variant), ghost,
                game.numFlashes())).toList();
        livesCounter3D = createLivesCounter3D(variant, assets, game.canStartNewGame());
        livesCounter3D.livesCountPy.bind(livesCounterPy);
        message3D = createMessage3D(assets);

        wallFillMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(opaqueColor(wallFillColorPy.get(), wallOpacityPy.get())),
            wallFillColorPy, wallOpacityPy));

        wallStrokeMaterialPy.bind(wallStrokeColorPy.map(Ufx::coloredMaterial));

        Map<String, String> colorMap = buildColorMap(level.mapConfig());
        buildWorld3D(world, assets, colorMap);
        addFood3D(world, assets, colorMap);

        // Walls and house must be added after the guys! Otherwise, transparency is not working correctly.
        root.getChildren().addAll(pac3D.shape3D(), pac3D.shape3D().light());
        ghosts3D.forEach(ghost3D -> root.getChildren().add(ghost3D.root()));
        root.getChildren().addAll(message3D, livesCounter3D, worldGroup);

        PY_3D_WALL_HEIGHT.addListener((py,ov,nv) -> obstacleHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    //TODO this should be done elsewhere
    @SuppressWarnings("unchecked")
    private Map<String, String> buildColorMap(Map<String, Object> mapConfig) {
        return switch (context.gameVariant()) {
            case PACMAN, PACMAN_XXL -> (Map<String, String>) mapConfig.get("colorMap");
            case MS_PACMAN -> MsPacManArcadeGame.COLOR_MAPS.get((int) mapConfig.get("colorSchemeIndex"));
            case MS_PACMAN_TENGEN -> MsPacManTengenGameMapConfig.COLOR_MAPS_OF_NES_COLOR_SCHEMES.get((NES_ColorScheme) mapConfig.get("nesColorScheme"));
        };
    }

    public void addLevelCounter() {
        // Place level counter at top right maze corner
        double x = context.level().world().map().terrain().numCols() * TS - 2 * TS;
        double y = 2 * TS;
        Node levelCounter3D = createLevelCounter3D(
            context.currentGameSceneConfig().spriteSheet(),
            context.game().levelCounter(), x, y);
        root.getChildren().add(levelCounter3D);
    }

    /**
     * Updates level from game state.
     */
    public void update(GameContext context) {
        pac3D.update(context);
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));

        boolean houseAccessRequired = context.level()
            .ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);

        boolean ghostNearHouseEntry = context.level()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(context.level().world().houseEntryPosition()) <= HOUSE_SENSITIVITY)
            .anyMatch(Ghost::isVisible);

        houseUsedPy.set(houseAccessRequired);
        houseOpenPy.set(ghostNearHouseEntry);

        int symbolsDisplayed = Math.max(0, context.game().lives() - 1);
        if (!context.level().pac().isVisible() && context.gameState() == GameState.STARTING_GAME) {
            livesCounterPy.set(symbolsDisplayed + 1);
        } else {
            livesCounterPy.set(symbolsDisplayed);
        }
    }

    private void buildWorld3D(GameWorld world, AssetStorage assets, Map<String, String> mapColorScheme) {
        //TODO check this
        obstacleHeightPy.set(PY_3D_WALL_HEIGHT.get());

        wallStrokeColorPy.set(Color.web(mapColorScheme.get("stroke")));
        wallFillColorPy.set(Color.web(mapColorScheme.get("fill")));

        TileMap terrain = world.map().terrain();
        Box floor = createFloor(assets.get("floor_textures"), terrain.numCols() * TS - 1, terrain.numRows() * TS - 1);

        terrain.terrainData().ifPresent(terrainData -> {
            terrainData.doubleStrokePaths()
                .filter(path -> !world.isPartOfHouse(path.startTile()))
                .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path,
                    borderWallHeightPy, BORDER_WALL_THICKNESS, OBSTACLE_COAT_HEIGHT,
                    wallFillMaterialPy, wallStrokeMaterialPy));

            terrainData.singleStrokePaths()
                .forEach(path -> WallBuilder.buildWallAlongPath(mazeGroup, path,
                    obstacleHeightPy, OBSTACLE_THICKNESS, OBSTACLE_COAT_HEIGHT,
                    wallFillMaterialPy, wallStrokeMaterialPy));
        });

        house3D = new House3D(world, mapColorScheme);
        house3D.heightPy.bind(houseHeightPy);
        house3D.fillMaterialPy.bind(wallFillColorPy.map(fillColor -> opaqueColor(fillColor, HOUSE_OPACITY)).map(Ufx::coloredMaterial));
        house3D.strokeMaterialPy.bind(wallStrokeMaterialPy);
        house3D.usedPy.bind(houseUsedPy);
        house3D.openPy.bind(houseOpenPy);

        mazeGroup.getChildren().add(house3D.root());
        worldGroup.getChildren().addAll(floor, mazeGroup);
        root.getChildren().add(house3D.door3D());
    }

    private Box createFloor(Map<String, PhongMaterial> textures, double sizeX, double sizeY) {
        var floor = new Box(sizeX, sizeY, FLOOR_THICKNESS);
        // Place floor such that left-upper corner is at origin and floor surface is at z=0
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().bind(floor.depthProperty().multiply(0.5));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor.materialProperty().bind(Bindings.createObjectBinding(
            () -> createFloorMaterial(floorColorPy.get(), floorTextureNamePy.get(), textures),
            floorColorPy, floorTextureNamePy
        ));
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);
        return floor;
    }

    private PhongMaterial createFloorMaterial(Color color, String textureName, Map<String, PhongMaterial> textures) {
        return GameAssets3D.NO_TEXTURE.equals(textureName) || !textures.containsKey(textureName)
            ? coloredMaterial(color)
            : textures.get(textureName);
    }

    private void addFood3D(GameWorld world, AssetStorage assets, Map<String, String> mapColorScheme) {
        TileMap foodMap = world.map().food();
        Color foodColor = Color.web(mapColorScheme.get("pellet"));
        Material foodMaterial = coloredMaterial(foodColor);
        Model3D pelletModel3D = assets.get("model3D.pellet");
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

    public void replaceBonus3D(Bonus bonus, GameSpriteSheet spriteSheet) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            worldGroup.getChildren().remove(bonus3D);
        }
        bonus3D = new Bonus3D(bonus,
            spriteSheet.subImage(spriteSheet.bonusSymbolSprite(bonus.symbol())),
            spriteSheet.subImage(spriteSheet.bonusValueSprite(bonus.symbol())));
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
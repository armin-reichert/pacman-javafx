/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.TileMapPath;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.Theme;
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
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;
import static java.lang.Math.PI;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    static final int   MAX_LIVES             = 5;
    static final float LIVE_SHAPE_SIZE       = 10;
    static final float FLOOR_THICKNESS       = 0.4f;
    static final float OBSTACLE_HEIGHT       = 5.5f;
    static final float OBSTACLE_THICKNESS    = 0.5f;
    static final float BORDER_WALL_HEIGHT    = 8.0f;
    static final float BORDER_WALL_THICKNESS = 2.5f;
    static final float WALL_COAT_HEIGHT      = 0.1f;
    static final float HOUSE_HEIGHT          = 12.0f;
    static final float HOUSE_WALL_THICKNESS  = 1.5f;
    static final float HOUSE_OPACITY         = 0.4f;
    static final float HOUSE_SENSITIVITY     = 1.5f * TS;
    static final float PAC_SIZE              = 14.5f;
    static final float GHOST_SIZE            = 14;
    static final float ENERGIZER_RADIUS      = 3.5f;
    static final float PELLET_RADIUS         = 1.0f;

    public final StringProperty                floorTextureNamePy   = new SimpleStringProperty(this, "floorTextureName", NO_TEXTURE);
    public final ObjectProperty<Color>         floorColorPy         = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
    public final DoubleProperty                borderWallHeightPy   = new SimpleDoubleProperty(this, "borderWallHeight", BORDER_WALL_HEIGHT);
    public final DoubleProperty                obstacleHeightPy     = new SimpleDoubleProperty(this, "obstacleHeight", OBSTACLE_HEIGHT);
    public final DoubleProperty                houseHeightPy        = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);
    public final BooleanProperty               houseUsedPy          = new SimpleBooleanProperty(this, "houseUsed", false);
    public final BooleanProperty               houseOpenPy          = new SimpleBooleanProperty(this, "houseOpen", false);
    public final DoubleProperty                wallOpacityPy        = new SimpleDoubleProperty(this, "wallOpacity",1.0);

    private final GameContext context;

    private final Group root = new Group();
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Pac3D pac3D;
    private final List<MutableGhost3D> ghosts3D;
    private final Set<Pellet3D> pellets3D = new HashSet<>();
    private final Set<Energizer3D> energizers3D = new HashSet<>();
    private Door3D door3D;
    private final LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;
    private Message3D message3D;

    public GameLevel3D(GameContext context) {
        this.context = checkNotNull(context);

        GameModel game  = context.game();
        GameWorld world = game.world();
        Theme theme     = context.theme();

        pac3D = game.variant() == GameVariant.MS_PACMAN
            ? new MsPacMan3D(context, game.pac(), PAC_SIZE, theme.get("model3D.pacman"))
            : new PacMan3D(context, game.pac(), PAC_SIZE, theme.get("model3D.pacman"));
        pac3D.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Model3D ghostModel3D = theme.get("model3D.ghost");
        ghosts3D = game.ghosts().map(ghost -> new MutableGhost3D(ghostModel3D, theme, ghost, GHOST_SIZE)).toList();
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));

        livesCounter3D = createLivesCounter();
        updateLivesCounter();
        createMessage3D();
        buildWorld3D(world);
        addFood(world);

        // Walls must be added after the guys! Otherwise, transparency is not working correctly.
        root.getChildren().addAll(pac3D.node(), createPacLight(pac3D));
        root.getChildren().addAll(ghosts3D);
        root.getChildren().addAll(message3D, livesCounter3D, worldGroup);
    }

    public void update() {
        var game = context.game();
        if (game.pac().isAlive()) {
            pac3D.updateAlive();
        }
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));
        houseUsedPy.set(
            game.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .anyMatch(Ghost::isVisible));
        Vector2f houseEntryPosition = game.world().houseEntryPosition();
        houseOpenPy.set(
            game.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .filter(ghost -> ghost.position().euclideanDistance(houseEntryPosition) <= HOUSE_SENSITIVITY)
                .anyMatch(Ghost::isVisible));

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
        obstacleHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);

        Color wallStrokeColor = getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_STROKE, Color.rgb(33, 33, 255));
        PhongMaterial wallStrokeMaterial = coloredMaterial(wallStrokeColor);
        Color wallFillColor = getColorFromMap(terrain, GameWorld.PROPERTY_COLOR_WALL_FILL, Color.rgb(0, 0, 0));
        PhongMaterial wallFillMaterial = new PhongMaterial(opaqueColor(wallFillColor, wallOpacityPy.get()));
        wallFillMaterial.setSpecularColor(wallFillMaterial.getDiffuseColor().brighter());

        Box floor = createFloor(terrain.numCols() * TS - 1, terrain.numRows() * TS - 1);
        worldGroup.getChildren().add(floor);

        worldGroup.getChildren().add(mazeGroup);
        terrain.computeTerrainPaths();
        terrain.doubleStrokePaths()
            .filter(path -> !context.game().world().isPartOfHouse(path.startTile()))
            .forEach(path -> buildWallAlongPath(mazeGroup, path, borderWallHeightPy, BORDER_WALL_THICKNESS, wallFillMaterial, wallStrokeMaterial));
        terrain.singleStrokePaths()
            .forEach(path -> buildWallAlongPath(mazeGroup, path, obstacleHeightPy, OBSTACLE_THICKNESS, wallFillMaterial, wallStrokeMaterial));

        PhongMaterial houseFillMaterial = coloredMaterial(opaqueColor(wallFillColor, HOUSE_OPACITY));
        addHouse(world, houseFillMaterial, wallStrokeMaterial);
    }

    private void addHouse(GameWorld world, PhongMaterial fillMaterial, PhongMaterial strokeMaterial) {
        WorldMap map = world.map();

        // tile coordinates
        int xMin = world.houseTopLeftTile().x();
        int xMax = xMin + world.houseSize().x() - 1;
        int yMin = world.houseTopLeftTile().y();
        int yMax = yMin + world.houseSize().y() - 1;

        Vector2i leftDoorTile = world.houseLeftDoorTile(), rightDoorTile = world.houseRightDoorTile();
        mazeGroup.getChildren().addAll(
            createHouseWall(xMin, yMin, leftDoorTile.x() - 1, yMin, fillMaterial, strokeMaterial),
            createHouseWall(rightDoorTile.x() + 1, yMin, xMax, yMin, fillMaterial, strokeMaterial),
            createHouseWall(xMin, yMin, xMin, yMax, fillMaterial, strokeMaterial),
            createHouseWall(xMax, yMin, xMax, yMax, fillMaterial, strokeMaterial),
            createHouseWall(xMin, yMax, xMax, yMax, fillMaterial, strokeMaterial)
        );

        Color doorColor = getColorFromMap(map.terrain(), GameWorld.PROPERTY_COLOR_DOOR, Color.rgb(254,184,174));
        door3D = new Door3D(leftDoorTile, rightDoorTile, doorColor, PY_3D_FLOOR_COLOR);
        door3D.drawModePy.bind(PY_3D_DRAW_MODE);

        // TODO: If door is added to given parent, it is not visible through transparent house wall in front.
        // TODO: If is added to the level 3D group, it shows the background wallpaper when its color is transparent! WTF?
        root.getChildren().add(door3D);

        // pixel coordinates
        float centerX = world.houseTopLeftTile().x() * TS + world.houseSize().x() * HTS;
        float centerY = world.houseTopLeftTile().y() * TS + world.houseSize().y() * HTS;

        var light = new PointLight();
        light.lightOnProperty().bind(houseUsedPy);
        light.setColor(Color.GHOSTWHITE);
        light.setMaxRange(3 * TS);
        light.setTranslateX(centerX);
        light.setTranslateY(centerY - 6);
        light.setTranslateZ(-HOUSE_HEIGHT);
        mazeGroup.getChildren().add(light);

        houseOpenPy.addListener((py, wasOpen, isOpen) -> {
            if (isOpen) {
                door3D.playTraversalAnimation();
            }
        });
    }

    private PointLight createPacLight(Pac3D pac3D) {
        Color lightColor = context.game().variant() == GameVariant.MS_PACMAN
            ? context.theme().color("ms_pacman.color.head").desaturate()
            : context.theme().color("pacman.color.head").desaturate();

        var light = new PointLight();
        light.setColor(lightColor);
        light.lightOnProperty().bind(pac3D.lightOnProperty());
        light.maxRangeProperty().bind(pac3D.lightRangeProperty());
        light.translateXProperty().bind(pac3D.node().translateXProperty());
        light.translateYProperty().bind(pac3D.node().translateYProperty());
        light.translateZProperty().bind(pac3D.node().translateZProperty().subtract(PAC_SIZE));
        return light;
    }

    private Box createFloor(double width, double height) {
        var floor = new Box(width, height, FLOOR_THICKNESS);
        // Place floor such that left-upper corner is at origin and floor surface is at z=0
        floor.translateXProperty().bind(floor.widthProperty().multiply(0.5));
        floor.translateYProperty().bind(floor.heightProperty().multiply(0.5));
        floor.translateZProperty().bind(floor.depthProperty().multiply(0.5));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor.materialProperty().bind(Bindings.createObjectBinding(
            () -> {
                Color floorColor = floorColorPy.get();
                String textureName = floorTextureNamePy.get();
                Map<String, PhongMaterial> floorTextures = context.theme().get("floorTextures");
                return NO_TEXTURE.equals(textureName)
                    ? coloredMaterial(floorColor)
                    : floorTextures.getOrDefault(textureName, coloredMaterial(floorColor));
            }, floorColorPy, floorTextureNamePy
        ));
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);
        return floor;
    }

    private Node createHouseWall(int x1, int y1, int x2, int y2, PhongMaterial fillMaterial, PhongMaterial strokeMaterial) {
        return createWall(v2i(x1, y1), v2i(x2, y2), HOUSE_WALL_THICKNESS, houseHeightPy, fillMaterial, strokeMaterial);
    }

    private void buildWallAlongPath(
        Group parent, TileMapPath path,
        DoubleProperty wallHeightPy, double thickness,
        PhongMaterial wallFillMaterial, PhongMaterial wallStrokeMaterial)
    {
        Vector2i startTile = path.startTile(), endTile = startTile;
        Direction prevDir = null;
        Node segment;
        for (Direction dir : path) {
            if (prevDir != dir) {
                segment = createWall(startTile, endTile, thickness, wallHeightPy, wallFillMaterial, wallStrokeMaterial);
                parent.getChildren().add(segment);
                startTile = endTile;
            }
            endTile = endTile.plus(dir.vector());
            prevDir = dir;
        }
        segment = createWall(startTile, endTile, thickness, wallHeightPy, wallFillMaterial, wallStrokeMaterial);
        parent.getChildren().add(segment);
    }

    private static Node createWall(
        Vector2i tile1, Vector2i tile2,
        double thickness, DoubleProperty wallHeightPy,
        PhongMaterial fillMaterial, PhongMaterial strokeMaterial)
    {
        if (tile1.y() == tile2.y()) { // horizontal wall
            Vector2i left  = tile1.x() < tile2.x() ? tile1 : tile2;
            Vector2i right = tile1.x() < tile2.x() ? tile2 : tile1;
            Vector2i origin = left.plus(right).scaled(HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return createWall(origin, length + thickness, thickness, wallHeightPy, fillMaterial, strokeMaterial);
        }
        else if (tile1.x() == tile2.x()) { // vertical wall
            Vector2i top    = tile1.y() < tile2.y() ? tile1 : tile2;
            Vector2i bottom = tile1.y() < tile2.y() ? tile2 : tile1;
            Vector2i origin = top.plus(bottom).scaled(HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return createWall(origin, thickness, length, wallHeightPy, fillMaterial, strokeMaterial);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", tile1, tile2));
    }

    private static Node createWall(
        Vector2i origin, double sizeX, double sizeY, DoubleProperty wallHeightPy,
        PhongMaterial fillMaterial, PhongMaterial strokeMaterial) {

        var base = new Box(sizeX, sizeY, wallHeightPy.get());
        base.setTranslateX(origin.x());
        base.setTranslateY(origin.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
        base.depthProperty().bind(wallHeightPy);
        base.materialProperty().set(fillMaterial);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, WALL_COAT_HEIGHT);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.multiply(-1).subtract(WALL_COAT_HEIGHT));
        top.materialProperty().set(strokeMaterial);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    private void addFood(GameWorld world) {
        TileMap foodMap = world.map().food();
        Color foodColor = getColorFromMap(foodMap, GameWorld.PROPERTY_COLOR_FOOD, Color.WHITE);
        Material foodMaterial = coloredMaterial(foodColor);
        Model3D pelletModel3D = context.theme().get("model3D.pellet");
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
                pellets3D.add(pellet3D);
            }
        });
    }

    private Node createLivesCounterShape(GameVariant variant) {
        Theme theme = context.theme();
        return switch (variant) {
            case MS_PACMAN -> new Group(
                PacModel3D.createPacShape(
                    theme.get("model3D.pacman"), LIVE_SHAPE_SIZE,
                    theme.color("ms_pacman.color.head"),
                    theme.color("ms_pacman.color.eyes"),
                    theme.color("ms_pacman.color.palate")),
                PacModel3D.createFemaleParts(LIVE_SHAPE_SIZE,
                    theme.color("ms_pacman.color.hairbow"),
                    theme.color("ms_pacman.color.hairbow.pearls"),
                    theme.color("ms_pacman.color.boobs"))
            );
            case PACMAN, PACMAN_XXL ->
                 PacModel3D.createPacShape(
                    theme.get("model3D.pacman"), LIVE_SHAPE_SIZE,
                    theme.color("pacman.color.head"),
                    theme.color("pacman.color.eyes"),
                    theme.color("pacman.color.palate")
                );
        };
    }

    private LivesCounter3D createLivesCounter() {
        Node[] shapes = new Node[MAX_LIVES];
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
        GameWorld world = context.game().world();
        double spacing = 2 * TS;
        // this is the *right* edge of the level counter:
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(world.map().terrain().numCols() * TS - spacing);
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
        message3D = new Message3D();
        message3D.setRotation(Rotate.X_AXIS, 90);
        message3D.setVisible(false);
        message3D.beginBatch();
        message3D.setBorderColor(Color.WHITE);
        message3D.setTextColor(Color.YELLOW);
        message3D.setFont(context.theme().font("font.arcade", 6));
        message3D.endBatch();
    }

    public void showAnimatedMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        double dist = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(dist); // under floor
        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(dist + 0.5 * obstacleHeightPy.get()));
        var moveDownAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveDownAnimation.setDelay(Duration.seconds(displaySeconds));
        moveDownAnimation.setToZ(dist);
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

    public RotateTransition createMazeRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), root);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Transition createWallsDisappearAnimation(double seconds) {
        return new Transition() {
            private final double initialWallHeight = obstacleHeightPy.get();
            private final double initialOuterWallHeight = borderWallHeightPy.get();
            {
                setCycleDuration(Duration.seconds(seconds));
                setInterpolator(Interpolator.LINEAR);
                setOnFinished(e -> {
                    mazeGroup.setVisible(false);
                    obstacleHeightPy.bind(PY_3D_WALL_HEIGHT);
                });
            }

            @Override
            protected void interpolate(double t) {
                obstacleHeightPy.unbind();
                obstacleHeightPy.set((1-t) * initialWallHeight);
                borderWallHeightPy.set((1-t) * initialOuterWallHeight);
            }
        };
    }

    public Transition createMazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        return new Transition() {
            private final double initialWallHeight = obstacleHeightPy.get();

            {
                setCycleDuration(Duration.seconds(0.33));
                setCycleCount(numFlashes);
                setInterpolator(Interpolator.LINEAR);
                setOnFinished(e -> obstacleHeightPy.bind(PY_3D_WALL_HEIGHT));
            }

            @Override
            protected void interpolate(double t) {
                // t = [0, 1] is mapped to the interval [pi/2, 3*pi/2]. The value of the sin-function in that interval
                // starts at 1, goes to 0 and back to 1
                double elongation = 0.5 * (1 + Math.sin(PI * (2*t + 0.5)));
                obstacleHeightPy.unbind(); // TODO when called in constructor does not work. Why?
                obstacleHeightPy.set(elongation * initialWallHeight);
            }
        };
    }

    public Group root() { return root; }

    public Pac3D pac3D() {
        return pac3D;
    }

    public List<MutableGhost3D> ghosts3D() {
        return ghosts3D;
    }

    public MutableGhost3D ghost3D(byte id) {
        return ghosts3D.get(id);
    }

    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public Door3D door3D() {
        return door3D;
    }

    public Stream<Pellet3D> pellets3D() {
        return pellets3D.stream();
    }

    public Stream<Energizer3D> energizers3D() {
        return energizers3D.stream();
    }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        checkTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    //TODO performance?
    public Optional<Pellet3D> pellet3D(Vector2i tile) {
        checkTileNotNull(tile);
        return pellets3D().filter(p3D -> p3D.tile().equals(tile)).findFirst();
    }
}
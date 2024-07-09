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
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui3d.animation.Squirting;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.mapeditor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;
import static java.lang.Math.PI;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    static final float FLOOR_THICKNESS       = 0.4f;
    static final float INNER_WALL_HEIGHT     = 4.5f;
    static final float INNER_WALL_THICKNESS  = 0.5f;
    static final float OUTER_WALL_HEIGHT     = 6.0f;
    static final float OUTER_WALL_THICKNESS  = 2.0f;
    static final float WALL_COAT_HEIGHT      = 0.1f;
    static final float HOUSE_HEIGHT          = 12.0f;
    static final float HOUSE_OPACITY         = 0.4f;
    static final float HOUSE_SENSITIVITY     = 1.5f * TS;
    static final float PAC_SIZE              = 13.5f;
    static final float GHOST_SIZE            = 13.0f;
    static final float ENERGIZER_RADIUS      = 3.5f;
    static final float PELLET_RADIUS         = 1.0f;

    public final StringProperty floorTextureNamePy  = new SimpleStringProperty(this, "floorTextureName", NO_TEXTURE);
    public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
    public final DoubleProperty outerWallHeightPy   = new SimpleDoubleProperty(this, "outerWallHeight", OUTER_WALL_HEIGHT);
    public final DoubleProperty wallHeightPy        = new SimpleDoubleProperty(this, "wallHeight", INNER_WALL_HEIGHT);
    public final DoubleProperty houseHeightPy       = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);
    public final BooleanProperty houseUsedPy        = new SimpleBooleanProperty(this, "houseUsed", false);
    public final BooleanProperty houseOpenPy        = new SimpleBooleanProperty(this, "houseOpen", false) {
        @Override
        protected void invalidated() {
            if (get()) {
                door3D.playTraversalAnimation();
            }
        }
    };
    public final ObjectProperty<PhongMaterial> houseFillMaterialPy  = new SimpleObjectProperty<>(this, "houseFillMaterial");
    public final ObjectProperty<PhongMaterial> wallFillMaterialPy   = new SimpleObjectProperty<>(this, "wallFillMaterial");
    public final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial");
    public final ObjectProperty<Color>         wallFillColorPy      = new SimpleObjectProperty<>(this, "wallFillColor", Color.GREEN);
    public final ObjectProperty<Color>         wallStrokeColorPy    = new SimpleObjectProperty<>(this, "wallStrokeColor", Color.WHITE);
    public final DoubleProperty                wallOpacityPy        = new SimpleDoubleProperty(this, "wallOpacity",1.0);
    public final ObjectProperty<Color>         foodColorPy          = new SimpleObjectProperty<>(this, "foodColor", Color.PINK);
    public final ObjectProperty<PhongMaterial> foodMaterialPy       = new SimpleObjectProperty<>(this, "foodMaterial");

    private final GameContext context;
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private final Set<Pellet3D> pellets3D = new HashSet<>();
    private final Set<Energizer3D> energizers3D = new HashSet<>();
    private Door3D door3D;
    private Group levelCounter3D;
    private LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;
    private Message3D message3D;

    public GameLevel3D(GameContext context) {
        this.context = checkNotNull(context);

        var floor = new Box();
        floor.setWidth(context.game().world().numCols() * TS - 1);
        floor.setHeight(context.game().world().numRows() * TS - 1);
        floor.setDepth(FLOOR_THICKNESS);
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

        // Maze
        WorldMap map = context.game().world().map();

        wallStrokeMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(wallStrokeColorPy.get()),
            wallStrokeColorPy
        ));

        wallFillMaterialPy.bind(Bindings.createObjectBinding(
            () -> {
                double opacity = wallOpacityPy.get();
                Color fillColor = wallFillColorPy.get();
                Color color = opaqueColor(fillColor, opacity);
                PhongMaterial fillMaterial = new PhongMaterial(color);
                fillMaterial.setSpecularColor(color.brighter());
                return fillMaterial;
            }, wallOpacityPy, wallFillColorPy
        ));

        houseFillMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(opaqueColor(wallFillColorPy.get(), HOUSE_OPACITY)),
                wallFillColorPy
        ));

        foodMaterialPy.bind(Bindings.createObjectBinding(
            () -> coloredMaterial(foodColorPy.get()),
            foodColorPy
        ));

        wallFillColorPy.set(getColorFromMap(map.terrain(), WorldMap.PROPERTY_COLOR_WALL_FILL,
            Color.rgb(0,0,0)));
        wallStrokeColorPy.set(getColorFromMap(map.terrain(), WorldMap.PROPERTY_COLOR_WALL_STROKE,
            Color.rgb(33, 33, 255)));
        foodColorPy.set(getColorFromMap(map.terrain(), WorldMap.PROPERTY_COLOR_FOOD,
            Color.PINK));

        pac3D = switch (context.game().variant()) {
            case MS_PACMAN          -> new MsPacMan3D(PAC_SIZE, context.game().pac(), context.theme());
            case PACMAN, PACMAN_XXL -> new PacMan3D(PAC_SIZE, context.game().pac(), context.theme());
        };

        Model3D ghostModel3D = context.theme().get("model3D.ghost");
        ghosts3D = context.game().ghosts()
            .map(ghost -> new Ghost3D(ghostModel3D, context.theme(), ghost, GHOST_SIZE)).toList();

        createLivesCounter3D();
        createLevelCounter3D();
        createMessage3D();

        addMaze(mazeGroup);

        addHouse(mazeGroup);
        addPellets(this); // when put inside maze group, transparency does not work!

        getChildren().addAll(pac3D, pac3D.light());
        getChildren().addAll(ghosts3D);
        // Walls must come after the guys! Otherwise, transparency is not working correctly.
        worldGroup.getChildren().addAll(floor, mazeGroup);
        getChildren().addAll(message3D, levelCounter3D, livesCounter3D, worldGroup);

        updateLivesCounter();

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    public void showLevelStartMessage() {
        World world = context.game().world();
        House house = world.house();
        if (context.gameState() == GameState.LEVEL_TEST) {
            double x = world.numCols() * HTS;
            double y = (world.numRows() - 2) * TS;
            showMessage("TEST LEVEL " + context.game().levelNumber(), 5,x, y);
        } else if (!context.game().isDemoLevel()) {
            double x = TS * (house.topLeftTile().x() + 0.5 * house.size().x());
            double y = TS * (house.topLeftTile().y() +       house.size().y());
            double seconds = context.game().isPlaying() ? 0.5 : 2.5;
            showMessage("READY!", seconds, x, y);
        }
    }

    public void getReadyToPlay() {
        stopHunting();
        pac3D.init(context);
        ghosts3D.forEach(ghost3D -> ghost3D.init(context));
        showLevelStartMessage();
    }

    public void startHunting() {
        livesCounter3D.startAnimation();
        startEnergizerAnimation();
    }

    public void stopHunting() {
        stopEnergizerAnimation();
        bonus3D().ifPresent(bonus3D -> bonus3D.setVisible(false));
        livesCounter3D().stopAnimation();
    }

    public void update() {
        var game = context.game();
        pac3D.update(context);
        ghosts3D().forEach(ghost3D -> ghost3D.update(context));
        bonus3D().ifPresent(bonus -> bonus.update(context));
        houseUsedPy.set(
            game.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
                .anyMatch(Ghost::isVisible));
        Vector2f houseEntryPosition = game.world().house().door().entryPosition();
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

    private void addHouse(Group parent) {
        World world = context.game().world();
        WorldMap map = world.map();
        House house = world.house();
        Door door = house.door();

        // tile coordinates
        int xMin = house.topLeftTile().x();
        int xMax = xMin + house.size().x() - 1;
        int yMin = house.topLeftTile().y();
        int yMax = yMin + house.size().y() - 1;

        Vector2i leftDoorTile = door.leftWing(), rightDoorTile = door.rightWing();
        parent.getChildren().addAll(
            createHouseWall(xMin, yMin, leftDoorTile.x() - 1, yMin),
            createHouseWall(rightDoorTile.x() + 1, yMin, xMax, yMin),
            createHouseWall(xMin, yMin, xMin, yMax),
            createHouseWall(xMax, yMin, xMax, yMax),
            createHouseWall(xMin, yMax, xMax, yMax)
        );

        Color doorColor = getColorFromMap(map.terrain(), WorldMap.PROPERTY_COLOR_DOOR, Color.rgb(254,184,174));
        door3D = new Door3D(leftDoorTile, rightDoorTile, doorColor, PY_3D_FLOOR_COLOR);
        door3D.drawModePy.bind(PY_3D_DRAW_MODE);

        // TODO: If door is added to given parent, it is not visible through transparent house wall in front.
        // TODO: If is added to the level 3D group, it shows the background wallpaper when its color is transparent! WTF?
        getChildren().add(door3D);

        // pixel coordinates
        float centerX = house.topLeftTile().x() * TS + house.size().x() * HTS;
        float centerY = house.topLeftTile().y() * TS + house.size().y() * HTS;

        var houseLight = new PointLight();
        houseLight.lightOnProperty().bind(houseUsedPy);
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(centerX);
        houseLight.setTranslateY(centerY - 6);
        houseLight.setTranslateZ(-HOUSE_HEIGHT);
        parent.getChildren().add(houseLight);
    }

    private Node createHouseWall(int x1, int y1, int x2, int y2) {
        return createWall(v2i(x1, y1), v2i(x2, y2), INNER_WALL_THICKNESS, houseHeightPy,
            houseFillMaterialPy, wallStrokeMaterialPy);
    }

    private void addPellets(Group parent) {
        var world = context.game().world();
        Color color = getColorFromMap(world.map().food(), WorldMap.PROPERTY_COLOR_FOOD, Color.WHITE);
        foodColorPy.set(color);
        Model3D pelletModel3D = context.theme().get("model3D.pellet");
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            if (world.isEnergizerTile(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                addEnergizerAnimation(world, energizer3D);
                energizers3D.add(energizer3D);
                energizer3D.root().materialProperty().bind(foodMaterialPy);
                energizer3D.placeAtTile(tile);
                parent.getChildren().add(energizer3D.root());
            } else {
                var pellet3D = new Pellet3D(pelletModel3D, PELLET_RADIUS);
                pellets3D.add(pellet3D);
                pellet3D.root().materialProperty().bind(foodMaterialPy);
                pellet3D.placeAtTile(tile);
                parent.getChildren().add(pellet3D.root());
            }
        });
    }

    private void addMaze(Group parent) {
        House house = context.game().world().house();
        TileMap terrainMap = context.game().world().map().terrain();
        terrainMap.computeTerrainPaths();
        terrainMap.outerPaths()
            .filter(path -> !house.contains(path.startTile()))
            .forEach(path -> addWallSegmentsAlongPath(parent, path, outerWallHeightPy, OUTER_WALL_THICKNESS));
        terrainMap.innerPaths()
            .forEach(path -> addWallSegmentsAlongPath(parent, path, wallHeightPy, INNER_WALL_THICKNESS));
    }

    private void addWallSegmentsAlongPath(Group parent, TileMapPath path, DoubleProperty heightPy, double thickness) {
        Vector2i startTile = path.startTile(), endTile = startTile;
        Direction prevDir = null;
        Node segment;
        for (Direction dir : path) {
            if (prevDir != dir) {
                segment = createWall(startTile, endTile, thickness, heightPy, wallFillMaterialPy, wallStrokeMaterialPy);
                parent.getChildren().add(segment);
                startTile = endTile;
            }
            endTile = endTile.plus(dir.vector());
            prevDir = dir;
        }
        segment = createWall(startTile, endTile, thickness, heightPy, wallFillMaterialPy, wallStrokeMaterialPy);
        parent.getChildren().add(segment);
    }

    private static Node createWall(
        Vector2i beginTile, Vector2i endTile,
        double thickness, DoubleProperty depthPy,
        ObjectProperty<PhongMaterial> fillMaterialPy, ObjectProperty<PhongMaterial> strokeMaterialPy)
    {
        if (beginTile.y() == endTile.y()) { // horizontal
            if (beginTile.x() > endTile.x()) {
                var tmp = beginTile;
                beginTile = endTile;
                endTile = tmp;
            }
            return createWall(
                (beginTile.x() + endTile.x()) * HTS + HTS,
                beginTile.y() * TS + HTS,
                (endTile.x() - beginTile.x()) * TS + thickness,
                thickness,
                depthPy,
                fillMaterialPy,
                strokeMaterialPy);
        }
        else if (beginTile.x() == endTile.x()) { // vertical
            if (beginTile.y() > endTile.y()) {
                var tmp = beginTile;
                beginTile = endTile;
                endTile = tmp;
            }
            return createWall(
                beginTile.x() * TS + HTS,
                (beginTile.y() + endTile.y()) * HTS + HTS,
                thickness,
                (endTile.y() - beginTile.y()) * TS,
                depthPy,
                fillMaterialPy,
                strokeMaterialPy);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", beginTile, endTile));
    }

    private static Node createWall(
        double x, double y, double sizeX, double sizeY, DoubleProperty depthPy,
        ObjectProperty<PhongMaterial> fillMaterialPy, ObjectProperty<PhongMaterial> strokeMaterialPy) {

        var base = new Box(sizeX, sizeY, depthPy.get());
        base.setTranslateX(x);
        base.setTranslateY(y);
        base.translateZProperty().bind(depthPy.multiply(-0.5));
        base.depthProperty().bind(depthPy);
        base.materialProperty().bind(fillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, WALL_COAT_HEIGHT);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(depthPy.multiply(-1).subtract(WALL_COAT_HEIGHT));
        top.materialProperty().bind(strokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    private void createLivesCounter3D() {
        Supplier<Pac3D> pacShapeFactory = () -> switch (context.game().variant()) {
            case MS_PACMAN          -> new MsPacMan3D(10, null, context.theme());
            case PACMAN, PACMAN_XXL -> new PacMan3D(10, null, context.theme());
        };
        livesCounter3D = new LivesCounter3D(5, pacShapeFactory);
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.setVisible(context.gameController().hasCredit());
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);

        livesCounter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        livesCounter3D.light().setLightOn(context.gameController().hasCredit());
    }

    public void createLevelCounter3D() {
        World world = context.game().world();
        double spacing = 2 * TS;
        // this is the *right* edge of the level counter:
        levelCounter3D = new Group();
        levelCounter3D.setTranslateX(world.numCols() * TS - spacing);
        levelCounter3D.setTranslateY(spacing);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : context.game().levelCounter()) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            switch (context.game().variant()) {
                case MS_PACMAN -> {
                    var ss = (MsPacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
                    material.setDiffuseMap(ss.subImage(ss.bonusSymbolSprite(symbol)));
                }
                case PACMAN, PACMAN_XXL -> {
                    var ss = (PacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
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

    public void showMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        double dist = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(dist); // under floor
        var moveUpAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveUpAnimation.setToZ(-(dist + 0.5 * wallHeightPy.get()));
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
                var ss = (MsPacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
        }
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
    }

    private void addEnergizerAnimation(World world, Energizer3D energizer3D) {
        var squirting = new Squirting() {
            @Override
            protected boolean reachedFinalPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.root());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(coloredMaterial(foodColorPy.get().desaturate()));
        squirting.setOnFinished(e -> getChildren().remove(squirting.root()));
        getChildren().add(squirting.root());

        energizer3D.setEatenAnimation(squirting);
    }

    public void eat(Eatable3D eatable3D) {
        checkNotNull(eatable3D);
        if (eatable3D instanceof Energizer3D energizer3D) {
            energizer3D.stopPumping();
        }
        // Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
        // the pellet disappears too early (collision by tile equality is too coarse).
        var hiding = doAfterSec(0.05, () -> eatable3D.root().setVisible(false));
        var energizerExplosion = eatable3D.getEatenAnimation().orElse(null);
        if (energizerExplosion != null && PY_3D_ENERGIZER_EXPLODES.get()) {
            new SequentialTransition(hiding, energizerExplosion).play();
        } else {
            hiding.play();
        }
    }

    public void updateFood() {
        World world = context.game().world();
        pellets3D.forEach(pellet3D -> pellet3D.root().setVisible(!world.hasEatenFoodAt(pellet3D.tile())));
        energizers3D.forEach(energizer3D -> energizer3D.root().setVisible(!world.hasEatenFoodAt(energizer3D.tile())));
    }

    public RotateTransition createMazeRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), this);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Transition createWallsDisappearAnimation(double seconds) {
        return new Transition() {
            private final double initialWallHeight = wallHeightPy.get();
            private final double initialOuterWallHeight = outerWallHeightPy.get();
            {
                setCycleDuration(Duration.seconds(seconds));
                setInterpolator(Interpolator.LINEAR);
                setOnFinished(e -> {
                    mazeGroup.setVisible(false);
                    wallHeightPy.bind(PY_3D_WALL_HEIGHT);
                });
            }

            @Override
            protected void interpolate(double t) {
                wallHeightPy.unbind();
                wallHeightPy.set((1-t) * initialWallHeight);
                outerWallHeightPy.set((1-t) * initialOuterWallHeight);
            }
        };
    }

    public Transition createMazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        return new Transition() {
            private final double initialWallHeight = wallHeightPy.get();

            {
                setCycleDuration(Duration.seconds(0.33));
                setCycleCount(numFlashes);
                setInterpolator(Interpolator.LINEAR);
                setOnFinished(e -> wallHeightPy.bind(PY_3D_WALL_HEIGHT));
            }

            @Override
            protected void interpolate(double t) {
                // t = [0, 1] is mapped to the interval [pi/2, 3*pi/2]. The value of the sin-function in that interval
                // starts at 1, goes to 0 and back to 1
                double elongation = 0.5 * (1 + Math.sin(PI * (2*t + 0.5)));
                wallHeightPy.unbind(); // TODO when called in constructor does not work. Why?
                wallHeightPy.set(elongation * initialWallHeight);
            }
        };
    }

    public Pac3D pac3D() {
        return pac3D;
    }

    public List<Ghost3D> ghosts3D() {
        return ghosts3D;
    }

    public Ghost3D ghost3D(byte id) {
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

    public void startEnergizerAnimation() {
        energizers3D.forEach(Energizer3D::startPumping);
    }

    public void stopEnergizerAnimation() {
        energizers3D.forEach(Energizer3D::stopPumping);
    }

    public Optional<Energizer3D> energizer3D(Vector2i tile) {
        checkTileNotNull(tile);
        return energizers3D().filter(e3D -> e3D.tile().equals(tile)).findFirst();
    }

    public Optional<Pellet3D> pellet3D(Vector2i tile) {
        checkTileNotNull(tile);
        return pellets3D().filter(p3D -> p3D.tile().equals(tile)).findFirst();
    }
}
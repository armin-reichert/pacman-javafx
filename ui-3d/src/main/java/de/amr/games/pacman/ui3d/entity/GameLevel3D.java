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
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui3d.animation.Squirting;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.mapeditor.TileMapRenderer.getColorFromMap;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;
import static java.lang.Math.PI;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    private static final float FLOOR_THICKNESS       = 0.4f;
    private static final float WALL_HEIGHT           = 2.0f;
    private static final float WALL_THICKNESS        = 0.75f;
    private static final float WALL_THICKNESS_DWALL  = 1.5f;
    private static final float WALL_COAT_HEIGHT      = 0.1f;
    private static final float HOUSE_HEIGHT          = 12.0f;
    private static final float HOUSE_OPACITY         = 0.4f;
    private static final float HOUSE_SENSITIVITY     = 1.5f * TS;
    private static final float PAC_SIZE              = 13.5f;
    private static final float GHOST_SIZE            = 13.0f;
    private static final float ENERGIZER_RADIUS      = 3.5f;
    private static final float PELLET_RADIUS         = 1.0f;

    public final ObjectProperty<String> floorTextureNamePy = new SimpleObjectProperty<>(this, "floorTextureName") {
        @Override
        protected void invalidated() {
            Color floorColor = floorColorPy.get();
            String textureName = get();
            if (NO_TEXTURE.equals(textureName)) {
                floor.setMaterial(coloredMaterial(floorColor));
            } else {
                Map<String, PhongMaterial> floorTextures = context.theme().getMap("floorTextures");
                floor.setMaterial(floorTextures.getOrDefault(textureName, coloredMaterial(floorColor)));
            }
        }
    };

    public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            Color floorColor = get();
            String textureName = floorTextureNamePy.get();
            Map<String, PhongMaterial> floorTextures = context.theme().getMap("floorTextures");
            floor.setMaterial(floorTextures.getOrDefault(textureName, coloredMaterial(floorColor)));
        }
    };

    public final DoubleProperty wallHeightPy  = new SimpleDoubleProperty(this, "wallHeight", WALL_HEIGHT);

    public final DoubleProperty houseHeightPy = new SimpleDoubleProperty(this, "houseHeight", HOUSE_HEIGHT);

    public final BooleanProperty houseUsedPy = new SimpleBooleanProperty(this, "houseUsed", false);

    public final BooleanProperty houseOpenPy = new SimpleBooleanProperty(this, "houseOpen", false) {
        @Override
        protected void invalidated() {
            if (get()) {
                doorWings3D().map(DoorWing3D::traversalAnimation).forEach(Transition::play);
            }
        }
    };

    public final ObjectProperty<PhongMaterial> houseFillMaterialPy = new SimpleObjectProperty<>(this, "houseFillMaterial");

    public final ObjectProperty<PhongMaterial> wallStrokeMaterialPy = new SimpleObjectProperty<>(this, "wallStrokeMaterial");

    public final ObjectProperty<PhongMaterial> wallFillMaterialPy = new SimpleObjectProperty<>(this, "wallFillMaterial");

    public final ObjectProperty<PhongMaterial> foodMaterialPy = new SimpleObjectProperty<>(this, "foodMaterial");

    public final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity",1.0) {
        @Override
        protected void invalidated() {
            double opacity = get();
            Color fillColor = wallFillColorPy.get();
            Color color = opaqueColor(fillColor, opacity);
            PhongMaterial fillMaterial = wallFillMaterialPy.get();
            fillMaterial.setDiffuseColor(color);
            fillMaterial.setSpecularColor(color.brighter());
        }
    };

    public final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(this, "wallStrokeColor", Color.WHITE) {
        @Override
        protected void invalidated() {
            Color strokeColor = get();
            wallStrokeMaterialPy.set(coloredMaterial(strokeColor));        }
    };

    public final ObjectProperty<Color> wallFillColorPy = new SimpleObjectProperty<>(this, "wallFillColor", Color.GREEN) {
        @Override
        protected void invalidated() {
            Color fillColor = get();
            double opacity = wallOpacityPy.get();
            wallFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, opacity)));
            houseFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, HOUSE_OPACITY)));
        }
    };

    public final ObjectProperty<Color> foodColorPy = new SimpleObjectProperty<>(this, "foodColor", Color.PINK) {
        @Override
        protected void invalidated() {
            foodMaterialPy.set(coloredMaterial(get()));
        }
    };

    private final GameContext context;
    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Group levelCounterGroup = new Group();
    private final Box floor;
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private final Set<Pellet3D> pellets3D = new HashSet<>();
    private final Set<Energizer3D> energizers3D = new HashSet<>();

    private LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;
    private Message3D message3D;

    public GameLevel3D(GameContext context) {
        this.context = checkNotNull(context);

        // Floor
        floor = new Box(context.game().world().numCols() * TS - 1, context.game().world().numRows() * TS - 1, FLOOR_THICKNESS);
        // place floor such that surface is at z=0
        floor.getTransforms().add(new Translate(0.5 * floor.getWidth(), 0.5 * floor.getHeight(), 0.5 * floor.getDepth()));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTextureNamePy.bind(PY_3D_FLOOR_TEXTURE);

        // Maze
        WorldMap map = context.game().world().map();
        wallStrokeColorPy.set(getColorFromMap(map.terrain(), "wall_stroke_color", Color.rgb(33, 33, 255)));
        wallFillColorPy.set(getColorFromMap(map.terrain(), "wall_fill_color", Color.rgb(0,0,0)));
        foodColorPy.set(getColorFromMap(map.terrain(), "food_color", Color.PINK));
        addMazeWalls(mazeGroup);
        buildGhostHouse(mazeGroup);
        addFood3D(mazeGroup);

        pac3D = switch (context.game().variant()) {
            case MS_PACMAN          -> new MsPacMan3D(PAC_SIZE, context.game().pac(), context.theme());
            case PACMAN, PACMAN_XXL -> new PacMan3D(PAC_SIZE, context.game().pac(), context.theme());
        };
        ghosts3D = context.game().ghosts().map(this::createGhost3D).toList();

        createLivesCounter3D();
        createLevelCounter3D();
        createMessage3D();

        worldGroup.getChildren().addAll(floor, mazeGroup);
        getChildren().addAll(ghosts3D);
        getChildren().addAll(pac3D, pac3D.light());
        // Walls group must come after the guys! Otherwise, transparency is not working correctly.
        getChildren().addAll(message3D, levelCounterGroup, livesCounter3D, worldGroup);

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
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

        //TODO reconsider this:
        int numLivesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.READY && !game.pac().isVisible()) {
            numLivesCounterEntries += 1;
        }
        livesCounter3D.livesShownPy.set(numLivesCounterEntries);
    }

    private void buildGhostHouse(Group parent) {
        WorldMap map = context.game().world().map();
        House house = context.game().world().house();
        Vector2i leftDoorTile = house.door().leftWing();
        Vector2i rightDoorTile = house.door().rightWing();

        // tiles
        int tilesX = house.size().x();
        int tilesY = house.size().y();

        // tile coordinates
        int xMin = map.numCols() / 2 - (int) Math.ceil(0.5 * tilesX);
        int yMin = map.numRows() / 2 - (int) Math.ceil(0.5 * tilesY);
        int yMax = yMin + tilesY - 1;
        int xMax = xMin + tilesX - 1;

        parent.getChildren().addAll(
            houseWall(xMin, yMin, leftDoorTile.x() - 1,yMin),
            houseWall(rightDoorTile.x() + 1, yMin, xMax,yMin),
            houseWall(xMin,yMin,  xMin,yMax),
            houseWall(xMax, yMin,  xMax,yMax),
            houseWall(xMin,yMax, xMax,yMax)
        );

        Color doorColor = getColorFromMap(map.terrain(), "door_color",Color.rgb(254,184,174));
        for (Vector2i wingTile : List.of(leftDoorTile, rightDoorTile)) {
            var doorWing3D = new DoorWing3D(wingTile, doorColor, PY_3D_FLOOR_COLOR.get());
            doorWing3D.drawModePy.bind(PY_3D_DRAW_MODE);
            parent.getChildren().add(doorWing3D);
        }

        // pixel coordinates
        float centerX = house.topLeftTile().x() * TS + house.size().x() * HTS;
        float centerY = house.topLeftTile().y() * TS + house.size().y() * HTS;

        var houseLight = new PointLight();
        houseLight.lightOnProperty().bind(houseUsedPy);
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(centerX);
        houseLight.setTranslateY(centerY);
        houseLight.setTranslateZ(-TS);
        parent.getChildren().add(houseLight);
    }

    private Node houseWall(int x1, int y1, int x2, int y2) {
        return createWall(v2i(x1, y1), v2i(x2, y2), WALL_THICKNESS, houseHeightPy, houseFillMaterialPy);
    }

    private void addFood3D(Group parent) {
        var world = context.game().world();
        Color color = getColorFromMap(world.map().food(), "food_color", Color.WHITE);
        foodColorPy.set(color);
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            if (world.isEnergizerTile(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                addEnergizerAnimation(world, energizer3D);
                energizers3D.add(energizer3D);
                energizer3D.root().materialProperty().bind(foodMaterialPy);
                energizer3D.placeAtTile(tile);
                parent.getChildren().add(energizer3D.root());
            } else {
                var pellet3D = new Pellet3D(context.theme().get("model3D.pellet"), PELLET_RADIUS);
                pellets3D.add(pellet3D);
                pellet3D.root().materialProperty().bind(foodMaterialPy);
                pellet3D.placeAtTile(tile);
                parent.getChildren().add(pellet3D.root());
            }
        });
    }

    private Stream<DoorWing3D> doorWings3D() {
        return worldGroup.getChildren().stream()
            .filter(node -> node instanceof DoorWing3D)
            .map(DoorWing3D.class::cast);
    }

    private void addMazeWalls(Group parent) {
        TileMap terrainMap = context.game().world().map().terrain();
        terrainMap.computePaths();
        House house = context.game().world().house();
        terrainMap.dwallPaths()
            .filter(path -> !house.contains(path.startTile()))
            .forEach(path -> buildWallsAlongPath(parent, path, WALL_THICKNESS_DWALL));
        terrainMap.wallPaths()
            .forEach(path -> buildWallsAlongPath(parent, path, WALL_THICKNESS));
    }

    private void buildWallsAlongPath(Group parent, TileMapPath tileMapPath, double thickness) {
        Vector2i wallStart = tileMapPath.startTile();
        Vector2i wallEnd = wallStart;
        Direction prevDir = null;
        for (int i = 0; i < tileMapPath.size(); ++i) {
            var dir = tileMapPath.dir(i);
            if (prevDir != dir) {
                parent.getChildren().add(createWall(wallStart, wallEnd, thickness, wallHeightPy, wallFillMaterialPy));
                wallStart = wallEnd;
            }
            wallEnd = wallEnd.plus(dir.vector());
            prevDir = dir;
        }
        parent.getChildren().add(createWall(wallStart, wallEnd, thickness, wallHeightPy, wallFillMaterialPy));
    }

    private Node createWall(
        Vector2i beginTile, Vector2i endTile,
        double thickness, DoubleProperty depthPy, ObjectProperty<PhongMaterial> fillMaterialPy) {

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
                fillMaterialPy);
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
                fillMaterialPy);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", beginTile, endTile));
    }

    private Group createWall(double x, double y, double sizeX, double sizeY, DoubleProperty depthPy,
                             ObjectProperty<PhongMaterial> fillMaterialPy) {

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
        top.materialProperty().bind(wallStrokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    private void createLivesCounter3D() {
        Supplier<Pac3D> pacShapeFactory = switch (context.game().variant()) {
            case MS_PACMAN          -> () -> new MsPacMan3D(10, null, context.theme());
            case PACMAN, PACMAN_XXL -> () -> new PacMan3D(10, null, context.theme());
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
        levelCounterGroup.setTranslateX(world.numCols() * TS - spacing);
        levelCounterGroup.setTranslateY(spacing);
        levelCounterGroup.setTranslateZ(-6);
        levelCounterGroup.getChildren().clear();
        int n = 0;
        for (byte symbol : context.game().levelCounter()) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounterGroup.getChildren().add(cube);

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
        message3D.beginBatch();
        message3D.setBorderColor(Color.WHITE);
        message3D.setTextColor(Color.YELLOW);
        message3D.setFont(context.theme().font("font.arcade", 6));
        message3D.setVisible(false);
        message3D.endBatch();
    }

    public void showMessage(String text, double displaySeconds, double x, double y) {
        message3D.setText(text);
        message3D.setVisible(true);
        message3D.rotate(Rotate.X_AXIS, 90);
        double radius = 0.5 * message3D.getBoundsInLocal().getHeight();
        message3D.setTranslateX(x);
        message3D.setTranslateY(y);
        message3D.setTranslateZ(radius);
        var moveOutAnimation = new TranslateTransition(Duration.seconds(1), message3D);
        moveOutAnimation.setToZ(-(radius + 0.5 * wallHeightPy.get()));
        var moveInAnimation = new TranslateTransition(Duration.seconds(0.5), message3D);
        moveInAnimation.setDelay(Duration.seconds(displaySeconds));
        moveInAnimation.setToZ(radius);
        moveInAnimation.setOnFinished(e -> message3D.setVisible(false));
        new SequentialTransition(moveOutAnimation, moveInAnimation).play();
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

    private Ghost3D createGhost3D(Ghost ghost) {
        return new Ghost3D(context.theme().get("model3D.ghost"), context.theme(), ghost, GHOST_SIZE);
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

    public Transition createLevelRotateAnimation(double seconds) {
        var rotation = new RotateTransition(Duration.seconds(seconds), this);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Animation createMazeDisappearAnimation(double seconds) {
        return new Transition() {
            private final DoubleProperty valuePy = new SimpleDoubleProperty();
            {
                setRate(-1); // value goes 1 -> 0
                setCycleDuration(Duration.seconds(seconds));
                setInterpolator(Interpolator.EASE_BOTH);
                setOnFinished(e -> {
                    mazeGroup.setVisible(false);
                    wallHeightPy.bind(PY_3D_WALL_HEIGHT);
                });
            }

            @Override
            public void play() {
                wallHeightPy.bind(valuePy.multiply(wallHeightPy.get()));
                super.play();
            }

            @Override
            protected void interpolate(double t) {
                valuePy.set(t);
            }
        };
    }

    public Animation createMazeFlashingAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        return new Transition() {
            private final DoubleProperty elongationPy = new SimpleDoubleProperty();
            {
                setCycleDuration(Duration.seconds(0.33));
                setCycleCount(numFlashes);
                setOnFinished(e -> wallHeightPy.bind(PY_3D_WALL_HEIGHT));
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            public void play() {
                wallHeightPy.bind(elongationPy.multiply(wallHeightPy.get()));
                super.play();
            }

            @Override
            protected void interpolate(double t) {
                // t = [0, 1] is mapped to the interval [pi/2, 3*pi/2]. The value of the sin-function in that interval
                // starts at 1, goes to 0 and back to 1
                elongationPy.set(0.5 * (1 + Math.sin(PI * (2*t + 0.5))));
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
/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.*;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.opaqueColor;
import static de.amr.games.pacman.ui.fx.util.Ufx.doAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    private static final double WALL_TOP_THICKNESS = 0.1;
    private static final float PAC_SIZE   = 12.0f;
    private static final float GHOST_SIZE = 11.0f;

    public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);
    public final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity", 0.5) {
        @Override
        protected void invalidated() {
            Color color = ResourceManager.opaqueColor(fillColor, get());
            mazeWallFillMaterialPy.get().setDiffuseColor(color);
            mazeWallFillMaterialPy.get().setSpecularColor(color.brighter());
        }
    };
    private final DoubleProperty houseHeightPy = new SimpleDoubleProperty(this, "houseHeight", 12.0);
    private final ObjectProperty<PhongMaterial> houseFillMaterialPy = new SimpleObjectProperty<>(new PhongMaterial());

    private final ObjectProperty<PhongMaterial> mazeWallStrokeMaterialPy = new SimpleObjectProperty<>(new PhongMaterial());
    private final ObjectProperty<PhongMaterial> mazeWallFillMaterialPy = new SimpleObjectProperty<>(new PhongMaterial());

    private final GameSceneContext context;

    private final Group worldGroup = new Group();
    private final Group wallsGroup = new Group();
    private final Group doorGroup = new Group();
    private final Group foodGroup = new Group();
    private final Group levelCounterGroup = new Group();
    private final PointLight houseLight = new PointLight();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;

    private       Message3D message3D;
    private       LivesCounter3D livesCounter3D;
    private       Bonus3D bonus3D;
    private       Color fillColor = Color.BLUE;

    public GameLevel3D(GameSceneContext context) {
        checkNotNull(context);
        this.context = context;

        pac3D = createPac3D(context.game().pac());
        ghosts3D = context.game().ghosts().map(this::createGhost3D).toList();
        createWorld3D();
        createLivesCounter3D();
        createLevelCounter3D();
        createMessage3D();

        getChildren().addAll(ghosts3D);
        // Walls must be added *last*! Otherwise, transparency is not working correctly.
        getChildren().addAll(pac3D, pac3D.light(), message3D, levelCounterGroup, livesCounter3D, foodGroup, doorGroup, worldGroup);

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private Pac3D createPac3D(Pac pac) {
        return switch (context.game()) {
            case GameVariants.MS_PACMAN -> Pac3D.createMsPacMan3D(context.theme(), pac, PAC_SIZE);
            case GameVariants.PACMAN -> Pac3D.createPacMan3D(context.theme(), pac, PAC_SIZE);
            default -> throw new IllegalGameVariantException(context.game());
        };
    }

    private void createMsPacManMaze3D(World world, int levelNumber) {
        int mapNumber  = context.game().mapNumber(levelNumber);
        int mazeNumber = context.game().mazeNumber(levelNumber);
        Color strokeColor = mapNumber == mazeNumber && world.terrainMap().getProperties().containsKey("wall_color")
            ? Color.web(world.terrainMap().getProperty("wall_color"))
            : context.theme().color("mspacman.maze.wallTopColor", mazeNumber - 1);
        mazeWallStrokeMaterialPy.set(ResourceManager.coloredMaterial(strokeColor));
        fillColor = mapNumber == mazeNumber && world.terrainMap().getProperties().containsKey("wall_fill_color")
            ? Color.web(world.terrainMap().getProperty("wall_fill_color"))
            : context.theme().color("mspacman.maze.wallMiddleColor", mazeNumber - 1);
        mazeWallFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, wallOpacityPy.get())));
        houseFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, 0.25)));
        buildMaze3D(wallsGroup);

        Color foodColor = mapNumber == mazeNumber && world.foodMap().getProperties().containsKey("food_color")
            ? Color.web(world.foodMap().getProperty("food_color"))
            : context.theme().color("mspacman.maze.foodColor", mazeNumber - 1);
        createFood3D(world, foodColor);
        addGhostHouse(world.house(), context.theme().color("mspacman.maze.doorColor"));
    }

    private void createPacManMaze3D(World world) {
        Color strokeColor = world.terrainMap().getProperties().containsKey("wall_color")
            ? Color.web(world.terrainMap().getProperty("wall_color"))
            : context.theme().color("pacman.maze.wallBaseColor");
        mazeWallStrokeMaterialPy.set(ResourceManager.coloredMaterial(strokeColor));
        fillColor = world.terrainMap().getProperties().containsKey("wall_fill_color")
            ? Color.web(world.terrainMap().getProperty("wall_fill_color"))
            : context.theme().color("pacman.maze.wallTopColor");
        mazeWallFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, wallOpacityPy.get())));
        houseFillMaterialPy.set(coloredMaterial(opaqueColor(fillColor, 0.25)));
        buildMaze3D(wallsGroup);

        Color foodColor = world.foodMap().getProperties().containsKey("food_color")
            ? Color.web(world.foodMap().getProperty("food_color"))
            : context.theme().color("pacman.maze.foodColor");
        createFood3D(world, foodColor);
        addGhostHouse(world.house(), context.theme().color("pacman.maze.doorColor"));
    }

    private void createFood3D(World world, Color foodColor) {
        Material foodMaterial = coloredMaterial(foodColor);
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            if (world.isEnergizerTile(tile)) {
                var energizer3D = new Energizer3D(3.5);
                energizer3D.root().setMaterial(foodMaterial);
                energizer3D.placeAtTile(tile);
                foodGroup.getChildren().add(energizer3D.root());
                addEnergizerAnimation(world, energizer3D, foodColor);

            } else {
                var pellet3D = new Pellet3D(context.theme().get("model3D.pellet"), 1.0);
                pellet3D.root().setMaterial(foodMaterial);
                pellet3D.placeAtTile(tile);
                foodGroup.getChildren().add(pellet3D.root());
            }
        });
    }

    private void createWorld3D() {
        switch (context.game()) {
            case GameVariants.MS_PACMAN -> createMsPacManMaze3D(context.game().world(), context.game().levelNumber());
            case GameVariants.PACMAN    -> createPacManMaze3D(context.game().world());
            default -> throw new IllegalGameVariantException(context.game());
        }

        var floorTextures = new HashMap<String, PhongMaterial>();
        for (var textureName : context.theme().getArray("texture.names")) {
            String key = "texture." + textureName;
            floorTextures.put(key, context.theme().get(key));
        }

        var floor3D = new Floor3D(context.game().world().numCols() * TS - 1, context.game().world().numRows() * TS - 1, 0.4, floorTextures);
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor3D.colorPy.bind(PY_3D_FLOOR_COLOR);
        floor3D.texturePy.bind(PY_3D_FLOOR_TEXTURE);
        floor3D.getTransforms().add(new Translate(0.5 * floor3D.getWidth(), 0.5 * floor3D.getHeight(), 0.5 * floor3D.getDepth()));

        worldGroup.getChildren().addAll(houseLight, floor3D, wallsGroup);
    }

    private void addHouseWall(int x1, int y1, int x2, int y2) {
        wallsGroup.getChildren().add(createWall(v2i(x1,y1), v2i(x2,y2), houseHeightPy, houseFillMaterialPy));
    }

    private void addGhostHouse(House house, Color doorColor) {
        addHouseWall(10,15, 12,15);
        addHouseWall(10,15, 10,19);
        addHouseWall(10,19, 17,19);
        addHouseWall(17,19, 17,15);
        addHouseWall(17,15, 15,15);
        for (var wing : List.of(house.door().leftWing(), house.door().rightWing())) {
            var doorWing3D = new DoorWing3D(wing, doorColor);
            doorWing3D.drawModePy.bind(PY_3D_DRAW_MODE);
            doorGroup.getChildren().add(doorWing3D);
        }

        Vector2f houseCenter = house.topLeftTile().toFloatVec().scaled(TS).plus(house.size().toFloatVec().scaled(HTS));
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(houseCenter.x());
        houseLight.setTranslateY(houseCenter.y());
        houseLight.setTranslateZ(-TS);
    }

    private static Direction targetDirection(Direction dir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> dir == Direction.LEFT  ? Direction.DOWN : Direction.RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> dir == Direction.RIGHT ? Direction.DOWN : Direction.LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> dir == Direction.DOWN  ? Direction.LEFT : Direction.UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> dir == Direction.DOWN  ? Direction.RIGHT : Direction.UP;
            default -> dir;
        };
    }

    private void addMazeWallPath(
        TileMap terrainMap,
        List<List<Vector2i>> pathList,
        Set<Vector2i> explored,
        Vector2i startTile, Direction startDirection)
    {
        Logger.info("Build path starting at {} moving {}", startTile, startDirection);
        List<Vector2i> path = new ArrayList<>();
        Vector2i current = startTile;
        Direction moveDir = startDirection;
        while (true) {
            path.add(current);
            explored.add(current);
            current = current.plus(moveDir.vector());
            if (!terrainMap.insideBounds(current)) {
                break;
            }
            if (explored.contains(current)) {
                path.add(startTile); // close path
                break;
            }
            moveDir = targetDirection(moveDir, terrainMap.get(current));
        }
        pathList.add(path);
    }

    private void buildMaze3D(Group root) {
        TileMap terrainMap = context.game().world().terrainMap();
        Set<Vector2i> explored = new HashSet<>();
        List<List<Vector2i>> pathList = new ArrayList<>();

        // Obstacles inside maze
        terrainMap.tiles()
            .filter(tile -> tile.x() > 0)
            .filter(tile -> !explored.contains(tile))
            .filter(tile -> terrainMap.get(tile) == Tiles.CORNER_NW)
            .forEach(tile -> addMazeWallPath(terrainMap, pathList, explored, tile, Direction.RIGHT));

        // Loose ends left maze border
        List<Vector2i> startTilesLeftBorder = new ArrayList<>();
        for (int row = 0; row < terrainMap.numRows(); ++row) {
            if (terrainMap.get(row, 0) == Tiles.TUNNEL) {
                startTilesLeftBorder.add(new Vector2i(0, row - 1));
                startTilesLeftBorder.add(new Vector2i(0, row + 1));
            }
        }
        startTilesLeftBorder.stream()
            .filter(tile -> !explored.contains(tile))
            .forEach(tile -> addMazeWallPath(terrainMap, pathList, explored, tile,
                targetDirection(Direction.RIGHT, terrainMap.get(tile))));

        // Loose ends right maze border
        List<Vector2i> startTilesRightBorder = new ArrayList<>();
        for (int row = 0; row < terrainMap.numRows(); ++row) {
            if (terrainMap.get(row, terrainMap.numCols() - 1) == Tiles.TUNNEL) {
                startTilesRightBorder.add(new Vector2i(terrainMap.numCols() - 1, row - 1));
                startTilesRightBorder.add(new Vector2i(terrainMap.numCols() - 1, row + 1));
            }
        }
        startTilesRightBorder.stream()
            .filter(tile -> !explored.contains(tile))
            .forEach(tile -> addMazeWallPath(terrainMap, pathList, explored, tile,
                targetDirection(Direction.LEFT, terrainMap.get(tile))));

        for (var path: pathList) {
            Logger.info("Build path: {}", path);
            buildWallsAlongPath(root, terrainMap, path);
        }
    }

    private void buildWallsAlongPath(Group parent, TileMap terrainMap, List<Vector2i> path) {
        int from = 0;
        int to = from;
        while (true) {
            if (to == path.size()) {
                parent.getChildren().add(createWall(path.get(from), path.get(to-1)));
                break;
            }
            if (!isWall(terrainMap.get(path.get(to)))) {
                parent.getChildren().add(createWall(path.get(from), path.get(to)));
                from = to;
            }
            ++to;
        }
    }

    private boolean isWall(byte tileValue) {
        return tileValue == Tiles.WALL_H || tileValue == Tiles.WALL_V || tileValue == Tiles.DWALL_H || tileValue == Tiles.DWALL_V;
    }

    private Node createWall(Vector2i first, Vector2i second) {
        return createWall(first, second, wallHeightPy, mazeWallFillMaterialPy);
    }

    private Node createWall(Vector2i first, Vector2i second, DoubleProperty heightPy, ObjectProperty<PhongMaterial> fillMaterialPy) {
        if (first.y() == second.y()) {
            // horizontal
            Logger.info("Hor. Wall from {} to {}", first, second);
            if (first.x() > second.x()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double w = (second.x() - first.x()) * 8;
            double m = (first.x() + second.x()) * 4;

            var base = new Box(w, 1, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(m + 4);
            base.setTranslateY(first.y() * 8 + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(w, 1, WALL_TOP_THICKNESS);
            top.materialProperty().bind(mazeWallStrokeMaterialPy);
            top.translateXProperty().bind(base.translateXProperty());
            top.translateYProperty().bind(base.translateYProperty());
            top.translateZProperty().bind(heightPy.multiply(-1).subtract(WALL_TOP_THICKNESS));

            return new Group(base, top);
        }
        else if (first.x() == second.x()){
            // vertical
            Logger.info("Vert. Wall from {} to {}", first, second);
            if (first.y() > second.y()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double h = (second.y() - first.y()) * 8;
            double m = (first.y() + second.y()) * 4;

            var base = new Box(1, h, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(first.x() * 8 + 4);
            base.setTranslateY(m + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(1, h, WALL_TOP_THICKNESS);
            top.materialProperty().bind(mazeWallStrokeMaterialPy);
            top.translateXProperty().bind(base.translateXProperty());
            top.translateYProperty().bind(base.translateYProperty());
            top.translateZProperty().bind(heightPy.multiply(-1).subtract(WALL_TOP_THICKNESS));

            return new Group(base, top);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", first, second));
    }

    private void createLivesCounter3D() {
        var theme = context.theme();
        livesCounter3D = new LivesCounter3D(
            theme.get  ("livescounter.entries"),
            theme.color("livescounter.pillar.color"),
            theme.get  ("livescounter.pillar.height"),
            theme.color("livescounter.plate.color"),
            theme.get  ("livescounter.plate.thickness"),
            theme.get  ("livescounter.plate.radius"),
            theme.color("livescounter.light.color"));
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (int i = 0; i < livesCounter3D.maxLives(); ++i) {
            var pac3D = switch (context.game()) {
                case GameVariants.MS_PACMAN -> Pac3D.createMsPacMan3D(context.theme(), null, theme.get("livescounter.pac.size"));
                case GameVariants.PACMAN -> Pac3D.createPacMan3D(context.theme(), null,  theme.get("livescounter.pac.size"));
                default -> throw new IllegalGameVariantException(context.game());
            };
            livesCounter3D.addItem(pac3D, true);
        }
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
            var sprite = switch (context.game()) {
                case GameVariants.MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
                case GameVariants.PACMAN -> context.<PacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
                default -> throw new IllegalGameVariantException(context.game());
            };
            material.setDiffuseMap(context.spriteSheet().subImage(sprite));
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
        moveOutAnimation.setToZ(-(radius + 0.8 * wallHeightPy.get()));
        var moveInAnimation = new TranslateTransition(Duration.seconds(0.5), message3D);
        moveInAnimation.setDelay(Duration.seconds(displaySeconds));
        moveInAnimation.setToZ(radius);
        moveInAnimation.setOnFinished(e -> message3D.setVisible(false));
        new SequentialTransition(moveOutAnimation, moveInAnimation).play();
    }

    public void update() {
        GameModel game = context.game();
        World world = game.world();
        boolean hasCredit = GameController.it().hasCredit();

        pac3D.update(game);
        ghosts3D().forEach(ghost3D -> ghost3D.update(game));
        if (bonus3D != null) {
            bonus3D.update(world);
        }
        updateHouseState(world.house());
        // reconsider this:
        int numLivesDisplayed = game.lives() - 1;
        if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter3D.update(numLivesDisplayed);
        livesCounter3D.setVisible(hasCredit);
    }

    public void setHouseLightOn(boolean state) {
        houseLight.setLightOn(state);
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            worldGroup.getChildren().remove(bonus3D);
        }
        switch (context.game()) {
            case GameVariants.PACMAN -> {
                var ss = context.<PacManGameSpriteSheet>spriteSheet();
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            case GameVariants.MS_PACMAN -> {
                var ss = context.<MsPacManGameSpriteSheet>spriteSheet();
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            default -> throw new IllegalGameVariantException(context.game());
        }
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
    }

    private Ghost3D createGhost3D(Ghost ghost) {
        return new Ghost3D(
            context.theme().get("model3D.ghost"),
            context.theme(),
            ghost,
            context.game().level().orElseThrow().numFlashes(),
            GHOST_SIZE);
    }

    private void addEnergizerAnimation(World world, Energizer3D energizer3D, Color foodColor) {
        var squirting = new Squirting() {
            @Override
            protected boolean reachedFinalPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.containsPoint(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.root());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(coloredMaterial(foodColor.desaturate()));
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
        var hiding = doAfterSeconds(0.05, () -> eatable3D.root().setVisible(false));
        var energizerExplosion = eatable3D.getEatenAnimation().orElse(null);
        if (energizerExplosion != null && PY_3D_ENERGIZER_EXPLODES.get()) {
            new SequentialTransition(hiding, energizerExplosion).play();
        } else {
            hiding.play();
        }
    }

    public Transition createLevelRotateAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), this);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Transition createLevelCompleteAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSeconds(1.0);
        }
        var animation = new SinusCurveAnimation(numFlashes);
        animation.setOnFinished(e -> wallHeightPy.bind(PY_3D_WALL_HEIGHT));
        animation.setAmplitude(wallHeightPy.get());
        animation.elongationPy.set(wallHeightPy.get());
        // this should in fact be done on animation start:
        wallHeightPy.bind(animation.elongationPy);
        return animation;
    }

    private void updateHouseState(House house) {
        boolean houseUsed = context.game().ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(house.door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        setHouseLightOn(houseUsed);
        if (houseOpen) {
            for (var node : doorGroup.getChildren()) {
                DoorWing3D wing3D = (DoorWing3D) node.getUserData();
                wing3D.traversalAnimation().play();
            }
        }
    }

    public Pac3D pac3D() {
        return pac3D;
    }

    public List<Ghost3D> ghosts3D() {
        return ghosts3D;
    }

    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    public Stream<Eatable3D> allEatables() {
        return foodGroup.getChildren().stream().map(Node::getUserData).map(Eatable3D.class::cast);
    }

    public Stream<Energizer3D> energizers3D() {
        return allEatables().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
    }

    public void startEnergizerAnimation() {
        energizers3D().forEach(Energizer3D::startPumping);
    }

    public void stopEnergizerAnimation() {
        energizers3D().forEach(Energizer3D::stopPumping);
    }

    public Optional<Eatable3D> eatableAt(Vector2i tile) {
        checkTileNotNull(tile);
        return allEatables().filter(eatable -> eatable.tile().equals(tile)).findFirst();
    }
}
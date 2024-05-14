/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapMaze;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.model.world.World;
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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer.getTileMapColor;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.opaqueColor;
import static de.amr.games.pacman.ui.fx.util.Ufx.doAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    static final double WALL_THICKNESS = 0.75;
    static final double WALL_TOP_THICKNESS = 0.1;
    static final float PAC_SIZE   = 14.0f;
    static final float GHOST_SIZE = 13.0f;
    static final double ENERGIZER_RADIUS = 3.5;
    static final double PELLET_RADIUS = 1.0;

    public final ObjectProperty<String> floorTexturePy = new SimpleObjectProperty<>(this, "floorTexture", NO_TEXTURE) {
        @Override
        protected void invalidated() {
            var material = floorTextures.getOrDefault("texture." + floorTexturePy.get(), coloredMaterial(floorColorPy.get()));
            floor.setMaterial(material);
        }
    };

    public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            var material = floorTextures.getOrDefault("texture." + floorTexturePy.get(), coloredMaterial(floorColorPy.get()));
            floor.setMaterial(material);
        }
    };

    private final Map<String, PhongMaterial> floorTextures = new HashMap<>();

    private final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);
    private final DoubleProperty houseHeightPy = new SimpleDoubleProperty(this, "houseHeight", 12.0);

    private final ObjectProperty<PhongMaterial> houseFillMaterialPy = new SimpleObjectProperty<>();
    private final ObjectProperty<PhongMaterial> mazeWallStrokeMaterialPy = new SimpleObjectProperty<>();
    private final ObjectProperty<PhongMaterial> mazeWallFillMaterialPy = new SimpleObjectProperty<>();
    private final ObjectProperty<PhongMaterial> mazeFoodMaterialPy = new SimpleObjectProperty<>();

    public final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(this, "wallOpacity",1.0) {
        @Override
        protected void invalidated() {
            Color color = ResourceManager.opaqueColor(wallFillColorPy.get(), get());
            mazeWallFillMaterialPy.get().setDiffuseColor(color);
            mazeWallFillMaterialPy.get().setSpecularColor(color.brighter());
        }
    };

    private final ObjectProperty<Color> wallStrokeColorPy = new SimpleObjectProperty<>(Color.WHITE) {
        @Override
        protected void invalidated() {
            mazeWallStrokeMaterialPy.set(coloredMaterial(get()));        }
    };

    private final ObjectProperty<Color> wallFillColorPy = new SimpleObjectProperty<>(Color.GREEN) {
        @Override
        protected void invalidated() {
            mazeWallFillMaterialPy.set(coloredMaterial(opaqueColor(wallFillColorPy.get(), wallOpacityPy.get())));
            houseFillMaterialPy.set(coloredMaterial(opaqueColor(wallFillColorPy.get(), 0.4)));
        }
    };

    private final ObjectProperty<Color> foodColorPy = new SimpleObjectProperty<>(Color.PINK) {
        @Override
        protected void invalidated() {
            mazeFoodMaterialPy.set(coloredMaterial(get()));
        }
    };

    private final GameSceneContext context;

    private final Group worldGroup = new Group();
    private final Group mazeGroup = new Group();
    private final Group foodGroup = new Group();
    private final Group levelCounterGroup = new Group();
    private final PointLight houseLight = new PointLight();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private final Box floor = new Box();
    private Message3D message3D;
    private LivesCounter3D livesCounter3D;
    private Bonus3D bonus3D;

    public GameLevel3D(GameSceneContext context) {
        this.context = checkNotNull(context);

        List<String> textureNames = context.theme().getArray("texture.names");
        for (String textureName : textureNames) {
            String key = "texture." + textureName;
            floorTextures.put(key, context.theme().get(key));
        }
        floor.setWidth(context.game().world().numCols() * TS - 1);
        floor.setHeight(context.game().world().numRows() * TS - 1);
        floor.setDepth(0.4);
        floor.getTransforms().add(new Translate(0.5 * floor.getWidth(), 0.5 * floor.getHeight(), 0.5 * floor.getDepth()));
        floor.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floorColorPy.bind(PY_3D_FLOOR_COLOR);
        floorTexturePy.bind(PY_3D_FLOOR_TEXTURE);

        switch (context.game().variant()) {
            case MS_PACMAN -> createMsPacManMaze3D(context.game().levelNumber());
            case PACMAN    -> createPacManMaze3D();
        }

        pac3D = switch (context.game().variant()) {
            case MS_PACMAN -> Pac3D.createMsPacMan3D(context.theme(), context.game().pac(), PAC_SIZE);
            case PACMAN    -> Pac3D.createPacMan3D(context.theme(), context.game().pac(), PAC_SIZE);
        };
        ghosts3D = context.game().ghosts().map(this::createGhost3D).toList();

        createLivesCounter3D();
        createLevelCounter3D();
        createMessage3D();

        worldGroup.getChildren().addAll(floor, mazeGroup);

        // Walls must be added after the guys! Otherwise, transparency is not working correctly.
        getChildren().addAll(ghosts3D);
        getChildren().addAll(pac3D, pac3D.light(), message3D, levelCounterGroup, livesCounter3D, foodGroup, worldGroup);

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D.forEach(ghost3D -> ghost3D.drawModePy.bind(PY_3D_DRAW_MODE));
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);
    }

    private void createMsPacManMaze3D(int levelNumber) {
        var game = context.game();
        var world = game.world();
        MapMaze mm = game.mapMaze(levelNumber);
        //TODO store these in terrain maps
        wallStrokeColorPy.set(context.theme().get("mspacman.wallStrokeColor", mm.mapNumber(), mm.mazeNumber()));
        wallFillColorPy.set(context.theme().get("mspacman.wallFillColor",   mm.mapNumber(), mm.mazeNumber()));
        foodColorPy.set(context.theme().get("mspacman.foodColor", mm.mapNumber(), mm.mazeNumber()));
        buildWalls(mazeGroup);
        addGhostHouse(mazeGroup);
        createFood3D(foodGroup);
    }

    private void createPacManMaze3D() {
        var world = context.game().world();
        wallStrokeColorPy.set(getTileMapColor(world.terrainMap(), "wall_stroke_color", Color.rgb(33, 33, 255)));
        wallFillColorPy.set(getTileMapColor(world.terrainMap(), "wall_fill_color", Color.rgb(0,0,0)));
        foodColorPy.set(getTileMapColor(world.foodMap(), "food_color", Color.PINK));
        buildWalls(mazeGroup);
        addGhostHouse(mazeGroup);
        createFood3D(foodGroup);
    }

    private void createFood3D(Group parent) {
        var world = context.game().world();
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            if (world.isEnergizerTile(tile)) {
                var energizer3D = new Energizer3D(ENERGIZER_RADIUS);
                energizer3D.root().materialProperty().bind(mazeFoodMaterialPy);
                energizer3D.placeAtTile(tile);
                parent.getChildren().add(energizer3D.root());
                addEnergizerAnimation(world, energizer3D);

            } else {
                var pellet3D = new Pellet3D(context.theme().get("model3D.pellet"), PELLET_RADIUS);
                pellet3D.root().materialProperty().bind(mazeFoodMaterialPy);
                pellet3D.placeAtTile(tile);
                parent.getChildren().add(pellet3D.root());
            }
        });
    }

    private void addGhostHouse(Group parent) {
        addHouseWall(parent, 10,15, 12,15);
        addHouseWall(parent, 10,15, 10,19);
        addHouseWall(parent, 10,19, 17,19);
        addHouseWall(parent, 17,19, 17,15);
        addHouseWall(parent, 17,15, 15,15);

        House house = context.game().world().house();
        Color doorColor   = getTileMapColor(context.game().world().terrainMap(), "door_color", Color.rgb(254,184,174));
        for (Vector2i wingTile : List.of(house.door().leftWing(), house.door().rightWing())) {
            var doorWing3D = new DoorWing3D(wingTile, doorColor, PY_3D_FLOOR_COLOR.get());
            doorWing3D.drawModePy.bind(PY_3D_DRAW_MODE);
            parent.getChildren().add(doorWing3D);
        }

        float centerX = house.topLeftTile().x() * TS + house.size().x() * HTS;
        float centerY = house.topLeftTile().y() * TS + house.size().y() * HTS;
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(centerX);
        houseLight.setTranslateY(centerY);
        houseLight.setTranslateZ(-TS);
        parent.getChildren().add(houseLight);
    }

    private void addHouseWall(Group parent, int x1, int y1, int x2, int y2) {
        parent.getChildren().add(createWall(v2i(x1, y1), v2i(x2, y2), houseHeightPy, houseFillMaterialPy));
    }

    private Stream<DoorWing3D> doorWings3D() {
        return worldGroup.getChildren().stream()
            .filter(node -> node instanceof DoorWing3D)
            .map(DoorWing3D.class::cast);
    }

    private static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }

    private static List<Vector2i> buildTerrainMapPath(
        TileMap terrainMap, Set<Vector2i> explored, Vector2i startTile, Direction startDirection)
    {
        Logger.trace("Build path starting at {} moving {}", startTile, startDirection);
        List<Vector2i> path = new ArrayList<>();
        Vector2i current = startTile;
        Direction moveDir = startDirection;
        while (true) {
            path.add(current);
            explored.add(current);
            var next = current.plus(moveDir.vector());
            if (!terrainMap.insideBounds(next)) {
                break;
            }
            if (explored.contains(next)) {
                path.add(next);
                break;
            }
            moveDir = newMoveDir(moveDir, terrainMap.get(next));
            current = next;
        }
        return path;
    }

    private void buildWalls(Group parent) {
        TileMap terrainMap = context.game().world().terrainMap();
        var explored = new HashSet<Vector2i>();

        // Obstacles inside maze
        terrainMap.tiles()
            .filter(tile -> terrainMap.get(tile) == Tiles.CORNER_NW)
            .filter(corner -> corner.x() > 0 && corner.x() < terrainMap.numCols() - 1)
            .filter(corner -> corner.y() > 0 && corner.y() < terrainMap.numRows() - 1)
            .map(corner -> buildTerrainMapPath(terrainMap, explored, corner, RIGHT))
            .forEach(path -> buildWallsAlongPath(parent, terrainMap, path));

        // Paths starting at left and right maze border (over and under tunnel ends)
        var handlesLeft = new ArrayList<Vector2i>();
        var handlesRight = new ArrayList<Vector2i>();
        for (int row = 0; row < terrainMap.numRows(); ++row) {
            if (terrainMap.get(row, 0) == Tiles.TUNNEL) {
                handlesLeft.add(new Vector2i(0, row - 1));
                handlesLeft.add(new Vector2i(0, row + 1));
            }
            if (terrainMap.get(row, terrainMap.numCols() - 1) == Tiles.TUNNEL) {
                handlesRight.add(new Vector2i(terrainMap.numCols() - 1, row - 1));
                handlesRight.add(new Vector2i(terrainMap.numCols() - 1, row + 1));
            }
        }

        handlesLeft.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> buildTerrainMapPath(terrainMap, explored, handle, newMoveDir(RIGHT, terrainMap.get(handle))))
            .forEach(path -> buildWallsAlongPath(parent, terrainMap, path));

        handlesRight.stream()
            .filter(handle -> !explored.contains(handle))
            .map(handle -> buildTerrainMapPath(terrainMap, explored, handle, newMoveDir(LEFT, terrainMap.get(handle))))
            .forEach(path -> buildWallsAlongPath(parent, terrainMap, path));
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

    private static boolean isWall(byte tileValue) {
        return tileValue == Tiles.WALL_H || tileValue == Tiles.WALL_V || tileValue == Tiles.DWALL_H || tileValue == Tiles.DWALL_V;
    }

    private Node createWall(Vector2i first, Vector2i second) {
        return createWall(first, second, wallHeightPy, mazeWallFillMaterialPy);
    }

    private Node createWall(Vector2i first, Vector2i second, DoubleProperty heightPy, ObjectProperty<PhongMaterial> fillMaterialPy) {
        if (first.y() == second.y()) {
            // horizontal
            Logger.trace("Hor. Wall from {} to {}", first, second);
            if (first.x() > second.x()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double w = (second.x() - first.x()) * 8 + WALL_THICKNESS;
            double m = (first.x() + second.x()) * 4;

            var base = new Box(w, WALL_THICKNESS, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(m + 4);
            base.setTranslateY(first.y() * 8 + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(w, WALL_THICKNESS, WALL_TOP_THICKNESS);
            top.materialProperty().bind(mazeWallStrokeMaterialPy);
            top.translateXProperty().bind(base.translateXProperty());
            top.translateYProperty().bind(base.translateYProperty());
            top.translateZProperty().bind(heightPy.multiply(-1).subtract(WALL_TOP_THICKNESS));

            return new Group(base, top);
        }
        else if (first.x() == second.x()){
            // vertical
            Logger.trace("Vert. Wall from {} to {}", first, second);
            if (first.y() > second.y()) {
                var tmp = first;
                first = second;
                second = tmp;
            }
            double h = (second.y() - first.y()) * 8;
            double m = (first.y() + second.y()) * 4;

            var base = new Box(WALL_THICKNESS, h, heightPy.get());
            base.materialProperty().bind(fillMaterialPy);
            base.depthProperty().bind(heightPy);
            base.drawModeProperty().bind(PY_3D_DRAW_MODE);
            base.setTranslateX(first.x() * 8 + 4);
            base.setTranslateY(m + 4);
            base.translateZProperty().bind(heightPy.multiply(-0.5));

            var top = new Box(WALL_THICKNESS, h, WALL_TOP_THICKNESS);
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
            theme.get("livescounter.entries"),
            theme.get("livescounter.pillar.color"),
            theme.get("livescounter.pillar.height"),
            theme.get("livescounter.plate.color"),
            theme.get("livescounter.plate.thickness"),
            theme.get("livescounter.plate.radius"),
            theme.get("livescounter.light.color"));
        livesCounter3D.setTranslateX(2 * TS);
        livesCounter3D.setTranslateY(2 * TS);
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (int i = 0; i < livesCounter3D.maxLives(); ++i) {
            var pac3D = switch (context.game().variant()) {
                case MS_PACMAN -> Pac3D.createMsPacMan3D(context.theme(), null, theme.get("livescounter.pac.size"));
                case PACMAN    -> Pac3D.createPacMan3D(context.theme(), null,  theme.get("livescounter.pac.size"));
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
            var sprite = switch (context.game().variant()) {
                case MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
                case PACMAN    -> context.<PacManGameSpriteSheet>spriteSheet().bonusSymbolSprite(symbol);
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
        boolean hasCredit = GameController.it().hasCredit();

        pac3D.update(game);
        ghosts3D().forEach(ghost3D -> ghost3D.update(game));
        if (bonus3D != null) {
            bonus3D.update(game.world());
        }
        updateHouseState(game.world().house());
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
        switch (context.game().variant()) {
            case PACMAN -> {
                var ss = context.<PacManGameSpriteSheet>spriteSheet();
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
            case MS_PACMAN -> {
                var ss = context.<MsPacManGameSpriteSheet>spriteSheet();
                bonus3D = new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(bonus.symbol())), ss.subImage(ss.bonusValueSprite(bonus.symbol())));
            }
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
            doorWings3D().map(DoorWing3D::traversalAnimation).forEach(Transition::play);
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
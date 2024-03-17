/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx.v3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.Ufx.actionAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;
import static de.amr.games.pacman.ui.fx.v3d.entity.Pac3D.createMsPacManShape;
import static de.amr.games.pacman.ui.fx.v3d.entity.Pac3D.createPacManShape;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    private static final int    FLOOR_PLAN_RESOLUTION = 4;
    private static final double PAC_SIZE   = 9.0;
    private static final double GHOST_SIZE = 9.0;
    private static final double LIVES_COUNTER_PAC_SIZE = 10.0;
    private static final double MESSAGE_EXTENDED_Z = -5;
    private static final double MESSAGE_RETRACTED_Z =  5;

    public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);

    private final GameLevel level;
    private final GameSceneContext context;
    private final FloorPlan floorPlan;
    private final WallBuilder wallBuilder;
    private final Group root = new Group();
    private final Group worldGroup = new Group();
    private       Group door3D;
    private final Group foodGroup = new Group();
    private final PointLight houseLight = new PointLight();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private final LevelCounter3D levelCounter3D;
    private final LivesCounter3D livesCounter3D;
    private final Text3D messageText3D;
    private Bonus3D bonus3D;

    public GameLevel3D(GameLevel level, GameSceneContext context) {
        checkLevelNotNull(level);
        checkNotNull(context);

        this.level = level;
        this.context = context;

        switch (context.gameVariant()) {
            case MS_PACMAN -> {
                int mapNumber  = ArcadeWorld.mapNumberMsPacMan(level.number());
                int mazeNumber = ArcadeWorld.mazeNumberMsPacMan(level.number());
                floorPlan = getFloorPlan(GameVariant.MS_PACMAN, mapNumber);
                wallBuilder = createWallBuilder(GameVariant.MS_PACMAN, mazeNumber);
                createWorld3D();
                createFood3D(context.theme().color("mspacman.maze.foodColor", mazeNumber - 1));
                pac3D = new Pac3D(createMsPacManShape(context.theme(), PAC_SIZE), level.pac(),
                    context.theme().color("mspacman.color.head"));
                pac3D.setWalkingAnimation(new HipSwaying(level.pac(), pac3D.root()));
                pac3D.setLight(new PointLight(Color.rgb(255, 255, 0, 0.75)));
                ghosts3D = level.ghosts().map(this::createGhost3D).toList();
                livesCounter3D = new LivesCounter3D(() -> createMsPacManShape(context.theme(), LIVES_COUNTER_PAC_SIZE), true);
            }
            case PACMAN -> {
                floorPlan = getFloorPlan(GameVariant.PACMAN, 1);
                wallBuilder = createWallBuilder(GameVariant.PACMAN, 1);
                createWorld3D();
                createFood3D(context.theme().color("pacman.maze.foodColor"));
                pac3D = new Pac3D(createPacManShape(context.theme(), PAC_SIZE), level.pac(),
                    context.theme().color("pacman.color.head"));
                pac3D.setWalkingAnimation(new HeadBanging(level.pac(), pac3D.root()));
                pac3D.setLight(new PointLight(Color.rgb(255, 255, 0, 0.75)));
                ghosts3D = level.ghosts().map(this::createGhost3D).toList();
                livesCounter3D = new LivesCounter3D(() -> createPacManShape(context.theme(), LIVES_COUNTER_PAC_SIZE), false);
            }
            default -> throw new IllegalGameVariantException(context.gameVariant());
        }

        messageText3D = Text3D.create("READY!", Color.YELLOW, context.theme().font("font.arcade", 6));
        messageText3D.root().setTranslateZ(MESSAGE_RETRACTED_Z);
        messageText3D.root().setVisible(false);
        messageText3D.rotate(Rotate.X_AXIS, 90);

        livesCounter3D.root().setTranslateX(2 * TS);
        livesCounter3D.root().setTranslateY(2 * TS);

        levelCounter3D = new LevelCounter3D();
        populateLevelCounter(context.game(), context.spriteSheet());
        // this is the *right* edge of the level counter:
        levelCounter3D.root().setTranslateX((level.world().numCols() - 2) * TS);
        levelCounter3D.root().setTranslateY(2 * TS);
        levelCounter3D.root().setTranslateZ(-HTS);

        root.getChildren().add(messageText3D.root());
        root.getChildren().add(levelCounter3D.root());
        root.getChildren().add(livesCounter3D.root());
        root.getChildren().addAll(pac3D.root());
        root.getChildren().add(pac3D.light());
        for (var ghost3D : ghosts3D) {
            root.getChildren().add(ghost3D.root());
        }
        root.getChildren().add(foodGroup);
        root.getChildren().add(door3D);
        // Walls must be added *after* the rest. Otherwise, transparency is not working correctly!
        root.getChildren().add(worldGroup);

        // Bindings
        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (var g3D: ghosts3D) {
            g3D.drawModePy.bind(PY_3D_DRAW_MODE);
        }
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        wallHeightPy.bind(PY_3D_WALL_HEIGHT);
    }

    private WallBuilder createWallBuilder(GameVariant variant, int mazeNumber) {
        var wallBuilder = new WallBuilder((float) TS / FLOOR_PLAN_RESOLUTION);
        switch (variant) {
            case MS_PACMAN -> {
                wallBuilder.setWallBaseColor  (context.theme().color("mspacman.maze.wallBaseColor",  mazeNumber - 1));
                wallBuilder.setWallMiddleColor(context.theme().color("mspacman.maze.wallMiddleColor",mazeNumber - 1));
                wallBuilder.setWallTopColor   (context.theme().color("mspacman.maze.wallTopColor",   mazeNumber - 1));
                wallBuilder.setHouseDoorColor (context.theme().color("mspacman.maze.doorColor"));
            }
            case PACMAN -> {
                wallBuilder.setWallBaseColor  (context.theme().color("pacman.maze.wallBaseColor"));
                wallBuilder.setWallMiddleColor(context.theme().color("pacman.maze.wallMiddleColor"));
                wallBuilder.setWallTopColor   (context.theme().color("pacman.maze.wallTopColor"));
                wallBuilder.setHouseDoorColor (context.theme().color("pacman.maze.doorColor"));
            }
        }
        wallBuilder.wallOpacityPy.bind(PY_3D_WALL_OPACITY);
        wallBuilder.drawModePy.bind(PY_3D_DRAW_MODE);
        return wallBuilder;
    }

    private void createWorld3D() {
        House house = level.world().house();
        Vector2f houseCenter = house.topLeftTile().toFloatVec().scaled(TS).plus(house.size().toFloatVec().scaled(HTS));
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(houseCenter.x());
        houseLight.setTranslateY(houseCenter.y());
        houseLight.setTranslateZ(-TS);

        door3D = wallBuilder.createDoorGroup(house.door());

        var floorTextures = new HashMap<String, PhongMaterial>();
        for (var textureName : context.theme().getArray("texture.names")) {
            String key = "texture." + textureName;
            floorTextures.put(key, context.theme().get(key));
        }

        var floor3D = new Floor3D(level.world().numCols() * TS - 1, level.world().numRows() * TS - 1, 0.4, floorTextures);
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor3D.colorPy.bind(PY_3D_FLOOR_COLOR);
        floor3D.texturePy.bind(PY_3D_FLOOR_TEXTURE);

        var floorGroup = new Group();
        floorGroup.getChildren().add(floor3D);
        floorGroup.getTransforms().add(new Translate(0.5 * floor3D.getWidth(), 0.5 * floor3D.getHeight(), 0.5 * floor3D.getDepth()));

        var wallsGroup = new Group();
        addWalls(wallsGroup);

        worldGroup.getChildren().addAll(floorGroup, wallsGroup, houseLight);
        Logger.info("3D world created (resolution={}, wall height={})", floorPlan.resolution(), wallHeightPy.get());
    }

    public void setHouseLightOn(boolean state) {
        houseLight.setLightOn(state);
    }

    private void addWalls(Group wallsGroup) {
        addCorners(wallsGroup);
        addHorizontalWalls(wallsGroup);
        addVerticalWalls(wallsGroup);
    }

    private void addHorizontalWalls(Group wallsGroup) {
        var wd = new WallData();
        wd.type = FloorPlan.HWALL;
        wd.numBricksY = 1;
        for (short y = 0; y < floorPlan.sizeY(); ++y) {
            wd.x = -1;
            wd.y = y;
            wd.numBricksX = 0;
            for (short x = 0; x < floorPlan.sizeX(); ++x) {
                if (floorPlan.cell(x, y) == FloorPlan.HWALL) {
                    if (wd.numBricksX == 0) {
                        wd.x = x;
                    }
                    wd.numBricksX++;
                } else if (wd.numBricksX > 0) {
                    addWall(wallsGroup, wd);
                    wd.numBricksX = 0;
                }
            }
            if (wd.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
                addWall(wallsGroup, wd);
            }
        }
    }

    private void addVerticalWalls(Group wallsGroup) {
        var wd = new WallData();
        wd.type = FloorPlan.VWALL;
        wd.numBricksX = 1;
        for (short x = 0; x < floorPlan.sizeX(); ++x) {
            wd.x = x;
            wd.y = -1;
            wd.numBricksY = 0;
            for (short y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.VWALL) {
                    if (wd.numBricksY == 0) {
                        wd.y = y;
                    }
                    wd.numBricksY++;
                } else if (wd.numBricksY > 0) {
                    addWall(wallsGroup, wd);
                    wd.numBricksY = 0;
                }
            }
            if (wd.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
                addWall(wallsGroup, wd);
            }
        }
    }

    private void addCorners(Group wallsGroup) {
        var wd = new WallData();
        wd.type = FloorPlan.CORNER;
        wd.numBricksX = 1;
        wd.numBricksY = 1;
        for (short x = 0; x < floorPlan.sizeX(); ++x) {
            for (short y = 0; y < floorPlan.sizeY(); ++y) {
                if (floorPlan.cell(x, y) == FloorPlan.CORNER) {
                    wd.x = x;
                    wd.y = y;
                    addWall(wallsGroup, wd);
                }
            }
        }
    }

    private void addWall(Group wallsGroup, WallData wd) {
        boolean partOfHouse = level.world().house().contains(floorPlan.tileOfCell(wd.x, wd.y));
        if (!partOfHouse) {
            wallsGroup.getChildren().add(wallBuilder.createMazeWall(wd, PY_3D_WALL_THICKNESS, wallHeightPy));
        } else if (!isWallInsideHouse(wd, level.world().house())) {
            // only outer house wall gets built
            wallsGroup.getChildren().add(wallBuilder.createHouseWall(wd));
        }
    }

    private boolean isWallInsideHouse(WallData wd, House house) {
        int res = floorPlan.resolution();
        Vector2i bottomRightTile = house.topLeftTile().plus(house.size());
        double xMin = house.topLeftTile().x() * res;
        double yMin = house.topLeftTile().y() * res;
        double xMax = (bottomRightTile.x() - 1) * res;
        double yMax = (bottomRightTile.y() - 1) * res;
        return wd.x > xMin && wd.y > yMin && wd.x <= xMax && wd.y <= yMax;
    }

    private FloorPlan getFloorPlan(GameVariant variant, int mapNumber) {
        ResourceManager rm = () -> PacManGames3dUI.class;
        String name = switch (variant) {
            case MS_PACMAN -> "fp-mspacman-map-" + mapNumber + "-res-" + FLOOR_PLAN_RESOLUTION + ".txt";
            case PACMAN    -> "fp-pacman-map-"   + mapNumber + "-res-" + FLOOR_PLAN_RESOLUTION + ".txt";
        };
        return FloorPlan.read(rm.url("floorplans/" + name));
    }

    public void update() {
        GameState gameState = GameController.it().state();
        boolean hasCredit = GameController.it().hasCredit();

        pac3D.update();
        for (var ghost3D : ghosts3D) {
            ghost3D.update(level);
        }
        if (bonus3D != null) {
            bonus3D.update(level);
        }
        // reconsider this:
        boolean hideOne = level.pac().isVisible() || gameState == GameState.GHOST_DYING;
        livesCounter3D.update(hideOne ? level.game().lives() - 1 : level.game().lives());
        livesCounter3D.root().setVisible(hasCredit);
        updateHouseState(level.world().house());
    }

    public void populateLevelCounter(GameModel game, SpriteSheet spriteSheet) {
        levelCounter3D.populate(game, spriteSheet);
    }

    public void showMessage(String text, double displaySeconds, double x, double y) {
        messageText3D.setText(text);
        messageText3D.root().setVisible(true);
        messageText3D.root().setTranslateX(x);
        messageText3D.root().setTranslateY(y);
        messageText3D.root().setTranslateZ(MESSAGE_RETRACTED_Z);
        var extend = new TranslateTransition(Duration.seconds(1), messageText3D.root());
        extend.setToZ(MESSAGE_EXTENDED_Z);
        var retract = new TranslateTransition(Duration.seconds(0.5), messageText3D.root());
        retract.setDelay(Duration.seconds(displaySeconds));
        retract.setToZ(MESSAGE_RETRACTED_Z);
        retract.setOnFinished(e -> messageText3D.root().setVisible(false));
        new SequentialTransition(extend, retract).play();
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            root.getChildren().remove(bonus3D.root());
        }
        bonus3D = createBonus3D(bonus);
        bonus3D.showEdible();
        // add bonus before last element (wall group) to make transparency work
        root.getChildren().add(root.getChildren().size() - 1, bonus3D.root());
    }

    private Bonus3D createBonus3D(Bonus bonus) {
        byte symbol = bonus.symbol();
        switch (level.game().variant()) {
            case PACMAN -> {
                PacManGameSpriteSheet ss = context.spriteSheet();
                return new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(symbol)),
                    ss.subImage(ss.bonusValueSprite(symbol)));
            }
            case MS_PACMAN -> {
                MsPacManGameSpriteSheet ss = context.spriteSheet();
                return new Bonus3D(bonus,
                    ss.subImage(ss.bonusSymbolSprite(symbol)),
                    ss.subImage(ss.bonusValueSprite(symbol)));
            }
            default -> throw new IllegalGameVariantException(level.game().variant());
        }
    }

    private Ghost3D createGhost3D(Ghost ghost) {
        return new Ghost3D(
            context.theme().get("model3D.ghost"),
            context.theme(),
            ghost,
            level.pac(),
            level.data().numFlashes(),
            GHOST_SIZE);
    }

    private void createFood3D(Color foodColor) {
        var world = level.world();
        var foodMaterial = coloredMaterial(foodColor);
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

    private void addEnergizerAnimation(World world, Energizer3D energizer3D, Color foodColor) {
        var squirting = new Squirting() {
            @Override
            protected boolean reachedFinalPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.root());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(coloredMaterial(foodColor.desaturate()));
        squirting.setOnFinished(e -> root.getChildren().remove(squirting.root()));
        root.getChildren().add(squirting.root());

        energizer3D.setEatenAnimation(squirting);
    }

    public void eat(Eatable3D eatable3D) {
        checkNotNull(eatable3D);
        if (eatable3D instanceof Energizer3D energizer3D) {
            energizer3D.stopPumping();
        }
        // Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
        // the pellet disappears too early (collision by tile equality is too coarse).
        var hiding = actionAfterSeconds(0.05, () -> eatable3D.root().setVisible(false));
        var energizerExplosion = eatable3D.getEatenAnimation().orElse(null);
        if (energizerExplosion != null && PY_3D_ENERGIZER_EXPLODES.get()) {
            new SequentialTransition(hiding, energizerExplosion).play();
        } else {
            hiding.play();
        }
    }

    public Transition createLevelRotateAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), root);
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }

    public Transition createLevelCompleteAnimation() {
        if (level.data().numFlashes() == 0) {
            return pauseSeconds(1.0);
        }
        var animation = new SinusCurveAnimation(level.data().numFlashes());
        animation.setOnFinished(e -> wallHeightPy.bind(PY_3D_WALL_HEIGHT));
        animation.setAmplitude(wallHeightPy.get());
        animation.elongationPy.set(wallHeightPy.get());
        // this should in fact be done on animation start:
        wallHeightPy.bind(animation.elongationPy);
        return animation;
    }

    private void updateHouseState(House house) {
        boolean houseUsed = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(house.door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        setHouseLightOn(houseUsed);
        if (houseOpen) {
            for (var node : door3D.getChildren()) {
                DoorWing3D wing3D = (DoorWing3D) node.getUserData();
                wing3D.traversalAnimation().play();
            }
        }
    }

    public Group root() {
        return root;
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

    public Optional<Eatable3D> eatableAt(Vector2i tile) {
        checkTileNotNull(tile);
        return allEatables().filter(eatable -> eatable.tile().equals(tile)).findFirst();
    }
}
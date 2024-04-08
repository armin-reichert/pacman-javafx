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
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
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
import javafx.scene.shape.Box;
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
import static de.amr.games.pacman.ui.fx.util.Ufx.doAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D extends Group {

    private static final byte  FLOOR_PLAN_RESOLUTION = 4;
    private static final float PAC_SIZE   = 9.0f;
    private static final float GHOST_SIZE = 9.0f;

    private static FloorPlan readFloorPlanFromFile(GameModel variant, int mapNumber) {
        ResourceManager rm = () -> PacManGames3dUI.class;
        String variantMarker = switch (variant) {
            case GameVariants.MS_PACMAN -> "mspacman";
            case GameVariants.PACMAN    -> "pacman";
            default -> throw new IllegalGameVariantException(variant);
        };
        var path = String.format("floorplans/fp-%s-map-%d-res-%d.txt", variantMarker, mapNumber, FLOOR_PLAN_RESOLUTION);
        return FloorPlan.read(rm.url(path));
    }

    public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);

    private final GameLevel level;
    private final GameSceneContext context;
    private       FloorPlan floorPlan;
    private       WallBuilder wallBuilder;

    private final Group worldGroup = new Group();
    private final Group doorGroup = new Group();
    private final Group foodGroup = new Group();
    private final Group levelCounterGroup = new Group();
    private final PointLight houseLight = new PointLight();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private       Message3D message3D;
    private       LivesCounter3D livesCounter3D;
    private       Bonus3D bonus3D;

    public GameLevel3D(GameLevel level, GameSceneContext context) {
        checkLevelNotNull(level);
        checkNotNull(context);
        this.level = level;
        this.context = context;

        pac3D = createPac3D(level.pac());
        ghosts3D = level.ghosts().map(this::createGhost3D).toList();
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
    }

    private Pac3D createPac3D(Pac pac) {
        return switch (context.game()) {
            case GameVariants.MS_PACMAN -> Pac3D.createMsPacMan3D(context.theme(), pac, PAC_SIZE);
            case GameVariants.PACMAN -> Pac3D.createPacMan3D(context.theme(), pac, PAC_SIZE);
            default -> throw new IllegalGameVariantException(context.game());
        };
    }

    private void createWorld3D() {
        switch (context.game()) {
            case GameVariants.MS_PACMAN -> {
                int mapNumber  = ArcadeWorld.mapNumberMsPacMan(level.number());
                int mazeNumber = ArcadeWorld.mazeNumberMsPacMan(level.number());
                floorPlan = readFloorPlanFromFile(GameVariants.MS_PACMAN, mapNumber);
                wallBuilder = createWallBuilder(
                    context.theme().color("mspacman.maze.wallBaseColor",  mazeNumber - 1),
                    context.theme().color("mspacman.maze.wallMiddleColor",mazeNumber - 1),
                    context.theme().color("mspacman.maze.wallTopColor",   mazeNumber - 1));
                createFood3D(context.theme().color("mspacman.maze.foodColor", mazeNumber - 1));
            }
            case GameVariants.PACMAN -> {
                floorPlan = readFloorPlanFromFile(GameVariants.PACMAN, 1);
                wallBuilder = createWallBuilder(
                    context.theme().color("pacman.maze.wallBaseColor"),
                    context.theme().color("pacman.maze.wallMiddleColor"),
                    context.theme().color("pacman.maze.wallTopColor"));
                createFood3D(context.theme().color("pacman.maze.foodColor"));
            }
            default -> throw new IllegalGameVariantException(context.game());
        }

        House house = level.world().house();
        Vector2f houseCenter = house.topLeftTile().toFloatVec().scaled(TS).plus(house.size().toFloatVec().scaled(HTS));
        houseLight.setColor(Color.GHOSTWHITE);
        houseLight.setMaxRange(3 * TS);
        houseLight.setTranslateX(houseCenter.x());
        houseLight.setTranslateY(houseCenter.y());
        houseLight.setTranslateZ(-TS);

        var floorTextures = new HashMap<String, PhongMaterial>();
        for (var textureName : context.theme().getArray("texture.names")) {
            String key = "texture." + textureName;
            floorTextures.put(key, context.theme().get(key));
        }

        var floor3D = new Floor3D(level.world().numCols() * TS - 1, level.world().numRows() * TS - 1, 0.4, floorTextures);
        floor3D.drawModeProperty().bind(PY_3D_DRAW_MODE);
        floor3D.colorPy.bind(PY_3D_FLOOR_COLOR);
        floor3D.texturePy.bind(PY_3D_FLOOR_TEXTURE);
        floor3D.getTransforms().add(new Translate(0.5 * floor3D.getWidth(), 0.5 * floor3D.getHeight(), 0.5 * floor3D.getDepth()));

        addDoor3D(house.door());

        var wallsGroup = new Group();
        addCorners(wallsGroup);
        addHorizontalWalls(wallsGroup);
        addVerticalWalls(wallsGroup);

        worldGroup.getChildren().addAll(houseLight, floor3D, wallsGroup);
        Logger.info("3D world created (resolution={}, wall height={})", floorPlan.resolution(), wallHeightPy.get());
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
        double spacing = 2 * TS;
        // this is the *right* edge of the level counter:
        levelCounterGroup.setTranslateX(level.world().numCols() * TS - spacing);
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

    private WallBuilder createWallBuilder(Color wallBaseColor, Color wallMiddleColor, Color wallTopColor) {
        var wallBuilder = new WallBuilder((float) TS / FLOOR_PLAN_RESOLUTION);
        wallBuilder.setWallBaseColor(wallBaseColor);
        wallBuilder.setWallMiddleColor(wallMiddleColor);
        wallBuilder.setWallTopColor(wallTopColor);
        wallBuilder.wallOpacityPy.bind(PY_3D_WALL_OPACITY);
        wallBuilder.drawModePy.bind(PY_3D_DRAW_MODE);
        return wallBuilder;
    }

    private void addDoor3D(Door door) {
        Color color = switch (context.game()) {
            case GameVariants.MS_PACMAN -> context.theme().color("mspacman.maze.doorColor");
            case GameVariants.PACMAN -> context.theme().color("pacman.maze.doorColor");
            default -> throw new IllegalGameVariantException(context.game());
        };
        for (var wing : List.of(door.leftWing(), door.rightWing())) {
            var doorWing3D = new DoorWing3D(wing, color);
            doorWing3D.drawModePy.bind(PY_3D_DRAW_MODE);
            doorGroup.getChildren().add(doorWing3D);
        }
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

    public void update(World world) {
        GameState gameState = GameController.it().state();
        boolean hasCredit = GameController.it().hasCredit();

        pac3D.update(level.world());
        ghosts3D().forEach(ghost3D -> ghost3D.update(world));
        if (bonus3D != null) {
            bonus3D.update(level.world());
        }
        updateHouseState(level.world().house());
        // reconsider this:
        boolean hideOne = level.pac().isVisible() || gameState == GameState.GHOST_DYING;
        livesCounter3D.update(hideOne ? level.game().lives() - 1 : level.game().lives());
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
        switch (level.game()) {
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
            default -> throw new IllegalGameVariantException(level.game());
        }
        bonus3D.showEdible();
        worldGroup.getChildren().add(bonus3D);
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
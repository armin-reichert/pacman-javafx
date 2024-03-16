/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx.v3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
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

    private final GameLevel level;
    private final SpriteSheet spriteSheet;

    private final Group root = new Group();
    private final World3D world3D;
    private final Group door3D;
    private final Group foodGroup = new Group();
    private final Pac3D pac3D;
    private final List<Ghost3D> ghosts3D;
    private final LevelCounter3D levelCounter3D;
    private final LivesCounter3D livesCounter3D;
    private final Text3D messageText3D;

    private Bonus3D bonus3D;

    public GameLevel3D(GameLevel level, Theme theme, SpriteSheet spriteSheet) {
        checkLevelNotNull(level);
        checkNotNull(theme);
        checkNotNull(spriteSheet);

        this.level = level;
        var world = level.world();

        this.spriteSheet = spriteSheet;
        var textureMap = new HashMap<String, PhongMaterial>();
        for (var textureName : theme.getArray("texture.names")) {
            String key = "texture." + textureName;
            textureMap.put(key, theme.get(key));
        }

        switch (level.game().variant()) {
            case MS_PACMAN -> {
                int mapNumber = ArcadeWorld.mapNumberMsPacMan(level.number());
                int mazeIndex = level.game().mazeNumber(level.number()) - 1;
                var factory = new MazeFactory((float) TS / FLOOR_PLAN_RESOLUTION);
                factory.drawModePy.bind(PY_3D_DRAW_MODE);
                factory.setWallBaseColor  (theme.color("mspacman.maze.wallBaseColor", mazeIndex));
                factory.setWallMiddleColor(theme.color("mspacman.maze.wallMiddleColor", mazeIndex));
                factory.setWallTopColor   (theme.color("mspacman.maze.wallTopColor", mazeIndex));
                factory.setHouseDoorColor (theme.color("mspacman.maze.doorColor"));
                world3D = new World3D(world, getFloorPlan(mapNumber), textureMap, factory);
                door3D = factory.createDoorGroup(world.house().door());
                createFood(world, theme, theme.color("mspacman.maze.foodColor", mazeIndex));
                pac3D = new Pac3D(createMsPacManShape(theme, PAC_SIZE), level.pac(), theme.color("mspacman.color.head"));
                pac3D.setWalkingAnimation(new HipSwaying(level.pac(), pac3D.root()));
                pac3D.setLight(new PointLight());
                pac3D.light().setColor(Color.rgb(255, 255, 0, 0.75));
                ghosts3D = level.ghosts()
                    .map(ghost -> new Ghost3D(theme.get("model3D.ghost"), theme, ghost, level.pac(), level.data().numFlashes(), GHOST_SIZE))
                    .toList();
                livesCounter3D = new LivesCounter3D(() -> createMsPacManShape(theme, LIVES_COUNTER_PAC_SIZE), true);
            }
            case PACMAN -> {
                var factory = new MazeFactory((float) TS / FLOOR_PLAN_RESOLUTION);
                factory.drawModePy.bind(PY_3D_DRAW_MODE);
                factory.setWallBaseColor  (theme.color("pacman.maze.wallBaseColor"));
                factory.setWallMiddleColor(theme.color("pacman.maze.wallMiddleColor"));
                factory.setWallTopColor   (theme.color("pacman.maze.wallTopColor"));
                factory.setHouseDoorColor (theme.color("pacman.maze.doorColor"));
                world3D = new World3D(world, getFloorPlan(1), textureMap, factory);
                door3D = factory.createDoorGroup(world.house().door());
                createFood(world, theme, theme.color("pacman.maze.foodColor"));
                pac3D = new Pac3D(createPacManShape(theme, PAC_SIZE), level.pac(), theme.color("pacman.color.head"));
                pac3D.setWalkingAnimation(new HeadBanging(level.pac(), pac3D.root()));
                pac3D.setLight(new PointLight());
                pac3D.light().setColor(Color.rgb(255, 255, 0, 0.75));
                ghosts3D = level.ghosts()
                    .map(ghost -> new Ghost3D(theme.get("model3D.ghost"), theme, ghost, level.pac(), level.data().numFlashes(), GHOST_SIZE))
                    .toList();
                livesCounter3D = new LivesCounter3D(() -> createPacManShape(theme, LIVES_COUNTER_PAC_SIZE), false);
            }
            default -> throw new IllegalGameVariantException(level.game().variant());
        }

        messageText3D = Text3D.create("READY!", Color.YELLOW, theme.font("font.arcade", 6));
        messageText3D.root().setTranslateZ(MESSAGE_RETRACTED_Z);
        messageText3D.root().setVisible(false);
        messageText3D.rotate(Rotate.X_AXIS, 90);

        livesCounter3D.root().setTranslateX(2 * TS);
        livesCounter3D.root().setTranslateY(2 * TS);

        levelCounter3D = new LevelCounter3D();
        populateLevelCounter(level.game(), spriteSheet);
        // this is the *right* edge of the level counter:
        levelCounter3D.root().setTranslateX((world.numCols() - 2) * TS);
        levelCounter3D.root().setTranslateY(2 * TS);
        levelCounter3D.root().setTranslateZ(-HTS);

        root.getChildren().add(messageText3D.root());
        root.getChildren().add(levelCounter3D.root());
        root.getChildren().add(livesCounter3D.root());
        root.getChildren().addAll(pac3D.root());
        if (pac3D.light() != null) {
            root.getChildren().add(pac3D.light());
        }
        for (var ghost3D : ghosts3D) {
            root.getChildren().add(ghost3D.root());
        }
        root.getChildren().add(foodGroup);
        root.getChildren().add(door3D);
        // Walls must be added *after* the rest. Otherwise, transparency is not working correctly!
        root.getChildren().add(world3D.root());

        // Bindings
        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);

        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (var g3D: ghosts3D) {
            g3D.drawModePy.bind(PY_3D_DRAW_MODE);
        }
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        world3D.drawModePy.bind(PY_3D_DRAW_MODE);

        world3D.floor().colorPy   .bind(PY_3D_FLOOR_COLOR);
        world3D.floor().texturePy .bind(PY_3D_FLOOR_TEXTURE);
        world3D.wallHeightPy      .bind(PY_3D_WALL_HEIGHT);
        world3D.wallOpacityPy     .bind(PY_3D_WALL_OPACITY);
        world3D.wallThicknessPy   .bind(PY_3D_WALL_THICKNESS);
    }

    private FloorPlan getFloorPlan(int mapNumber) {
        ResourceManager rm = () -> PacManGames3dUI.class;
        String name = switch (level.game().variant()) {
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
                PacManGameSpriteSheet ss = (PacManGameSpriteSheet) spriteSheet;
                return new Bonus3D(bonus,
                    spriteSheet.subImage(ss.bonusSymbolSprite(symbol)),
                    spriteSheet.subImage(ss.bonusValueSprite(symbol)));
            }
            case MS_PACMAN -> {
                MsPacManGameSpriteSheet ss = (MsPacManGameSpriteSheet) spriteSheet;
                return new Bonus3D(bonus,
                    spriteSheet.subImage(ss.bonusSymbolSprite(symbol)),
                    spriteSheet.subImage(ss.bonusValueSprite(symbol)));
            }
            default -> throw new IllegalGameVariantException(level.game().variant());
        }
    }

    private void createFood(World world, Theme theme, Color foodColor) {
        var foodMaterial = coloredMaterial(foodColor);
        var particleMaterial = coloredMaterial(foodColor.desaturate());
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            Eatable3D food3D = world.isEnergizerTile(tile)
                ? createEnergizer3D(world, tile, foodMaterial, particleMaterial)
                : createNormalPellet3D(theme.get("model3D.pellet"), tile, foodMaterial);
            foodGroup.getChildren().add(food3D.root());
        });
    }

    private Pellet3D createNormalPellet3D(Model3D pelletModel3D, Vector2i tile, PhongMaterial material) {
        var pellet3D = new Pellet3D(pelletModel3D, 1.0);
        pellet3D.root().setMaterial(material);
        pellet3D.placeAtTile(tile);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(World world, Vector2i tile, PhongMaterial material, PhongMaterial particleMaterial) {
        var energizer3D = new Energizer3D(3.5);
        energizer3D.root().setMaterial(material);
        energizer3D.placeAtTile(tile);
        var squirting = createEnergizerAnimation(world, energizer3D.root(), particleMaterial);
        squirting.setOnFinished(e -> root.getChildren().remove(squirting.root()));
        energizer3D.setEatenAnimation(squirting);
        root.getChildren().add(squirting.root());
        return energizer3D;
    }

    private Squirting createEnergizerAnimation(World world, Node energizerNode, PhongMaterial particleMaterial) {
        var squirting = new Squirting() {
            @Override
            protected boolean reachesEndPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizerNode);
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(particleMaterial);
        return squirting;
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

    public void eat(Eatable3D eatable3D) {
        checkNotNull(eatable3D);

        if (eatable3D instanceof Energizer3D energizer3D) {
            energizer3D.stopPumping();
        }
        // Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
        // the pellet disappears too early (collision by same tile in game model is too simplistic).
        var delayHiding = Ufx.actionAfterSeconds(0.05, () -> eatable3D.root().setVisible(false));
        var eatenAnimation = eatable3D.getEatenAnimation();
        if (eatenAnimation.isPresent() && PacManGames3dUI.PY_3D_ENERGIZER_EXPLODES.get()) {
            new SequentialTransition(delayHiding, eatenAnimation.get()).play();
        } else {
            delayHiding.play();
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
        double wallHeight = PY_3D_WALL_HEIGHT.get();
        var animation = new SinusCurveAnimation(level.data().numFlashes());
        animation.setAmplitude(wallHeight);
        animation.elongationPy.set(world3D.wallHeightPy.get());
        world3D.wallHeightPy.bind(animation.elongationPy);
        animation.setOnFinished(e -> {
            world3D.wallHeightPy.bind(PY_3D_WALL_HEIGHT);
            PY_3D_WALL_HEIGHT.set(wallHeight);
        });
        return animation;
    }

    private void updateHouseState(House house) {
        boolean houseUsed = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(house.door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        world3D.setHouseLightOn(houseUsed);
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
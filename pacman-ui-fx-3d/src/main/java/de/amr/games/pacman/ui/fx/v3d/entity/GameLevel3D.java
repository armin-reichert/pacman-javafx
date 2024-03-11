/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.animation.Squirting;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
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
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;
import static de.amr.games.pacman.ui.fx.v3d.entity.Pac3D.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    private static final double PAC_SIZE = 9.0;
    private static final double GHOST_SIZE = 9.0;
    private static final double LIVES_COUNTER_PAC_SIZE = 10.0;
    private static final double MESSAGE_EXTENDED_Z = -5;
    private static final double MESSAGE_RETRACTED_Z =  5;

    private final GameLevel level;
    private final SpriteSheet spriteSheet;

    private final Group root = new Group();
    private final World3D world3D;
    private final Group doorGroup;
    private final Group foodGroup = new Group();
    private final Pac3D pac3D;
    private final Pac3DLight pac3DLight;
    private final List<Ghost3D> ghosts3D;
    private final LevelCounter3D levelCounter3D;
    private final LivesCounter3D livesCounter3D;
    private final Scores3D scores3D;
    private final Text3D messageText3D;
    private        Bonus3D bonus3D;

    public GameLevel3D(GameLevel level, Theme theme, SpriteSheet spriteSheet) {
        checkLevelNotNull(level);
        checkNotNull(theme);
        checkNotNull(spriteSheet);

        this.level = level;
        World world = level.world();

        this.spriteSheet = spriteSheet;
        var textureMap = new HashMap<String, PhongMaterial>();
        for (var textureName : theme.getArray("texture.names")) {
            String key = "texture." + textureName;
            textureMap.put(key, theme.get(key));
        }

        switch (level.game().variant()) {
            case MS_PACMAN -> {
                int mazeNumber = level.game().mazeNumber(level.number());
                var wallBaseColor   = theme.color("mspacman.maze.wallBaseColor", mazeNumber - 1);
                var wallMiddleColor = theme.color("mspacman.maze.wallMiddleColor", mazeNumber - 1);
                var wallTopColor    = theme.color("mspacman.maze.wallTopColor", mazeNumber - 1);
                var doorColor       = theme.color("mspacman.maze.doorColor");
                world3D = new World3D(world, textureMap, wallBaseColor, wallMiddleColor, wallTopColor, doorColor);
                doorGroup = world3D.createDoor();
                addFood(world, theme.color("mspacman.maze.foodColor", mazeNumber - 1), theme.get("model3D.pellet"));
                pac3D = createMsPacMan3D(theme.get("model3D.pacman"), theme, level.pac(), PAC_SIZE);
                pac3DLight = new Pac3DLight(pac3D);
                ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, theme.get("model3D.ghost"), theme, GHOST_SIZE)).toList();
                livesCounter3D = new LivesCounter3D(() -> createMsPacManGroup(theme.get("model3D.pacman"), theme, LIVES_COUNTER_PAC_SIZE), true);
            }
            case PACMAN -> {
                var wallBaseColor   = theme.color("pacman.maze.wallBaseColor");
                var wallMiddleColor = theme.color("pacman.maze.wallMiddleColor");
                var wallTopColor    = theme.color("pacman.maze.wallTopColor");
                var doorColor       = theme.color("pacman.maze.doorColor");
                world3D = new World3D(world, textureMap, wallBaseColor, wallMiddleColor, wallTopColor, doorColor);
                doorGroup = world3D.createDoor();
                addFood(world, theme.color("pacman.maze.foodColor"), theme.get("model3D.pellet"));
                pac3D = createPacMan3D(theme.get("model3D.pacman"), theme, level.pac(), PAC_SIZE);
                pac3DLight = new Pac3DLight(pac3D);
                ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, theme.get("model3D.ghost"), theme, GHOST_SIZE)).toList();
                livesCounter3D = new LivesCounter3D(() -> createPacManGroup(theme.get("model3D.pacman"), theme, LIVES_COUNTER_PAC_SIZE), false);
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
        // this is the *right* edge of the level counter:
        levelCounter3D.root().setTranslateX((world.numCols() - 2) * TS);
        levelCounter3D.root().setTranslateY(2 * TS);
        levelCounter3D.root().setTranslateZ(-HTS);
        updateLevelCounterSprites();

        scores3D = new Scores3D(theme.font("font.arcade", 8));
        scores3D.root().setTranslateX(TS);
        scores3D.root().setTranslateY(-3 * TS);
        scores3D.root().setTranslateZ(-3 * TS);

        root.getChildren().add(messageText3D.root());
        root.getChildren().add(scores3D.root());
        root.getChildren().add(levelCounter3D.root());
        root.getChildren().add(livesCounter3D.root());
        root.getChildren().addAll(pac3D.root(), pac3DLight);
        for (var ghost3D : ghosts3D) {
            root.getChildren().add(ghost3D.root());
        }
        root.getChildren().add(foodGroup);
        root.getChildren().add(doorGroup);
        // Walls must be added *after* the rest. Otherwise, transparency is not working correctly!
        root.getChildren().add(world3D.root());

        // center over origin
        root.setTranslateX(-world.numCols() * HTS);
        root.setTranslateY(-world.numRows() * HTS);

        // Bindings
        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);

        pac3D.drawModePy.bind(PY_3D_DRAW_MODE);
        for (var g3D: ghosts3D) {
            g3D.drawModePy.bind(PY_3D_DRAW_MODE);
        }
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        world3D.drawModePy.bind(PY_3D_DRAW_MODE);

        world3D.floorColorPy        .bind(PY_3D_FLOOR_COLOR);
        world3D.floorTexturePy      .bind(PY_3D_FLOOR_TEXTURE);
        world3D.wallHeightPy        .bind(PY_3D_WALL_HEIGHT);
        world3D.wallOpacityPy       .bind(PY_3D_WALL_OPACITY);
        world3D.wallThicknessPy     .bind(PY_3D_WALL_THICKNESS);
        world3D.houseWallOpacityPy  .bind(PY_3D_HOUSE_WALL_OPACITY);
        world3D.houseWallThicknessPy.bind(PY_3D_HOUSE_WALL_THICKNESS);
    }

    public void updateLevelCounterSprites() {
        levelCounter3D.updateSprites(level.game().levelCounter(), spriteSheet, level.game().variant());
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

    public void addFood(World world, Color foodColor, Model3D pelletModel3D) {
        foodGroup.getChildren().clear();
        var foodMaterial = coloredMaterial(foodColor);
        world.tiles().filter(world::hasFoodAt).forEach(tile -> {
            Eatable3D food3D = world.isEnergizerTile(tile)
                ? createEnergizer3D(world, foodColor, tile, foodMaterial)
                : createNormalPellet3D(pelletModel3D, tile, foodMaterial);
            foodGroup.getChildren().add(food3D.root());
        });
    }

    private Pellet3D createNormalPellet3D(Model3D pelletModel3D, Vector2i tile, PhongMaterial material) {
        var pellet3D = new Pellet3D(pelletModel3D, 1.0);
        pellet3D.root().setMaterial(material);
        pellet3D.placeAtTile(tile);
        return pellet3D;
    }

    private Energizer3D createEnergizer3D(World world, Color foodColor, Vector2i tile, PhongMaterial material) {
        var energizer3D = new Energizer3D(3.5);
        energizer3D.root().setMaterial(material);
        energizer3D.placeAtTile(tile);
        var squirting = new Squirting(root) {
            @Override
            protected boolean reachesEndPosition(Drop drop) {
                return drop.getTranslateZ() >= -1 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY());
            }
        };
        squirting.setOrigin(energizer3D.root());
        squirting.setDropCountMin(15);
        squirting.setDropCountMax(45);
        squirting.setDropMaterial(coloredMaterial(foodColor.desaturate()));
        energizer3D.setEatenAnimation(squirting);
        return energizer3D;
    }


    public void update() {
        GameState gameState = GameController.it().state();
        boolean hasCredit = GameController.it().hasCredit();

        pac3D.update();
        pac3DLight.update();
        for (var ghost3D : ghosts3D) {
            ghost3D.update();
        }
        if (bonus3D != null) {
            bonus3D.update(level);
        }
        // reconsider this:
        boolean hideOne = level.pac().isVisible() || gameState == GameState.GHOST_DYING;
        livesCounter3D.update(hideOne ? level.game().lives() - 1 : level.game().lives());
        livesCounter3D.root().setVisible(hasCredit);
        scores3D.update(level);
        if (hasCredit) {
            scores3D.setShowPoints(true);
        } else {
            scores3D.setShowText(Color.RED, "GAME OVER!");
        }
        updateHouseState(level.world().house());
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

    private void updateHouseState(House house) {
        boolean houseUsed = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(house.door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        world3D.houseLighting().setLightOn(houseUsed);
        if (houseOpen) {
            for (var node : doorGroup.getChildren()) {
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

    public Ghost3D ghost3D(byte id) {
        checkGhostID(id);
        return ghosts3D.get(id);
    }

    public World3D world3D() {
        return world3D;
    }

    public Optional<Bonus3D> bonus3D() {
        return Optional.ofNullable(bonus3D);
    }

    public Scores3D scores3D() {
        return scores3D;
    }

    public LevelCounter3D levelCounter3D() {
        return levelCounter3D;
    }

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }

    /**
     * @return all 3D pellets, including energizers
     */
    public Stream<Eatable3D> eatables3D() {
        return foodGroup.getChildren().stream().map(Node::getUserData).map(Eatable3D.class::cast);
    }

    public Stream<Energizer3D> energizers3D() {
        return eatables3D().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
    }

    public Optional<Eatable3D> eatableAt(Vector2i tile) {
        checkTileNotNull(tile);
        return eatables3D().filter(eatable -> eatable.tile().equals(tile)).findFirst();
    }
}
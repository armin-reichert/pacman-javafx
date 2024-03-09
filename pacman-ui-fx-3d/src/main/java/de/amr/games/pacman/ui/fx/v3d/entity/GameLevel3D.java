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
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;
import static de.amr.games.pacman.ui.fx.v3d.entity.Pac3D.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    private static final double READY_TEXT_OUT_Z = -5;
    private static final double READY_TEXT_IN_Z  =  5;

    private static Text3D createText3D(String text, Font font) {
        Text3D text3D = new Text3D();
        text3D.beginBatch();
        text3D.setTextColor(Color.YELLOW);
        text3D.setFont(font);
        text3D.setText(text);
        text3D.endBatch();
        text3D.rotate(Rotate.X_AXIS, 90);
        return text3D;
    }

    private final GameLevel level;
    private final Group root = new Group();
    private final World3D world3D;
    private final Group foodGroup;
    private final Pac3D pac3D;
    private final Pac3DLight pac3DLight;
    private final Ghost3D[] ghosts3D;
    private final LevelCounter3D levelCounter3D;
    private final LivesCounter3D livesCounter3D;
    private final Scores3D scores3D;
    private final SpriteSheet spriteSheet;
    private Bonus3D bonus3D;
    private final Text3D readyText3D;

    public GameLevel3D(GameLevel level, Theme theme, SpriteSheet spriteSheet) {
        checkLevelNotNull(level);
        checkNotNull(theme);
        checkNotNull(spriteSheet);

        this.level = level;
        this.spriteSheet = spriteSheet;

        var pelletModel3D = theme.<Model3D>get("model3D.pellet");
        var pacModel3D = theme.<Model3D>get("model3D.pacman");
        var ghostModel3D = theme.<Model3D>get("model3D.ghost");

        double pacSize = 9.0;
        double ghostSize = 9.0;
        double livesCounterPacSize = 10.0;

        switch (level.game().variant()) {
            case MS_PACMAN -> {
                int mazeNumber = level.game().mazeNumber(level.number());
                var foodColor       = theme.color("mspacman.maze.foodColor", mazeNumber - 1);
                var wallBaseColor   = theme.color("mspacman.maze.wallBaseColor", mazeNumber - 1);
                var wallMiddleColor = theme.color("mspacman.maze.wallMiddleColor", mazeNumber - 1);
                var wallTopColor    = theme.color("mspacman.maze.wallTopColor", mazeNumber - 1);
                var doorColor       = theme.color("mspacman.maze.doorColor");
                world3D = new World3D(level.world(), theme, pelletModel3D, foodColor, wallBaseColor, wallMiddleColor, wallTopColor, doorColor);
                foodGroup = world3D.addFood();
                pac3D = createMsPacMan3D(pacModel3D, theme, level.pac(), pacSize);
                pac3DLight = new Pac3DLight(pac3D);
                ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, ghostModel3D, theme, ghostSize)).toArray(Ghost3D[]::new);
                livesCounter3D = new LivesCounter3D(() -> createMsPacManGroup(pacModel3D, theme, livesCounterPacSize), true);
            }
            case PACMAN -> {
                var foodColor       = theme.color("pacman.maze.foodColor");
                var wallBaseColor   = theme.color("pacman.maze.wallBaseColor");
                var wallMiddleColor = theme.color("pacman.maze.wallMiddleColor");
                var wallTopColor    = theme.color("pacman.maze.wallTopColor");
                var doorColor       = theme.color("pacman.maze.doorColor");
                world3D = new World3D(level.world(), theme, pelletModel3D, foodColor, wallBaseColor, wallMiddleColor, wallTopColor, doorColor);
                foodGroup = world3D.addFood();
                pac3D = createPacMan3D(pacModel3D, theme, level.pac(), pacSize);
                pac3DLight = new Pac3DLight(pac3D);
                ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, ghostModel3D, theme, ghostSize)).toArray(Ghost3D[]::new);
                livesCounter3D = new LivesCounter3D(() -> createPacManGroup(pacModel3D, theme, livesCounterPacSize), false);
            }
            default -> throw new IllegalGameVariantException(level.game().variant());
        }

        var house = level.world().house();
        readyText3D = createText3D("READY!", theme.font("font.arcade", 6));
        readyText3D.root().setTranslateX(0.5 * level.world().numCols() * TS);
        readyText3D.root().setTranslateY(house.topLeftTile().plus(house.size()).y() * TS);

        livesCounter3D.root().setTranslateX(2 * TS);
        livesCounter3D.root().setTranslateY(2 * TS);

        levelCounter3D = new LevelCounter3D();
        levelCounter3D.setRightPosition((level.world().numCols() - 2) * TS, 2 * TS, -HTS);
        updateLevelCounterSprites();

        scores3D = new Scores3D(theme.font("font.arcade", 8));
        scores3D.root().setTranslateX(TS);
        scores3D.root().setTranslateY(-3 * TS);
        scores3D.root().setTranslateZ(-3 * TS);

        root.getChildren().add(readyText3D.root());
        root.getChildren().add(scores3D.root());
        root.getChildren().add(levelCounter3D.root());
        root.getChildren().add(livesCounter3D.root());
        root.getChildren().addAll(pac3D.getRoot(), pac3DLight);
        for (int id = 0; id < 4; ++id) {
            root.getChildren().add(ghosts3D[id].root());
        }
        root.getChildren().add(foodGroup);
        // Walls must be added *after* the rest. Otherwise, transparency is not working correctly!
        root.getChildren().add(world3D.getRoot());

        // Bindings
        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        for (var g3D: ghosts3D) {
            g3D.drawModePy.bind(PY_3D_DRAW_MODE);
        }
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        world3D.drawModePy.bind(PY_3D_DRAW_MODE);

        world3D.floorColorPy.bind(PY_3D_FLOOR_COLOR);
        world3D.floorTexturePy.bind(PY_3D_FLOOR_TEXTURE);
        world3D.wallHeightPy.bind(PY_3D_WALL_HEIGHT);
        world3D.wallOpacityPy.bind(PY_3D_WALL_OPACITY);
        world3D.wallThicknessPy.bind(PY_3D_WALL_THICKNESS);
        world3D.houseWallOpacityPy.bind(PY_3D_HOUSE_WALL_OPACITY);
        world3D.houseWallThicknessPy.bind(PY_3D_HOUSE_WALL_THICKNESS);

        // center over origin
        double centerX = level.world().numCols() * HTS;
        double centerY = level.world().numRows() * HTS;
        root.setTranslateX(-centerX);
        root.setTranslateY(-centerY);
    }

    public void updateLevelCounterSprites() {
        levelCounter3D.updateSprites(level.game().levelCounter(), spriteSheet, level.game().variant());
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            root.getChildren().remove(bonus3D.getRoot());
        }
        bonus3D = createBonus3D(bonus);
        bonus3D.showEdible();
        // add bonus before last element (wall group) to make transparency work
        root.getChildren().add(root.getChildren().size() - 1, bonus3D.getRoot());
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

    public void update() {
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
        boolean hideOneLife = level.pac().isVisible() || GameController.it().state() == GameState.GHOST_DYING;
        int numLivesShown = hideOneLife ? level.game().lives() - 1 : level.game().lives();
        livesCounter3D.update(numLivesShown);
        livesCounter3D.root().setVisible(hasCredit);
        scores3D.update(level);
        if (hasCredit) {
            scores3D.setShowPoints(true);
        } else {
            scores3D.setShowText(Color.RED, "GAME OVER!");
        }
        updateHouseState();
    }

    public void showReadyMessage(String text) {
        Logger.info("Show ready message");
        readyText3D.setText(text);
        readyText3D.root().setVisible(true);
        readyText3D.root().setTranslateZ(READY_TEXT_IN_Z);
        var animation = new TranslateTransition(Duration.seconds(1.5), readyText3D.root());
        animation.setDelay(Duration.seconds(0.25));
        animation.setFromZ(READY_TEXT_IN_Z);
        animation.setToZ(READY_TEXT_OUT_Z);
        animation.play();
    }

    public void hideReadyMessage() {
        if (!readyText3D.root().isVisible()) {
            return;
        }
        var animation = new TranslateTransition(Duration.seconds(0.75), readyText3D.root());
        animation.setDelay(Duration.seconds(0.5));
        animation.setToZ(READY_TEXT_IN_Z);
        animation.setOnFinished(e -> readyText3D.root().setVisible(false));
        animation.play();
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

    private void updateHouseState() {
        boolean houseUsed = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .anyMatch(Ghost::isVisible);
        boolean houseOpen = level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
            .filter(ghost -> ghost.position().euclideanDistance(level.world().house().door().entryPosition()) <= 1.5 * TS)
            .anyMatch(Ghost::isVisible);
        world3D.houseLighting().setLightOn(houseUsed);
        if (houseOpen) {
            world3D.doorWings3D().forEach(DoorWing3D::playTraversalAnimation);
        }
    }

    public Group root() {
        return root;
    }

    public int levelNumber() {
        return level.number();
    }

    public Pac3D pac3D() {
        return pac3D;
    }

    public Ghost3D[] ghosts3D() {
        return ghosts3D;
    }

    public Ghost3D ghost3D(byte id) {
        checkGhostID(id);
        return ghosts3D[id];
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
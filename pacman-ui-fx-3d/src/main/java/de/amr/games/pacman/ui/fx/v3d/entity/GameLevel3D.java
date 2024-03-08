/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
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
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.function.Function;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_DRAW_MODE;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui.fx.v3d.entity.Pac3D.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

    private final GameLevel level;
    private final Group root = new Group();
    private final World3D world3D;
    private final Pac3D pac3D;
    private final Pac3DLight pac3DLight;
    private final Ghost3D[] ghosts3D;
    private final LevelCounter3D levelCounter3D;
    private final LivesCounter3D livesCounter3D;
    private final Scores3D scores3D;
    private final SpriteSheet spriteSheet;
    private Bonus3D bonus3D;

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
                pac3D = createPacMan3D(pacModel3D, theme, level.pac(), pacSize);
                pac3DLight = new Pac3DLight(pac3D);
                ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, ghostModel3D, theme, ghostSize)).toArray(Ghost3D[]::new);
                livesCounter3D = new LivesCounter3D(() -> createPacManGroup(pacModel3D, theme, livesCounterPacSize), false);
            }
            default -> throw new IllegalGameVariantException(level.game().variant());
        }

        livesCounter3D.setPosition(2 * TS, 2 * TS, 0);
        levelCounter3D = new LevelCounter3D();
        levelCounter3D.setRightPosition((level.world().numCols() - 2) * TS, 2 * TS, -HTS);
        updateLevelCounter3D();
        scores3D = new Scores3D(theme.font("font.arcade", 8));
        scores3D.setPosition(TS, -3 * TS, -3 * TS);

        root.getChildren().add(scores3D.root());
        root.getChildren().add(levelCounter3D.getRoot());
        root.getChildren().add(livesCounter3D.root());
        root.getChildren().addAll(pac3D.getRoot(), pac3DLight);
        for (int id = 0; id < 4; ++id) {
            root.getChildren().add(ghosts3D[id].root());
        }
        // World must be added *after* the guys. Otherwise, a semi-transparent house is not rendered correctly!
        root.getChildren().add(world3D.getRoot());

        pac3D.lightedPy.bind(PY_3D_PAC_LIGHT_ENABLED);
        ghosts3D[GameModel.RED_GHOST].drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D[GameModel.PINK_GHOST].drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D[GameModel.CYAN_GHOST].drawModePy.bind(PY_3D_DRAW_MODE);
        ghosts3D[GameModel.ORANGE_GHOST].drawModePy.bind(PY_3D_DRAW_MODE);
        world3D.drawModePy.bind(PY_3D_DRAW_MODE);
        world3D.floorColorPy.bind(PacManGames3dUI.PY_3D_FLOOR_COLOR);
        world3D.floorTexturePy.bind(PacManGames3dUI.PY_3D_FLOOR_TEXTURE);
        world3D.wallHeightPy.bind(PacManGames3dUI.PY_3D_WALL_HEIGHT);
        world3D.wallThicknessPy.bind(PacManGames3dUI.PY_3D_WALL_THICKNESS);
        livesCounter3D.drawModePy.bind(PY_3D_DRAW_MODE);
    }

    public void replaceBonus3D(Bonus bonus) {
        checkNotNull(bonus);
        if (bonus3D != null) {
            root.getChildren().remove(bonus3D.getRoot());
        }
        bonus3D = createBonus3D(bonus);
        bonus3D.showEdible();
        root.getChildren().add(bonus3D.getRoot());
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

    public void updateLevelCounter3D() {
        Function<Byte, Rectangle2D> spriteSupplier = switch (level.game().variant()) {
            case MS_PACMAN -> ((MsPacManGameSpriteSheet) spriteSheet)::bonusSymbolSprite;
            case PACMAN -> ((PacManGameSpriteSheet) spriteSheet)::bonusSymbolSprite;
        };
        var bonusSprites = level.game().levelCounter().stream()
            .map(spriteSupplier)
            .map(spriteSheet::subImage)
            .toArray(Image[]::new);
        levelCounter3D.update(bonusSprites);
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

    public LivesCounter3D livesCounter3D() {
        return livesCounter3D;
    }
}
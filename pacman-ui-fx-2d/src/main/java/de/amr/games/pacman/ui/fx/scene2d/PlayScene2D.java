/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_HOME;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
        setScoreVisible(true);
    }

    @Override
    public void update() {
        if (context.game().level() != null) {
            context.game().pac().setUseAutopilot(context.game().level().demoLevel() || PY_USE_AUTOPILOT.get());
            updateSound();
        }
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
            if (!context.gameController().hasCredit()) {
                context.actionHandler().addCredit();
            }
        } else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
            context.actionHandler().cheatEatAllPellets();
        } else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
            context.actionHandler().cheatAddLives();
        } else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
            context.actionHandler().cheatEnterNextLevel();
        } else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
            context.actionHandler().cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, PACMAN_DYING, LEVEL_COMPLETE -> context.stopAllSounds();
            case GAME_OVER -> {
                context.stopAllSounds();
                context.playAudioClip("audio.game_over");
            }
            default -> {}
        }
    }

    @Override
    protected void drawSceneContent() {
        if (context.game().level() == null) {
            return;
        }
        boolean flashing = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        switch (context.game()) {
            case GameVariants.MS_PACMAN ->
                drawMsPacManMaze(context.game(), ArcadeWorld.mazeNumberMsPacMan(context.game().level().number()), flashing);
            case GameVariants.PACMAN -> drawPacManMaze(context.game(), flashing);
            default -> throw new IllegalGameVariantException(context.game());
        }
        if (context.game().level().demoLevel() || context.gameState() == GameState.GAME_OVER) {
            // text "GAME OVER" is also drawn in demo mode
            drawText("GAME  OVER", Color.RED, sceneFont(8), t(9), t(21));
        } else {
            switch (context.gameState()) {
                case READY      -> drawText("READY!", Color.YELLOW, sceneFont(8), t(11), t(21));
                case LEVEL_TEST -> drawText("TEST    L" + context.game().level().number(), Color.YELLOW, sceneFont(8), t(8.5), t(21));
            }
        }
        context.game().bonus().ifPresent(this::drawBonus);
        drawPac(context.game().pac());
        context.game().ghosts().toList().reversed().forEach(this::drawGhost);
        if (!isCreditVisible()) {
            int numLivesDisplayed = context.game().lives() - 1;
            if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            drawLivesCounter(numLivesDisplayed);
        }
        drawLevelCounter();
    }

    private void drawPacManMaze(GameModel game, boolean flashing) {
        World world = game.world();
        if (world == null) {
            return;
        }
        PacManGameSpriteSheet sheet = context.spriteSheet();
        double x = 0, y = t(3);
        if (flashing && game.blinking().isRunning()) {
            if (game.blinking().isOn()) {
                drawImage(sheet.getFlashingMazeImage(), x, y);
            } else {
                drawSprite(sheet.getEmptyMazeSprite(), x, y);
            }
        } else {
            drawSprite(sheet.getFullMazeSprite(), x, y);
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
            if (game.blinking().isOff()) {
                world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
            }
        }
    }

    private void drawMsPacManMaze(GameModel game, int mazeNumber, boolean flashing) {
        World world = game.world();
        if (world == null) {
            return;
        }
        MsPacManGameSpriteSheet sheet = context.spriteSheet();
        double x = 0, y = t(3);
        if (flashing && game.blinking().isRunning()) {
            if (game.blinking().isOn()) {
                var flashingMazeSprite = sheet.highlightedMaze(mazeNumber);
                drawSprite(sheet.getFlashingMazesImage(), flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
            } else {
                drawSprite(sheet.source(), sheet.emptyMaze(mazeNumber), x, y);
            }
        } else {
            drawSprite(sheet.filledMaze(mazeNumber), x, y);
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
            if (game.blinking().isOff()) {
                world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
            }
        }
    }

    private void hideTileContent(World world, Vector2i tile) {
        g.setFill(context.theme().color("canvas.background"));
        double r = world.isEnergizerTile(tile) ? 4.5 : 2;
        double cx = t(tile.x()) + HTS;
        double cy = t(tile.y()) + HTS;
        g.fillRect(s(cx - r), s(cy - r), s(2 * r), s(2 * r));
    }

    @Override
    protected void drawSceneInfo() {
        drawTileGrid(ArcadeWorld.TILES_X, ArcadeWorld.TILES_Y);
        if (context.game() == GameVariants.PACMAN) {
            ArcadeWorld.PACMAN_RED_ZONE.forEach(tile -> {
                // "No Trespassing" symbol
                g.setFill(Color.RED);
                g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
                g.setFill(Color.WHITE);
                g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
            });
        }
        g.setFill(Color.YELLOW);
        g.setFont(Font.font("Sans", FontWeight.BOLD, 24));
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch() {
        if (!context.game().level().demoLevel() && context.gameState() == GameState.HUNTING) {
            context.ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    private void updateSound() {
        if (context.game().level().demoLevel()) {
            return;
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            context.stopAudioClip("audio.pacman_munch");
        }
        if (!context.game().pac().isDead() && context.game().ghosts(RETURNING_HOME, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.ensureAudioLoop("audio.ghost_returning");
        } else {
            context.stopAudioClip("audio.ghost_returning");
        }
    }
}
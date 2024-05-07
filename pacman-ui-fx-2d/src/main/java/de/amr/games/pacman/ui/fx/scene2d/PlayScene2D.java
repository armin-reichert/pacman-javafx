/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.FoodMapRenderer;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.TerrainMapRenderer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_HOME;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    private final TerrainMapRenderer terrainMapRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodMapRenderer = new FoodMapRenderer();

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
        context.game().level().ifPresent(level -> {
            context.game().pac().setUseAutopilot(context.game().isDemoLevel() || PY_USE_AUTOPILOT.get());
            updateSound();
        });
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
        if (context.game().level().isEmpty()) {
            return;
        }
        boolean flashing = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        switch (context.game()) {
            case GameVariants.MS_PACMAN -> drawMsPacManMaze(context.game(),
                context.game().mazeNumber(context.game().levelNumber()), flashing);
            case GameVariants.PACMAN -> {
                if (flashing && context.game().blinking().isRunning()) {
                    drawPacManMazeFlashing();
                } else {
                    drawPacManMazeNormal();
                }
            }
            default -> throw new IllegalGameVariantException(context.game());
        }
        if (context.game().isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            // text "GAME OVER" is also drawn in demo mode
            drawText("GAME  OVER", Color.RED, sceneFont(8), t(9), t(21));
        } else {
            switch (context.gameState()) {
                case READY      -> drawText("READY!", Color.YELLOW, sceneFont(8), t(11), t(21));
                case LEVEL_TEST -> drawText("TEST    L" + context.game().levelNumber(), Color.YELLOW, sceneFont(8), t(8.5), t(21));
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
        if (!context.game().isDemoLevel()) {
            drawLevelCounter();
        }
    }

    private void drawPacManMazeFlashing() {
        if (PY_USE_SPRITE_SHEET_FOR_MAZE.get()) {
            PacManGameSpriteSheet sheet = context.spriteSheet();
            if (context.game().blinking().isOn()) {
                drawImage(sheet.getFlashingMazeImage(), 0, t(3));
            } else {
                drawSprite(sheet.getEmptyMazeSprite(), 0, t(3));
            }
        } else {
            terrainMapRenderer.setScaling(scalingPy.get());
            if (context.game().blinking().isOn()) {
                terrainMapRenderer.setWallColor(Color.WHITE);
            } else {
                terrainMapRenderer.setWallColor(context.theme().color("pacman.maze.wallColor"));
            }
            terrainMapRenderer.drawMap(g, context.game().world().tileMap());
        }
    }

    private void drawPacManMazeNormal() {
        World world = context.game().world();
        if (PY_USE_SPRITE_SHEET_FOR_MAZE.get()) {
            PacManGameSpriteSheet sheet = context.spriteSheet();
            drawSprite(sheet.getFullMazeSprite(), 0, t(3));
            world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
            if (context.game().blinking().isOff()) {
                world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
            }
        } else {
            terrainMapRenderer.setScaling(scalingPy.get());
            terrainMapRenderer.setWallColor(context.theme().color("pacman.maze.wallColor"));
            terrainMapRenderer.drawMap(g, world.tileMap());

            foodMapRenderer.setScaling(scalingPy.get());
            foodMapRenderer.setPelletColor(context.theme().color("pacman.maze.foodColor"));
            foodMapRenderer.setEnergizerColor(context.theme().color("pacman.maze.foodColor"));
            world.tiles()
                .filter(world::hasFoodAt)
                .filter(not(world::isEnergizerTile))
                .forEach(tile -> foodMapRenderer.drawPellet(g, tile));
            if (context.game().blinking().isOn()) {
                world.energizerTiles()
                    .filter(world::hasFoodAt)
                    .forEach(tile -> foodMapRenderer.drawEnergizer(g, tile));
            }
        }
    }

    private void drawMsPacManMaze(GameModel game, int mazeNumber, boolean flashing) {
        World world = game.world();
        if (world == null) {
            return;
        }
        double x = 0, y = t(3);
        if (flashing && game.blinking().isRunning()) {
            MsPacManGameSpriteSheet sheet = context.spriteSheet();
            if (game.blinking().isOn()) {
                var flashingMazeSprite = sheet.highlightedMaze(mazeNumber);
                drawSprite(sheet.getFlashingMazesImage(), flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
            } else {
                drawSprite(sheet.source(), sheet.emptyMaze(mazeNumber), x, y);
            }
        } else {
            //renderer.setScaling(scalingPy.get());
            //renderer.setWallColor(context.theme().color("mspacman.maze.wallBaseColor", mazeNumber-1));
            //renderer.drawMap(g, world.tileMap());

            MsPacManGameSpriteSheet sheet = context.spriteSheet();
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
        drawTileGrid(GameModel.ARCADE_MAP_TILES_X, GameModel.ARCADE_MAP_TILES_Y);
        if (context.game() == GameVariants.PACMAN && context.game().world() != null) {
            context.game().world().forbiddenPassages().forEach((tile, directions) -> {
                // TODO indicate direction
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
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING) {
            context.ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    private void updateSound() {
        if (context.game().isDemoLevel()) {
            return;
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            context.stopAudioClip("audio.pacman_munch");
        }
        if (context.game().pac().isAlive() && context.game().ghosts(RETURNING_HOME, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.ensureAudioLoop("audio.ghost_returning");
        } else {
            context.stopAudioClip("audio.ghost_returning");
        }
    }
}